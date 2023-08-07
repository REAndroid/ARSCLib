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
package com.reandroid.arsc.array;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockArray;
import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.io.BlockLoad;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.AlignItem;
import com.reandroid.arsc.item.IntegerReference;


import java.io.IOException;
import java.io.OutputStream;

public abstract class OffsetBlockArray<T extends Block> extends BlockArray<T> implements BlockLoad {
    private final OffsetArray mOffsetArray;
    private final IntegerReference mItemStart;
    private final IntegerReference mItemCount;
    private final AlignItem alignItem;

    public OffsetBlockArray(OffsetArray offsets, IntegerReference itemCount, IntegerReference itemStart){
        super();
        this.mOffsetArray = offsets;
        this.mItemCount = itemCount;
        this.mItemStart = itemStart;
        this.alignItem = new AlignItem();
        if(itemCount instanceof Block){
            ((Block)itemCount).setBlockLoad(this);
        }
    }
    OffsetArray getOffsetArray(){
        return mOffsetArray;
    }
    protected AlignItem getAlignItem(){
        return alignItem;
    }
    @Override
    public void clearChildes(){
        super.clearChildes();
        mOffsetArray.clear();
        mItemStart.set(0);
        mItemCount.set(0);
        alignItem.clear();
    }
    @Override
    public int countBytes(){
        int result = super.countBytes();
        int alignSize = getAlignItem().countBytes();
        return result + alignSize;
    }
    @Override
    public void onCountUpTo(BlockCounter counter){
        super.onCountUpTo(counter);
        if(counter.FOUND){
            return;
        }
        getAlignItem().onCountUpTo(counter);
    }
    @Override
    public byte[] getBytes(){
        byte[] results = super.getBytes();
        if(results == null){
            return null;
        }
        byte[] alignBytes = alignItem.getBytes();
        results = addBytes(results, alignBytes);
        return results;
    }
    @Override
    public int onWriteBytes(OutputStream stream) throws IOException {
        int result = super.onWriteBytes(stream);
        if(result == 0){
            return 0;
        }
        result += alignItem.writeBytes(stream);
        return result;
    }
    @Override
    protected void onRefreshed() {
        calculateOffsets();
        refreshCount();
        refreshStart();
        refreshAlignment(getAlignItem());
    }
    private void calculateOffsets() {
        T[] childes = getChildes();
        int count = 0;
        if(childes != null){
            count = childes.length;
        }
        OffsetArray offsetArray = getOffsetArray();
        offsetArray.setSize(count);
        if(count == 0){
            return;
        }
        int sum = 0;
        int length = childes.length;
        for(int i = 0; i < length; i++){
            T item = childes[i];
            int offset;
            if(item == null || item.isNull()){
                offset = -1;
            }else {
                offset = sum;
                sum += item.countBytes();
            }
            offsetArray.setOffset(i, offset);
        }
    }
    public void refreshCountAndStart(){
        refreshCount();
        refreshStart();
    }
    public void refreshCount(){
        mItemCount.set(childesCount());
    }
    private void refreshStart(){
        int count = childesCount();
        if(count == 0){
            mItemStart.set(0);
            alignItem.clear();
            return;
        }
        Block parent = getParent();
        if(parent == null){
            return;
        }
        int start = parent.countUpTo(this);
        mItemStart.set(start);
    }
    void refreshAlignment(BlockReader reader, AlignItem alignItem) throws IOException{
        refreshAlignment(alignItem);
    }
    void refreshAlignment(AlignItem alignItem){
        if(childesCount() == 0){
            alignItem.clear();
            return;
        }
        alignItem.align(this);
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        T[] childes = getChildes();
        if(childes == null || childes.length == 0){
            return;
        }
        int noEntry = OffsetArray.NO_ENTRY;
        int[] offsetArray = mOffsetArray.getOffsets();
        int length = childes.length;
        int zeroPosition = getZeroPosition();
        reader.seek(zeroPosition);
        int maximumPosition = zeroPosition;
        for(int i = 0; i < length; i++){
            T item = childes[i];
            int offset = offsetArray[i];
            if(offset == noEntry){
                item.setNull(true);
                continue;
            }
            int itemStart = zeroPosition + offset;
            reader.seek(itemStart);
            item.readBytes(reader);
            int position = reader.getPosition();
            if(position > maximumPosition){
                maximumPosition = position;
            }
        }
        if(maximumPosition > 0){
            reader.seek(maximumPosition);
            refreshAlignment(reader, getAlignItem());
        }
    }
    private int getZeroPosition(){
        int start = mItemStart.get();
        if(start < 0){
            start = 0;
        }
        return start;
    }
    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender == mItemCount){
            int count = mItemCount.get();
            setChildesCount(count);
            getOffsetArray().setSize(count);
        }
    }

    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(": count = ");
        int realCount = childesCount();
        builder.append(realCount);
        int count = mItemCount.get();
        if(realCount != count){
            builder.append(", countValue=");
            builder.append(count);
        }
        builder.append(", start=");
        builder.append(mItemStart.get());
        return builder.toString();
    }
}
