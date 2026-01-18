/*     */ package gofd.gFMenu.menu;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.inventory.Inventory;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ 
/*     */ public class LayoutMenuData
/*     */ {
/*     */   private final String name;
/*     */   private String title;
/*     */   private Map<Character, Integer> layoutSlots;
/*     */   private List<String> commands;
/*     */   private String permission;
/*     */   private List<String> openEvents;
/*     */   private List<String> closeEvents;
/*     */   private final Map<Character, LayoutMenuItem> items;
/*     */   private List<String> rawLayout;
/*     */   private boolean centerEnabled = false;
/*  24 */   private int menuSize = 54;
/*     */   
/*     */   public LayoutMenuData(String name) {
/*  27 */     this.name = name;
/*  28 */     this.layoutSlots = new HashMap<>();
/*  29 */     this.commands = new ArrayList<>();
/*  30 */     this.openEvents = new ArrayList<>();
/*  31 */     this.closeEvents = new ArrayList<>();
/*  32 */     this.items = new HashMap<>();
/*  33 */     this.rawLayout = new ArrayList<>();
/*  34 */     this.title = "菜单";
/*  35 */     this.permission = null;
/*     */   }
/*     */   public void open(Player player) {
/*     */     int size;
/*  39 */     if (this.permission != null && !player.hasPermission(this.permission)) {
/*  40 */       player.sendMessage("§c你没有权限打开这个菜单！");
/*     */ 
/*     */       
/*     */       return;
/*     */     } 
/*     */     
/*  46 */     if (this.rawLayout.isEmpty()) {
/*  47 */       size = this.menuSize;
/*     */     } else {
/*  49 */       size = Math.min(this.rawLayout.size() * 9, 54);
/*     */     } 
/*     */     
/*  52 */     Inventory inventory = Bukkit.createInventory(null, size, this.title);
/*     */ 
/*     */     
/*  55 */     for (LayoutMenuItem item : this.items.values()) {
/*  56 */       ItemStack stack = item.toItemStack();
/*  57 */       if (stack != null) {
/*  58 */         int slot = item.getSlot();
/*  59 */         if (slot >= 0 && slot < size) {
/*  60 */           inventory.setItem(slot, stack);
/*     */         }
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/*  66 */     executeEvents(player, this.openEvents);
/*     */ 
/*     */     
/*  69 */     player.openInventory(inventory);
/*     */     
/*  71 */     logMenuOpen();
/*     */   }
/*     */   
/*     */   private void executeEvents(Player player, List<String> events) {
/*  75 */     if (events == null || events.isEmpty())
/*     */       return; 
/*  77 */     for (String event : events) {
/*  78 */       if (event.startsWith("sound:")) {
/*  79 */         String str = event.substring(6).trim(); continue;
/*     */       } 
/*  81 */       if (event.startsWith("tell:")) {
/*  82 */         player.sendMessage(event.substring(5).trim().replace("&", "§"));
/*     */       }
/*     */     } 
/*     */   }
/*     */   
/*     */   private void logMenuOpen() {
/*  88 */     StringBuilder sb = new StringBuilder();
/*  89 */     sb.append("\n§7========== 菜单信息 ==========\n");
/*  90 */     sb.append("§7菜单名称: §f").append(this.name).append("\n");
/*  91 */     sb.append("§7标题: §f").append(this.title).append("\n");
/*  92 */     sb.append("§7图标数量: §f").append(this.items.size()).append("\n");
/*     */     
/*  94 */     if (!this.rawLayout.isEmpty()) {
/*  95 */       sb.append("§7布局类型: §fTrMenu格式\n");
/*  96 */       sb.append("§7居中模式: §").append(this.centerEnabled ? "a启用" : "c禁用").append("\n");
/*     */     } else {
/*  98 */       sb.append("§7布局类型: §fDeluxeMenus格式\n");
/*  99 */       sb.append("§7菜单大小: §f").append(this.menuSize).append(" 槽位\n");
/*     */     } 
/*     */     
/* 102 */     sb.append("§7===================================");
/* 103 */     Bukkit.getLogger().info(sb.toString().replace("§", ""));
/*     */   }
/*     */   
/*     */   public void addItem(char iconChar, LayoutMenuItem item) {
/* 107 */     this.items.put(Character.valueOf(iconChar), item);
/*     */   }
/*     */   
/*     */   public String getName() {
/* 111 */     return this.name;
/* 112 */   } public String getTitle() { return this.title; }
/* 113 */   public void setTitle(String title) { this.title = title; }
/* 114 */   public Map<Character, Integer> getLayoutSlots() { return this.layoutSlots; }
/* 115 */   public void setLayoutSlots(Map<Character, Integer> layoutSlots) { this.layoutSlots = layoutSlots; }
/* 116 */   public List<String> getCommands() { return this.commands; }
/* 117 */   public void setCommands(List<String> commands) { this.commands = commands; }
/* 118 */   public String getPermission() { return this.permission; }
/* 119 */   public void setPermission(String permission) { this.permission = permission; }
/* 120 */   public List<String> getOpenEvents() { return this.openEvents; }
/* 121 */   public void setOpenEvents(List<String> openEvents) { this.openEvents = openEvents; }
/* 122 */   public List<String> getCloseEvents() { return this.closeEvents; }
/* 123 */   public void setCloseEvents(List<String> closeEvents) { this.closeEvents = closeEvents; }
/* 124 */   public Map<Character, LayoutMenuItem> getItems() { return this.items; }
/* 125 */   public List<String> getRawLayout() { return this.rawLayout; }
/* 126 */   public void setRawLayout(List<String> rawLayout) { this.rawLayout = rawLayout; }
/* 127 */   public boolean isCenterEnabled() { return this.centerEnabled; }
/* 128 */   public void setCenterEnabled(boolean centerEnabled) { this.centerEnabled = centerEnabled; }
/* 129 */   public int getMenuSize() { return this.menuSize; } public void setMenuSize(int menuSize) {
/* 130 */     this.menuSize = menuSize;
/*     */   }
/*     */ }

