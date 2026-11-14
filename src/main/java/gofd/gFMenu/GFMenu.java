package gofd.gFMenu;

import gofd.gFMenu.commands.MenuCommand;
import gofd.gFMenu.checkin.CheckinManager;
import gofd.gFMenu.menu.MenuManager;
import gofd.gFMenu.menu.editor.MenuEditor;
import gofd.gFMenu.menu.events.BookListener;
import gofd.gFMenu.menu.events.ChatListener;
import gofd.gFMenu.menu.events.MenuListener;
import gofd.gFMenu.update.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.concurrent.atomic.AtomicBoolean;

public final class GFMenu extends JavaPlugin {

    private static GFMenu instance;
    private MenuManager menuManager;
    private LanguageManager languageManager;
    private CheckinManager checkinManager;
    private int cleanupTaskId = -1;
    private final AtomicBoolean updateCheckInFlight = new AtomicBoolean();

    public static GFMenu getInstance() {
        return instance;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public CheckinManager getCheckinManager() {
        return checkinManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        languageManager = new LanguageManager(this);
        menuManager = new MenuManager(this);
        checkinManager = new CheckinManager(this);

        PluginCommand mainCommand = getCommand("gfmenu");
        if (mainCommand == null) {
            getLogger().severe("Command gfmenu is missing from plugin.yml.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        MenuEditor editor = new MenuEditor(this, menuManager);
        menuManager.setReloadListener(editor::invalidateForReload);
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
        getLogger().info(languageManager.getMessage("plugin.enabled", getDescription().getVersion()));
        getLogger().info("  GGGG   FFFFF M         M");
        getLogger().info(" G       F     MM       MM");
        getLogger().info(" G   GGG FFFFF M  M   M  M");
        getLogger().info(" G    G  F     M   M M   M");
        getLogger().info("  GGGG   F     M    M    M");
        getLogger().info(languageManager.getMessage("plugin.author"));
        getLogger().info(languageManager.getMessage("plugin.current_language", languageManager.getCurrentLanguage()));

        if (getConfig().getBoolean("update-check", true)) {
            checkForUpdates(null);
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

        // Preserve the original shutdown banner and author lines.
        getLogger().info(languageManager == null
                ? "GFMenu 已禁用！"
                : languageManager.getMessage("plugin.disabled"));
        getLogger().info("  GGGG   FFFFF M         M");
        getLogger().info(" G       F     MM       MM");
        getLogger().info(" G   GGG FFFFF M  M   M  M");
        getLogger().info(" G    G  F     M   M M   M");
        getLogger().info("  GGGG   F     M    M    M");
        getLogger().info(languageManager == null
                ? "作者: GOFD QQ:2816958994"
                : languageManager.getMessage("plugin.author"));
        instance = null;
    }

    public boolean reloadPlugin() {
        return reloadPlugin(null);
    }

    /**
     * Reloads config and language synchronously (cheap), then schedules the menu reload
     * asynchronously. {@code onMenusReloaded} runs on the main thread once menus swapped in.
     *
     * @return {@code true} if the reload was scheduled successfully
     */
    public boolean reloadPlugin(Runnable onMenusReloaded) {
        try {
            reloadConfig();
            languageManager.reload();
            menuManager.reloadGlobalConfig();
            checkinManager.reloadConfig();
        } catch (RuntimeException exception) {
            getLogger().log(Level.SEVERE, "加载失败了，请联系作者: GOFD QQ:2816958994", exception);
            return false;
        }
        return menuManager.reloadMenusAsync(onMenusReloaded);
    }

    /** Starts a non-blocking update check; a non-null requester receives the result in-game. */
    public boolean checkForUpdates(CommandSender requester) {
        if (!updateCheckInFlight.compareAndSet(false, true)) {
            if (requester != null) {
                requester.sendMessage(languageManager.getMessage("update.busy"));
            }
            return false;
        }
        if (requester != null) {
            requester.sendMessage(languageManager.getMessage("update.checking"));
        }

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            UpdateChecker.Result result = UpdateChecker.check(getDescription().getVersion());
            if (!isEnabled()) {
                updateCheckInFlight.set(false);
                return;
            }
            Bukkit.getScheduler().runTask(this, () -> {
                updateCheckInFlight.set(false);
                reportUpdateResult(result, requester);
            });
        });
        return true;
    }

    private void reportUpdateResult(UpdateChecker.Result result, CommandSender requester) {
        if (result.failed()) {
            reportUpdateMessage(requester, "update.failed", Level.WARNING);
            return;
        }
        if (!result.updateAvailable()) {
            reportUpdateMessage(requester, "update.latest", Level.INFO);
            return;
        }
        reportUpdateMessage(requester, "update.new_available", Level.WARNING,
                result.version(), getDescription().getVersion());
        if (result.downloadUrl() != null && !result.downloadUrl().isBlank()) {
            reportUpdateMessage(requester, "update.download", Level.WARNING, result.downloadUrl());
        }
        if (result.changelog() != null && !result.changelog().isBlank()) {
            reportUpdateMessage(requester, "update.changelog", Level.INFO, result.changelog());
        }
    }

    private void reportUpdateMessage(CommandSender requester, String key, Level level, Object... arguments) {
        String message = languageManager.getMessage(key, arguments);
        if (requester == null) {
            getLogger().log(level, message);
        } else {
            requester.sendMessage(message);
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
