package gofd.gFMenu.menu;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Parser for TrMenu's character-based layout. Spaces are real slot placeholders. */
public final class TrMenuLayoutParser {

    private TrMenuLayoutParser() {
    }

    public static Map<Character, Integer> parseLayout(List<String> layoutRows, boolean centerEnabled) {
        Map<Character, Integer> slots = new LinkedHashMap<>();
        if (layoutRows == null) {
            return slots;
        }

        int rowCount = Math.min(layoutRows.size(), 6);
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            String row = cleanRowString(layoutRows.get(rowIndex));
            if (row.isEmpty()) {
                continue;
            }

            int visibleLength = Math.min(row.length(), 9);
            boolean hasExplicitSpacing = row.length() >= 9 || row.startsWith(" ") || row.endsWith(" ");
            int startColumn = centerEnabled && !hasExplicitSpacing && visibleLength < 9
                    ? (9 - visibleLength) / 2
                    : 0;

            for (int column = 0; column < visibleLength; column++) {
                char icon = row.charAt(column);
                if (icon != ' ') {
                    slots.put(icon, rowIndex * 9 + startColumn + column);
                }
            }
        }
        return slots;
    }

    public static String cleanRowString(String row) {
        if (row == null) {
            return "";
        }
        String trimmed = row.trim();
        if (trimmed.length() >= 2
                && ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
                || (trimmed.startsWith("'") && trimmed.endsWith("'")))) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return row;
    }

    public static int calculateInventorySize(List<String> layoutRows) {
        int rows = layoutRows == null ? 0 : Math.min(layoutRows.size(), 6);
        return Math.max(9, rows * 9);
    }
}
