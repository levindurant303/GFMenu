package gofd.gFMenu.menu.events;

import gofd.gFMenu.menu.MenuManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;

/** Completes a book-input action when the player confirms the book edit. */
public final class BookListener implements Listener {

    private final MenuManager menuManager;

    public BookListener(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent event) {
        if (!menuManager.getActionEngine().hasActiveBookInput(event.getPlayer())) {
            return;
        }
        menuManager.getActionEngine().finishBookInput(event.getPlayer(), event.getNewBookMeta().getPages());
    }
}
