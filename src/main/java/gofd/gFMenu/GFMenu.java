package gofd.gFMenu;

import gofd.gFMenu.commands.MenuCommand;
import gofd.gFMenu.menu.MenuManager;
import gofd.gFMenu.menu.events.MenuListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class GFMenu extends JavaPlugin {

    private static GFMenu instance;
    private MenuManager menuManager;

    @Override
    public void onEnable() {
        instance = this;
        this.menuManager = new MenuManager(this);
        // 注册主命令
        PluginCommand mainCommand = getCommand("gfmenu");
        if (mainCommand != null) {
            mainCommand.setExecutor(new MenuCommand(menuManager));
        } else {
            getLogger().warning("无法注册主命令 'gfmenu'，请检查 plugin.yml");
        }
        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(new MenuListener(menuManager), this);

        // 注册主命令
        getCommand("gfmenu").setExecutor(new MenuCommand(menuManager));

        // 确保数据文件夹存在
        saveDefaultConfig();

        // 加载所有菜单
        menuManager.loadAllMenus();

        getLogger().info("GFMenu v1.0.0 已启用！");
        getLogger().info("  GGGG   FFFFF M         M");
        getLogger().info(" G       F     MM       MM");
        getLogger().info(" G   GGG FFFFF M  M   M  M");
        getLogger().info(" G    G  F     M   M M   M");
        getLogger().info("  GGGG   F     M    M    M");
        getLogger().info("作者:GOFD QQ:2816958994");
    }

    @Override
    public void onDisable() {
        menuManager.unloadAllMenus();
        getLogger().info("GFMenu 已禁用！");
    }

    public static GFMenu getInstance() {
        return instance;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }
}