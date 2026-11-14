package gofd.gFMenu.menu;

import gofd.gFMenu.GFMenu;
import gofd.gFMenu.menu.actions.ActionEngine;
import gofd.gFMenu.menu.format.MenuFormat;
import gofd.gFMenu.menu.parser.DeluxeMenuParser;
import gofd.gFMenu.menu.parser.MenuParser;
import gofd.gFMenu.menu.parser.TrMenuParser;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class MenuManager {

    private final GFMenu plugin;
    private final Map<String, LayoutMenuData> loadedMenus = new LinkedHashMap<>();
    private final Map<String, File> menuFiles = new LinkedHashMap<>();
    private final Map<String, MenuFormat> menuFormats = new LinkedHashMap<>();
    private final Map<MenuFormat, MenuParser> parsers = new LinkedHashMap<>();
    private final CommandRegistry commandRegistry;
    private final ActionEngine actionEngine;
    /** Guards against overlapping asynchronous reloads. */
    private final AtomicBoolean asyncReloadInFlight = new AtomicBoolean();
    private final AtomicBoolean backupInFlight = new AtomicBoolean();
    /** Notified on the main thread right before reloaded data is swapped in (used to close editor sessions). */
    private Runnable reloadListener;
    private File menusFolder;
    private boolean globalCenterEnabled = true;
    private boolean preserveSpaces = true;
    private boolean enableReports;

    public MenuManager(GFMenu plugin) {
        this.plugin = plugin;
        loadGlobalConfig();
        parsers.put(MenuFormat.TRMENU, new TrMenuParser(globalCenterEnabled));
        parsers.put(MenuFormat.DELUXE, new DeluxeMenuParser());
        commandRegistry = new CommandRegistry(plugin, this);
        actionEngine = new ActionEngine(plugin, this);
    }

    public ActionEngine getActionEngine() {
        return actionEngine;
    }

    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public synchronized void loadAllMenus() {
        commandRegistry.unregisterAllCommands();
        loadedMenus.clear();
        menuFiles.clear();
        menuFormats.clear();

        if (!menusFolder.exists() && !menusFolder.mkdirs()) {
            plugin.getLogger().severe("Unable to create menu directory: " + menusFolder.getAbsolutePath());
            return;
        }

        File[] files = listMenuFiles(menusFolder);
        if (files == null || files.length == 0) {
            plugin.getLogger().info("No menu files found in " + menusFolder.getAbsolutePath());
            return;
        }

        int loadedCount = 0;
        for (File file : files) {
            if (file.isFile() && loadMenu(file)) {
                loadedCount++;
            }
        }
        plugin.getLogger().info("Loaded " + loadedCount + " menu(s).");
    }

    public synchronized void reloadMenus() {
        loadAllMenus();
    }

    /**
     * Reloads every menu without blocking the main thread: file IO and YAML parsing run
     * asynchronously, then the parsed result is swapped in atomically on the next tick.
     * The old menus stay fully functional until the swap, so players never see a gap.
     * <p>
     * Menu file bytes are snapshotted on the main thread before parsing. Since the
     * editor's {@link #saveMenuConfiguration} writes files on the main thread too, the
     * snapshot read can never overlap a write, and the async parse only touches memory.
     *
     * @return {@code true} if the reload was scheduled, {@code false} if one is already running
     */
    public boolean reloadMenusAsync(Runnable onComplete) {
        if (!asyncReloadInFlight.compareAndSet(false, true)) {
            plugin.getLogger().warning("Menu reload already in progress; skipping this request.");
            return false;
        }
        File folder = menusFolder;
        if (!folder.exists() && !folder.mkdirs()) {
            asyncReloadInFlight.set(false);
            plugin.getLogger().severe("Unable to create menu directory: " + folder.getAbsolutePath());
            return false;
        }
        File[] files = listMenuFiles(folder);
        Map<File, byte[]> snapshots = snapshotMenuBytes(files);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ParsedReload parsed = snapshots.isEmpty()
                    ? new ParsedReload(new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), 0)
                    : parseMenusOffThread(snapshots);
            if (!plugin.isEnabled()) {
                asyncReloadInFlight.set(false);
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    installReload(parsed);
                } catch (RuntimeException exception) {
                    plugin.getLogger().log(java.util.logging.Level.SEVERE,
                            "Failed to install reloaded menus", exception);
                } finally {
                    asyncReloadInFlight.set(false);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            });
        });
        return true;
    }

    /** {@code true} while an asynchronous menu reload is still in progress. */
    public boolean isReloadInFlight() {
        return asyncReloadInFlight.get();
    }

    public boolean createMenuBackup(BiConsumer<File, IOException> onComplete) {
        if (!backupInFlight.compareAndSet(false, true)) {
            return false;
        }
        Map<File, byte[]> snapshots = snapshotMenuBytes(listMenuFiles(menusFolder));
        if (snapshots.isEmpty()) {
            backupInFlight.set(false);
            IOException failure = new IOException("No menu files were found.");
            Bukkit.getScheduler().runTask(plugin, () -> onComplete.accept(null, failure));
            return true;
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
        File backupFile = new File(new File(plugin.getDataFolder(), "backups"), "gfmenu-menus-" + timestamp + ".zip");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            IOException failure = null;
            try {
                Files.createDirectories(backupFile.toPath().getParent());
                try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(backupFile.toPath()))) {
                    for (Map.Entry<File, byte[]> entry : snapshots.entrySet()) {
                        output.putNextEntry(new ZipEntry(entry.getKey().getName()));
                        output.write(entry.getValue());
                        output.closeEntry();
                    }
                }
            } catch (IOException exception) {
                failure = exception;
            }
            backupInFlight.set(false);
            IOException result = failure;
            if (plugin.isEnabled()) {
                Bukkit.getScheduler().runTask(plugin, () -> onComplete.accept(result == null ? backupFile : null, result));
            }
        });
        return true;
    }

    public List<String> validateMenu(String menuName) {
        File file = getMenuFile(menuName);
        LayoutMenuData menu = getMenu(menuName);
        if (file == null || menu == null) {
            return List.of("ERROR: menu is not loaded");
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> issues = new ArrayList<>();
        if (config.getKeys(false).isEmpty()) {
            return List.of("ERROR: configuration is empty or invalid YAML");
        }
        if (menuFormats.get(normalizeName(menuName)) == MenuFormat.TRMENU) {
            String layoutPath = config.contains("layout") ? "layout" : "Layout";
            List<String> layout = config.getStringList(layoutPath);
            if (layout.size() > 6) {
                issues.add("WARNING: layout has more than 6 rows; extra rows are ignored");
            }
            Map<Character, List<Integer>> positions = TrMenuLayoutParser.parseLayoutPositions(layout,
                    config.getBoolean("Settings.center", globalCenterEnabled));
            Set<Character> configuredIcons = new LinkedHashSet<>();
            org.bukkit.configuration.ConfigurationSection icons = config.getConfigurationSection("Icons");
            if (icons != null) {
                for (String key : icons.getKeys(false)) {
                    if (key.length() != 1) {
                        issues.add("ERROR: Icons." + key + " must use a one-character layout key");
                        continue;
                    }
                    char icon = key.charAt(0);
                    configuredIcons.add(icon);
                    org.bukkit.configuration.ConfigurationSection iconConfig = icons.getConfigurationSection(key);
                    if (iconConfig == null) {
                        issues.add("ERROR: Icons." + key + " must be a configuration section");
                        continue;
                    }
                    if (menu.getPositionsForIcon(icon).isEmpty()) {
                        issues.add("WARNING: Icons." + key + " is not used in layout and has no slots");
                    }
                    org.bukkit.configuration.ConfigurationSection display = iconConfig.getConfigurationSection("display");
                    String material = display == null
                            ? iconConfig.getString("material", "STONE")
                            : display.getString("material", "STONE");
                    if (org.bukkit.Material.matchMaterial(material) == null) {
                        issues.add("ERROR: Icons." + key + " has unknown material " + material);
                    }
                }
            }
            for (char icon : positions.keySet()) {
                if (!configuredIcons.contains(icon)) {
                    issues.add("WARNING: layout uses " + icon + " but Icons." + icon + " is missing");
                }
            }
        } else if (menuFormats.get(normalizeName(menuName)) == MenuFormat.DELUXE) {
            Set<Integer> occupiedSlots = new LinkedHashSet<>();
            org.bukkit.configuration.ConfigurationSection nestedItems = config.getConfigurationSection("items");
            if (nestedItems != null) {
                validateDeluxeItems(nestedItems, "items.", menu.getInventorySize(), occupiedSlots, issues);
            }
            validateDeluxeItems(config, "", menu.getInventorySize(), occupiedSlots, issues);
        }
        return List.copyOf(issues);
    }

    private static void validateDeluxeItems(org.bukkit.configuration.ConfigurationSection root, String prefix,
                                            int inventorySize, Set<Integer> occupiedSlots, List<String> issues) {
        for (String key : root.getKeys(false)) {
            org.bukkit.configuration.ConfigurationSection item = root.getConfigurationSection(key);
            if (item == null || !item.contains("slot")) {
                continue;
            }
            String path = prefix + key;
            int slot = item.getInt("slot", -1);
            if (slot < 0 || slot >= inventorySize) {
                issues.add("ERROR: " + path + ".slot is outside the menu inventory");
            } else if (!occupiedSlots.add(slot)) {
                issues.add("WARNING: multiple items use slot " + slot);
            }
            String material = item.getString("material", "STONE");
            org.bukkit.Material parsedMaterial = org.bukkit.Material.matchMaterial(material);
            if (parsedMaterial == null || parsedMaterial.isAir()) {
                issues.add("ERROR: " + path + " has unknown material " + material);
            }
        }
    }

    /** Registers a callback invoked on the main thread just before reloaded menu data replaces the old data. */
    public void setReloadListener(Runnable listener) {
        this.reloadListener = listener;
    }

    /** Reads menu files into memory on the main thread, avoiding races with editor writes. */
    private static Map<File, byte[]> snapshotMenuBytes(File[] files) {
        Map<File, byte[]> snapshots = new LinkedHashMap<>();
        if (files == null) {
            return snapshots;
        }
        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }
            try {
                snapshots.put(file, Files.readAllBytes(file.toPath()));
            } catch (IOException exception) {
                // Skip unreadable files; they will be reported by the caller's parse step.
            }
        }
        return snapshots;
    }

    private ParsedReload parseMenusOffThread(Map<File, byte[]> snapshots) {
        Map<String, LayoutMenuData> menus = new LinkedHashMap<>();
        Map<String, File> filesByName = new LinkedHashMap<>();
        Map<String, MenuFormat> formats = new LinkedHashMap<>();
        int loadedCount = 0;
        for (Map.Entry<File, byte[]> entry : snapshots.entrySet()) {
            File file = entry.getKey();
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(new ByteArrayInputStream(entry.getValue()), StandardCharsets.UTF_8));
                ParsedMenu parsedMenu = parseMenu(file, config);
                LayoutMenuData previous = menus.get(parsedMenu.key());
                if (previous != null) {
                    plugin.getLogger().warning("Menu name collision after case normalization: "
                            + file.getName() + " replaces " + previous.getName());
                }
                menus.put(parsedMenu.key(), parsedMenu.menu());
                filesByName.put(parsedMenu.key(), parsedMenu.file());
                formats.put(parsedMenu.key(), parsedMenu.format());
                loadedCount++;
            } catch (Exception exception) {
                plugin.getLogger().severe("Failed to load menu " + file.getName() + ": " + exception.getMessage());
            }
        }
        return new ParsedReload(menus, filesByName, formats, loadedCount);
    }

    private synchronized void installReload(ParsedReload parsed) {
        commandRegistry.unregisterAllCommands();
        if (reloadListener != null) {
            reloadListener.run();
        }
        loadedMenus.clear();
        menuFiles.clear();
        menuFormats.clear();
        loadedMenus.putAll(parsed.menus());
        menuFiles.putAll(parsed.files());
        menuFormats.putAll(parsed.formats());
        parsed.menus().values().forEach(commandRegistry::registerMenuCommands);
        plugin.getLogger().info("Loaded " + parsed.loadedCount() + " menu(s).");
    }

    private static File[] listMenuFiles(File folder) {
        return folder.listFiles((directory, name) -> {
            String lowercase = name.toLowerCase(Locale.ROOT);
            return lowercase.endsWith(".yml") || lowercase.endsWith(".yaml");
        });
    }

    public synchronized void unloadAllMenus() {
        commandRegistry.unregisterAllCommands();
        loadedMenus.clear();
        menuFiles.clear();
        menuFormats.clear();
    }

    private boolean loadMenu(File file) {
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            installMenu(parseMenu(file, config));
            return true;
        } catch (Exception exception) {
            plugin.getLogger().severe("Failed to load menu " + file.getName() + ": " + exception.getMessage());
            return false;
        }
    }

    private ParsedMenu parseMenu(File file, YamlConfiguration config) {
        if (config.getKeys(false).isEmpty()) {
            throw new IllegalArgumentException("Menu configuration is empty");
        }

        String menuName = fileNameWithoutExtension(file.getName());
        MenuFormat format = detectMenuFormat(config);
        MenuParser parser = parsers.getOrDefault(format, parsers.get(MenuFormat.TRMENU));
        LayoutMenuData menu = parser.parse(menuName, config);
        if (format == MenuFormat.TRMENU && !config.contains("Settings.center")) {
            menu.setCenterEnabled(globalCenterEnabled);
        }
        return new ParsedMenu(normalizeName(menuName), file, format, menu);
    }

    private void installMenu(ParsedMenu parsedMenu) {
        LayoutMenuData previous = loadedMenus.get(parsedMenu.key());
        if (previous != null) {
            plugin.getLogger().warning("Menu name collision after case normalization: "
                    + parsedMenu.file().getName() + " replaces " + previous.getName());
            commandRegistry.unregisterMenuCommands(parsedMenu.key());
        }
        loadedMenus.put(parsedMenu.key(), parsedMenu.menu());
        menuFiles.put(parsedMenu.key(), parsedMenu.file());
        menuFormats.put(parsedMenu.key(), parsedMenu.format());
        commandRegistry.registerMenuCommands(parsedMenu.menu());
    }

    private MenuFormat detectMenuFormat(YamlConfiguration config) {
        if (config.contains("layout") || config.contains("Layout") || config.contains("Icons")) {
            return MenuFormat.TRMENU;
        }
        if (config.contains("menu_title") || config.contains("open_command") || config.contains("items")) {
            return MenuFormat.DELUXE;
        }
        return MenuFormat.TRMENU;
    }

    public void openMenu(Player player, String menuName) {
        LayoutMenuData menu = getMenu(menuName);
        if (menu == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage("menu_not_found"));
            return;
        }
        menu.open(player, actionEngine, plugin.getLanguageManager());
    }

    public LayoutMenuData getMenu(String menuName) {
        return menuName == null ? null : loadedMenus.get(normalizeName(menuName));
    }

    public LayoutMenuItem getMenuItem(LayoutMenuData menu, int slot) {
        return menu == null ? null : menu.getItemAtSlot(slot);
    }

    public LayoutMenuItem getMenuItem(String menuName, int slot) {
        return getMenuItem(getMenu(menuName), slot);
    }

    public int getLoadedMenuCount() {
        return loadedMenus.size();
    }

    public Map<String, LayoutMenuData> getAllMenuData() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(loadedMenus));
    }

    /** Kept for compatibility with older callers that expect a flattened item map. */
    public Map<String, LayoutMenuItem> getLoadedMenus() {
        Map<String, LayoutMenuItem> allItems = new LinkedHashMap<>();
        loadedMenus.forEach((menuName, menu) -> menu.getItems().forEach((icon, item) ->
                allItems.put(menuName + "_" + item.getSlot(), item)));
        return Collections.unmodifiableMap(allItems);
    }

    public File getMenuFile(String menuName) {
        return menuName == null ? null : menuFiles.get(normalizeName(menuName));
    }

    public MenuFormat getMenuFormat(String menuName) {
        return menuName == null ? MenuFormat.UNKNOWN : menuFormats.getOrDefault(normalizeName(menuName), MenuFormat.UNKNOWN);
    }

    public synchronized boolean saveMenuConfiguration(String menuName, YamlConfiguration config) {
        File file = getMenuFile(menuName);
        if (file == null || config == null) {
            return false;
        }
        try {
            ParsedMenu parsedMenu = parseMenu(file, config);
            config.save(file);
            commandRegistry.unregisterMenuCommands(menuName);
            installMenu(parsedMenu);
            return true;
        } catch (IOException | RuntimeException exception) {
            plugin.getLogger().severe("Failed to save menu " + menuName + ": " + exception.getMessage());
            return false;
        }
    }

    public void reloadGlobalConfig() {
        loadGlobalConfig();
    }

    private void loadGlobalConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        globalCenterEnabled = plugin.getConfig().getBoolean("layout.default-center",
                plugin.getConfig().getBoolean("Settings.center", true));
        preserveSpaces = plugin.getConfig().getBoolean("layout.preserve-spaces", true);
        enableReports = plugin.getConfig().getBoolean("layout.enable-reports", false);
        String configuredDirectory = plugin.getConfig().getString("menus-directory", "menus");
        menusFolder = new File(plugin.getDataFolder(), configuredDirectory == null || configuredDirectory.isBlank()
                ? "menus"
                : configuredDirectory);
    }

    public String getGlobalConfigStatus() {
        String enabled = plugin.getLanguageManager().getMessage("state.enabled");
        String disabled = plugin.getLanguageManager().getMessage("state.disabled");
        return String.join("\n",
                plugin.getLanguageManager().getMessage("config.status.header"),
                plugin.getLanguageManager().getMessage("config.status.default_center",
                        globalCenterEnabled ? enabled : disabled),
                plugin.getLanguageManager().getMessage("config.status.preserve_spaces",
                        preserveSpaces ? enabled : disabled),
                plugin.getLanguageManager().getMessage("config.status.layout_reports",
                        enableReports ? enabled : disabled),
                plugin.getLanguageManager().getMessage("config.status.loaded_menus", getLoadedMenuCount()));
    }

    public void setReportsEnabled(boolean enabled) {
        enableReports = enabled;
    }

    public boolean isReportsEnabled() {
        return enableReports;
    }

    private static String fileNameWithoutExtension(String name) {
        int index = name.lastIndexOf('.');
        return index <= 0 ? name : name.substring(0, index);
    }

    private static String normalizeName(String menuName) {
        return menuName.toLowerCase(Locale.ROOT);
    }

    private record ParsedMenu(String key, File file, MenuFormat format, LayoutMenuData menu) {
    }

    private record ParsedReload(Map<String, LayoutMenuData> menus, Map<String, File> files,
                                Map<String, MenuFormat> formats, int loadedCount) {
    }
}
