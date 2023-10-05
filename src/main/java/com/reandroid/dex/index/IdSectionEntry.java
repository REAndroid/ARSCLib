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
package com.reandroid.dex.index;

import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.DexBlockItem;
import com.reandroid.dex.base.DexItemArray;
import com.reandroid.dex.base.FixedSizeBlock;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.key.KeyItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.writer.SmaliFormat;

import java.io.IOException;

public abstract class IdSectionEntry extends DexBlockItem
        implements SmaliFormat, BlockRefresh, KeyItem, FixedSizeBlock, UsageMarker {

    private int mUsageType;

    IdSectionEntry(int bytesLength) {
        super(bytesLength);
    }

    @SuppressWarnings("unchecked")
    public void removeSelf(){
        DexItemArray<IdSectionEntry> itemArray = getParentInstance(DexItemArray.class);
        if(itemArray != null){
            itemArray.remove(this);
            setParent(null);
            setIndex(-1);
        }
    }
    @Override
    public Key getKey(){
        return null;
    }
    public void setKey(Key key){
    }
    abstract void cacheItems();
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        cacheItems();
    }

    @Override
    public int getUsageType() {
        return mUsageType;
    }
    @Override
    public void addUsageType(int usage){
        this.mUsageType |= usage;
    }
    @Override
    public boolean containsUsage(int usage){
        if(usage == 0){
            return this.mUsageType == 0;
        }
        return (this.mUsageType & usage) == usage;
    }
    @Override
    public void clearUsageType(){
        this.mUsageType = USAGE_NONE;
    }
}
