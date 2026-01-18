/*     */ package gofd.gFMenu.menu.actions.actions;
/*     */ 
/*     */ import gofd.gFMenu.GFMenu;
/*     */ import gofd.gFMenu.menu.MenuManager;
/*     */ import gofd.gFMenu.menu.actions.ActionEngine;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ import org.bukkit.entity.Player;

/*     */ public class CatcherSession
/*     */ {
/*     */   private final GFMenu plugin;
/*     */   private final MenuManager menuManager;
/*     */   private final Player player;
/*     */   private final String catcherId;
/*     */   private final List<String> startActions;
/*     */   private final List<String> endActions;
/*     */   private final List<String> cancelActions;
/*     */   private final Map<String, Object> metaData;
/*     */   private final long startTime;
/*     */   private boolean active;
/*     */   
/*     */   public CatcherSession(GFMenu plugin, MenuManager menuManager, Player player, String catcherId, List<String> startActions, List<String> endActions, List<String> cancelActions) {
/*  31 */     this.plugin = plugin;
/*  32 */     this.menuManager = menuManager;
/*  33 */     this.player = player;
/*  34 */     this.catcherId = catcherId;
/*  35 */     this.startActions = startActions;
/*  36 */     this.endActions = endActions;
/*  37 */     this.cancelActions = cancelActions;
/*  38 */     this.metaData = new ConcurrentHashMap<>();
/*  39 */     this.startTime = System.currentTimeMillis();
/*  40 */     this.active = true;
/*     */ 
/*     */     
/*  43 */     executeStartActions();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void executeStartActions() {
/*  50 */     if (this.startActions != null && !this.startActions.isEmpty()) {
/*  51 */       ActionEngine engine = this.menuManager.getActionEngine();
/*  52 */       for (String action : this.startActions) {
/*  53 */         engine.executeSingleAction(this.player, action);
/*     */       }
/*     */     } 
/*     */ 
/*     */     
/*  58 */     this.player.sendMessage("§a请输入内容（输入 'cancel' 取消）:");
/*  59 */     this.player.sendMessage("§7等待输入中...");
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean handleChatInput(String message) {
/*  66 */     if (!this.active) return false;
/*     */ 
/*     */     
/*  69 */     if (message.equalsIgnoreCase("cancel")) {
/*  70 */       cancel();
/*  71 */       return true;
/*     */     } 
/*     */ 
/*     */     
/*  75 */     this.metaData.put("input", message);
/*  76 */     this.metaData.put("input_" + this.catcherId, message);
/*  77 */     this.metaData.put("catcher_input", message);
/*     */ 
/*     */     
/*  80 */     this.active = false;
/*     */ 
/*     */     
/*  83 */     executeEndActionsWithPlaceholders(message);
/*     */     
/*  85 */     return true;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void executeEndActionsWithPlaceholders(String input) {
/*  92 */     if (this.endActions != null && !this.endActions.isEmpty()) {
/*  93 */       ActionEngine engine = this.menuManager.getActionEngine();
/*     */       
/*  95 */       for (String action : this.endActions) {
/*     */         
/*  97 */         String processedAction = action;
/*     */ 
/*     */         
/* 100 */         processedAction = processedAction.replace("%trmenu_meta_input-" + this.catcherId + "%", input);
/*     */ 
/*     */         
/* 103 */         processedAction = processedAction.replace("{input}", input);
/*     */ 
/*     */         
/* 106 */         processedAction = processedAction.replace("%input%", input);
/* 107 */         processedAction = processedAction.replace("%player_input%", input);
/*     */ 
/*     */         
/* 110 */         engine.executeSingleAction(this.player, processedAction);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void cancel() {
/* 119 */     if (!this.active)
/*     */       return; 
/* 121 */     this.active = false;
/*     */ 
/*     */     
/* 124 */     if (this.cancelActions != null && !this.cancelActions.isEmpty()) {
/* 125 */       ActionEngine engine = this.menuManager.getActionEngine();
/* 126 */       for (String action : this.cancelActions) {
/* 127 */         engine.executeSingleAction(this.player, action);
/*     */       }
/*     */     } 
/*     */     
/* 131 */     this.player.sendMessage("§c输入已取消。");
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void forceEnd() {
/* 138 */     if (!this.active)
/*     */       return; 
/* 140 */     this.active = false;
/* 141 */     this.player.sendMessage("§c输入会话已超时或结束。");
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean isTimeout() {
/* 148 */     return (System.currentTimeMillis() - this.startTime > 30000L);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Player getPlayer() {
/* 155 */     return this.player;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String getCatcherId() {
/* 162 */     return this.catcherId;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean isActive() {
/* 169 */     return this.active;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Map<String, Object> getMetaData() {
/* 176 */     return this.metaData;
/*     */   }
/*     */ }

