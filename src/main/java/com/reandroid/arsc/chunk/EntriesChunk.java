/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reandroid.arsc.chunk;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.Creator;
import com.reandroid.common.Mergeable;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.CountedBlockList;
import com.reandroid.arsc.header.CountHeader;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONItem;
import com.reandroid.utils.ObjectsUtil;

import java.util.Iterator;

public class EntriesChunk<T extends Block> extends Chunk<CountHeader>
        implements JSONConvert<JSONItem>, Mergeable<EntriesChunk<? extends T>> {

    private final CountedBlockList<T> itemList;

    EntriesChunk(int initialSize, CountHeader header, Creator<? extends T> creator) {
        super(initialSize + 1, header);

        this.itemList = new CountedBlockList<>(creator, header.count());

        setupBlock(itemList);
    }
    public EntriesChunk(CountHeader header, Creator<? extends T> creator) {
        this(0, header, creator);
    }
    public EntriesChunk(ChunkType chunkType, Creator<? extends T> creator) {
        this(0, new CountHeader(chunkType), creator);
    }

    public T get(int i) {
        return itemList.get(i);
    }
    public int size() {
        return itemList.size();
    }
    public boolean isEmpty() {
        return itemList.size() == 0;
    }
    public Iterator<T> iterator() {
        return itemList.iterator();
    }
    public void clear() {
        itemList.clearBlocks();
    }
    public T createNext() {
        return itemList.createNext();
    }
    public void setSize(int size) {
        itemList.setSize(size);
    }
    public void ensureCapacity(int size) {
        itemList.ensureCapacity(size);
    }

    public BlockList<T> itemList() {
        return itemList;
    }

    @Override
    public JSONItem toJson() {
        return BlockList.toJsonArray(ObjectsUtil.cast(itemList));
    }

    @Override
    public void fromJson(JSONItem json) {
        BlockList.fromJsonArray(ObjectsUtil.cast(itemList), (JSONArray) json);
    }
    public boolean canMerge(T item) {
        return true;
    }
    @Override
    public void merge(EntriesChunk<? extends T> entriesChunk) {
        if (entriesChunk == null || entriesChunk == this) {
            return;
        }
        BlockList<T> destinationList = this.itemList();
        BlockList<T> sourceList = ObjectsUtil.cast(entriesChunk.itemList());
        int size = sourceList.size();
        if (destinationList.size() == 0) {
            destinationList.setSize(size);
            for (int i = 0; i < size; i++) {
                ((Mergeable<?>)destinationList.get(i)).merge(ObjectsUtil.cast(sourceList.get(i)));
            }
        } else {
            destinationList.ensureCapacity(size);
            for (int i = 0; i < size; i++) {
                T source = sourceList.get(i);
                if (canMerge(source)) {
                    ((Mergeable<?>) destinationList.createNext()).merge(
                            ObjectsUtil.cast(source));
                }
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": count = " + size();
    }
}
