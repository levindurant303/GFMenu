package gofd.gFMenu.update;

import java.math.BigInteger;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VersionComparator {
    private static final Pattern VERSION = Pattern.compile("^[vVrR]?(\\d+(?:\\.\\d+)*)(?:[-+]([0-9A-Za-z.-]+))?$");

    private VersionComparator() {
    }

    public static boolean isValid(String version) {
        return version != null && VERSION.matcher(version.trim()).matches();
    }

    public static int compare(String left, String right) {
        Matcher first = VERSION.matcher(left == null ? "" : left.trim());
        Matcher second = VERSION.matcher(right == null ? "" : right.trim());
        if (!first.matches() || !second.matches()) {
            return 0;
        }
        String[] firstParts = first.group(1).split("\\.");
        String[] secondParts = second.group(1).split("\\.");
        for (int index = 0; index < Math.max(firstParts.length, secondParts.length); index++) {
            BigInteger firstPart = index < firstParts.length ? new BigInteger(firstParts[index]) : BigInteger.ZERO;
            BigInteger secondPart = index < secondParts.length ? new BigInteger(secondParts[index]) : BigInteger.ZERO;
            int comparison = firstPart.compareTo(secondPart);
            if (comparison != 0) {
                return comparison;
            }
        }
        String firstSuffix = first.group(2);
        String secondSuffix = second.group(2);
        if (firstSuffix == null || secondSuffix == null) {
            if (firstSuffix == null && secondSuffix == null) {
                return 0;
            }
            String suffix = firstSuffix == null ? secondSuffix : firstSuffix;
            boolean isHotfix = suffix.toLowerCase(Locale.ROOT).startsWith("hotfix");
            return firstSuffix == null ? (isHotfix ? -1 : 1) : (isHotfix ? 1 : -1);
        }
        return compareSuffix(firstSuffix, secondSuffix);
    }

    private static int compareSuffix(String left, String right) {
        String[] leftParts = left.toLowerCase(Locale.ROOT).split("[.-]");
        String[] rightParts = right.toLowerCase(Locale.ROOT).split("[.-]");
        for (int index = 0; index < Math.min(leftParts.length, rightParts.length); index++) {
            boolean leftNumeric = leftParts[index].matches("\\d+");
            boolean rightNumeric = rightParts[index].matches("\\d+");
            int comparison;
            if (leftNumeric && rightNumeric) {
                comparison = new BigInteger(leftParts[index]).compareTo(new BigInteger(rightParts[index]));
            } else if (leftNumeric != rightNumeric) {
                comparison = leftNumeric ? -1 : 1;
            } else {
                comparison = leftParts[index].compareTo(rightParts[index]);
            }
            if (comparison != 0) {
                return comparison;
            }
        }
        return Integer.compare(leftParts.length, rightParts.length);
    }
}
