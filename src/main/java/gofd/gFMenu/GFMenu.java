package gofd.gFMenu;

import gofd.gFMenu.commands.MenuCommand;
import gofd.gFMenu.menu.MenuManager;
import gofd.gFMenu.menu.editor.MenuEditor;
import gofd.gFMenu.menu.events.BookListener;
import gofd.gFMenu.menu.events.ChatListener;
import gofd.gFMenu.menu.events.MenuListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GFMenu extends JavaPlugin {

    private static final String SPIGOT_API_URL = "https://api.spigotmc.org/legacy/update.php?resource=137753";
    private static final String GITHUB_API_URL = "https://api.github.com/repos/levindurant303/GFMenu/releases/latest";
    private static final String DOWNLOAD_URL = "https://www.spigotmc.org/resources/gfmenu.137753/";

    private static GFMenu instance;
    private MenuManager menuManager;
    private LanguageManager languageManager;
    private int cleanupTaskId = -1;

    public static GFMenu getInstance() { return instance; }
    public MenuManager getMenuManager() { return menuManager; }
    public LanguageManager getLanguageManager() { return languageManager; }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getConfig().addDefault("update-check", true);
        getConfig().options().copyDefaults(true);
        saveConfig();

        languageManager = new LanguageManager(this);
        menuManager = new MenuManager(this);

        PluginCommand mainCommand = getCommand("gfmenu");
        if (mainCommand == null) {
            getLogger().severe("Command gfmenu is missing from plugin.yml.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        MenuEditor editor = new MenuEditor(this, menuManager);
        MenuCommand menuCommand = new MenuCommand(this, menuManager, editor);
        mainCommand.setExecutor(menuCommand);
        mainCommand.setTabCompleter(menuCommand);

        Bukkit.getPluginManager().registerEvents(new MenuListener(menuManager), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(menuManager), this);
        Bukkit.getPluginManager().registerEvents(new BookListener(menuManager), this);
        Bukkit.getPluginManager().registerEvents(editor, this);

        menuManager.loadAllMenus();
        cleanupTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                this, () -> menuManager.getActionEngine().cleanupTimeoutSessions(), 600L, 600L);

        printStartupBanner();

        if (getConfig().getBoolean("update-check", true)) {
            Bukkit.getScheduler().runTaskAsynchronously(this, this::checkForUpdates);
        }
    }

    @Override
    public void onDisable() {
        if (cleanupTaskId >= 0) {
            Bukkit.getScheduler().cancelTask(cleanupTaskId);
        }
        if (menuManager != null) {
            menuManager.unloadAllMenus();
        }
        printShutdownBanner();
        instance = null;
    }

    public boolean reloadPlugin() {
        try {
            reloadConfig();
            languageManager.reload();
            menuManager.reloadGlobalConfig();
            menuManager.reloadMenus();
            getLogger().info(languageManager.getCurrentLanguage().equals("zh_CN") ? "重载完成！" : "Reload complete!");
            return true;
        } catch (RuntimeException exception) {
            getLogger().log(Level.SEVERE, "加载失败了，请联系作者: GOFD QQ:2816958994", exception);
            return false;
        }
    }

    public String getGlobalConfigStatus() { return menuManager.getGlobalConfigStatus(); }
    public void setLayoutReportsEnabled(boolean enabled) { menuManager.setReportsEnabled(enabled); }
    public boolean areLayoutReportsEnabled() { return menuManager.isReportsEnabled(); }

    private void printStartupBanner() {
        String lang = languageManager.getCurrentLanguage();
        boolean isChinese = lang.startsWith("zh");

        String status = isChinese ? "已启用！" : "enabled!";
        String author = isChinese ? "作者: GOFD QQ:2816958994" : "Author: GOFD QQ:2816958994";
        String langLabel = isChinese ? "当前语言: " : "Current language: ";

        getLogger().info("GFMenu v" + getDescription().getVersion() + " " + status);
        getLogger().info("  GGGG   FFFFF M         M");
        getLogger().info(" G       F     MM       MM");
        getLogger().info(" G   GGG FFFFF M  M   M  M");
        getLogger().info(" G    G  F     M   M M   M");
        getLogger().info("  GGGG   F     M    M    M");
        getLogger().info(author);
        getLogger().info(langLabel + lang);
    }
    private void printShutdownBanner() {
        boolean isChinese = "zh_CN".equals(languageManager.getCurrentLanguage());
        String status = isChinese ? "已禁用！" : "disabled!";
        String author = isChinese ? "作者: GOFD QQ:2816958994" : "Author: GOFD QQ:2816958994";

        getLogger().info("GFMenu " + status);
        getLogger().info("  GGGG   FFFFF M         M");
        getLogger().info(" G       F     MM       MM");
        getLogger().info(" G   GGG FFFFF M  M   M  M");
        getLogger().info(" G    G  F     M   M M   M");
        getLogger().info("  GGGG   F     M    M    M");
        getLogger().info(author);
    }

    private void checkForUpdates() {
        getLogger().info(ChatColor.translateAlternateColorCodes('&',
                languageManager.getMessage("update.checking")));

        String latestVersion = null;
        String changelog = null;

        // Spigot API
        try {
            URL url = new URL(SPIGOT_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String version = reader.readLine();
                    if (version != null && !version.isEmpty()) {
                        latestVersion = version.trim();
                    }
                }
            }
        } catch (Exception ignored) {}

        // GitHub API（备选）
        if (latestVersion == null) {
            try {
                URL url = new URL(GITHUB_API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        String json = response.toString();
                        Pattern tagPattern = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
                        Matcher tagMatcher = tagPattern.matcher(json);
                        if (tagMatcher.find()) {
                            latestVersion = tagMatcher.group(1);
                        }
                        Pattern bodyPattern = Pattern.compile("\"body\"\\s*:\\s*\"([^\"]*)\"");
                        Matcher bodyMatcher = bodyPattern.matcher(json);
                        if (bodyMatcher.find()) {
                            changelog = bodyMatcher.group(1)
                                    .replace("\\n", "\n")
                                    .replace("\\r", "")
                                    .replace("\\t", "    ");
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        if (latestVersion == null) {
            getLogger().info(ChatColor.translateAlternateColorCodes('&',
                    languageManager.getMessage("update.failed")));
            return;
        }

        String currentVersion = getDescription().getVersion();
        if (compareVersions(latestVersion, currentVersion) > 0) {
            getLogger().info(ChatColor.translateAlternateColorCodes('&',
                    languageManager.getMessage("update.new_available", latestVersion, currentVersion)));
            getLogger().info(ChatColor.translateAlternateColorCodes('&',
                    languageManager.getMessage("update.download", DOWNLOAD_URL)));
            if (changelog != null && !changelog.isEmpty()) {
                getLogger().info(ChatColor.translateAlternateColorCodes('&',
                        languageManager.getMessage("update.changelog", changelog)));
            }
        } else {
            getLogger().info(ChatColor.translateAlternateColorCodes('&',
                    languageManager.getMessage("update.latest")));
        }
    }

    private int compareVersions(String v1, String v2) {
        if (v1 == null || v2 == null) return 0;
        String[] p1 = v1.split("\\.");
        String[] p2 = v2.split("\\.");
        int len = Math.max(p1.length, p2.length);
        for (int i = 0; i < len; i++) {
            int n1 = i < p1.length ? Integer.parseInt(p1[i].replaceAll("[^0-9]", "")) : 0;
            int n2 = i < p2.length ? Integer.parseInt(p2[i].replaceAll("[^0-9]", "")) : 0;
            if (n1 != n2) return Integer.compare(n1, n2);
        }
        return 0;
    }
}
