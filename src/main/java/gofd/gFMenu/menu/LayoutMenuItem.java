/*     */ package gofd.gFMenu.menu;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.enchantments.Enchantment;
/*     */ import org.bukkit.inventory.ItemFlag;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ import org.bukkit.inventory.meta.ItemMeta;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class LayoutMenuItem
/*     */ {
/*  22 */   private String material = "STONE";
/*  23 */   private int amount = 1;
/*  24 */   private String name = "物品";
/*  25 */   private List<String> lore = new ArrayList<>();
/*  26 */   private Map<String, List<String>> actions = new HashMap<>();
/*  27 */   private int slot = 0;
/*  28 */   private char iconChar = ' ';
/*     */   private boolean glowing = false;
/*  30 */   private String skullOwner = null;
/*     */ 
/*     */   
/*     */   public ItemStack toItemStack() {
/*  34 */     Material mat = Material.getMaterial(this.material.toUpperCase());
/*  35 */     if (mat == null) {
/*  36 */       mat = Material.STONE;
/*     */     }
/*     */     
/*  39 */     ItemStack item = new ItemStack(mat, this.amount);
/*  40 */     ItemMeta meta = item.getItemMeta();
/*     */     
/*  42 */     if (meta != null) {
/*     */       
/*  44 */       if (this.name != null && !this.name.isEmpty()) {
/*  45 */         meta.setDisplayName(this.name.replace("&", "§"));
/*     */       }
/*     */ 
/*     */       
/*  49 */       if (this.lore != null && !this.lore.isEmpty()) {
/*  50 */         List<String> coloredLore = new ArrayList<>();
/*  51 */         for (String line : this.lore) {
/*  52 */           coloredLore.add(line.replace("&", "§"));
/*     */         }
/*  54 */         meta.setLore(coloredLore);
/*     */       } 
/*     */ 
/*     */       
/*  58 */       if (this.glowing) {
/*  59 */         meta.addEnchant(Enchantment.LURE, 1, true);
/*  60 */         meta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ENCHANTS });
/*     */       } 
/*     */       
/*  63 */       item.setItemMeta(meta);
/*     */     } 
/*     */ 
/*     */     
/*  67 */     if (this.skullOwner == null || this.material.equalsIgnoreCase("PLAYER_HEAD"));
/*     */ 
/*     */ 
/*     */     
/*  71 */     return item;
/*     */   }
/*     */   
/*     */   public List<String> getActions(String type) {
/*  75 */     if (this.actions.containsKey(type))
/*  76 */       return this.actions.get(type); 
/*  77 */     if (this.actions.containsKey("all")) {
/*  78 */       return this.actions.get("all");
/*     */     }
/*  80 */     return new ArrayList<>();
/*     */   }
/*     */   
/*     */   public boolean hasActions(String type) {
/*  84 */     return (this.actions.containsKey(type) || this.actions.containsKey("all"));
/*     */   }
/*     */   
/*     */   public char getIconChar() {
/*  88 */     return this.iconChar;
/*  89 */   } public void setIconChar(char iconChar) { this.iconChar = iconChar; }
/*  90 */   public int getSlot() { return this.slot; }
/*  91 */   public void setSlot(int slot) { this.slot = slot; }
/*  92 */   public String getMaterial() { return this.material; }
/*  93 */   public void setMaterial(String material) { this.material = material; }
/*  94 */   public int getAmount() { return this.amount; }
/*  95 */   public void setAmount(int amount) { this.amount = amount; }
/*  96 */   public String getName() { return this.name; }
/*  97 */   public void setName(String name) { this.name = name; }
/*  98 */   public List<String> getLore() { return this.lore; }
/*  99 */   public void setLore(List<String> lore) { this.lore = lore; }
/* 100 */   public Map<String, List<String>> getActions() { return this.actions; }
/* 101 */   public void setActions(String type, List<String> actions) { this.actions.put(type, actions); }
/* 102 */   public boolean isGlowing() { return this.glowing; }
/* 103 */   public void setGlowing(boolean glowing) { this.glowing = glowing; }
/* 104 */   public String getSkullOwner() { return this.skullOwner; } public void setSkullOwner(String skullOwner) {
/* 105 */     this.skullOwner = skullOwner;
/*     */   }
/*     */ }

