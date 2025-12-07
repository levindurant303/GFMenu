package gofd.gFMenu.menu;

import java.util.*;

/**
 * 正确解析TrMenu布局格式
 * 每个字符代表一个槽位，空格是占位符
 * 示例: "Q L K N R" 表示5个槽位，中间有空格作为分隔
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
            // 清理行：移除引号
            String cleanRow = cleanRowString(row);

            // 逐字符解析，不按空格分割！
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
    private static String cleanRowString(String row) {
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

    /**
     * 调试方法：显示布局解析结果
     */
    public static String debugLayout(List<String> layoutRows) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 布局解析调试 ===\n");

        Map<Character, Integer> slotMap = parseLayout(layoutRows);

        sb.append("原始布局行:\n");
        for (int i = 0; i < layoutRows.size(); i++) {
            sb.append(String.format("  行 %d: %s\n", i, layoutRows.get(i)));
        }

        sb.append("\n清理后的行:\n");
        for (int i = 0; i < layoutRows.size(); i++) {
            String cleanRow = cleanRowString(layoutRows.get(i));
            sb.append(String.format("  行 %d: ", i));
            for (int j = 0; j < cleanRow.length(); j++) {
                char ch = cleanRow.charAt(j);
                if (ch == ' ') {
                    sb.append("[空格]");
                } else {
                    sb.append("[").append(ch).append("]");
                }
            }
            sb.append("\n");
        }

        sb.append("\n字符到槽位映射:\n");
        List<Map.Entry<Character, Integer>> sorted = new ArrayList<>(slotMap.entrySet());
        sorted.sort(Map.Entry.comparingByValue());

        for (Map.Entry<Character, Integer> entry : sorted) {
            int slot = entry.getValue();
            int row = slot / 9;
            int col = slot % 9;
            sb.append(String.format("  '%c' -> 槽位 %d (行 %d, 列 %d)\n",
                    entry.getKey(), slot, row, col));
        }

        // 生成布局网格预览
        sb.append("\n布局网格预览:\n");
        int rows = layoutRows.size();
        Character[][] grid = new Character[rows][9];

        // 初始化网格
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < 9; j++) {
                grid[i][j] = ' ';
            }
        }

        // 填充网格
        for (Map.Entry<Character, Integer> entry : slotMap.entrySet()) {
            int slot = entry.getValue();
            int row = slot / 9;
            int col = slot % 9;

            if (row < rows && col < 9) {
                grid[row][col] = entry.getKey();
            }
        }

        // 显示网格
        sb.append("   ");
        for (int j = 0; j < 9; j++) {
            sb.append(String.format(" %d ", j));
        }
        sb.append("\n");

        for (int i = 0; i < rows; i++) {
            sb.append(String.format("%d: ", i));
            for (int j = 0; j < 9; j++) {
                char ch = grid[i][j];
                if (ch == ' ') {
                    sb.append("[ ]");
                } else {
                    sb.append("[").append(ch).append("]");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 获取每行最大列数（用于验证）
     */
    public static int getMaxColumns(List<String> layoutRows) {
        int max = 0;
        for (String row : layoutRows) {
            String cleanRow = cleanRowString(row);
            if (cleanRow.length() > max) {
                max = cleanRow.length();
            }
        }
        return Math.min(max, 9); // 最多9列
    }
}