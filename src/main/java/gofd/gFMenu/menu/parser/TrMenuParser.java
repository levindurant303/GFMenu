/*     */ package gofd.gFMenu.menu.parser;
/*     */ 
/*     */ import gofd.gFMenu.menu.LayoutMenuData;
/*     */ import gofd.gFMenu.menu.LayoutMenuItem;
/*     */ import gofd.gFMenu.menu.format.MenuFormat;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import org.bukkit.configuration.ConfigurationSection;
/*     */ import org.bukkit.configuration.file.YamlConfiguration;
/*     */ 
/*     */ public class TrMenuParser
/*     */   implements MenuParser {
/*     */   public MenuFormat getFormat() {
/*  15 */     return MenuFormat.TRMENU;
/*     */   }
/*     */ 
/*     */   
/*     */   public LayoutMenuData parse(String menuName, YamlConfiguration config) {
/*  20 */     LayoutMenuData menuData = new LayoutMenuData(menuName);
/*     */ 
/*     */     
/*  23 */     String title = config.getString("Title", "菜单");
/*  24 */     menuData.setTitle(title);
/*     */ 
/*     */     
/*  27 */     List<String> layoutRows = config.getStringList("layout");
/*  28 */     menuData.setRawLayout(layoutRows);
/*     */ 
/*     */     
/*  31 */     boolean centerEnabled = config.getBoolean("Settings.center", true);
/*  32 */     menuData.setCenterEnabled(centerEnabled);
/*     */ 
/*     */     
/*  35 */     Map<Character, Integer> slotMap = parseLayout(layoutRows, centerEnabled);
/*  36 */     menuData.setLayoutSlots(slotMap);
/*     */ 
/*     */     
/*  39 */     if (config.contains("Icons")) {
/*  40 */       ConfigurationSection iconsSection = config.getConfigurationSection("Icons");
/*  41 */       parseIcons(menuData, iconsSection);
/*     */     } 
/*     */ 
/*     */     
/*  45 */     if (config.contains("Bindings.Commands")) {
/*  46 */       List<String> commands = config.getStringList("Bindings.Commands");
/*  47 */       menuData.setCommands(commands);
/*     */     } 
/*     */ 
/*     */     
/*  51 */     if (config.contains("Settings.permission")) {
/*  52 */       menuData.setPermission(config.getString("Settings.permission"));
/*     */     }
/*     */ 
/*     */     
/*  56 */     if (config.contains("Events.Open")) {
/*  57 */       menuData.setOpenEvents(config.getStringList("Events.Open"));
/*     */     }
/*     */     
/*  60 */     if (config.contains("Events.Close")) {
/*  61 */       menuData.setCloseEvents(config.getStringList("Events.Close"));
/*     */     }
/*     */     
/*  64 */     return menuData;
/*     */   }
/*     */   
/*     */   private Map<Character, Integer> parseLayout(List<String> layoutRows, boolean centerEnabled) {
/*  68 */     Map<Character, Integer> slotMap = new HashMap<>();
/*     */     
/*  70 */     if (layoutRows == null || layoutRows.isEmpty()) {
/*  71 */       return slotMap;
/*     */     }
/*     */     
/*  74 */     for (int rowIndex = 0; rowIndex < layoutRows.size(); rowIndex++) {
/*  75 */       String row = layoutRows.get(rowIndex);
/*  76 */       String cleanRow = cleanRowString(row);
/*     */       
/*  78 */       if (!cleanRow.isEmpty()) {
/*     */ 
/*     */         
/*  81 */         int startColumn = centerEnabled ? ((9 - cleanRow.length()) / 2) : 0;
/*  82 */         startColumn = Math.max(startColumn, 0);
/*     */         
/*  84 */         for (int i = 0; i < cleanRow.length(); i++) {
/*  85 */           char ch = cleanRow.charAt(i);
/*  86 */           if (ch != ' ') {
/*  87 */             int slot = rowIndex * 9 + startColumn + i;
/*  88 */             slotMap.put(Character.valueOf(ch), Integer.valueOf(slot));
/*     */           } 
/*     */         } 
/*     */       } 
/*     */     } 
/*  93 */     return slotMap;
/*     */   }
/*     */   
/*     */   private void parseIcons(LayoutMenuData menuData, ConfigurationSection iconsSection) {
/*  97 */     for (String iconKey : iconsSection.getKeys(false)) {
/*  98 */       if (iconKey.length() != 1)
/*     */         continue; 
/* 100 */       char iconChar = iconKey.charAt(0);
/* 101 */       Integer slot = (Integer)menuData.getLayoutSlots().get(Character.valueOf(iconChar));
/*     */       
/* 103 */       if (slot == null) {
/* 104 */         System.err.println("[GFMenu] 警告: 图标 '" + iconChar + "' 没有对应的布局槽位");
/*     */         
/*     */         continue;
/*     */       } 
/* 108 */       ConfigurationSection iconConfig = iconsSection.getConfigurationSection(iconKey);
/* 109 */       LayoutMenuItem item = parseIconItem(slot.intValue(), iconChar, iconConfig);
/* 110 */       menuData.addItem(iconChar, item);
/*     */     } 
/*     */   }
/*     */   
/*     */   private LayoutMenuItem parseIconItem(int slot, char iconChar, ConfigurationSection config) {
/* 115 */     LayoutMenuItem item = new LayoutMenuItem();
/* 116 */     item.setSlot(slot);
/* 117 */     item.setIconChar(iconChar);
/*     */     
/* 119 */     if (config.contains("display")) {
/* 120 */       ConfigurationSection display = config.getConfigurationSection("display");
/* 121 */       item.setMaterial(display.getString("material", "STONE"));
/* 122 */       item.setName(display.getString("name", "物品"));
/* 123 */       item.setLore(display.getStringList("lore"));
/* 124 */       item.setAmount(display.getInt("amount", 1));
/*     */     } 
/*     */ 
/*     */     
/* 128 */     if (config.contains("actions")) {
/* 129 */       Object actionsObj = config.get("actions");
/* 130 */       if (actionsObj instanceof List) {
/* 131 */         item.setActions("all", config.getStringList("actions"));
/* 132 */       } else if (actionsObj instanceof ConfigurationSection) {
/* 133 */         ConfigurationSection actionsSec = (ConfigurationSection)actionsObj;
/* 134 */         for (String actionType : actionsSec.getKeys(false)) {
/* 135 */           item.setActions(actionType, actionsSec.getStringList(actionType));
/*     */         }
/*     */       } 
/*     */     } 
/*     */     
/* 140 */     return item;
/*     */   }
/*     */   
/*     */   private String cleanRowString(String row) {
/* 144 */     if (row == null) return ""; 
/* 145 */     String cleanRow = row.trim();
/*     */     
/* 147 */     if (cleanRow.startsWith("'") && cleanRow.endsWith("'")) {
/* 148 */       cleanRow = cleanRow.substring(1, cleanRow.length() - 1);
/* 149 */     } else if (cleanRow.startsWith("\"") && cleanRow.endsWith("\"")) {
/* 150 */       cleanRow = cleanRow.substring(1, cleanRow.length() - 1);
/*     */     } 
/*     */     
/* 153 */     return cleanRow;
/*     */   }
/*     */ }

