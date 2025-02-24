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
package com.reandroid.arsc.list;

import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.base.BlockLocator;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.header.StringPoolHeader;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.AlignItem;
import com.reandroid.arsc.item.StringCreator;
import com.reandroid.arsc.item.StringItem;
import com.reandroid.arsc.pool.StringPool;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;
import java.util.Comparator;

public class StringItemList<T extends StringItem> extends OffsetBlockList<T> implements JSONConvert<JSONArray> {

    private final AlignItem alignment;
    private final StringPoolHeader header;
    private int mBytesCount;
    private boolean mSortRequired;

    public StringItemList(AlignItem alignment,
                          StringPoolHeader header,
                          OffsetReferenceList<?> offsetReferenceList,
                          StringCreator<? extends T> stringCreator) {
        super(header.getStartStrings(), offsetReferenceList);

        this.alignment = alignment;
        this.header = header;

        header.setEncodingChangedListener(StringItemList.this::onStringEncodingChanged);

        setCreator(() -> stringCreator.newInstance(header.isUtf8()));
    }

    public boolean isUtf8() {
        return header.isUtf8();
    }
    public void setUtf8(boolean utf8) {
        header.setUtf8(utf8);
    }

    void onStringEncodingChanged(boolean utf8) {
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).setUtf8(utf8);
        }
        resetCountBytes();
    }

    @Override
    public void onPreRemove(T block) {
        StringPool<T> stringPool = getStringPool();
        stringPool.onStringRemoved(block);
        block.onRemoved();
        super.onPreRemove(block);
        resetCountBytes();
    }

    public void sort() {
        sort(CompareUtil.getComparableComparator());
    }
    @Override
    public boolean sort(Comparator<? super T> comparator) {
        mSortRequired = false;
        boolean sorted = super.sort(comparator);
        StringPool<T> stringPool = getStringPool();
        if (stringPool != null) {
            stringPool.onSortedInternal();
        }
        mSortRequired = false;
        return sorted;
    }

    @Override
    public void add(int index, T item) {
        getStringPool().onPreAddInternal(index, item);
        super.add(index, item);
    }

    private StringPool<T> getStringPool() {
        return ObjectsUtil.cast(getParentInstance(StringPool.class));
    }

    @Override
    public AlignItem getAlignment() {
        return alignment;
    }

    @Override
    public int buildOffsetList() {
        int count = super.buildOffsetList();
        this.mBytesCount = count - getAlignment().size();
        return count;
    }

    public void onStringChanged(String old, T stringItem) {
        StringPool<T> stringPool = getStringPool();
        if (stringPool != null) {
            resetCountBytes();
            stringPool.onStringChanged(old, stringItem);
        }
        mSortRequired = true;
    }

    @Override
    protected void onPreRefresh() {
        super.onPreRefresh();
        if (mSortRequired) {
            sort();
        }
    }

    @Override
    public int countBytes() {
        int count = this.mBytesCount;
        if (count == 0 && size() != 0) {
            count = super.countBytes();
            this.mBytesCount = count;
        }
        return count;
    }

    @Override
    public void onCountUpTo(BlockCounter counter) {
        if (counter.FOUND) {
            return;
        }
        counter.setCurrent(this);
        if (counter.END == this) {
            counter.FOUND = true;
            return;
        }
        if (counter.END instanceof StringItem || counter instanceof BlockLocator) {
            super.onCountUpTo(counter);
            return;
        }
        counter.addCount(countBytes());
    }

    private void resetCountBytes() {
        mBytesCount = 0;
    }

    @Override
    public void readChildes(BlockReader reader) throws IOException {
        super.readChildes(reader);
        resetCountBytes();
        mSortRequired = false;
    }

    @Override
    public JSONArray toJson() {
        return BlockList.toJsonArray(this);
    }
    @Override
    public void fromJson(JSONArray json) {
        if (json != null) {
            int start = size();
            int length = json.length();
            setSize(start + length);
            for (int i = 0; i < length; i++) {
                get(start + i).fromJson(json.getJSONObject(i));
            }
        }
    }
}
