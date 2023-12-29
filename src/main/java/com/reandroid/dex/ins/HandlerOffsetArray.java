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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.DexBlockItem;

import java.io.IOException;
import java.util.Objects;

public class HandlerOffsetArray extends DexBlockItem {

    private final IntegerReference itemCount;
    private int itemsStart;

    public HandlerOffsetArray(IntegerReference itemCount) {
        super(0);
        this.itemCount = itemCount;
    }

    @Override
    protected byte[] getBytesInternal() {
        return super.getBytesInternal();
    }
    public HandlerOffset getOrCreate(int index) {
        ensureSize(index + 1);
        return get(index);
    }
    public HandlerOffset get(int index) {
        if(index < 0 || index >= size()){
            return null;
        }
        return new HandlerOffset(this, index);
    }
    public int getOffset(int i) {
        if(i >= size()){
            return -1;
        }
        return getShortUnsigned(getBytesInternal(), i * 8 + 6);
    }
    public void setOffset(int index, int value) {
        ensureSize(index + 1);
        putShort(getBytesInternal(), index * 8 + 6, value);
    }
    public int[] getOffsets() {
        int size = size();
        int[] results = new int[size];
        for(int i = 0; i < size; i++){
            results[i] = getOffset(i);
        }
        return results;
    }
    public int size() {
        return countBytes() / 8;
    }
    public void setSize(int size) {
        setBytesLength(size * 8, false);
        itemCount.set(size);
    }
    public void put(int i, int value) {
        setOffset(i, value);
    }

    public int indexOf(int value){
        int size = size();
        for(int i = 0; i < size; i++){
            if(value == getOffset(i)){
                return i;
            }
        }
        return -1;
    }

    public void clear() {
        setSize(0);
    }
    private void ensureSize(int size){
        if(size > size()){
            setSize(size);
        }
    }

    int getItemsStart() {
        return itemsStart;
    }
    int getMinStart() {
        int result = 0;
        int size = size();
        for(int i = 0; i < size; i++){
            int offset = getOffset(i);
            if(i == 0 || offset < result){
                result = offset;
            }
        }
        return result;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        setBytesLength(itemCount.get() * 8, false);
        super.onReadBytes(reader);
        itemsStart = reader.getPosition();
    }

    public void merge(HandlerOffsetArray array){
        byte[] coming = array.getBytesInternal();
        int length = coming.length;
        setBytesLength(length, false);
        byte[] bytes = getBytesInternal();
        for(int i = 0; i < length; i++){
            bytes[i] = coming[i];
        }
        itemCount.set(array.itemCount.get());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        HandlerOffsetArray offsetArray = (HandlerOffsetArray) obj;
        return Block.areEqual(getBytesInternal(), offsetArray.getBytesInternal());
    }
    @Override
    public int hashCode() {
        return Block.hashCodeOf(getBytesInternal());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int size = size();
        builder.append("size = ");
        builder.append(size);
        builder.append('[');
        for(int i = 0; i < size; i++){
            if(i != 0){
                builder.append(", ");
            }
            builder.append(get(i));
        }
        builder.append(']');
        return builder.toString();
    }

}
