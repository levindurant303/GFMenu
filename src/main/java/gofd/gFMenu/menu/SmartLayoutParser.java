package gofd.gFMenu.menu;

import java.util.*;

/**
 * 智能布局解析器
 * 1. 正确解析TrMenu格式（空格作为占位符）
 * 2. 支持自动居中布局
 * 3. 支持自定义偏移调整
 */
public class SmartLayoutParser {

    /**
     * 解析布局 - 核心方法
     *
     * @param layoutRows 布局行列表
     * @param centerEnabled 是否启用居中
     * @param rowOffsets 行偏移配置（可选）
     * @return 字符到槽位的映射
     */
    public static Map<Character, Integer> parseLayout(List<String> layoutRows,
                                                      boolean centerEnabled,
                                                      Map<Integer, Integer> rowOffsets) {
        Map<Character, Integer> slotMap = new HashMap<>();

        if (layoutRows == null || layoutRows.isEmpty()) {
            return slotMap;
        }

        // 分析每行的布局结构
        List<RowAnalysis> rowAnalyses = analyzeRows(layoutRows);

        // 处理每一行
        for (int rowIndex = 0; rowIndex < rowAnalyses.size(); rowIndex++) {
            RowAnalysis analysis = rowAnalyses.get(rowIndex);

            // 获取行偏移（如果有配置）
            int offset = rowOffsets != null ? rowOffsets.getOrDefault(rowIndex, 0) : 0;

            // 根据是否居中计算起始列
            int startColumn;
            if (centerEnabled && analysis.charCount > 0) {
                startColumn = calculateCenteredStart(analysis.charCount) + offset;
            } else {
                startColumn = 0 + offset; // 左对齐 + 偏移
            }

            // 确保起始列在有效范围内
            startColumn = Math.max(0, Math.min(startColumn, 8));

            // 放置字符到槽位
            placeCharactersInRow(analysis, rowIndex, startColumn, slotMap);
        }

        return slotMap;
    }

    /**
     * 分析每行的布局结构
     */
    private static List<RowAnalysis> analyzeRows(List<String> layoutRows) {
        List<RowAnalysis> analyses = new ArrayList<>();

        for (String row : layoutRows) {
            RowAnalysis analysis = new RowAnalysis();
            analysis.originalRow = row;
            analysis.cleanedRow = cleanRowString(row);
            analysis.characters = new ArrayList<>();
            analysis.positions = new ArrayList<>();

            // 逐字符分析
            for (int i = 0; i < analysis.cleanedRow.length(); i++) {
                char ch = analysis.cleanedRow.charAt(i);

                if (ch != ' ') {
                    // 记录字符及其在原始行中的位置
                    analysis.characters.add(ch);
                    analysis.positions.add(i);
                }
            }

            analysis.charCount = analysis.characters.size();
            analysis.rowLength = analysis.cleanedRow.length();
            analysis.needsCentering = analysis.charCount > 0 && analysis.charCount < 9;

            analyses.add(analysis);
        }

        return analyses;
    }

    /**
     * 计算居中起始列
     */
    private static int calculateCenteredStart(int charCount) {
        if (charCount >= 9) return 0; // 太满，无法居中

        int totalSpace = 9 - charCount;
        return totalSpace / 2; // 整数除法，自动居中
    }

    /**
     * 将字符放置到行中的槽位
     */
    private static void placeCharactersInRow(RowAnalysis analysis, int rowIndex,
                                             int startColumn, Map<Character, Integer> slotMap) {
        // 检查是否有足够的空间放置所有字符
        if (startColumn + analysis.charCount > 9) {
            // 空间不足，调整起始列
            startColumn = Math.max(0, 9 - analysis.charCount);
        }

        // 放置每个字符
        for (int i = 0; i < analysis.characters.size(); i++) {
            char ch = analysis.characters.get(i);
            int column = startColumn + i;

            // 计算槽位
            int slot = rowIndex * 9 + column;
            slotMap.put(ch, slot);

            // 记录调试信息
            analysis.slotPositions.put(ch, slot);
        }
    }

    /**
     * 清理行字符串
     */
    private static String cleanRowString(String row) {
        if (row == null) return "";

        String cleanRow = row.trim();

        // 移除包围的引号
        if ((cleanRow.startsWith("'") && cleanRow.endsWith("'")) ||
                (cleanRow.startsWith("\"") && cleanRow.endsWith("\""))) {
            cleanRow = cleanRow.substring(1, cleanRow.length() - 1);
        }

        return cleanRow;
    }

    /**
     * 生成布局可视化
     */
    public static String visualizeLayout(List<String> layoutRows, boolean centerEnabled) {
        StringBuilder sb = new StringBuilder();
        sb.append("┌─────────────────────────────────────────────────────────┐\n");
        sb.append("│                   布局可视化 (").append(centerEnabled ? "居中" : "左对齐").append(")                   │\n");
        sb.append("├─────────────────────────────────────────────────────────┤\n");

        // 创建网格
        int rows = Math.min(layoutRows.size(), 6);
        String[][] grid = new String[rows][9];

        // 初始化网格
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < 9; j++) {
                grid[i][j] = "   "; // 3个空格表示空槽
            }
        }

        // 解析布局
        Map<Character, Integer> slotMap = parseLayout(layoutRows, centerEnabled, null);

        // 填充网格
        for (Map.Entry<Character, Integer> entry : slotMap.entrySet()) {
            int slot = entry.getValue();
            int row = slot / 9;
            int col = slot % 9;

            if (row < rows && col < 9) {
                grid[row][col] = String.format("[%c]", entry.getKey());
            }
        }

        // 显示网格
        sb.append("│     0    1    2    3    4    5    6    7    8    │\n");
        sb.append("├─────────────────────────────────────────────────────────┤\n");

        for (int i = 0; i < rows; i++) {
            sb.append("│").append(i).append(" ");
            for (int j = 0; j < 9; j++) {
                sb.append(grid[i][j]).append(" ");
            }
            sb.append("│\n");
        }

        sb.append("├─────────────────────────────────────────────────────────┤\n");

        // 显示详细信息
        List<RowAnalysis> analyses = analyzeRows(layoutRows);
        sb.append("│ 行号 │ 字符 │ 原布局                             │\n");
        sb.append("├──────┼──────┼───────────────────────────────────┤\n");

        for (int i = 0; i < analyses.size(); i++) {
            RowAnalysis analysis = analyses.get(i);
            sb.append(String.format("│ %4d │ %4d │ %-35s │\n",
                    i, analysis.charCount,
                    analysis.originalRow.length() > 35 ?
                            analysis.originalRow.substring(0, 32) + "..." :
                            analysis.originalRow));
        }

        sb.append("└─────────────────────────────────────────────────────────┘\n");

        return sb.toString();
    }

    /**
     * 行分析内部类
     */
    private static class RowAnalysis {
        String originalRow;
        String cleanedRow;
        List<Character> characters;
        List<Integer> positions;
        Map<Character, Integer> slotPositions = new HashMap<>();
        int charCount;
        int rowLength;
        boolean needsCentering;

        @Override
        public String toString() {
            return String.format("Row[chars=%d, length=%d, centering=%s]",
                    charCount, rowLength, needsCentering);
        }
    }

    /**
     * 布局配置类
     */
    public static class LayoutConfig {
        private boolean centerEnabled = false;
        private Map<Integer, Integer> rowOffsets = new HashMap<>();
        private int globalOffset = 0;

        public LayoutConfig() {}

        public LayoutConfig(boolean centerEnabled) {
            this.centerEnabled = centerEnabled;
        }

        public boolean isCenterEnabled() {
            return centerEnabled;
        }

        public void setCenterEnabled(boolean centerEnabled) {
            this.centerEnabled = centerEnabled;
        }

        public Map<Integer, Integer> getRowOffsets() {
            return rowOffsets;
        }

        public void setRowOffset(int row, int offset) {
            rowOffsets.put(row, offset);
        }

        public int getGlobalOffset() {
            return globalOffset;
        }

        public void setGlobalOffset(int globalOffset) {
            this.globalOffset = globalOffset;
        }

        public void applyGlobalOffset() {
            for (Map.Entry<Integer, Integer> entry : rowOffsets.entrySet()) {
                rowOffsets.put(entry.getKey(), entry.getValue() + globalOffset);
            }
        }
    }
}
