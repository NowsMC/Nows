/*
 * Copyright 2026 TamKungZ_ (Nows MC — https://nows.space)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package space.nows.platform.core.mod;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Small version constraint matcher for loader metadata. */
public final class ModVersionConstraint {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\d+|[a-z]+");

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
        boolean matchedAny = false;
        for (String part : normalized.split(",")) {
            String single = part.trim();
            if (single.isEmpty()) {
                continue;
            }
            matchedAny = true;
            if (!matchesSingle(single, actualVersion)) {
                return false;
            }
        }
        return matchedAny;
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
        if (expected.isBlank()) {
            return false;
        }
        if ((operator.equals("=") || operator.equals("==")) && matchesWildcard(expected, actualVersion)) {
            return true;
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
        if (min.isEmpty() && max.isEmpty()) {
            return false;
        }
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
        VersionKey leftKey = VersionKey.parse(left);
        VersionKey rightKey = VersionKey.parse(right);
        int length = Math.max(leftKey.parts().size(), rightKey.parts().size());
        for (int index = 0; index < length; index++) {
            VersionPart leftPart = leftKey.partOrDefault(index);
            VersionPart rightPart = rightKey.partOrDefault(index);
            int comparison = comparePart(leftPart, rightPart);
            if (comparison != 0) {
                return comparison;
            }
        }
        return 0;
    }

    private static boolean matchesWildcard(String expected, String actual) {
        List<String> expectedParts = rawParts(expected);
        if (expectedParts.stream().noneMatch(ModVersionConstraint::isWildcardPart)) {
            return false;
        }
        List<String> actualParts = rawParts(actual);
        for (int index = 0; index < expectedParts.size(); index++) {
            String expectedPart = expectedParts.get(index);
            if (isWildcardPart(expectedPart)) {
                return true;
            }
            String actualPart = index < actualParts.size() ? actualParts.get(index) : "0";
            if (compareRawPart(actualPart, expectedPart) != 0) {
                return false;
            }
        }
        return actualParts.size() <= expectedParts.size();
    }

    private static boolean isWildcardPart(String part) {
        return part.equals("*") || part.equals("x");
    }

    private static List<String> rawParts(String version) {
        String normalized = normalize(version);
        List<String> result = new ArrayList<>();
        for (String raw : normalized.split("[\\s._+\\-/]+")) {
            if (!raw.isBlank()) {
                result.add(raw);
            }
        }
        return result.isEmpty() ? List.of("0") : result;
    }

    private static int compareRawPart(String left, String right) {
        return comparePart(VersionPart.parse(left), VersionPart.parse(right));
    }

    private static int comparePart(VersionPart left, VersionPart right) {
        if (left.number() && right.number()) {
            return new BigInteger(left.value()).compareTo(new BigInteger(right.value()));
        }
        if (left.number() != right.number()) {
            VersionPart number = left.number() ? left : right;
            VersionPart label = left.number() ? right : left;
            if (number.value().matches("0+") && qualifierRank(label.value()) == 0) {
                return 0;
            }
            return left.number() ? 1 : -1;
        }
        int leftRank = qualifierRank(left.value());
        int rightRank = qualifierRank(right.value());
        if (leftRank != rightRank) {
            return Integer.compare(leftRank, rightRank);
        }
        if (leftRank != -5) {
            return 0;
        }
        return left.value().compareTo(right.value());
    }

    private static int qualifierRank(String value) {
        return switch (value) {
            case "" -> 0;
            case "snapshot", "nightly", "dev", "devel", "development", "canary" -> -60;
            case "alpha", "a" -> -50;
            case "beta", "b" -> -40;
            case "milestone", "m" -> -30;
            case "preview", "pre" -> -25;
            case "rc", "cr" -> -20;
            case "build" -> -10;
            case "release", "final", "ga", "stable" -> 0;
            default -> -5;
        };
    }

    private static String normalize(String version) {
        String normalized = version == null ? "" : version.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > 1 && normalized.charAt(0) == 'v' && Character.isDigit(normalized.charAt(1))) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private record VersionKey(List<VersionPart> parts) {
        private static VersionKey parse(String version) {
            String normalized = normalize(version);
            int metadata = normalized.indexOf('+');
            if (metadata >= 0) {
                normalized = normalized.substring(0, metadata);
            }
            normalized = normalized.replaceAll("\"[^\"]*\"|'[^']*'", " ");
            Matcher matcher = TOKEN_PATTERN.matcher(normalized);
            List<VersionPart> parts = new ArrayList<>();
            while (matcher.find()) {
                parts.add(VersionPart.parse(matcher.group()));
            }
            return new VersionKey(parts.isEmpty() ? List.of(VersionPart.zero()) : List.copyOf(parts));
        }

        private VersionPart partOrDefault(int index) {
            return index < parts.size() ? parts.get(index) : VersionPart.zero();
        }
    }

    private record VersionPart(String value, boolean number) {
        private static VersionPart parse(String value) {
            String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT);
            return new VersionPart(normalized, normalized.matches("\\d+"));
        }

        private static VersionPart zero() {
            return new VersionPart("0", true);
        }
    }
}
