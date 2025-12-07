package gofd.gFMenu.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class LayoutMenuData {

    private final String name;
    private String title;
    private Map<Character, Integer> layoutSlots;
    private List<String> commands;
    private List<String> aliases;
    private String permission;
    private List<String> openEvents;
    private List<String> closeEvents;
    private final Map<Character, LayoutMenuItem> items;
    private List<String> rawLayout;

    // 新增：布局配置
    private boolean centerEnabled = false;
    private Map<Integer, Integer> rowOffsets = new HashMap<>();

    public LayoutMenuData(String name) {
        this.name = name;
        this.layoutSlots = new HashMap<>();
        this.commands = new ArrayList<>();
        this.aliases = new ArrayList<>();
        this.openEvents = new ArrayList<>();
        this.closeEvents = new ArrayList<>();
        this.items = new HashMap<>();
        this.rawLayout = new ArrayList<>();
        this.title = "菜单";
        this.permission = null;
    }

    public void open(Player player) {
        // 权限检查
        if (permission != null && !player.hasPermission(permission)) {
            player.sendMessage("§c你没有权限打开这个菜单！");
            return;
        }

        // 计算库存大小
        int size = calculateInventorySize();

        // 创建库存
        Inventory inventory = Bukkit.createInventory(null, size,
                title.replace("&", "§"));

        // 设置物品
        for (LayoutMenuItem item : items.values()) {
            ItemStack itemStack = item.toItemStack();
            if (itemStack != null) {
                int slot = item.getSlot();
                if (slot >= 0 && slot < size) {
                    inventory.setItem(slot, itemStack);

                    // 调试信息
                    if (Bukkit.getLogger() != null) {
                        Bukkit.getLogger().info(String.format(
                                "菜单 '%s': 图标 '%c' 放置在槽位 %d (行%d,列%d)",
                                name, item.getIconChar(), slot, slot/9, slot%9));
                    }
                }
            }
        }

        // 执行打开事件
        executeEvents(player, openEvents);

        player.openInventory(inventory);

        // 记录打开日志
        logMenuOpen();
    }

    private int calculateInventorySize() {
        if (rawLayout == null || rawLayout.isEmpty()) {
            return 9 * 3; // 默认3行
        }

        int rows = rawLayout.size();
        if (rows > 6) rows = 6;

        return rows * 9;
    }

    private void executeEvents(Player player, List<String> events) {
        if (events == null) return;

        // 这里可以扩展事件执行器
        for (String event : events) {
            if (event.startsWith("sound:")) {
                // 音效处理（可以后续实现）
            } else if (event.startsWith("message:")) {
                String message = event.substring(8).trim();
                player.sendMessage(message.replace("&", "§"));
            }
        }
    }

    private void logMenuOpen() {
        if (Bukkit.getLogger() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n§7========== 菜单布局信息 ==========\n");
            sb.append("§7菜单名称: §f").append(name).append("\n");
            sb.append("§7标题: §f").append(title).append("\n");
            sb.append("§7居中模式: §").append(centerEnabled ? "a启用" : "c禁用").append("\n");
            sb.append("§7布局行数: §f").append(rawLayout.size()).append("\n");
            sb.append("§7图标数量: §f").append(items.size()).append("\n");

            // 显示每行的布局
            if (!rawLayout.isEmpty()) {
                sb.append("§7布局定义:\n");
                for (int i = 0; i < rawLayout.size(); i++) {
                    sb.append(String.format("§7  行%d: §f%s\n", i, rawLayout.get(i)));
                }
            }

            // 显示图标位置
            if (!items.isEmpty()) {
                sb.append("§7图标位置:\n");
                List<LayoutMenuItem> sortedItems = new ArrayList<>(items.values());
                sortedItems.sort(Comparator.comparingInt(LayoutMenuItem::getSlot));

                for (LayoutMenuItem item : sortedItems) {
                    int slot = item.getSlot();
                    int row = slot / 9;
                    int col = slot % 9;
                    sb.append(String.format("§7  %c → 槽位%d (行%d,列%d)\n",
                            item.getIconChar(), slot, row, col));
                }
            }

            sb.append("§7===================================");
            Bukkit.getLogger().info(sb.toString().replace("§", ""));
        }
    }

    // ================== Getters and Setters ==================

    public String getName() { return name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Map<Character, Integer> getLayoutSlots() { return layoutSlots; }
    public void setLayoutSlots(Map<Character, Integer> layoutSlots) {
        this.layoutSlots = layoutSlots;
    }

    public List<String> getCommands() { return commands; }
    public void setCommands(List<String> commands) {
        this.commands = commands != null ? commands : new ArrayList<>();
    }

    public List<String> getAliases() { return aliases; }
    public void setAliases(List<String> aliases) {
        this.aliases = aliases != null ? aliases : new ArrayList<>();
    }

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }

    public List<String> getOpenEvents() { return openEvents; }
    public void setOpenEvents(List<String> openEvents) {
        this.openEvents = openEvents != null ? openEvents : new ArrayList<>();
    }

    public List<String> getCloseEvents() { return closeEvents; }
    public void setCloseEvents(List<String> closeEvents) {
        this.closeEvents = closeEvents != null ? closeEvents : new ArrayList<>();
    }

    public Map<Character, LayoutMenuItem> getItems() { return items; }

    public List<String> getRawLayout() { return rawLayout; }
    public void setRawLayout(List<String> rawLayout) {
        this.rawLayout = rawLayout != null ? rawLayout : new ArrayList<>();
    }

    // 新增：居中配置相关方法
    public boolean isCenterEnabled() { return centerEnabled; }
    public void setCenterEnabled(boolean centerEnabled) {
        this.centerEnabled = centerEnabled;
    }

    public Map<Integer, Integer> getRowOffsets() { return rowOffsets; }
    public void setRowOffsets(Map<Integer, Integer> rowOffsets) {
        this.rowOffsets = rowOffsets != null ? rowOffsets : new HashMap<>();
    }

    public void setRowOffset(int row, int offset) {
        rowOffsets.put(row, offset);
    }

    public void addItem(char key, LayoutMenuItem item) {
        items.put(key, item);
    }
}
