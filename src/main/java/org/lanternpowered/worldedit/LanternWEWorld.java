/*
 * This file is part of LanternWorldEdit, licensed under the MIT License (MIT).
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
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

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.LazyBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.sponge.SpongeWorld;
import com.sk89q.worldedit.util.TreeGenerator;
import org.lanternpowered.server.data.io.store.ObjectSerializer;
import org.lanternpowered.server.data.io.store.ObjectSerializerRegistry;
import org.lanternpowered.server.data.io.store.ObjectStore;
import org.lanternpowered.server.data.io.store.ObjectStoreRegistry;
import org.lanternpowered.server.game.registry.type.block.BlockRegistryModule;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class LanternWEWorld extends SpongeWorld {

    LanternWEWorld(World world) {
        super(world);
    }

    @Override
    protected BlockState getBlockState(BaseBlock baseBlock) {
        return BlockRegistryModule.get().getStateByInternalIdAndData(baseBlock.getId(), (byte) baseBlock.getData()).orElse(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void applyTileEntityData(TileEntity tileEntity, BaseBlock baseBlock) {
        final ObjectStore store = ObjectStoreRegistry.get().get(tileEntity.getClass())
                .orElseThrow(() -> new IllegalStateException("Missing object store for tile " + tileEntity.getType()));
        final CompoundTag tag = baseBlock.getNbtData();
        final DataView dataView = tag == null ? new MemoryDataContainer(DataView.SafetyMode.NO_DATA_CLONED) : DataViewNbt.from(tag);
        store.deserialize(tileEntity, dataView);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void applyEntityData(Entity entity, BaseEntity baseEntity) {
        final ObjectStore store = ObjectStoreRegistry.get().get(entity.getClass())
                .orElseThrow(() -> new IllegalStateException("Missing object store for entity " + entity.getType()));
        final CompoundTag tag = baseEntity.getNbtData();
        final DataView dataView = tag == null ? new MemoryDataContainer(DataView.SafetyMode.NO_DATA_CLONED) : DataViewNbt.from(tag);
        for (String field : Constants.NO_COPY_ENTITY_NBT_FIELDS) {
            dataView.remove(DataQuery.of(field));
        }
        store.deserialize(entity, dataView);
    }

    @Override
    public boolean clearContainerBlockContents(Vector position) {
        final Optional<TileEntity> optTile = getWorld().getTileEntity(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        if (optTile.isPresent() && optTile.get() instanceof Carrier) {
            final Carrier carrier = (Carrier) optTile.get();
            carrier.getInventory().clear();
            return true;
        }
        return false;
    }

    @Override
    public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, Vector position) throws MaxChangedBlocksException {
        // TODO
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public BaseBlock getBlock(Vector position) {
        final int state = BlockRegistryModule.get().getStateInternalIdAndData(
                getWorld().getBlock(position.getBlockX(), position.getBlockY(), position.getBlockZ()));
        final Optional<TileEntity> optTile = getWorld().getTileEntity(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        final BaseBlock baseBlock = new BaseBlock(state >> 4, state & 0xf);
        if (optTile.isPresent()) {
            final TileEntity tile = optTile.get();
            final ObjectSerializer serializer = ObjectSerializerRegistry.get().get(tile.getClass()).get();
            final DataView dataView = serializer.serialize(tile);
            baseBlock.setNbtData(DataViewNbt.to(dataView));
        }
        return baseBlock;
    }

    @Override
    public BaseBlock getLazyBlock(Vector position) {
        final int state = BlockRegistryModule.get().getStateInternalIdAndData(
                getWorld().getBlock(position.getBlockX(), position.getBlockY(), position.getBlockZ()));
        return new LazyBlock(state >> 4, state & 0xf, this, position);
    }
}
