package gofd.gFMenu;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class LanguageManager {

    private final GFMenu plugin;
    private final Map<String, String> messages = new HashMap<>();
    private String currentLanguage = "zh_CN";

    public LanguageManager(GFMenu plugin) {
        this.plugin = plugin;
        loadLanguage();
    }

    private void loadLanguage() {
        messages.clear(); // 清空现有消息

        // 1. 从配置文件获取语言设置
        currentLanguage = plugin.getConfig().getString("language", "zh_CN");

        // 2. 尝试加载自定义语言文件
        File langFile = new File(plugin.getDataFolder(), "languages/" + currentLanguage + ".yml");

        if (langFile.exists()) {
            try {
                loadFromFile(langFile);
                plugin.getLogger().info("已加载语言文件: " + currentLanguage);
                return; // 加载成功则返回
            } catch (Exception e) {
                plugin.getLogger().warning("加载语言文件失败: " + e.getMessage());
            }
        }

        // 3. 加载内置默认消息
        setDefaultMessages();
        plugin.getLogger().info("使用内置" + (currentLanguage.equals("zh_CN") ? "中文" : "英文") + "消息");
    }

    private void loadFromFile(File file) {
        try {
            // 如果文件不存在，创建默认内容
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                saveDefaultMessagesToFile(file);
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            for (String key : config.getKeys(true)) {
                if (config.isString(key)) {
                    messages.put(key, config.getString(key).replace("&", "§"));
                }
            }
            plugin.getLogger().info("已加载语言文件: " + currentLanguage);
        } catch (Exception e) {
            plugin.getLogger().warning("加载语言文件失败，使用默认消息: " + e.getMessage());
            setDefaultMessages();
        }
    }

    private void saveDefaultMessagesToFile(File file) {
        try {
            YamlConfiguration config = new YamlConfiguration();

            if (currentLanguage.equals("zh_CN")) {
                // 添加所有中文消息
                config.set("no_permission", "§c你没有权限！");
                config.set("player_only", "§c只有玩家可以使用！");
                config.set("menu_not_found", "§c菜单不存在！");
                config.set("reload_success", "§a插件已重载！");
                // ... 添加其他所有消息
            } else if (currentLanguage.equals("en_US")) {
                // 添加所有英文消息
                config.set("no_permission", "§cNo permission!");
                config.set("player_only", "§cPlayers only!");
                config.set("menu_not_found", "§cMenu not found!");
                config.set("reload_success", "§aPlugin reloaded!");
                // ... 添加其他所有消息
            }

            config.save(file);
        } catch (Exception e) {
            plugin.getLogger().warning("无法保存默认语言文件: " + e.getMessage());
        }
    }

    private void setDefaultMessages() {
        if (currentLanguage.equals("zh_CN")) {
            setChineseMessages();
        } else if (currentLanguage.equals("en_US")) {
            setEnglishMessages();
        } else {
            setChineseMessages(); // 默认中文
        }
    }

    private void setChineseMessages() {
        // ========== 基础消息 ==========
        messages.put("no_permission", "§c你没有权限！");
        messages.put("player_only", "§c只有玩家可以使用！");
        messages.put("console_only", "§c只有控制台可以使用！");
        messages.put("menu_not_found", "§c菜单不存在！");
        messages.put("reload_success", "§a插件已重载！");
        messages.put("reload_error", "§c重载出错: {0}");

        // ========== 菜单消息 ==========
        messages.put("menu_open_error", "§c无法打开菜单");
        messages.put("menu_permission_denied", "§c你没有权限打开这个菜单！");
        messages.put("menu_loaded_count", "§a已加载 {0} 个菜单");

        // ========== 输入捕获 ==========
        messages.put("catcher_start", "§a请输入内容（输入 'cancel' 取消）:");
        messages.put("catcher_waiting", "§7等待输入中...");
        messages.put("catcher_cancel", "§c输入已取消。");
        messages.put("catcher_timeout", "§c输入会话已超时或结束。");

        // ========== 命令 ==========
        messages.put("cmd_usage", "§c用法: {0}");
        messages.put("cmd_help_header", "§6=== GFMenu 命令帮助 ===");
        messages.put("cmd_help_item", "§7{0} §f- {1}");
        messages.put("cmd_reload_no_perm", "§c你没有权限重载插件！");
        messages.put("cmd_open_no_perm", "§c你没有权限打开菜单！");
        messages.put("cmd_list_header", "§6=== 已加载菜单列表 ===");
        messages.put("cmd_list_item", "§7- §f{0}");
        messages.put("cmd_list_footer", "§7总计: §f{0} §7个菜单");
        messages.put("cmd_list_empty", "§c没有已加载的菜单。");
        messages.put("cmd_debug_info", "§7已加载菜单数: §f{0}");
        messages.put("cmd_debug_commands", "§7菜单命令注册数: §f{0}");

        // ========== 配置 ==========
        messages.put("config_loaded", "§a配置已重载！");
        messages.put("config_status", "§6=== 全局配置状态 ===");
        messages.put("config_center", "§7默认居中: {0}");
        messages.put("config_spaces", "§7保留空格: {0}");
        messages.put("config_reports", "§7布局报告: {0}");
        messages.put("config_enabled", "§a启用");
        messages.put("config_disabled", "§c禁用");
        messages.put("config_yes", "§a是");
        messages.put("config_no", "§c否");

        // ========== 错误消息 ==========
        messages.put("error_general", "§c发生错误: {0}");
        messages.put("error_file", "§c文件错误: {0}");
        messages.put("error_menu_load", "§c加载菜单失败: {0}");
        messages.put("error_command_register", "§c注册命令失败: {0}");
    }

    private void setEnglishMessages() {
        // ========== Basic Messages ==========
        messages.put("no_permission", "§cNo permission!");
        messages.put("player_only", "§cPlayers only!");
        messages.put("console_only", "§cConsole only!");
        messages.put("menu_not_found", "§cMenu not found!");
        messages.put("reload_success", "§aPlugin reloaded!");
        messages.put("reload_error", "§cReload error: {0}");

        // ========== Menu Messages ==========
        messages.put("menu_open_error", "§cCannot open menu");
        messages.put("menu_permission_denied", "§cYou don't have permission to open this menu!");
        messages.put("menu_loaded_count", "§aLoaded {0} menus");

        // ========== Catcher ==========
        messages.put("catcher_start", "§aPlease input (type 'cancel' to cancel):");
        messages.put("catcher_waiting", "§7Waiting for input...");
        messages.put("catcher_cancel", "§cInput cancelled.");
        messages.put("catcher_timeout", "§cInput session timed out or ended.");

        // ========== Commands ==========
        messages.put("cmd_usage", "§cUsage: {0}");
        messages.put("cmd_help_header", "§6=== GFMenu Command Help ===");
        messages.put("cmd_help_item", "§7{0} §f- {1}");
        messages.put("cmd_reload_no_perm", "§cYou don't have permission to reload the plugin!");
        messages.put("cmd_open_no_perm", "§cYou don't have permission to open menus!");
        messages.put("cmd_list_header", "§6=== Loaded Menus List ===");
        messages.put("cmd_list_item", "§7- §f{0}");
        messages.put("cmd_list_footer", "§7Total: §f{0} §7menus");
        messages.put("cmd_list_empty", "§cNo menus loaded.");
        messages.put("cmd_debug_info", "§7Loaded menus: §f{0}");
        messages.put("cmd_debug_commands", "§7Registered commands: §f{0}");

        // ========== Config ==========
        messages.put("config_loaded", "§aConfig reloaded!");
        messages.put("config_status", "§6=== Global Config Status ===");
        messages.put("config_center", "§7Default center: {0}");
        messages.put("config_spaces", "§7Preserve spaces: {0}");
        messages.put("config_reports", "§7Layout reports: {0}");
        messages.put("config_enabled", "§aEnabled");
        messages.put("config_disabled", "§cDisabled");
        messages.put("config_yes", "§aYes");
        messages.put("config_no", "§cNo");

        // ========== Error Messages ==========
        messages.put("error_general", "§cError occurred: {0}");
        messages.put("error_file", "§cFile error: {0}");
        messages.put("error_menu_load", "§cFailed to load menu: {0}");
        messages.put("error_command_register", "§cFailed to register command: {0}");
    }

    /**
     * 获取消息
     * @param key 消息键
     * @return 格式化后的消息
     */
    public String getMessage(String key) {
        return messages.getOrDefault(key, "§c[Missing: " + key + "]");
    }

    /**
     * 获取带参数的消息
     * @param key 消息键
     * @param args 参数列表
     * @return 格式化后的消息
     */
    public String getMessage(String key, Object... args) {
        String message = getMessage(key);
        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return message;
    }

    /**
     * 重载语言
     */
    public void reload() {
        loadLanguage();
    }

    /**
     * 获取当前语言
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * 设置语言（仅测试用）
     */
    public void setLanguage(String lang) {
        this.currentLanguage = lang;
        reload();
    }

    /**
     * 检查消息是否存在
     */
    public boolean hasMessage(String key) {
        return messages.containsKey(key);
    }

    /**
     * 获取所有消息（调试用）
     */
    public Map<String, String> getAllMessages() {
        return new HashMap<>(messages);
    }
}