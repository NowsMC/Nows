package space.nows.mcnows.core.mod;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Small version constraint matcher for loader metadata. */
public final class ModVersionConstraint {
    private ModVersionConstraint() {
    }

    public static boolean matches(String constraint, String actualVersion) {
        String normalized = constraint == null || constraint.isBlank() ? "*" : constraint.trim();
        if (normalized.equals("*")) {
            return true;
        }
        if (actualVersion == null || actualVersion.isBlank()) {
            return false;
        }
        if (isRange(normalized)) {
            return matchesRange(normalized, actualVersion);
        }
        for (String part : normalized.split(",")) {
            String single = part.trim();
            if (!single.isEmpty() && !matchesSingle(single, actualVersion)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesSingle(String constraint, String actualVersion) {
        String operator = "=";
        String expected = constraint;
        for (String candidate : List.of(">=", "<=", "==", ">", "<", "=")) {
            if (constraint.startsWith(candidate)) {
                operator = candidate;
                expected = constraint.substring(candidate.length()).trim();
                break;
            }
        }
        int comparison = compare(actualVersion, expected);
        return switch (operator) {
            case ">=" -> comparison >= 0;
            case "<=" -> comparison <= 0;
            case ">" -> comparison > 0;
            case "<" -> comparison < 0;
            case "=", "==" -> comparison == 0;
            default -> false;
        };
    }

    private static boolean matchesRange(String range, String actualVersion) {
        boolean includeMin = range.startsWith("[");
        boolean includeMax = range.endsWith("]");
        String body = range.substring(1, range.length() - 1);
        int separator = body.indexOf(',');
        if (separator < 0) {
            return matchesSingle(body.trim(), actualVersion);
        }
        String min = body.substring(0, separator).trim();
        String max = body.substring(separator + 1).trim();
        if (!min.isEmpty()) {
            int comparison = compare(actualVersion, min);
            if (comparison < 0 || (!includeMin && comparison == 0)) {
                return false;
            }
        }
        if (!max.isEmpty()) {
            int comparison = compare(actualVersion, max);
            if (comparison > 0 || (!includeMax && comparison == 0)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isRange(String value) {
        return value.length() >= 2
                && (value.startsWith("[") || value.startsWith("("))
                && (value.endsWith("]") || value.endsWith(")"));
    }

    static int compare(String left, String right) {
        List<String> leftParts = parts(left);
        List<String> rightParts = parts(right);
        int length = Math.max(leftParts.size(), rightParts.size());
        for (int index = 0; index < length; index++) {
            String leftPart = index < leftParts.size() ? leftParts.get(index) : "0";
            String rightPart = index < rightParts.size() ? rightParts.get(index) : "0";
            int comparison = comparePart(leftPart, rightPart);
            if (comparison != 0) {
                return comparison;
            }
        }
        return 0;
    }

    private static List<String> parts(String version) {
        String normalized = version == null ? "" : version.trim().toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String raw : normalized.split("[._+\\-]")) {
            if (!raw.isBlank()) {
                result.add(raw);
            }
        }
        return result.isEmpty() ? List.of("0") : result;
    }

    private static int comparePart(String left, String right) {
        boolean leftNumber = left.matches("\\d+");
        boolean rightNumber = right.matches("\\d+");
        if (leftNumber && rightNumber) {
            return new BigInteger(left).compareTo(new BigInteger(right));
        }
        if (leftNumber != rightNumber) {
            return leftNumber ? 1 : -1;
        }
        return left.compareTo(right);
    }
}
