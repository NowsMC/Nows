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

package space.nows.mc.internal.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.nows.mc.internal.ClientHooks;

/**
 * Opens the window in which mods can use the registries: after createContents populated the vanilla content
 * and before freeze makes the registries read only.
 */
@Mixin(value = BuiltInRegistries.class, remap = false)
public abstract class BuiltInRegistriesMixin {
    @Inject(method = "freeze", at = @At("HEAD"), remap = false)
    private static void nows$openRegistryWindow(CallbackInfo ci) {
        ClientHooks.openRegistryWindow();
    }
}
