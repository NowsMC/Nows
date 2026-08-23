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

package space.nows.mcnows.mc.internal.registry;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import space.nows.mcnows.mc.api.registry.ItemLogic;

final class LogicItem extends Item {
    private final ItemLogic logic;

    LogicItem(Properties properties, ItemLogic logic) {
        super(properties);
        this.logic = logic;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return logic.useOn(context);
    }

    @Override
    public void onDestroyed(ItemEntity entity) {
        logic.destroyed(entity);
        super.onDestroyed(entity);
    }
}
