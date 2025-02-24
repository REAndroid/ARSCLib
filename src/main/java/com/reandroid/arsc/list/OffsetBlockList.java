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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.AlignItem;
import com.reandroid.arsc.item.IntegerReference;

import java.io.IOException;

public class OffsetBlockList<T extends Block> extends BlockList<T> {

    private final IntegerReference start;
    private final OffsetReferenceList<?> offsetReferenceList;

    public OffsetBlockList(IntegerReference start, OffsetReferenceList<?> offsetReferenceList, Creator<? extends T> creator) {
        super(creator);
        this.start = start;
        this.offsetReferenceList = offsetReferenceList;
    }
    public OffsetBlockList(IntegerReference start, OffsetReferenceList<?> offsetReferenceList) {
        super();
        this.start = start;
        this.offsetReferenceList = offsetReferenceList;
    }

    public IntegerReference getStart() {
        return start;
    }
    public IntegerReference getCountReference() {
        return getOffsetReferenceList().getCountReference();
    }
    public OffsetReferenceList<?> getOffsetReferenceList() {
        return offsetReferenceList;
    }
    public boolean isEmpty() {
        return size() == 0;
    }
    public void clear() {
        clearChildes();
    }
    public AlignItem getAlignment() {
        return null;
    }

    private void updateCountReference() {
        getOffsetReferenceList().setSize(size());
    }
    private void updateStartReference() {
        updateCountReference();
        Block parent = getParent();
        int start = 0;
        if (parent != null && !isEmpty()) {
            start = parent.countUpTo(this);
        }
        getStart().set(start);
    }
    public int buildOffsetList() {
        updateStartReference();
        OffsetReferenceList<?> referenceList = getOffsetReferenceList();
        int size = this.size();
        referenceList.setSize(size);
        int offset = 0;
        for (int i = 0; i < size; i++) {
            offset = referenceList.get(i).updateOffset(this.get(i), offset);
        }
        AlignItem alignment = getAlignment();
        if (alignment != null) {
            offset += alignment.align(offset);
        }
        return offset;
    }

    @Override
    public void setSize(int size, boolean notify) {
        super.setSize(size, true);
    }

    @Override
    protected void onPreRefresh() {
        super.onPreRefresh();
        updateCountReference();
    }

    @Override
    protected void refreshChildes() {
        int size = size();
        if (size != 0 && get(0) instanceof BlockRefresh) {
            for (int i = 0; i < size; i++) {
                ((BlockRefresh) get(i)).refresh();
            }
        }
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        buildOffsetList();
    }

    @Override
    protected void onReadBytes(BlockReader reader) throws IOException {
        if (getOffsetReferenceList().size() != 0) {
            int position = getStart().get();
            reader.seek(position);
            BlockReader listReader = reader.create(reader.available());
            readChildes(listReader);
            position = position + listReader.getPosition();
            listReader.close();
            reader.seek(position);
        }
    }

    @Override
    public void readChildes(BlockReader reader) throws IOException {
        OffsetReferenceList<?> referenceList = getOffsetReferenceList();
        int size = referenceList.size();
        this.setSize(size);
        for (int i = 0; i < size; i++) {
            referenceList.get(i).readTarget(reader, this.get(i));
        }
    }
}
