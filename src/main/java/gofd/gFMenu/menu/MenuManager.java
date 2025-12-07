package gofd.gFMenu.menu;

import gofd.gFMenu.GFMenu;
import gofd.gFMenu.menu.actions.ActionEngine;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class MenuManager {

    private final GFMenu plugin;
    private final Map<String, LayoutMenuData> loadedMenus;
    private final CommandRegistry commandRegistry;
    private final File menusFolder;
    private final ActionEngine actionEngine;

    public MenuManager(GFMenu plugin) {
        this.plugin = plugin;
        this.loadedMenus = new HashMap<>();
        this.menusFolder = new File(plugin.getDataFolder(), "menus");
        this.commandRegistry = new CommandRegistry(plugin, this);
        this.actionEngine = new ActionEngine(plugin, this);

        if (!menusFolder.exists()) {
            menusFolder.mkdirs();
        }
    }

    public void loadAllMenus() {
        commandRegistry.unregisterAllCommands();
        loadedMenus.clear();

        File[] menuFiles = menusFolder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".yml")
        );

        if (menuFiles == null) return;

        for (File file : menuFiles) {
            loadMenu(file);
        }

        plugin.getLogger().info("§a已加载 " + loadedMenus.size() + " 个菜单");
        plugin.getLogger().info("§a已注册 " + commandRegistry.getRegisteredCommandCount() + " 个命令");
    }

    private void loadMenu(File file) {
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            String menuName = file.getName().replace(".yml", "");

            LayoutMenuData menuData = new LayoutMenuData(menuName);

            // 1. 解析基础信息
            menuData.setTitle(config.getString("Title", "&f菜单"));

            // 2. 解析布局设置
            boolean centerEnabled = false;
            Map<Integer, Integer> rowOffsets = new HashMap<>();

            if (config.contains("Settings")) {
                ConfigurationSection settings = config.getConfigurationSection("Settings");
                if (settings != null) {
                    centerEnabled = settings.getBoolean("center", false);

                    // 解析行偏移
                    if (settings.contains("rowOffsets")) {
                        ConfigurationSection offsets = settings.getConfigurationSection("rowOffsets");
                        if (offsets != null) {
                            for (String key : offsets.getKeys(false)) {
                                try {
                                    int row = Integer.parseInt(key);
                                    int offset = offsets.getInt(key);
                                    rowOffsets.put(row, offset);
                                } catch (NumberFormatException e) {
                                    plugin.getLogger().warning("无效的行偏移键: " + key);
                                }
                            }
                        }
                    }
                }
            }

            menuData.setCenterEnabled(centerEnabled);
            menuData.setRowOffsets(rowOffsets);

            // 3. 解析布局定义
            List<String> layoutRows = config.getStringList("layout");
            menuData.setRawLayout(layoutRows);

            if (!layoutRows.isEmpty()) {
                // 使用智能布局解析器
                Map<Character, Integer> layoutSlots = SmartLayoutParser.parseLayout(
                        layoutRows, centerEnabled, rowOffsets
                );
                menuData.setLayoutSlots(layoutSlots);

                // 生成并显示布局预览
                String preview = SmartLayoutParser.visualizeLayout(layoutRows, centerEnabled);
                plugin.getLogger().info("菜单 '" + menuName + "' 布局预览:\n" + preview);
            }

            // 4. 解析命令绑定
            if (config.contains("Bindings.Commands")) {
                List<String> commands = config.getStringList("Bindings.Commands");
                menuData.setCommands(commands);

                commandRegistry.registerMenuCommands(menuData);
            }

            // 5. 解析权限
            if (config.contains("permission")) {
                menuData.setPermission(config.getString("permission"));
            }

            // 6. 解析别名
            if (config.contains("aliases")) {
                List<String> aliases = config.getStringList("aliases");
                menuData.setAliases(aliases);
            }

            // 7. 解析事件
            if (config.contains("Events")) {
                ConfigurationSection events = config.getConfigurationSection("Events");
                if (events != null) {
                    if (events.contains("Open")) {
                        menuData.setOpenEvents(events.getStringList("Open"));
                    }
                    if (events.contains("Close")) {
                        menuData.setCloseEvents(events.getStringList("Close"));
                    }
                }
            }

            // 8. 解析图标
            ConfigurationSection iconsSection = config.getConfigurationSection("Icons");
            if (iconsSection != null) {
                for (String iconKey : iconsSection.getKeys(false)) {
                    if (iconKey.length() == 1) {
                        char iconChar = iconKey.charAt(0);
                        Integer slot = menuData.getLayoutSlots().get(iconChar);

                        if (slot != null) {
                            ConfigurationSection iconConfig = iconsSection.getConfigurationSection(iconKey);
                            LayoutMenuItem item = parseMenuItem(iconConfig, slot, iconChar);
                            menuData.addItem(iconChar, item);

                            plugin.getLogger().info(String.format(
                                    "  图标 '%c' → 槽位 %d (行%d,列%d)",
                                    iconChar, slot, slot/9, slot%9));
                        } else {
                            plugin.getLogger().warning("菜单 " + menuName +
                                    ": 图标 '" + iconChar + "' 没有对应的布局槽位");
                        }
                    }
                }
            }

            loadedMenus.put(menuName.toLowerCase(), menuData);
            plugin.getLogger().info("§a成功加载菜单: " + menuName +
                    (centerEnabled ? " §e(居中模式)" : " §7(左对齐模式)"));

        } catch (Exception e) {
            plugin.getLogger().warning("加载菜单文件失败: " + file.getName());
            e.printStackTrace();
        }
    }

    private LayoutMenuItem parseMenuItem(ConfigurationSection config, int slot, char iconChar) {
        LayoutMenuItem item = new LayoutMenuItem();
        item.setSlot(slot);
        item.setIconChar(iconChar);

        if (config.contains("display")) {
            ConfigurationSection display = config.getConfigurationSection("display");
            item.setMaterial(display.getString("material", "STONE"));
            item.setName(display.getString("name", "&f物品"));
            item.setLore(display.getStringList("lore"));
            item.setAmount(display.getInt("amount", 1));
        }

        if (config.contains("actions")) {
            Object actionsObj = config.get("actions");

            if (actionsObj instanceof List) {
                List<String> actionList = config.getStringList("actions");
                item.setActions("all", actionList);
            } else if (actionsObj instanceof ConfigurationSection) {
                ConfigurationSection actionsSec = (ConfigurationSection) actionsObj;

                if (actionsSec.contains("all")) {
                    Object allObj = actionsSec.get("all");
                    if (allObj instanceof String) {
                        item.setActions("all", Collections.singletonList((String) allObj));
                    } else if (allObj instanceof List) {
                        item.setActions("all", actionsSec.getStringList("all"));
                    }
                }

                if (actionsSec.contains("left")) {
                    item.setActions("left", actionsSec.getStringList("left"));
                }

                if (actionsSec.contains("right")) {
                    item.setActions("right", actionsSec.getStringList("right"));
                }
            }
        }

        return item;
    }

    public void openMenu(Player player, String menuName) {
        LayoutMenuData menu = loadedMenus.get(menuName.toLowerCase());
        if (menu != null) {
            menu.open(player);
        } else {
            player.sendMessage("§c菜单不存在或未加载！");
        }
    }

    public LayoutMenuData getMenu(String menuName) {
        return loadedMenus.get(menuName.toLowerCase());
    }

    public Map<String, LayoutMenuData> getLoadedMenus() {
        return Collections.unmodifiableMap(loadedMenus);
    }

    public LayoutMenuItem getMenuItem(LayoutMenuData menu, int slot) {
        if (menu == null) return null;

        for (LayoutMenuItem item : menu.getItems().values()) {
            if (item.getSlot() == slot) {
                return item;
            }
        }
        return null;
    }

    public ActionEngine getActionEngine() {
        return actionEngine;
    }

    public void reloadMenus() {
        commandRegistry.unregisterAllCommands();
        loadedMenus.clear();
        loadAllMenus();
    }

    public void unloadAllMenus() {
        commandRegistry.unregisterAllCommands();
        loadedMenus.clear();
    }
}
