package gofd.gFMenu.menu;

import gofd.gFMenu.LanguageManager;
import gofd.gFMenu.menu.actions.ActionEngine;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Immutable-name runtime representation of one parsed menu file. */
public final class LayoutMenuData {

    private final String name;
    private String title = "Menu";
    private Map<Character, Integer> layoutSlots = new LinkedHashMap<>();
    private List<String> commands = new ArrayList<>();
    private String permission;
    private List<String> openEvents = new ArrayList<>();
    private List<String> closeEvents = new ArrayList<>();
    private final Map<Character, LayoutMenuItem> items = new LinkedHashMap<>();
    private List<String> rawLayout = new ArrayList<>();
    private boolean centerEnabled;
    private int menuSize = 54;

    public LayoutMenuData(String name) {
        this.name = name;
    }

    public void open(Player player, ActionEngine actionEngine, LanguageManager languageManager) {
        if (permission != null && !permission.isBlank() && !player.hasPermission(permission)) {
            player.sendMessage(languageManager.getMessage("menu_open_no_permission"));
            return;
        }

        MenuInventoryHolder holder = new MenuInventoryHolder(this);
        Inventory inventory = Bukkit.createInventory(holder, getInventorySize(), LayoutMenuItem.colorize(title));
        holder.setInventory(inventory);
        for (LayoutMenuItem item : items.values()) {
            if (item.getSlot() >= 0 && item.getSlot() < inventory.getSize()) {
                inventory.setItem(item.getSlot(), item.toItemStack());
            }
        }

        player.openInventory(inventory);
        actionEngine.executeActions(player, openEvents);
    }

    public int getInventorySize() {
        if (!rawLayout.isEmpty()) {
            return Math.max(9, Math.min(54, rawLayout.size() * 9));
        }
        int bounded = Math.max(9, Math.min(54, menuSize));
        return ((bounded + 8) / 9) * 9;
    }

    public LayoutMenuItem getItemAtSlot(int slot) {
        for (LayoutMenuItem item : items.values()) {
            if (item.getSlot() == slot) {
                return item;
            }
        }
        return null;
    }

    public void addItem(char iconChar, LayoutMenuItem item) {
        item.setIconChar(iconChar);
        items.put(iconChar, item);
    }

    public void removeItem(char iconChar) {
        items.remove(iconChar);
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "Menu" : title;
    }

    public Map<Character, Integer> getLayoutSlots() {
        return Map.copyOf(layoutSlots);
    }

    public void setLayoutSlots(Map<Character, Integer> layoutSlots) {
        this.layoutSlots = layoutSlots == null ? new LinkedHashMap<>() : new LinkedHashMap<>(layoutSlots);
    }

    public List<String> getCommands() {
        return List.copyOf(commands);
    }

    public void setCommands(List<String> commands) {
        this.commands = commands == null ? new ArrayList<>() : new ArrayList<>(commands);
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public List<String> getOpenEvents() {
        return List.copyOf(openEvents);
    }

    public void setOpenEvents(List<String> openEvents) {
        this.openEvents = openEvents == null ? new ArrayList<>() : new ArrayList<>(openEvents);
    }

    public List<String> getCloseEvents() {
        return List.copyOf(closeEvents);
    }

    public void setCloseEvents(List<String> closeEvents) {
        this.closeEvents = closeEvents == null ? new ArrayList<>() : new ArrayList<>(closeEvents);
    }

    public Map<Character, LayoutMenuItem> getItems() {
        return Map.copyOf(items);
    }

    public int getItemCount() {
        return items.size();
    }

    public List<String> getRawLayout() {
        return List.copyOf(rawLayout);
    }

    public void setRawLayout(List<String> rawLayout) {
        this.rawLayout = rawLayout == null ? new ArrayList<>() : new ArrayList<>(rawLayout);
    }

    public boolean isCenterEnabled() {
        return centerEnabled;
    }

    public void setCenterEnabled(boolean centerEnabled) {
        this.centerEnabled = centerEnabled;
    }

    public int getMenuSize() {
        return menuSize;
    }

    public void setMenuSize(int menuSize) {
        this.menuSize = menuSize;
    }
}
