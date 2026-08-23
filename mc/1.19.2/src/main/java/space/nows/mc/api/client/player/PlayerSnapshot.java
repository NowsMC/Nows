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

package space.nows.mc.api.client.player;

import space.nows.mc.api.McVec3;

import java.util.UUID;

public record PlayerSnapshot(
        UUID id,
        String name,
        McVec3 position,
        McVec3 velocity,
        float yaw,
        float pitch,
        float health,
        float maxHealth,
        int food,
        float saturation,
        int level,
        int totalExperience,
        float experienceProgress,
        boolean creative,
        boolean spectator,
        boolean flying,
        boolean mayFly,
        int selectedHotbarSlot
) {}
