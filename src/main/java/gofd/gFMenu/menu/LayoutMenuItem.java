package gofd.gFMenu.menu;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** A menu item together with the actions that belong to its menu slot. */
public final class LayoutMenuItem {

    private String material = Material.STONE.name();
    private int amount = 1;
    private String name = "";
    private List<String> lore = new ArrayList<>();
    private final Map<String, List<String>> actions = new LinkedHashMap<>();
    private int slot;
    private char iconChar = ' ';
    private boolean glowing;
    private String skullOwner;
    private String sourceKey;

    public ItemStack toItemStack() {
        Material parsedMaterial = Material.matchMaterial(material == null ? "" : material);
        if (parsedMaterial == null || parsedMaterial.isAir()) {
            parsedMaterial = Material.STONE;
        }

        int safeAmount = Math.max(1, Math.min(amount, parsedMaterial.getMaxStackSize()));
        ItemStack item = new ItemStack(parsedMaterial, safeAmount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        if (name != null && !name.isEmpty()) {
            meta.setDisplayName(colorize(name));
        }
        if (!lore.isEmpty()) {
            meta.setLore(lore.stream().map(LayoutMenuItem::colorize).toList());
        }
        if (glowing) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        if (meta instanceof SkullMeta skullMeta && skullOwner != null && !skullOwner.isBlank()) {
            skullMeta.setOwner(skullOwner);
        }
        item.setItemMeta(meta);
        return item;
    }

    public void updateDisplayFrom(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            throw new IllegalArgumentException("A menu item cannot be created from air");
        }
        material = stack.getType().name();
        amount = Math.max(1, stack.getAmount());

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            name = "";
            lore = new ArrayList<>();
            glowing = false;
            skullOwner = null;
            return;
        }

        name = meta.hasDisplayName() ? meta.getDisplayName() : "";
        lore = meta.hasLore() && meta.getLore() != null
                ? new ArrayList<>(meta.getLore())
                : new ArrayList<>();
        glowing = !stack.getEnchantments().isEmpty();
        skullOwner = meta instanceof SkullMeta skullMeta ? skullMeta.getOwner() : null;
    }

    public List<String> getActions(String type) {
        List<String> matching = actions.get(normalizeActionType(type));
        if (matching != null) {
            return List.copyOf(matching);
        }
        List<String> all = actions.get("all");
        return all == null ? List.of() : List.copyOf(all);
    }

    public boolean hasActions(String type) {
        return !getActions(type).isEmpty();
    }

    public Map<String, List<String>> getActions() {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        actions.forEach((type, values) -> copy.put(type, List.copyOf(values)));
        return Map.copyOf(copy);
    }

    public void setActions(String type, List<String> values) {
        String normalizedType = normalizeActionType(type);
        if (values == null || values.isEmpty()) {
            actions.remove(normalizedType);
            return;
        }
        actions.put(normalizedType, new ArrayList<>(values));
    }

    public void addAction(String type, String action) {
        if (action == null || action.isBlank()) {
            return;
        }
        actions.computeIfAbsent(normalizeActionType(type), ignored -> new ArrayList<>()).add(action);
    }

    public void removeAction(String type, int index) {
        String normalizedType = normalizeActionType(type);
        List<String> values = actions.get(normalizedType);
        if (values == null || index < 0 || index >= values.size()) {
            throw new IllegalArgumentException("Action index is out of range");
        }
        values.remove(index);
        if (values.isEmpty()) {
            actions.remove(normalizedType);
        }
    }

    private static String normalizeActionType(String type) {
        return type == null || type.isBlank() ? "all" : type.toLowerCase(Locale.ROOT);
    }

    public static String colorize(String value) {
        return ChatColor.translateAlternateColorCodes('&', value == null ? "" : value);
    }

    public char getIconChar() {
        return iconChar;
    }

    public void setIconChar(char iconChar) {
        this.iconChar = iconChar;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material == null || material.isBlank() ? Material.STONE.name() : material;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = Math.max(1, amount);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? "" : name;
    }

    public List<String> getLore() {
        return List.copyOf(lore);
    }

    public void setLore(List<String> lore) {
        this.lore = lore == null ? new ArrayList<>() : new ArrayList<>(lore);
    }

    public boolean isGlowing() {
        return glowing;
    }

    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
    }

    public String getSkullOwner() {
        return skullOwner;
    }

    public void setSkullOwner(String skullOwner) {
        this.skullOwner = skullOwner;
    }

    public String getSourceKey() {
        return sourceKey;
    }

    public void setSourceKey(String sourceKey) {
        this.sourceKey = sourceKey;
    }
}
