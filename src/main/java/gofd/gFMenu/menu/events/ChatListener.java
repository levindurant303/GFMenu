package gofd.gFMenu.menu.events;

import gofd.gFMenu.menu.MenuManager;
import gofd.gFMenu.menu.actions.ActionEngine;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** Transfers async chat input back to the main thread for catcher processing. */
public final class ChatListener implements Listener {

    private final MenuManager menuManager;

    public ChatListener(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ActionEngine actionEngine = menuManager.getActionEngine();
        if (!actionEngine.hasActiveCatcher(player)) {
            return;
        }
        event.setCancelled(true);
        String input = event.getMessage();
        Bukkit.getScheduler().runTask(actionEngine.getPlugin(), () -> actionEngine.endCatcherSession(player, input));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        menuManager.getActionEngine().cancelInputSessions(event.getPlayer());
    }
}
