/*    */ package gofd.gFMenu.menu.events;
/*    */ 
/*    */ import gofd.gFMenu.menu.LayoutMenuData;
/*    */ import gofd.gFMenu.menu.LayoutMenuItem;
/*    */ import gofd.gFMenu.menu.MenuManager;
/*    */ import java.util.Map;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.inventory.InventoryClickEvent;
/*    */ import org.bukkit.event.inventory.InventoryCloseEvent;
/*    */ import org.bukkit.event.inventory.InventoryOpenEvent;
/*    */ 
/*    */ public class MenuListener
/*    */   implements Listener {
/*    */   public MenuListener(MenuManager menuManager) {
/* 17 */     this.menuManager = menuManager;
/*    */   }
/*    */   private final MenuManager menuManager;
/*    */   @EventHandler
/*    */   public void onInventoryClick(InventoryClickEvent event) {
/* 22 */     if (!(event.getWhoClicked() instanceof Player)) {
/*    */       return;
/*    */     }
/*    */     
/* 26 */     Player player = (Player)event.getWhoClicked();
/*    */ 
/*    */     
/* 29 */     String inventoryTitle = event.getView().getTitle();
/* 30 */     int slot = event.getSlot();
/*    */ 
/*    */     
/* 33 */     for (Map.Entry<String, LayoutMenuData> entry : (Iterable<Map.Entry<String, LayoutMenuData>>)this.menuManager.getAllMenuData().entrySet()) {
/* 34 */       String menuTitle = ((LayoutMenuData)entry.getValue()).getTitle();
/*    */       
/* 36 */       if (inventoryTitle.equals(menuTitle)) {
/*    */         
/* 38 */         LayoutMenuItem menuItem = this.menuManager.getMenuItem(entry.getValue(), slot);
/*    */         
/* 40 */         if ((menuItem != null && menuItem.hasActions("left")) || menuItem.hasActions("right") || menuItem.hasActions("all"))
/*    */         {
/* 42 */           this.menuManager.getActionEngine().handleItemClick(event, slot, menuItem.getActions());
/*    */         }
/*    */ 
/*    */         
/* 46 */         event.setCancelled(true);
/*    */         return;
/*    */       } 
/*    */     } 
/*    */   }
/*    */   
/*    */   @EventHandler
/*    */   public void onInventoryOpen(InventoryOpenEvent event) {}
/*    */   
/*    */   @EventHandler
/*    */   public void onInventoryClose(InventoryCloseEvent event) {}
/*    */ }

