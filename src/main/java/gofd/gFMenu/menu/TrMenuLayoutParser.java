package gofd.gFMenu.menu;

import java.util.*;

/**
 * 正确解析TrMenu布局格式
 * 每个字符代表一个槽位，空格是占位符
 */
public class TrMenuLayoutParser {

    /**
     * 正确的TrMenu布局解析 - 逐字符解析
     */
    public static Map<Character, Integer> parseLayout(List<String> layoutRows) {
        Map<Character, Integer> slotMap = new HashMap<>();

        if (layoutRows == null || layoutRows.isEmpty()) {
            return slotMap;
        }

        int rowIndex = 0;
        for (String row : layoutRows) {
            String cleanRow = cleanRowString(row);

            for (int colIndex = 0; colIndex < cleanRow.length(); colIndex++) {
                char ch = cleanRow.charAt(colIndex);

                // 跳过空格（空格是占位符，不是布局字符）
                if (ch == ' ') {
                    continue;
                }

                // 计算槽位
                int slot = rowIndex * 9 + colIndex;
                slotMap.put(ch, slot);
            }

            rowIndex++;
        }

        return slotMap;
    }

    /**
     * 清理行字符串
     */
    static String cleanRowString(String row) {
        if (row == null) {
            return "";
        }

        String cleanRow = row.trim();

        // 移除单引号
        if (cleanRow.startsWith("'") && cleanRow.endsWith("'")) {
            cleanRow = cleanRow.substring(1, cleanRow.length() - 1);
        }

        // 移除双引号
        if (cleanRow.startsWith("\"") && cleanRow.endsWith("\"")) {
            cleanRow = cleanRow.substring(1, cleanRow.length() - 1);
        }

        return cleanRow;
    }

    /**
     * 计算库存大小（基于布局行数）
     */
    public static int calculateInventorySize(List<String> layoutRows) {
        if (layoutRows == null || layoutRows.isEmpty()) {
            return 9 * 3; // 默认3行
        }

        int rows = layoutRows.size();

        // 确保行数在1-6之间
        if (rows < 1) rows = 1;
        if (rows > 6) rows = 6;

        return rows * 9;
    }
}