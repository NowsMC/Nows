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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class NowsLoadingState {
    private static final int HISTORY_LIMIT = 7;
    private static final Object LOCK = new Object();
    private static String title = "Nows Loader";
    private static String stage = "Starting";
    private static String detail = "";
    private static int step;
    private static int totalSteps = 1;
    private static String subtask = "";
    private static int subStep;
    private static int subTotal;
    private static boolean failed;
    private static final ArrayDeque<String> history = new ArrayDeque<>();

    private NowsLoadingState() {
    }

    public static void start(String title, int totalSteps) {
        synchronized (LOCK) {
            NowsLoadingState.title = requireText(title, "title");
            NowsLoadingState.stage = "Starting";
            NowsLoadingState.detail = "";
            NowsLoadingState.step = 0;
            NowsLoadingState.totalSteps = Math.max(1, totalSteps);
            NowsLoadingState.subtask = "";
            NowsLoadingState.subStep = 0;
            NowsLoadingState.subTotal = 0;
            NowsLoadingState.failed = false;
            history.clear();
        }
    }

    public static void begin(String stage) {
        synchronized (LOCK) {
            NowsLoadingState.stage = requireText(stage, "stage");
            NowsLoadingState.detail = "";
            NowsLoadingState.subtask = "";
            NowsLoadingState.subStep = 0;
            NowsLoadingState.subTotal = 0;
        }
    }

    public static void detail(String detail) {
        synchronized (LOCK) {
            NowsLoadingState.detail = detail == null ? "" : detail.trim();
        }
    }

    public static void subtask(String subtask, int step, int total) {
        synchronized (LOCK) {
            NowsLoadingState.subtask = subtask == null ? "" : subtask.trim();
            NowsLoadingState.subStep = Math.max(0, step);
            NowsLoadingState.subTotal = Math.max(0, total);
        }
    }

    public static void complete(String stage) {
        synchronized (LOCK) {
            if (step < totalSteps) {
                step++;
            }
            String completed = requireText(stage, "stage");
            history.addFirst(completed);
            while (history.size() > HISTORY_LIMIT) {
                history.removeLast();
            }
            NowsLoadingState.stage = completed;
            NowsLoadingState.detail = "Done";
            NowsLoadingState.subtask = "";
            NowsLoadingState.subStep = 0;
            NowsLoadingState.subTotal = 0;
        }
    }

    public static void fail(String stage, Throwable failure) {
        synchronized (LOCK) {
            failed = true;
            NowsLoadingState.stage = requireText(stage, "stage");
            NowsLoadingState.detail = failure == null ? "Failed" : failure.getClass().getSimpleName() + ": " + failure.getMessage();
        }
    }

    public static NowsLoadingSnapshot snapshot() {
        synchronized (LOCK) {
            return new NowsLoadingSnapshot(
                    title,
                    stage,
                    detail,
                    step,
                    totalSteps,
                    subtask,
                    subStep,
                    subTotal,
                    failed,
                    List.copyOf(new ArrayList<>(history)));
        }
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
