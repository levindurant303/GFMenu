/*    */ package gofd.gFMenu.menu.actions.actions;
/*    */ 
/*    */ import gofd.gFMenu.menu.actions.Action;
/*    */ import gofd.gFMenu.menu.actions.ActionEngine;
/*    */ import java.util.Map;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ public class CloseAction
/*    */   implements Action
/*    */ {
/*    */   public void execute(Player player, String action, ActionEngine engine, Map<String, String> variables) {
/* 12 */     player.closeInventory();
/*    */   }
/*    */ 
/*    */   
/*    */   public boolean canExecute(Player player, String action) {
/* 17 */     return action.equals("close");
/*    */   }
/*    */ }

