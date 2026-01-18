/*    */ package gofd.gFMenu.menu.actions.actions;
/*    */ 
/*    */ import gofd.gFMenu.menu.actions.Action;
/*    */ import gofd.gFMenu.menu.actions.ActionEngine;
/*    */ import java.util.Map;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ public class MessageAction
/*    */   implements Action
/*    */ {
/*    */   public void execute(Player player, String action, ActionEngine engine, Map<String, String> variables) {
/* 12 */     String message = action.substring(5).trim();
/* 13 */     player.sendMessage(message.replace("&", "§"));
/*    */   }
/*    */ 
/*    */   
/*    */   public boolean canExecute(Player player, String action) {
/* 18 */     return action.startsWith("tell:");
/*    */   }
/*    */ }
