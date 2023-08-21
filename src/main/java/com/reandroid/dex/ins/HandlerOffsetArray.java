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
package com.reandroid.dex.ins;

import com.reandroid.arsc.array.OffsetArray;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.common.IntegerArray;
import com.reandroid.dex.base.DexBlockItem;

import java.io.IOException;

public class HandlerOffsetArray extends DexBlockItem implements OffsetArray, IntegerArray {
    private final IntegerReference itemCount;
    private int itemsStart;
    public HandlerOffsetArray(IntegerReference itemCount) {
        super(0);
        this.itemCount = itemCount;
    }
    public int getStartAddress(int index){
        return getInteger(getBytesInternal(), index * 8);
    }
    public int getCatchCodeUnit(int index){
        return getShortUnsigned(getBytesInternal(), index * 8 + 4);
    }
    @Override
    public int get(int i) {
        return getOffset(i);
    }
    @Override
    public int getOffset(int i) {
        return getShortUnsigned(getBytesInternal(), i * 8 + 6);
    }
    @Override
    public void setOffset(int index, int value) {
        ensureSize(index + 1);
        putShort(getBytesInternal(), index * 8 + 6, value);
    }
    @Override
    public int[] getOffsets() {
        int size = size();
        int[] results = new int[size];
        for(int i = 0; i < size; i++){
            results[i] = getOffset(i);
        }
        return results;
    }
    @Override
    public int size() {
        return countBytes() / 8;
    }
    @Override
    public void setSize(int size) {
        setBytesLength(size * 8, false);
        itemCount.set(size);
    }
    @Override
    public void put(int i, int value) {
        setOffset(i, value);
    }

    @Override
    public void clear() {
        setSize(0);
    }
    private void ensureSize(int size){
        if(size > size()){
            setSize(size);
        }
    }

    public int getItemsStart() {
        return itemsStart;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        setBytesLength(itemCount.get() * 8, false);
        super.onReadBytes(reader);
        itemsStart = reader.getPosition();
    }

    @Override
    public String toString() {
        return IntegerArray.toString(this);
    }
}
