/*    */ package gofd.gFMenu.menu.actions.actions;
/*    */ 
/*    */ import gofd.gFMenu.menu.actions.Action;
/*    */ import gofd.gFMenu.menu.actions.ActionEngine;
/*    */ import java.util.Map;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ public class CommandAction
/*    */   implements Action
/*    */ {
/*    */   public void execute(Player player, String action, ActionEngine engine, Map<String, String> variables) {
/* 12 */     String command = action.substring(8).trim();
/*    */ 
/*    */     
/* 15 */     if (command.startsWith("/")) {
/* 16 */       command = command.substring(1);
/*    */     }
/*    */     
/* 19 */     player.performCommand(command);
/*    */   }
/*    */ 
/*    */   
/*    */   public boolean canExecute(Player player, String action) {
/* 24 */     return action.startsWith("command:");
/*    */   }
/*    */ }
