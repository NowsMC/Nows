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
import java.util.Locale;

public record NowsLoadingSnapshot(
        String title,
        String stage,
        String detail,
        int step,
        int totalSteps,
        String subtask,
        int subStep,
        int subTotal,
        boolean failed,
        List<String> history
) {
    public float progress() {
        if (totalSteps <= 0) {
            return failed ? 1.0F : 0.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, step / (float) totalSteps));
    }

    public float subProgress() {
        if (subTotal <= 0) {
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, subStep / (float) subTotal));
    }

    public float overallProgress() {
        if (totalSteps <= 0) {
            return failed ? 1.0F : 0.0F;
        }
        float activeStep = Math.min(totalSteps, step + subProgress());
        return Math.max(0.0F, Math.min(1.0F, activeStep / (float) totalSteps));
    }

    public float displayProgress() {
        if (subTotal > 0 && step >= totalSteps) {
            return subProgress();
        }
        return overallProgress();
    }

    public String progressLabel() {
        return String.format(Locale.ROOT, "%d/%d", Math.max(0, step), Math.max(0, totalSteps));
    }

    public String subProgressLabel() {
        if (subTotal <= 0) {
            return "";
        }
        return String.format(Locale.ROOT, "%d/%d", Math.max(0, subStep), Math.max(0, subTotal));
    }

    public String displayProgressLabel() {
        if (subTotal > 0 && step >= totalSteps) {
            return subProgressLabel();
        }
        return progressLabel();
    }

    public boolean hasDetail() {
        return detail != null && !detail.isBlank() && !"Done".equals(detail);
    }

    public String currentDetailLine() {
        return hasDetail() ? detail : latestCompletedLine();
    }

    public String latestCompletedLine() {
        return history.isEmpty() ? "" : "Finished: " + history.get(0);
    }
}
