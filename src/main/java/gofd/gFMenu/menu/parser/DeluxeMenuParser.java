/*     */ package gofd.gFMenu.menu.parser;
/*     */ 
/*     */ import gofd.gFMenu.menu.LayoutMenuData;
/*     */ import gofd.gFMenu.menu.LayoutMenuItem;
/*     */ import gofd.gFMenu.menu.format.MenuFormat;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import org.bukkit.configuration.ConfigurationSection;
/*     */ import org.bukkit.configuration.file.YamlConfiguration;
/*     */ 
/*     */ public class DeluxeMenuParser
/*     */   implements MenuParser
/*     */ {
/*     */   public MenuFormat getFormat() {
/*  15 */     return MenuFormat.DELUXE;
/*     */   }
/*     */ 
/*     */   
/*     */   public LayoutMenuData parse(String menuName, YamlConfiguration config) {
/*  20 */     LayoutMenuData menuData = new LayoutMenuData(menuName);
/*     */ 
/*     */     
/*  23 */     String title = config.getString("menu_title", "菜单");
/*  24 */     menuData.setTitle(title.replace("&", "§"));
/*     */ 
/*     */     
/*  27 */     menuData.setCenterEnabled(false);
/*     */ 
/*     */     
/*  30 */     String openCommand = config.getString("open_command");
/*  31 */     if (openCommand != null && !openCommand.isEmpty()) {
/*  32 */       List<String> commands = new ArrayList<>();
/*  33 */       commands.add(openCommand);
/*  34 */       menuData.setCommands(commands);
/*     */     } 
/*     */ 
/*     */     
/*  38 */     if (config.getBoolean("open_requires_permission", false)) {
/*  39 */       menuData.setPermission(config.getString("open_permission"));
/*     */     }
/*     */ 
/*     */     
/*  43 */     int menuSize = config.getInt("menu_size", 54);
/*  44 */     List<String> layoutRows = generateVirtualLayout(menuSize);
/*  45 */     menuData.setRawLayout(layoutRows);
/*     */ 
/*     */     
/*  48 */     char iconChar = 'A';
/*  49 */     for (String itemKey : config.getKeys(false)) {
/*  50 */       ConfigurationSection itemConfig = config.getConfigurationSection(itemKey);
/*     */ 
/*     */       
/*  53 */       if (itemConfig == null || !itemConfig.contains("slot")) {
/*     */         continue;
/*     */       }
/*     */ 
/*     */       
/*  58 */       LayoutMenuItem item = parseDeluxeItem(iconChar, itemConfig);
/*  59 */       if (item != null) {
/*  60 */         menuData.addItem(iconChar, item);
/*  61 */         iconChar = (char)(iconChar + 1);
/*     */       } 
/*     */     } 
/*     */     
/*  65 */     return menuData;
/*     */   }
/*     */   
/*     */   private LayoutMenuItem parseDeluxeItem(char iconChar, ConfigurationSection config) {
/*  69 */     LayoutMenuItem item = new LayoutMenuItem();
/*     */ 
/*     */     
/*  72 */     int slot = config.getInt("slot", 0);
/*  73 */     if (slot < 0 || slot >= 54) {
/*  74 */       return null;
/*     */     }
/*  76 */     item.setSlot(slot);
/*  77 */     item.setIconChar(iconChar);
/*     */ 
/*     */     
/*  80 */     String material = config.getString("material", "STONE");
/*  81 */     item.setMaterial(material);
/*     */ 
/*     */     
/*  84 */     String name = config.getString("display_name", "物品");
/*  85 */     item.setName(name.replace("&", "§"));
/*     */ 
/*     */     
/*  88 */     List<String> lore = config.getStringList("lore");
/*  89 */     List<String> coloredLore = new ArrayList<>();
/*  90 */     for (String line : lore) {
/*  91 */       coloredLore.add(line.replace("&", "§"));
/*     */     }
/*  93 */     item.setLore(coloredLore);
/*     */ 
/*     */     
/*  96 */     item.setAmount(config.getInt("amount", 1));
/*     */ 
/*     */     
/*  99 */     List<String> allActions = new ArrayList<>();
/*     */ 
/*     */     
/* 102 */     List<String> leftCommands = config.getStringList("left_click_commands");
/* 103 */     if (!leftCommands.isEmpty()) {
/* 104 */       item.setActions("left", convertDeluxeCommands(leftCommands));
/*     */     }
/*     */ 
/*     */     
/* 108 */     List<String> rightCommands = config.getStringList("right_click_commands");
/* 109 */     if (!rightCommands.isEmpty()) {
/* 110 */       item.setActions("right", convertDeluxeCommands(rightCommands));
/*     */     }
/*     */ 
/*     */     
/* 114 */     if (config.getBoolean("glow", false)) {
/* 115 */       allActions.add("effect: GLOW-30-1");
/*     */     }
/*     */     
/* 118 */     if (!allActions.isEmpty()) {
/* 119 */       item.setActions("all", allActions);
/*     */     }
/*     */     
/* 122 */     return item;
/*     */   }
/*     */   
/*     */   private List<String> convertDeluxeCommands(List<String> deluxeCommands) {
/* 126 */     List<String> converted = new ArrayList<>();
/*     */     
/* 128 */     for (String cmd : deluxeCommands) {
/* 129 */       if (cmd == null || cmd.trim().isEmpty())
/*     */         continue; 
/* 131 */       String trimmed = cmd.trim();
/*     */ 
/*     */       
/* 134 */       if (trimmed.startsWith("[player]")) {
/* 135 */         String content = trimmed.substring(8).trim();
/* 136 */         if (content.startsWith("msg:")) {
/* 137 */           converted.add("tell: " + content.substring(4).trim()); continue;
/* 138 */         }  if (content.startsWith("cmd:"))
/* 139 */           converted.add("command: " + content.substring(4).trim());  continue;
/*     */       } 
/* 141 */       if (trimmed.startsWith("[console]")) {
/* 142 */         converted.add("op: " + trimmed.substring(9).trim()); continue;
/* 143 */       }  if (trimmed.startsWith("[close]")) {
/* 144 */         converted.add("close"); continue;
/* 145 */       }  if (trimmed.startsWith("[open]")) {
/* 146 */         converted.add("menu: " + trimmed.substring(6).trim()); continue;
/* 147 */       }  if (trimmed.startsWith("[sound]")) {
/* 148 */         converted.add("sound: " + trimmed.substring(7).trim());
/*     */       }
/*     */     } 
/*     */     
/* 152 */     return converted;
/*     */   }
/*     */   
/*     */   private List<String> generateVirtualLayout(int menuSize) {
/* 156 */     List<String> layout = new ArrayList<>();
/* 157 */     int rows = Math.max(1, menuSize / 9);
/*     */ 
/*     */     
/* 160 */     for (int i = 0; i < rows; i++) {
/* 161 */       layout.add("         ");
/*     */     }
/*     */     
/* 164 */     return layout;
/*     */   }
/*     */ }
