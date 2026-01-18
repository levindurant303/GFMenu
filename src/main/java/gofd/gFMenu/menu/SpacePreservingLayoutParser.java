package gofd.gFMenu.menu;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpacePreservingLayoutParser {

    /**
     * 解析TrMenu风格的布局
     */
    public static Map<Character, Integer> parseTrMenuLayout(List<String> layoutRows,
                                                            boolean centerEnabled,
                                                            boolean preserveSpaces,
                                                            Map<Integer, Integer> rowOffsets) {
        Map<Character, Integer> slotMap = new HashMap<>();
        if (layoutRows == null || layoutRows.isEmpty()) return slotMap;

        for (int rowIndex = 0; rowIndex < layoutRows.size(); rowIndex++) {
            String row = layoutRows.get(rowIndex);
            String cleanRow = cleanTrMenuRow(row, preserveSpaces);
            if (cleanRow.isEmpty()) continue;

            int offset = rowOffsets != null ? rowOffsets.getOrDefault(rowIndex, 0) : 0;
            int startColumn = calculateStartColumn(cleanRow, centerEnabled, offset, preserveSpaces);

            for (int i = 0; i < cleanRow.length(); i++) {
                char ch = cleanRow.charAt(i);
                if (!preserveSpaces || ch != ' ') {
                    int slot = rowIndex * 9 + startColumn + i;
                    if (slot >= 0 && slot < 54) {
                        slotMap.put(ch, slot);
                    }
                }
            }
        }
        return slotMap;
    }

    /**
     * 清理TrMenu格式的行字符串
     */
    private static String cleanTrMenuRow(String row, boolean preserveSpaces) {
        if (row == null) return "";
        String cleanRow = row.trim();

        if (cleanRow.startsWith("'") && cleanRow.endsWith("'")) {
            cleanRow = cleanRow.substring(1, cleanRow.length() - 1);
        } else if (cleanRow.startsWith("\"") && cleanRow.endsWith("\"")) {
            cleanRow = cleanRow.substring(1, cleanRow.length() - 1);
        }

        if (!preserveSpaces) {
            cleanRow = cleanRow.replaceAll(" ", "");
        }
        return cleanRow;
    }

    /**
     * 计算每行的起始列
     */
    private static int calculateStartColumn(String row, boolean centerEnabled, int offset, boolean preserveSpaces) {
        if (row.isEmpty()) return offset;
        if (!centerEnabled || row.length() > 9) return offset;

        int totalSpace = 9 - row.length();
        int baseStart = totalSpace / 2;
        baseStart = Math.max(baseStart, 0);

        int startColumn = baseStart + offset;
        startColumn = Math.max(startColumn, 0);
        startColumn = Math.min(startColumn, 8);

        return startColumn;
    }

    /**
     * 映射图标配置到槽位
     */
    public static void mapTrMenuIcons(LayoutMenuData menuData, ConfigurationSection iconsSection) {
        if (iconsSection == null) return;

        for (String iconKey : iconsSection.getKeys(false)) {
            if (iconKey.length() != 1) continue;

            char iconChar = iconKey.charAt(0);
            Integer slot = menuData.getLayoutSlots().get(iconChar);
            if (slot == null) continue;

            ConfigurationSection iconConfig = iconsSection.getConfigurationSection(iconKey);
            LayoutMenuItem item = new LayoutMenuItem();
            item.setSlot(slot);
            item.setIconChar(iconChar);

            if (iconConfig.contains("display")) {
                ConfigurationSection display = iconConfig.getConfigurationSection("display");
                item.setMaterial(display.getString("material", "STONE"));
                item.setName(display.getString("name", "&f物品"));
                item.setLore(display.getStringList("lore"));
                item.setAmount(display.getInt("amount", 1));
            }

            if (iconConfig.contains("actions")) {
                Object actionsObj = iconConfig.get("actions");
                if (actionsObj instanceof List) {
                    item.setActions("all", iconConfig.getStringList("actions"));
                } else if (actionsObj instanceof ConfigurationSection) {
                    ConfigurationSection actionsSec = (ConfigurationSection) actionsObj;
                    if (actionsSec.contains("all")) {
                        item.setActions("all", actionsSec.getStringList("all"));
                    }
                    if (actionsSec.contains("left")) {
                        item.setActions("left", actionsSec.getStringList("left"));
                    }
                    if (actionsSec.contains("right")) {
                        item.setActions("right", actionsSec.getStringList("right"));
                    }
                }
            }

            menuData.addItem(iconChar, item);
        }
    }
}