/*    */ package gofd.gFMenu.menu.actions.actions;
/*    */ 
/*    */ import gofd.gFMenu.menu.actions.Action;
/*    */ import gofd.gFMenu.menu.actions.ActionEngine;
/*    */ import java.util.Map;
/*    */ import org.bukkit.Sound;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ public class SoundAction
/*    */   implements Action
/*    */ {
/*    */   public void execute(Player player, String action, ActionEngine engine, Map<String, String> variables) {
/* 13 */     String soundData = action.substring(6).trim();
/*    */ 
/*    */     
/*    */     try {
/* 17 */       String[] parts = soundData.split("-");
/* 18 */       if (parts.length >= 1) {
/* 19 */         String soundName = parts[0].trim();
/* 20 */         float volume = (parts.length > 1) ? Float.parseFloat(parts[1]) : 1.0F;
/* 21 */         float pitch = (parts.length > 2) ? Float.parseFloat(parts[2]) : 1.0F;
/*    */ 
/*    */
            /* 24 */
            Sound sound = null;
            /*    */
            try {
                /* 26 */
                sound = Sound.valueOf(soundName.toUpperCase());
                /* 27 */
            } catch (IllegalArgumentException e) {
                /* 28 */
                for (Sound s : Sound.values()) {
                    /* 29 */
                    if (s.toString().equalsIgnoreCase(soundName.replace(":", "_").toLowerCase())) {
                        /* 30 */
                        sound = s;
/*    */               
/*    */               break;
/*    */             } 
/*    */           } 
/*    */         } 
/* 36 */         if (sound != null) {
/* 37 */           player.playSound(player.getLocation(), sound, volume, pitch);
/*    */         }
/*    */       } 
/* 40 */     } catch (Exception exception) {}
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public boolean canExecute(Player player, String action) {
/* 47 */     return action.startsWith("sound:");
/*    */   }
/*    */ }

