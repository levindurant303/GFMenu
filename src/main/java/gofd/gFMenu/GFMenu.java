package gofd.gFMenu;

import gofd.gFMenu.commands.MenuCommand;
import gofd.gFMenu.menu.MenuManager;
import gofd.gFMenu.menu.editor.MenuEditor;
import gofd.gFMenu.menu.events.BookListener;
import gofd.gFMenu.menu.events.ChatListener;
import gofd.gFMenu.menu.events.MenuListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class GFMenu extends JavaPlugin {

    private static GFMenu instance;
    private MenuManager menuManager;
    private LanguageManager languageManager;
    private int cleanupTaskId = -1;

    public static GFMenu getInstance() {
        return instance;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
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

        // Preserve the original startup banner and author lines.
        getLogger().info("GFMenu v1.0.3 已启用！");
        getLogger().info("  GGGG   FFFFF M         M");
        getLogger().info(" G       F     MM       MM");
        getLogger().info(" G   GGG FFFFF M  M   M  M");
        getLogger().info(" G    G  F     M   M M   M");
        getLogger().info("  GGGG   F     M    M    M");
        getLogger().info("作者: GOFD QQ:2816958994");
        getLogger().info("当前语言: " + languageManager.getCurrentLanguage());
    }

    @Override
    public void onDisable() {
        if (cleanupTaskId >= 0) {
            Bukkit.getScheduler().cancelTask(cleanupTaskId);
        }
        if (menuManager != null) {
            menuManager.unloadAllMenus();
        }

        // Preserve the original shutdown banner and author lines.
        getLogger().info("GFMenu 已禁用！");
        getLogger().info("  GGGG   FFFFF M         M");
        getLogger().info(" G       F     MM       MM");
        getLogger().info(" G   GGG FFFFF M  M   M  M");
        getLogger().info(" G    G  F     M   M M   M");
        getLogger().info("  GGGG   F     M    M    M");
        getLogger().info("作者: GOFD QQ:2816958994");
        instance = null;
    }

    public void reloadPlugin() {
        try {
            reloadConfig();
            languageManager.reload();
            menuManager.reloadGlobalConfig();
            menuManager.reloadMenus();
        } catch (RuntimeException exception) {
            getLogger().log(Level.SEVERE, "Unable to reload GFMenu", exception);
        }
    }

    public String getGlobalConfigStatus() {
        return menuManager.getGlobalConfigStatus();
    }

    public void setLayoutReportsEnabled(boolean enabled) {
        menuManager.setReportsEnabled(enabled);
    }

    public boolean areLayoutReportsEnabled() {
        return menuManager.isReportsEnabled();
    }
}
