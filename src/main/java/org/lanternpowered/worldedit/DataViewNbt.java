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

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.DoubleTag;
import com.sk89q.jnbt.EndTag;
import com.sk89q.jnbt.FloatTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.LongTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public final class DataViewNbt {

    public static CompoundTag to(DataView dataView) {
        return (CompoundTag) to0(dataView);
    }

    private static Tag to0(Object object) {
        if (object instanceof Byte) {
            return new ByteTag((Byte) object);
        } else if (object instanceof Short) {
            return new ShortTag((Short) object);
        } else if (object instanceof Integer) {
            return new IntTag((Integer) object);
        } else if (object instanceof Long) {
            return new LongTag((Long) object);
        } else if (object instanceof Double) {
            return new DoubleTag((Double) object);
        } else if (object instanceof Float) {
            return new FloatTag((Float) object);
        } else if (object instanceof String) {
            return new StringTag((String) object);
        } else if (object instanceof int[]) {
            return new IntArrayTag((int[]) object);
        } else if (object instanceof byte[]) {
            return new ByteArrayTag((byte[]) object);
        } else if (object instanceof DataView || object instanceof DataSerializable) {
            final DataView view = object instanceof DataSerializable ? ((DataSerializable) object).toContainer() : (DataView) object;
            final Map<String, Tag> result = new HashMap<>();
            for (Map.Entry<DataQuery, Object> entry : view.getValues(false).entrySet()) {
                result.put(entry.getKey().asString('.'), to0(entry.getValue()));
            }
            return new CompoundTag(result);
        } else if (object instanceof Map) {
            final Map<String, Tag> result = new HashMap<>();
            //noinspection unchecked
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) object).entrySet()) {
                result.put(entry.getKey().toString(), to0(entry.getValue()));
            }
            return new CompoundTag(result);
        } else if (object instanceof List) {
            final List<Tag> result = new ArrayList<>();
            for (Object value : ((List) object)) {
                result.add(to0(value));
            }
            return new ListTag(result.isEmpty() ? EndTag.class : result.get(0).getClass(), result);
        }
        throw new IllegalArgumentException("Unsupported object type: " + object);
    }

    public static DataView from(CompoundTag tag) {
        return (DataView) from(tag, null);
    }

    private static Object from(Tag tag, @Nullable DataView view) {
        if (tag instanceof CompoundTag) {
            final Map<String, Tag> map = ((CompoundTag) tag).getValue();
            if (view == null) {
                view = new MemoryDataContainer(DataView.SafetyMode.NO_DATA_CLONED);
            }
            for (Map.Entry<String, Tag> entry : map.entrySet()) {
                if (entry.getValue() instanceof CompoundTag) {
                    from(tag, view.createView(DataQuery.of(entry.getKey())));
                } else {
                    view.set(DataQuery.of(entry.getKey()), from(entry.getValue(), null));
                }
            }
            return view;
        } else if (tag instanceof ListTag) {
            return ((ListTag) tag).getValue().stream().map(entry -> from(entry, null)).collect(Collectors.toList());
        }
        return tag.getValue();
    }

    private DataViewNbt() {
    }
}
