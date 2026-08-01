package gofd.gFMenu.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Identifies a live GFMenu inventory without relying on its visible title. */
public final class MenuInventoryHolder implements InventoryHolder {

    private final LayoutMenuData menu;
    private Inventory inventory;

    public MenuInventoryHolder(LayoutMenuData menu) {
        this.menu = menu;
    }

    public LayoutMenuData getMenu() {
        return menu;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
