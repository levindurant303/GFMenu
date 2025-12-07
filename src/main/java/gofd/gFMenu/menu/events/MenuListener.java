package gofd.gFMenu.menu.events;

import gofd.gFMenu.menu.LayoutMenuData;
import gofd.gFMenu.menu.LayoutMenuItem;
import gofd.gFMenu.menu.MenuManager;
import gofd.gFMenu.menu.actions.ActionEngine;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryView;

import java.util.List;

public class MenuListener implements Listener {

    private final MenuManager menuManager;

    public MenuListener(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        InventoryView view = event.getView();
        String title = view.getTitle(); // 使用getTitle()而不是getName()

        // 查找对应的菜单
        for (LayoutMenuData menu : menuManager.getLoadedMenus().values()) {
            String menuTitle = menu.getTitle().replace("&", "§");

            if (title.equals(menuTitle)) {
                event.setCancelled(true);

                int slot = event.getRawSlot();
                if (slot < 0 || slot >= event.getInventory().getSize()) return;

                LayoutMenuItem menuItem = menuManager.getMenuItem(menu, slot);
                if (menuItem != null && menuItem.hasActions("all")) {
                    String clickType = event.getClick().toString().toLowerCase().contains("right") ? "right" : "left";
                    List<String> actions = menuItem.getActions(clickType);

                    // 使用动作引擎执行动作
                    ActionEngine engine = menuManager.getActionEngine();
                    if (engine != null) {
                        engine.executeActions(player, actions, menu);
                    }
                }
                break;
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        // 可以在这里处理打开事件
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // 可以在这里处理关闭事件
    }
}