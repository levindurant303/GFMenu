package gofd.gFMenu.menu.events;

import gofd.gFMenu.menu.LayoutMenuData;
import gofd.gFMenu.menu.LayoutMenuItem;
import gofd.gFMenu.menu.MenuInventoryHolder;
import gofd.gFMenu.menu.MenuManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

/** Handles only inventories created by GFMenu, never inventories matched by title text. */
public final class MenuListener implements Listener {

    private final MenuManager menuManager;

    public MenuListener(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder() instanceof MenuInventoryHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player) || event.getRawSlot() < 0
                || event.getRawSlot() >= topInventory.getSize()) {
            return;
        }

        LayoutMenuItem item = holder.getMenu().getItemAtSlot(event.getRawSlot());
        if (item != null) {
            menuManager.getActionEngine().handleItemClick(event, item.getActions());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory.getHolder() instanceof MenuInventoryHolder
                && event.getRawSlots().stream().anyMatch(slot -> slot < topInventory.getSize())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuInventoryHolder holder)
                || !(event.getPlayer() instanceof Player player)) {
            return;
        }
        LayoutMenuData menu = holder.getMenu();
        menuManager.getActionEngine().executeActions(player, menu.getCloseEvents());
    }
}
