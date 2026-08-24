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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NowsLoadingStateTest {
    @Test
    void tracksPhaseProgressAndRecentHistory() {
        NowsLoadingState.start("Nows Loader", 2);
        NowsLoadingState.begin("Discover Nows mods");
        NowsLoadingState.detail("mods/example.jar");
        NowsLoadingState.complete("Discover Nows mods");
        NowsLoadingState.begin("Run mod entrypoints");
        NowsLoadingState.subtask("Mod entrypoints", 1, 4);

        NowsLoadingSnapshot snapshot = NowsLoadingState.snapshot();

        assertEquals("Nows Loader", snapshot.title());
        assertEquals("Run mod entrypoints", snapshot.stage());
        assertEquals(1, snapshot.step());
        assertEquals(2, snapshot.totalSteps());
        assertEquals(0.5F, snapshot.progress());
        assertEquals("Mod entrypoints", snapshot.subtask());
        assertEquals(0.25F, snapshot.subProgress());
        assertFalse(snapshot.failed());
        assertEquals("Discover Nows mods", snapshot.history().get(0));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.history().add("mutated"));
    }

    @Test
    void exposesFailuresWithoutAdvancingProgress() {
        NowsLoadingState.start("Nows Loader", 4);
        NowsLoadingState.begin("Install Mixin integration");
        NowsLoadingState.fail("Install Mixin integration", new IllegalStateException("bad config"));

        NowsLoadingSnapshot snapshot = NowsLoadingState.snapshot();

        assertTrue(snapshot.failed());
        assertEquals(0, snapshot.step());
        assertEquals("Install Mixin integration", snapshot.stage());
        assertTrue(snapshot.detail().contains("IllegalStateException"));
    }
}
