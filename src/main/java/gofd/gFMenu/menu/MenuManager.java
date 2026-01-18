package gofd.gFMenu.menu;

import gofd.gFMenu.GFMenu;
import gofd.gFMenu.menu.actions.ActionEngine;
import gofd.gFMenu.menu.format.MenuFormat;
import gofd.gFMenu.menu.parser.DeluxeMenuParser;
import gofd.gFMenu.menu.parser.MenuParser;
import gofd.gFMenu.menu.parser.TrMenuParser;
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

    // 解析器集合
    private final Map<MenuFormat, MenuParser> parsers;

    // 全局配置
    private boolean globalCenterEnabled = true;
    private boolean preserveSpaces = true;
    private boolean enableReports = false;

    public MenuManager(GFMenu plugin) {
        this.plugin = plugin;
        this.loadedMenus = new HashMap<>();
        this.menusFolder = new File(plugin.getDataFolder(), "menus");
        this.commandRegistry = new CommandRegistry(plugin, this);
        this.actionEngine = new ActionEngine(plugin, this);

        // 初始化解析器
        this.parsers = new HashMap<>();
        this.parsers.put(MenuFormat.TRMENU, new TrMenuParser());
        this.parsers.put(MenuFormat.DELUXE, new DeluxeMenuParser());

        loadGlobalConfig();

        if (!menusFolder.exists()) {
            menusFolder.mkdirs();
        }
    }

    /**
     * 获取动作引擎
     */
    public ActionEngine getActionEngine() {
        return this.actionEngine;
    }

    /**
     * 获取命令注册器
     */
    public CommandRegistry getCommandRegistry() {
        return this.commandRegistry;
    }

    /**
     * 获取菜单中的特定槽位物品
     */
    public LayoutMenuItem getMenuItem(LayoutMenuData menu, int slot) {
        if (menu == null) return null;

        // 遍历菜单中的所有物品
        for (LayoutMenuItem item : menu.getItems().values()) {
            if (item.getSlot() == slot) {
                return item;
            }
        }

        return null;
    }

    /**
     * 通过菜单名称和槽位获取物品
     */
    public LayoutMenuItem getMenuItem(String menuName, int slot) {
        LayoutMenuData menu = getMenu(menuName);
        if (menu == null) return null;
        return getMenuItem(menu, slot);
    }

    /**
     * 获取已加载的菜单数量
     */
    public int getLoadedMenuCount() {
        return this.loadedMenus.size();
    }

    /**
     * 重新加载所有菜单
     */
    public void reloadMenus() {
        this.commandRegistry.unregisterAllCommands();
        this.loadedMenus.clear();
        loadAllMenus();
    }

    /**
     * 重新加载全局配置
     */
    public void reloadGlobalConfig() {
        loadGlobalConfig();
        this.plugin.getLogger().info("全局配置已重载");
    }

    /**
     * 卸载所有菜单
     */
    public void unloadAllMenus() {
        this.commandRegistry.unregisterAllCommands();
        this.loadedMenus.clear();
    }

    /**
     * 获取全局配置状态
     */
    public String getGlobalConfigStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("§6=== 全局配置状态 ===\n");
        sb.append("§7默认居中: §").append(this.globalCenterEnabled ? "a启用" : "c禁用").append("\n");
        sb.append("§7保留空格: §").append(this.preserveSpaces ? "a是" : "c否").append("\n");
        sb.append("§7布局报告: §").append(this.enableReports ? "a启用" : "c禁用").append("\n");

        sb.append("\n§7已加载菜单: §f").append(getLoadedMenuCount()).append(" 个\n");

        return sb.toString().replace("§", "");
    }

    /**
     * 设置报告开关
     */
    public void setReportsEnabled(boolean enabled) {
        this.enableReports = enabled;
    }

    /**
     * 检查报告是否启用
     */
    public boolean isReportsEnabled() {
        return this.enableReports;
    }

    /**
     * 检测菜单格式
     */
    private MenuFormat detectMenuFormat(YamlConfiguration config) {
        // 检测TrMenu格式
        if (config.contains("layout") || config.contains("Layout")) {
            return MenuFormat.TRMENU;
        }

        // 检测DeluxeMenus格式
        if (config.contains("menu_title") || config.contains("open_command")) {
            return MenuFormat.DELUXE;
        }

        return MenuFormat.UNKNOWN;
    }

    /**
     * 加载所有菜单
     */
    public void loadAllMenus() {
        this.commandRegistry.unregisterAllCommands();
        this.loadedMenus.clear();

        this.plugin.getLogger().info("开始加载菜单...");

        if (!this.menusFolder.exists()) {
            this.plugin.getLogger().warning("菜单文件夹不存在，已创建");
            this.menusFolder.mkdirs();
        }

        File[] menuFiles = this.menusFolder.listFiles();

        if (menuFiles == null || menuFiles.length == 0) {
            this.plugin.getLogger().warning("没有找到菜单文件");
            return;
        }

        int loadedCount = 0;
        for (File file : menuFiles) {
            if (file.isFile() && (file.getName().toLowerCase().endsWith(".yml") ||
                    file.getName().toLowerCase().endsWith(".yaml"))) {
                if (loadMenu(file)) {
                    loadedCount++;
                }
            }
        }

        this.plugin.getLogger().info("已加载 " + loadedCount + " 个菜单");
    }

    /**
     * 加载单个菜单文件
     */
    private boolean loadMenu(File file) {
        String fileName = file.getName();
        String menuName = fileName.replace(".yml", "").replace(".yaml", "");

        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            if (config.getKeys(false).isEmpty()) {
                return false;
            }

            // 检测并选择解析器
            MenuFormat format = detectMenuFormat(config);
            MenuParser parser = parsers.get(format);

            if (parser == null) {
                parser = parsers.get(MenuFormat.TRMENU);
            }

            // 解析菜单
            LayoutMenuData menuData = parser.parse(menuName, config);

            // 应用全局配置
            if (format == MenuFormat.TRMENU) {
                menuData.setCenterEnabled(this.globalCenterEnabled);
            }

            // 注册命令
            if (menuData.getCommands() != null && !menuData.getCommands().isEmpty()) {
                this.commandRegistry.registerMenuCommands(menuData);
            }

            // 添加到已加载菜单
            this.loadedMenus.put(menuName.toLowerCase(), menuData);
            this.plugin.getLogger().info("✓ 加载菜单: " + menuName + " (" +
                    menuData.getItems().size() + " 个物品)");

            return true;

        } catch (Exception e) {
            this.plugin.getLogger().severe("加载菜单失败: " + fileName + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * 打开菜单给玩家
     */
    public void openMenu(Player player, String menuName) {
        LayoutMenuData menu = this.loadedMenus.get(menuName.toLowerCase());
        if (menu != null) {
            menu.open(player);
        } else {
            player.sendMessage("§c菜单不存在或未加载！");
        }
    }

    /**
     * 获取菜单
     */
    public LayoutMenuData getMenu(String menuName) {
        return this.loadedMenus.get(menuName.toLowerCase());
    }

    /**
     * 获取所有已加载的菜单
     */
    public Map<String, LayoutMenuItem> getLoadedMenus() {
        Map<String, LayoutMenuItem> allItems = new HashMap<>();
        for (LayoutMenuData menu : loadedMenus.values()) {
            for (LayoutMenuItem item : menu.getItems().values()) {
                allItems.put(menu.getName() + "_" + item.getSlot(), item);
            }
        }
        return Collections.unmodifiableMap(allItems);
    }

    /**
     * 获取所有菜单数据
     */
    public Map<String, LayoutMenuData> getAllMenuData() {
        return Collections.unmodifiableMap(loadedMenus);
    }

    /**
     * 加载全局配置
     */
    private void loadGlobalConfig() {
        this.plugin.saveDefaultConfig();
        this.plugin.reloadConfig();

        this.globalCenterEnabled = this.plugin.getConfig().getBoolean("layout.default-center", true);
        this.preserveSpaces = this.plugin.getConfig().getBoolean("layout.preserve-spaces", true);
        this.enableReports = this.plugin.getConfig().getBoolean("layout.enable-reports", false);
    }
}