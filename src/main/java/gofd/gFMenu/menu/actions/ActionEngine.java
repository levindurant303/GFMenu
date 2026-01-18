/*     */ package gofd.gFMenu.menu.actions;
/*     */ 
/*     */ import gofd.gFMenu.GFMenu;
/*     */ import gofd.gFMenu.menu.MenuManager;
/*     */ import gofd.gFMenu.menu.actions.actions.CatcherSession;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collections;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import org.bukkit.Sound;
import org.bukkit.entity.Player;
/*     */ import org.bukkit.event.inventory.ClickType;
/*     */ import org.bukkit.event.inventory.InventoryClickEvent;
/*     */ import org.bukkit.plugin.Plugin;
/*     */ import org.jetbrains.annotations.NotNull;
/*     */ 
/*     */ public class ActionEngine
/*     */ {
/*     */   private final GFMenu plugin;
/*     */   private final MenuManager menuManager;
/*     */   private final Map<String, CatcherSession> activeCatchers;
/*     */   
/*     */   public ActionEngine(GFMenu plugin, MenuManager menuManager) {
/*  24 */     this.plugin = plugin;
/*  25 */     this.menuManager = menuManager;
/*  26 */     this.activeCatchers = new HashMap<>();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean hasActiveCatcher(Player player) {
/*  33 */     String playerId = player.getUniqueId().toString();
/*  34 */     CatcherSession session = this.activeCatchers.get(playerId);
/*  35 */     return (session != null && session.isActive());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void cancelCatcherSession(Player player) {
/*  42 */     String playerId = player.getUniqueId().toString();
/*  43 */     CatcherSession session = this.activeCatchers.get(playerId);
/*  44 */     if (session != null) {
/*  45 */       session.cancel();
/*  46 */       this.activeCatchers.remove(playerId);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void startCatcherSession(Player player, String catcherId, List<String> startActions, List<String> endActions, List<String> cancelActions) {
/*  57 */     cancelCatcherSession(player);
/*     */ 
/*     */     
/*  60 */     CatcherSession session = new CatcherSession(this.plugin, this.menuManager, player, catcherId, startActions, endActions, cancelActions);
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  65 */     String playerId = player.getUniqueId().toString();
/*  66 */     this.activeCatchers.put(playerId, session);
/*     */     
/*  68 */     this.plugin.getLogger().info("开始Catcher会话: 玩家=" + player.getName() + ", ID=" + catcherId);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void endCatcherSession(Player player, String input) {
/*  76 */     String playerId = player.getUniqueId().toString();
/*  77 */     CatcherSession session = this.activeCatchers.get(playerId);
/*     */     
/*  79 */     if (session != null) {
/*     */       
/*  81 */       session.handleChatInput(input);
/*  82 */       this.activeCatchers.remove(playerId);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public CatcherSession getCatcherSession(Player player) {
/*  90 */     String playerId = player.getUniqueId().toString();
/*  91 */     return this.activeCatchers.get(playerId);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void cleanupTimeoutSessions() {
/*  98 */     List<String> toRemove = new ArrayList<>();
/*     */     
/* 100 */     for (Map.Entry<String, CatcherSession> entry : this.activeCatchers.entrySet()) {
/* 101 */       CatcherSession session = entry.getValue();
/*     */       
/* 103 */       if (session.isTimeout()) {
/* 104 */         session.forceEnd();
/* 105 */         toRemove.add(entry.getKey());
/*     */       } 
/*     */     } 
/*     */     
/* 109 */     for (String key : toRemove) {
/* 110 */       this.activeCatchers.remove(key);
/*     */     }
/*     */     
/* 113 */     if (!toRemove.isEmpty()) {
/* 114 */       this.plugin.getLogger().info("清理了 " + toRemove.size() + " 个超时的Catcher会话");
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void handleCatcherAction(Player player, List<String> catcherConfig) {
/*     */     try {
/* 123 */       String catcherId = "default";
/* 124 */       List<String> startActions = null;
/* 125 */       List<String> endActions = null;
/* 126 */       List<String> cancelActions = null;
/*     */ 
/*     */       
/* 129 */       for (String line : catcherConfig) {
/* 130 */         if (line.contains("catcher:")) {
/*     */           
/* 132 */           int start = line.indexOf("catcher:") + 8;
/* 133 */           int end = line.indexOf(':', start);
/* 134 */           if (end > start)
/* 135 */             catcherId = line.substring(start, end).trim();  continue;
/*     */         } 
/* 137 */         if (line.contains("type: CHAT"))
/*     */           continue; 
/* 139 */         if (line.contains("start:")) {
/* 140 */           if (line.contains("tell:")) {
/* 141 */             String message = line.substring(line.indexOf("tell:") + 5).trim();
/* 142 */             startActions = Collections.singletonList("tell: " + message);
/*     */           }  continue;
/* 144 */         }  if (line.contains("cancel:")) {
/* 145 */           if (line.contains("tell:")) {
/* 146 */             String message = line.substring(line.indexOf("tell:") + 5).trim();
/* 147 */             cancelActions = Collections.singletonList("tell: " + message);
/*     */           }  continue;
/* 149 */         }  if (line.contains("end:"));
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 156 */       if (startActions != null) {
/* 157 */         startCatcherSession(player, catcherId, startActions, 
/* 158 */             (endActions != null) ? endActions : new ArrayList<>(), 
/* 159 */             (cancelActions != null) ? cancelActions : new ArrayList<>());
/*     */       }
/*     */     }
/* 162 */     catch (Exception e) {
/* 163 */       this.plugin.getLogger().severe("处理Catcher动作时出错: " + e.getMessage());
/* 164 */       e.printStackTrace();
/*     */     } 
/*     */   }
/*     */
/*     */
/*     */ 
/*     */   
/* 172 */ public void executeSingleAction(Player player, String action) {
/* 173 */   if (action == null || action.trim().isEmpty())
/* 174 */     return;
/* 175 */   String trimmed = action.trim();
/* 176 */
/* 177 */   try {
/* 178 */     if (trimmed.equals("close")) {
/* 179 */       player.closeInventory();
/* 180 */       return;
/* 181 */     }
/* 182 */
/* 183 */     if (trimmed.startsWith("command:")) {
/* 184 */       String command = trimmed.substring(8).trim();
/* 185 */       player.performCommand(command);
/* 186 */       return;
/* 187 */     }
/* 188 */
/* 189 */     if (trimmed.startsWith("op:")) {
/* 190 */       String command = trimmed.substring(3).trim();
/* 191 */       boolean wasOp = player.isOp();
/* 192 */       try {
/* 193 */         player.setOp(true);
/* 194 */         player.performCommand(command);
/* 195 */       } finally {
/* 196 */         player.setOp(wasOp);
/* 197 */       }
/* 198 */       return;
/* 199 */     }
/* 200 */
/* 201 */     if (trimmed.startsWith("tell:") || trimmed.startsWith("message:")) {
/* 202 */       int colonIndex = trimmed.indexOf(':');
/* 203 */       String message = trimmed.substring(colonIndex + 1).trim();
/* 204 */       player.sendMessage(message.replace("&", "§"));
/* 205 */       return;
/* 206 */     }
/* 207 */
/* 208 */     if (trimmed.startsWith("chat:")) {
/* 209 */       String message = trimmed.substring(5).trim();
/* 210 */       player.chat(message);
/* 211 */       return;
/* 212 */     }
/* 213 */
/* 214 */     if (trimmed.startsWith("menu:")) {
/* 215 */       String menuName = trimmed.substring(5).trim();
/* 216 */       this.menuManager.openMenu(player, menuName);
/* 217 */       return;
/* 218 */     }
/* 219 */
/* 220 */     // 新增：处理 sound: 动作
/* 221 */     if (trimmed.startsWith("sound:")) {
/* 222 */       String soundData = trimmed.substring(6).trim();
/* 223 */       String[] parts = soundData.split("-");
/* 224 */       String soundName = parts[0].trim();
/* 225 */       float volume = (parts.length > 1) ? Float.parseFloat(parts[1]) : 1.0F;
/* 226 */       float pitch = (parts.length > 2) ? Float.parseFloat(parts[2]) : 1.0F;
/* 227 */
/* 228 */       Sound sound = null;
/* 229 */       try {
/* 230 */         sound = Sound.valueOf(soundName.toUpperCase());
/* 231 */       } catch (IllegalArgumentException e) {
/* 232 */         for (Sound s : Sound.values()) {
/* 233 */           if (s.toString().equalsIgnoreCase(soundName.replace(":", "_").toLowerCase())) {
/* 234 */             sound = s;
/* 235 */             break;
/* 236 */           }
/* 237 */         }
/* 238 */       }
/* 239 */
/* 240 */       if (sound != null) {
/* 241 */         player.playSound(player.getLocation(), sound, volume, pitch);
/* 242 */       }
/* 243 */       return;
/* 244 */     }
/* 245 */
/* 246 */     if (trimmed.contains("catcher:")) {
/* 247 */       player.sendMessage("§a请输入内容（输入 'cancel' 取消）:");
/* 248 */       player.sendMessage("§7等待输入中...");
/* 249 */       return;
/* 250 */     }
/* 251 */
/* 252 */     this.plugin.getLogger().warning("未知的动作类型: " + action);
/* 253 */   } catch (Exception e) {
/* 254 */     this.plugin.getLogger().severe("执行动作时出错: " + action);
/* 255 */     e.printStackTrace();
/* 256 */   }
/* 257 */ }
/*     */   public void executeActions(Player player, List<String> actions) {
/* 246 */     if (actions == null || actions.isEmpty())
/*     */       return; 
/* 248 */     for (String action : actions) {
/* 249 */       executeSingleAction(player, action);
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void handleItemClick(InventoryClickEvent event, int slot, Map<String, List<String>> actions) {
/*     */     String clickType;
/* 257 */     Player player = (Player)event.getWhoClicked();
/*     */ 
/*     */     
/* 260 */     event.setCancelled(true);
/*     */ 
/*     */ 
/*     */     
/* 264 */     switch (event.getClick()) {
/*     */       case LEFT:
/* 266 */         clickType = "left";
/*     */         break;
/*     */       case RIGHT:
/* 269 */         clickType = "right";
/*     */         break;
/*     */       default:
/* 272 */         clickType = "all";
/*     */         break;
/*     */     } 
/*     */     
/* 276 */     List<String> clickActions = null;
/*     */ 
/*     */     
/* 279 */     if (actions.containsKey(clickType)) {
/* 280 */       clickActions = actions.get(clickType);
/*     */     
/*     */     }
/* 283 */     else if (actions.containsKey("all")) {
/* 284 */       clickActions = actions.get("all");
/*     */     } 
/*     */ 
/*     */     
/* 288 */     if (clickActions != null) {
/*     */       
/* 290 */       for (String action : clickActions) {
/* 291 */         if (action.contains("catcher:")) {
/*     */           
/* 293 */           player.closeInventory();
/* 294 */           handleCatcherAction(player, clickActions);
/*     */           
/*     */           return;
/*     */         } 
/*     */       } 
/*     */

/* 300 */       executeActions(player, clickActions);
/*     */     } 
/*     */   }
/*     */   @NotNull
/*     */   public Plugin getPlugin() {
/* 305 */     return null;
/*     */   }
/*     */ }
