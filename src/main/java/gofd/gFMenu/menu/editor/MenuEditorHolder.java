package gofd.gFMenu.menu.editor;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/** Identifies a transient editor inventory and its owner. */
public final class MenuEditorHolder implements InventoryHolder {

    private final String menuName;
    private final UUID ownerId;
    private Inventory inventory;

    public MenuEditorHolder(String menuName, UUID ownerId) {
        this.menuName = menuName;
        this.ownerId = ownerId;
    }

    public String getMenuName() {
        return menuName;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
