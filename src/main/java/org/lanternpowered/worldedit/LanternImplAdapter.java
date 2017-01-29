/*
 * This file is part of LanternWorldEdit, licensed under the MIT License (MIT).
 *
 * Copyright (c) LanternPowered https://www.lanternpowered.org
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, andor sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.lanternpowered.worldedit;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.sponge.SpongeWorld;
import com.sk89q.worldedit.sponge.adapter.SpongeImplAdapter;
import org.lanternpowered.server.data.io.store.ObjectSerializer;
import org.lanternpowered.server.data.io.store.ObjectSerializerRegistry;
import org.lanternpowered.server.game.registry.type.block.BlockRegistryModule;
import org.lanternpowered.server.game.registry.type.item.ItemRegistryModule;
import org.lanternpowered.server.game.registry.type.world.biome.BiomeRegistryModule;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;

public class LanternImplAdapter implements SpongeImplAdapter {

    @Override
    public int resolve(ItemType itemType) {
        return ItemRegistryModule.get().getInternalId(itemType);
    }

    @Override
    public int resolve(BlockType blockType) {
        return BlockRegistryModule.get().getStateInternalId(blockType.getDefaultState());
    }

    @Override
    public int resolve(BiomeType biomeType) {
        return BiomeRegistryModule.get().getInternalId(biomeType);
    }

    @Override
    public ItemType resolveItem(int i) {
        return ItemRegistryModule.get().getTypeByInternalId(i).orElse(null);
    }

    @Override
    public BlockType resolveBlock(int i) {
        return BlockRegistryModule.get().getStateByInternalId(i).map(BlockState::getType).orElse(null);
    }

    @Override
    public BiomeType resolveBiome(int i) {
        return BiomeRegistryModule.get().getByInternalId(i).orElse(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public BaseEntity createBaseEntity(Entity entity) {
        checkNotNull(entity, "entity");
        final ObjectSerializer serializer = ObjectSerializerRegistry.get().get(entity.getClass()).get();
        final DataView dataView = serializer.serialize(entity);
        return new BaseEntity(entity.getType().getId(), DataViewNbt.to(dataView));
    }

    @Override
    public ItemStack makeSpongeStack(BaseItemStack baseItemStack) {
        return null;
    }

    @Override
    public SpongeWorld getWorld(World world) {
        return new LanternWEWorld(world);
    }
}
