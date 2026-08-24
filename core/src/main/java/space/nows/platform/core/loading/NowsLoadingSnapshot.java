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

import java.util.List;

public record NowsLoadingSnapshot(
        String title,
        String stage,
        String detail,
        int step,
        int totalSteps,
        boolean failed,
        List<String> history
) {
    public float progress() {
        if (totalSteps <= 0) {
            return failed ? 1.0F : 0.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, step / (float) totalSteps));
    }
}
