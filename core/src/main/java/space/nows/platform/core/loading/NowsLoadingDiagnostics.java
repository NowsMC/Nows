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

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.util.Locale;

public record NowsLoadingDiagnostics(
        long heapUsedMb,
        long heapMaxMb,
        float heapProgress,
        long offHeapMb,
        double cpuPercent
) {
    public static NowsLoadingDiagnostics capture() {
        Runtime runtime = Runtime.getRuntime();
        long heapUsed = runtime.totalMemory() - runtime.freeMemory();
        long heapMax = runtime.maxMemory();
        long offHeap = 0L;
        for (BufferPoolMXBean bufferPool : ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class)) {
            long memoryUsed = bufferPool.getMemoryUsed();
            if (memoryUsed > 0L) {
                offHeap += memoryUsed;
            }
        }
        return new NowsLoadingDiagnostics(
                toMb(heapUsed),
                Math.max(1L, toMb(heapMax)),
                Math.max(0.0F, Math.min(1.0F, heapUsed / (float) Math.max(1L, heapMax))),
                toMb(offHeap),
                processCpuPercent());
    }

    public String summary() {
        String cpu = cpuPercent < 0.0D ? "--" : String.format(Locale.ROOT, "%.1f%%", cpuPercent);
        return String.format(Locale.ROOT,
                "Heap: %d/%d MB (%.1f%%)  OffHeap: %d MB  CPU: %s",
                heapUsedMb,
                heapMaxMb,
                heapProgress * 100.0F,
                offHeapMb,
                cpu);
    }

    private static long toMb(long bytes) {
        return bytes / (1024L * 1024L);
    }

    private static double processCpuPercent() {
        java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        if (bean instanceof com.sun.management.OperatingSystemMXBean operatingSystem) {
            double load = operatingSystem.getProcessCpuLoad();
            if (load >= 0.0D) {
                return Math.max(0.0D, Math.min(100.0D, load * 100.0D));
            }
        }
        return -1.0D;
    }
}
