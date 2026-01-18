package gofd.gFMenu;

import gofd.gFMenu.commands.MenuCommand;
import gofd.gFMenu.menu.MenuManager;
import gofd.gFMenu.menu.events.ChatListener;
import gofd.gFMenu.menu.events.MenuListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class GFMenu extends JavaPlugin {

    private static GFMenu instance;
    private MenuManager menuManager;
    private LanguageManager languageManager; // 新增语言管理器

    // 定期清理任务
    private int cleanupTaskId;

    public static GFMenu getInstance() {
        return instance;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    // 新增：获取语言管理器
    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        this.menuManager = new MenuManager(this);
        this.languageManager = new LanguageManager(this); // 初始化语言管理器

        // 注册主命令
        PluginCommand mainCommand = getCommand("gfmenu");
        if (mainCommand != null) {
            MenuCommand menuCommand = new MenuCommand(this, this.menuManager);
            mainCommand.setExecutor(menuCommand);
        } else {
            getLogger().warning("无法注册主命令 'gfmenu'");
        }

        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(new MenuListener(menuManager), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(menuManager), this);

        // 确保配置文件存在
        saveDefaultConfig();

        // 加载所有菜单
        menuManager.loadAllMenus();

        // 启动定期清理任务（每30秒清理一次超时会话）
        cleanupTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            menuManager.getActionEngine().cleanupTimeoutSessions();
        }, 600L, 600L); // 每30秒执行一次

        // 保留ASCII艺术和作者信息
        getLogger().info("GFMenu v1.0.3 已启用！");
        getLogger().info("  GGGG   FFFFF M         M");
        getLogger().info(" G       F     MM       MM");
        getLogger().info(" G   GGG FFFFF M  M   M  M");
        getLogger().info(" G    G  F     M   M M   M");
        getLogger().info("  GGGG   F     M    M    M");
        getLogger().info("作者:GOFD QQ:2816958994");

        // 显示语言信息
        getLogger().info("当前语言: " + languageManager.getCurrentLanguage());
    }

    @Override
    public void onDisable() {
        // 取消清理任务
        if (cleanupTaskId > 0) {
            Bukkit.getScheduler().cancelTask(cleanupTaskId);
        }

        menuManager.unloadAllMenus();
        getLogger().info("GFMenu 已禁用！");
        getLogger().info("  GGGG   FFFFF M         M");
        getLogger().info(" G       F     MM       MM");
        getLogger().info(" G   GGG FFFFF M  M   M  M");
        getLogger().info(" G    G  F     M   M M   M");
        getLogger().info("  GGGG   F     M    M    M");
        getLogger().info("作者:GOFD QQ:2816958994");
    }

    /**
     * 重载插件
     */
    public void reloadPlugin() {
        try {
            this.reloadConfig();
            languageManager.reload(); // 重载语言
            menuManager.reloadGlobalConfig();
            menuManager.reloadMenus();
            getLogger().info("插件已重载");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "重载插件时出错", e);
            getLogger().severe(languageManager.getMessage("reload_error", e.getMessage()));
        }
    }

    /**
     * 获取全局配置状态
     */
    public String getGlobalConfigStatus() {
        return menuManager.getGlobalConfigStatus();
    }

    /**
     * 启用或禁用布局报告
     */
    public void setLayoutReportsEnabled(boolean enabled) {
        menuManager.setReportsEnabled(enabled);
        getLogger().info("布局报告已" + (enabled ? "启用" : "禁用"));
    }

    /**
     * 检查布局报告是否启用
     */
    public boolean areLayoutReportsEnabled() {
        return menuManager.isReportsEnabled();
    }
}