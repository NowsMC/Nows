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

package space.nows.platform.core.loading;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class NowsAsciiFont {
    public static final int GLYPH_WIDTH = 5;
    public static final int GLYPH_HEIGHT = 7;
    public static final int GLYPH_SPACING = 1;

    private static final int[] UNKNOWN = rows(
            "11111",
            "10001",
            "00001",
            "00010",
            "00100",
            "00000",
            "00100");
    private static final int[] SPACE = rows(
            "00000",
            "00000",
            "00000",
            "00000",
            "00000",
            "00000",
            "00000");
    private static final Map<Character, int[]> GLYPHS = glyphs();

    private NowsAsciiFont() {
    }

    public static int[] glyph(char character) {
        if (character == ' ') {
            return SPACE;
        }
        return GLYPHS.getOrDefault(Character.toUpperCase(character), UNKNOWN);
    }

    public static int width(String text, int scale) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.length() * (GLYPH_WIDTH + GLYPH_SPACING) * scale - GLYPH_SPACING * scale;
    }

    public static int height(int scale) {
        return GLYPH_HEIGHT * scale;
    }

    private static Map<Character, int[]> glyphs() {
        Map<Character, int[]> glyphs = new HashMap<>();
        put(glyphs, 'A', "01110", "10001", "10001", "11111", "10001", "10001", "10001");
        put(glyphs, 'B', "11110", "10001", "10001", "11110", "10001", "10001", "11110");
        put(glyphs, 'C', "01111", "10000", "10000", "10000", "10000", "10000", "01111");
        put(glyphs, 'D', "11110", "10001", "10001", "10001", "10001", "10001", "11110");
        put(glyphs, 'E', "11111", "10000", "10000", "11110", "10000", "10000", "11111");
        put(glyphs, 'F', "11111", "10000", "10000", "11110", "10000", "10000", "10000");
        put(glyphs, 'G', "01111", "10000", "10000", "10011", "10001", "10001", "01111");
        put(glyphs, 'H', "10001", "10001", "10001", "11111", "10001", "10001", "10001");
        put(glyphs, 'I', "11111", "00100", "00100", "00100", "00100", "00100", "11111");
        put(glyphs, 'J', "00111", "00010", "00010", "00010", "10010", "10010", "01100");
        put(glyphs, 'K', "10001", "10010", "10100", "11000", "10100", "10010", "10001");
        put(glyphs, 'L', "10000", "10000", "10000", "10000", "10000", "10000", "11111");
        put(glyphs, 'M', "10001", "11011", "10101", "10101", "10001", "10001", "10001");
        put(glyphs, 'N', "10001", "11001", "10101", "10011", "10001", "10001", "10001");
        put(glyphs, 'O', "01110", "10001", "10001", "10001", "10001", "10001", "01110");
        put(glyphs, 'P', "11110", "10001", "10001", "11110", "10000", "10000", "10000");
        put(glyphs, 'Q', "01110", "10001", "10001", "10001", "10101", "10010", "01101");
        put(glyphs, 'R', "11110", "10001", "10001", "11110", "10100", "10010", "10001");
        put(glyphs, 'S', "01111", "10000", "10000", "01110", "00001", "00001", "11110");
        put(glyphs, 'T', "11111", "00100", "00100", "00100", "00100", "00100", "00100");
        put(glyphs, 'U', "10001", "10001", "10001", "10001", "10001", "10001", "01110");
        put(glyphs, 'V', "10001", "10001", "10001", "10001", "10001", "01010", "00100");
        put(glyphs, 'W', "10001", "10001", "10001", "10101", "10101", "10101", "01010");
        put(glyphs, 'X', "10001", "10001", "01010", "00100", "01010", "10001", "10001");
        put(glyphs, 'Y', "10001", "10001", "01010", "00100", "00100", "00100", "00100");
        put(glyphs, 'Z', "11111", "00001", "00010", "00100", "01000", "10000", "11111");
        put(glyphs, '0', "01110", "10001", "10011", "10101", "11001", "10001", "01110");
        put(glyphs, '1', "00100", "01100", "00100", "00100", "00100", "00100", "01110");
        put(glyphs, '2', "01110", "10001", "00001", "00010", "00100", "01000", "11111");
        put(glyphs, '3', "11110", "00001", "00001", "01110", "00001", "00001", "11110");
        put(glyphs, '4', "00010", "00110", "01010", "10010", "11111", "00010", "00010");
        put(glyphs, '5', "11111", "10000", "10000", "11110", "00001", "00001", "11110");
        put(glyphs, '6', "01110", "10000", "10000", "11110", "10001", "10001", "01110");
        put(glyphs, '7', "11111", "00001", "00010", "00100", "01000", "01000", "01000");
        put(glyphs, '8', "01110", "10001", "10001", "01110", "10001", "10001", "01110");
        put(glyphs, '9', "01110", "10001", "10001", "01111", "00001", "00001", "01110");
        put(glyphs, ':', "00000", "00100", "00100", "00000", "00100", "00100", "00000");
        put(glyphs, '.', "00000", "00000", "00000", "00000", "00000", "01100", "01100");
        put(glyphs, '/', "00001", "00001", "00010", "00100", "01000", "10000", "10000");
        put(glyphs, '%', "11001", "11010", "00010", "00100", "01000", "01011", "10011");
        put(glyphs, '(', "00010", "00100", "01000", "01000", "01000", "00100", "00010");
        put(glyphs, ')', "01000", "00100", "00010", "00010", "00010", "00100", "01000");
        put(glyphs, '-', "00000", "00000", "00000", "11111", "00000", "00000", "00000");
        put(glyphs, '_', "00000", "00000", "00000", "00000", "00000", "00000", "11111");
        return Collections.unmodifiableMap(glyphs);
    }

    private static void put(Map<Character, int[]> glyphs, char character, String... rows) {
        glyphs.put(character, rows(rows));
    }

    private static int[] rows(String... rows) {
        int[] bits = new int[rows.length];
        for (int row = 0; row < rows.length; row++) {
            int value = 0;
            for (int column = 0; column < rows[row].length(); column++) {
                value <<= 1;
                if (rows[row].charAt(column) == '1') {
                    value |= 1;
                }
            }
            bits[row] = value;
        }
        return bits;
    }
}
