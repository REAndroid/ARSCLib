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
package com.reandroid.dex.base;

import com.reandroid.arsc.base.*;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;

import java.io.IOException;

public class DexItemArray<T extends Block> extends CreatorArray<T>
        implements OffsetSupplier, DexArraySupplier<T> {

    private final IntegerPair countAndOffset;
    private PreloadArray<T> mPreloadArray;
    public DexItemArray(IntegerPair countAndOffset,
                        Creator<T> creator) {
        super(creator);
        this.countAndOffset = countAndOffset;
    }
    @Override
    public int countBytes() {
        return super.countBytes();
    }

    public void setPreloadArray(PreloadArray<T> preloadArray) {
        this.mPreloadArray = preloadArray;
    }
    @Override
    public IntegerReference getOffsetReference(){
        return getCountAndOffset().getSecond();
    }
    public IntegerPair getCountAndOffset() {
        return countAndOffset;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        IntegerPair countAndOffset = getCountAndOffset();
        if(skipReading(countAndOffset, reader)){
            return;
        }
        positionItem(this, reader);
        setChildesCount(countAndOffset.getFirst().get());
        readChildes(reader);
    }
    private boolean skipReading(IntegerPair countAndOffset, BlockReader reader){
        if(countAndOffset == null){
            return false;
        }
        IntegerReference reference = countAndOffset.getSecond();
        if(reference != null){
            int offset = reference.get();
            if(!isValidOffset(offset)){
                return true;
            }
            reader.seek(offset);
        }
        return false;
    }
    private void readChildes(BlockReader reader) throws IOException {
        T[] childes = getChildes();
        if(childes == null){
            return;
        }
        int length = childes.length;
        notifyPreload(childes);
        for(int i = 0; i < length; i++){
            Block block = childes[i];
            if(block == null){
                continue;
            }
            if(skipReading(block, reader)){
                continue;
            }
            positionItem(block, reader);
            block.readBytes(reader);
        }
    }
    private void notifyPreload(T[] childes){
        PreloadArray<T> preloadArray = this.mPreloadArray;
        if(preloadArray != null){
            preloadArray.onPreload(childes);
        }
    }
    private boolean skipReading(Block block, BlockReader reader){
        if(!(block instanceof OffsetSupplier)){
            return false;
        }
        OffsetSupplier offsetSupplier = (OffsetSupplier) block;
        IntegerReference reference = offsetSupplier.getOffsetReference();
        if(reference != null){
            int offset = reference.get();
            if(!isValidOffset(offset)){
                return true;
            }
            reader.seek(offset);
        }
        return false;
    }
    private void positionItem(Block block, BlockReader reader){
        if(!(block instanceof PositionedItem)){
            return;
        }
        PositionedItem positionedItem = (PositionedItem) block;
        positionedItem.setPosition(reader.getPosition());
    }
    protected boolean isValidOffset(int offset){
        return offset > 0;
    }
    @Override
    protected void onRefreshed() {
        IntegerReference count = getCountAndOffset().getFirst();
        count.set(getChildesCount());
    }
}
