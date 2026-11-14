package gofd.gFMenu.menu;

import gofd.gFMenu.LanguageManager;
import gofd.gFMenu.menu.actions.ActionEngine;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Immutable-name runtime representation of one parsed menu file. */
public final class LayoutMenuData {

    private final String name;
    private String title = "Menu";
    private Map<Character, Integer> layoutSlots = new LinkedHashMap<>();
    private Map<Character, List<Integer>> layoutPositions = new LinkedHashMap<>();
    private List<String> commands = new ArrayList<>();
    private String permission;
    private List<String> openEvents = new ArrayList<>();
    private List<String> closeEvents = new ArrayList<>();
    private final Map<Character, LayoutMenuItem> items = new LinkedHashMap<>();
    /** Slot lookup index kept in sync with {@link #items} so clicks resolve in O(1). */
    private final Map<Integer, LayoutMenuItem> slotIndex = new HashMap<>();
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
            for (int slot : getPositionsForIcon(item.getIconChar())) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    inventory.setItem(slot, item.toItemStack());
                }
            }
        }

        player.openInventory(inventory);
        actionEngine.executeActions(player, openEvents);
    }

    public int getInventorySize() {
        if (!rawLayout.isEmpty()) {
            return Math.max(9, Math.min(54, Math.max(rawLayout.size() * 9, menuSize)));
        }
        int bounded = Math.max(9, Math.min(54, menuSize));
        return ((bounded + 8) / 9) * 9;
    }

    public LayoutMenuItem getItemAtSlot(int slot) {
        return slotIndex.get(slot);
    }

    public void addItem(char iconChar, LayoutMenuItem item) {
        item.setIconChar(iconChar);
        LayoutMenuItem previous = items.put(iconChar, item);
        if (previous != null) {
            List<Integer> previousPositions = layoutPositions.getOrDefault(iconChar, List.of(previous.getSlot()));
            previousPositions.forEach(slot -> slotIndex.remove(slot, previous));
        }
        List<Integer> positions = layoutPositions.getOrDefault(iconChar, List.of(item.getSlot()));
        positions.forEach(slot -> slotIndex.putIfAbsent(slot, item));
    }

    public void removeItem(char iconChar) {
        LayoutMenuItem removed = items.remove(iconChar);
        if (removed != null) {
            List<Integer> positions = layoutPositions.getOrDefault(iconChar, List.of(removed.getSlot()));
            positions.forEach(slot -> slotIndex.remove(slot, removed));
        }
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
        Map<Character, List<Integer>> positions = new LinkedHashMap<>();
        this.layoutSlots.forEach((icon, slot) -> positions.put(icon, List.of(slot)));
        setLayoutPositions(positions);
    }

    public Map<Character, List<Integer>> getLayoutPositions() {
        Map<Character, List<Integer>> snapshot = new LinkedHashMap<>();
        layoutPositions.forEach((icon, slots) -> snapshot.put(icon, List.copyOf(slots)));
        return Map.copyOf(snapshot);
    }

    public List<Integer> getPositionsForIcon(char icon) {
        List<Integer> positions = layoutPositions.get(icon);
        if (positions != null && !positions.isEmpty()) {
            return List.copyOf(positions);
        }
        Integer slot = layoutSlots.get(icon);
        if (slot != null) {
            return List.of(slot);
        }
        LayoutMenuItem item = items.get(icon);
        return item == null ? List.of() : List.of(item.getSlot());
    }

    public void setLayoutPositions(Map<Character, List<Integer>> layoutPositions) {
        this.layoutPositions = new LinkedHashMap<>();
        this.layoutSlots = new LinkedHashMap<>();
        if (layoutPositions == null) {
            return;
        }
        layoutPositions.forEach((icon, slots) -> {
            if (slots == null || slots.isEmpty()) {
                return;
            }
            List<Integer> copy = List.copyOf(slots);
            this.layoutPositions.put(icon, copy);
            this.layoutSlots.put(icon, copy.get(0));
        });
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
