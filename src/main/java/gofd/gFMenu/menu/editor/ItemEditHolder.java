package gofd.gFMenu.menu.editor;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/** Identifies the book-based settings panel for one menu slot. */
public final class ItemEditHolder implements InventoryHolder {

    private final String menuName;
    private final UUID ownerId;
    private final int slot;
    private Inventory inventory;

    public ItemEditHolder(String menuName, UUID ownerId, int slot) {
        this.menuName = menuName;
        this.ownerId = ownerId;
        this.slot = slot;
    }

    public String getMenuName() {
        return menuName;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public int getSlot() {
        return slot;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
