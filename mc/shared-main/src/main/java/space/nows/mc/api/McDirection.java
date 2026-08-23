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

package space.nows.mc.api;

/** Stable six-way block/entity direction. */
public enum McDirection {
    DOWN(0, -1, 0, Axis.Y),
    UP(0, 1, 0, Axis.Y),
    NORTH(0, 0, -1, Axis.Z),
    SOUTH(0, 0, 1, Axis.Z),
    WEST(-1, 0, 0, Axis.X),
    EAST(1, 0, 0, Axis.X);

    private final int stepX;
    private final int stepY;
    private final int stepZ;
    private final Axis axis;

    McDirection(int stepX, int stepY, int stepZ, Axis axis) {
        this.stepX = stepX;
        this.stepY = stepY;
        this.stepZ = stepZ;
        this.axis = axis;
    }

    public int stepX() {
        return stepX;
    }

    public int stepY() {
        return stepY;
    }

    public int stepZ() {
        return stepZ;
    }

    public Axis axis() {
        return axis;
    }

    public boolean horizontal() {
        return axis != Axis.Y;
    }

    public enum Axis {
        X,
        Y,
        Z
    }
}
