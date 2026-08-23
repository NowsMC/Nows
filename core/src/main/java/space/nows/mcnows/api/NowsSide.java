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

package space.nows.mcnows.api;

import java.util.Locale;

/** Physical runtime side for loader and mod metadata. */
public enum NowsSide {
    CLIENT,
    SERVER,
    BOTH;

    public boolean supports(NowsSide runtimeSide) {
        return this == BOTH || this == runtimeSide;
    }

    public String metadataName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static NowsSide parse(String value) {
        if (value == null || value.isBlank()) {
            return BOTH;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "client" -> CLIENT;
            case "server" -> SERVER;
            case "both", "*", "common", "universal" -> BOTH;
            default -> throw new IllegalArgumentException(
                    "Invalid Nows side '" + value + "'; expected client, server or both");
        };
    }
}
