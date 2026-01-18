/*    */ package gofd.gFMenu.menu.events;
/*    */ 
/*    */ import gofd.gFMenu.menu.MenuManager;
/*    */ import gofd.gFMenu.menu.actions.ActionEngine;
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.player.AsyncPlayerChatEvent;
/*    */ 
/*    */ public class ChatListener implements Listener {
/*    */   private final MenuManager menuManager;
/*    */   
/*    */   public ChatListener(MenuManager menuManager) {
/* 15 */     this.menuManager = menuManager;
/*    */   }
/*    */   
/*    */   @EventHandler
/*    */   public void onPlayerChat(AsyncPlayerChatEvent event) {
/* 20 */     Player player = event.getPlayer();
/* 21 */     ActionEngine actionEngine = this.menuManager.getActionEngine();
/*    */ 
/*    */     
/* 24 */     if (actionEngine.hasActiveCatcher(player)) {
/*    */       
/* 26 */       event.setCancelled(true);
/*    */ 
/*    */       
/* 29 */       String message = event.getMessage();
/*    */ 
/*    */       
/* 32 */       Bukkit.getScheduler().runTask(this.menuManager
/* 33 */           .getActionEngine().getPlugin(), () -> actionEngine.endCatcherSession(player, message));
/*    */     } 
/*    */   }
/*    */ }


