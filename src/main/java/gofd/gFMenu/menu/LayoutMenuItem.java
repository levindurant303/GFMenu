package gofd.gFMenu.menu;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class LayoutMenuItem {

    private char iconChar;
    private int slot;
    private String material;
    private int amount;
    private String name;
    private List<String> lore;
    private Map<String, List<String>> actions; // key: all/left/right, value: 动作列表

    public LayoutMenuItem() {
        this.material = "STONE";
        this.amount = 1;
        this.name = "物品";
        this.lore = new ArrayList<>();
        this.actions = new HashMap<>();
        this.slot = 0;
        this.iconChar = ' ';
    }

    public ItemStack toItemStack() {
        Material mat = Material.getMaterial(material.toUpperCase());
        if (mat == null) {
            mat = Material.STONE;
        }

        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name.replace("&", "§"));

            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(line.replace("&", "§"));
            }
            meta.setLore(coloredLore);

            item.setItemMeta(meta);
        }

        return item;
    }

    public List<String> getActions(String type) {
        // 如果没有指定类型的动作，返回all类型的动作
        if (actions.containsKey(type)) {
            return actions.get(type);
        } else if (actions.containsKey("all")) {
            return actions.get("all");
        }
        return new ArrayList<>();
    }

    public boolean hasActions(String type) {
        return actions.containsKey(type) || actions.containsKey("all");
    }

    // Getters and Setters
    public char getIconChar() { return iconChar; }
    public void setIconChar(char iconChar) { this.iconChar = iconChar; }
    public int getSlot() { return slot; }
    public void setSlot(int slot) { this.slot = slot; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getLore() { return lore; }
    public void setLore(List<String> lore) { this.lore = lore; }
    public Map<String, List<String>> getActions() { return actions; }
    public void setActions(String type, List<String> actions) {
        this.actions.put(type, actions);
    }
}
