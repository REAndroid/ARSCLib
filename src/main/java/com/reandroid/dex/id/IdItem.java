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
package com.reandroid.dex.id;

import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.*;
import com.reandroid.dex.key.KeyItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.sections.SectionList;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;

public abstract class IdItem extends DexBlockItem
        implements SmaliFormat, BlockRefresh, KeyItem, FixedSizeBlock, UsageMarker {

    private int mUsageType;
    private Key mLastKey;

    IdItem(int bytesLength) {
        super(bytesLength);
    }

    public abstract Iterator<IdItem> usedIds();
    public boolean isSameContext(IdItem idItem){
        return getSectionList() == idItem.getSectionList();
    }

    @SuppressWarnings("unchecked")
    public void removeSelf(){
        BlockListArray<IdItem> itemArray = getParentInstance(BlockListArray.class);
        if(itemArray != null){
            itemArray.remove(this);
            setParent(null);
            setIndex(-1);
        }
    }
    public abstract SectionType<? extends IdItem> getSectionType();
    @Override
    public Key getKey(){
        return null;
    }

    @SuppressWarnings("unchecked")
    <T1 extends Key> T1 checkKey(T1 newKey){
        Key lastKey = this.mLastKey;
        if(lastKey == null || !lastKey.equals(newKey)){
            this.mLastKey = newKey;
            keyChanged(lastKey);
            lastKey = newKey;
        }
        return (T1) lastKey;
    }

    public void setKey(Key key){
    }
    void keyChanged(Key oldKey){
        if(oldKey == null){
            return;
        }
        SectionList sectionList = getSectionList();
        if(sectionList != null){
            sectionList.keyChanged(getSectionType(), oldKey);
        }
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
