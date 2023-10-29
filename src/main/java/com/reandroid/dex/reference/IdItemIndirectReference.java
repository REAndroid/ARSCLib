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
package com.reandroid.dex.reference;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.item.IndirectItem;
import com.reandroid.dex.base.DexBlockItem;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.pool.DexSectionPool;
import com.reandroid.dex.sections.SectionType;

public class IdItemIndirectReference<T extends IdItem> extends IndirectItem<DexBlockItem>
        implements IdReference<T> {

    private final SectionType<T> sectionType;
    private final int usageType;
    private T item;

    public IdItemIndirectReference(SectionType<T> sectionType, DexBlockItem blockItem, int offset, int usage) {
        super(blockItem, offset);
        this.sectionType = sectionType;
        this.usageType = usage;
        Block.putInteger(getBytesInternal(), getOffset(), 0xffffff);
    }
    public IdItemIndirectReference(SectionType<T> sectionType, DexBlockItem blockItem, int offset) {
        this(sectionType, blockItem, offset, IdItem.USAGE_NONE);
    }

    @Override
    public Key getKey(){
        T item = getItem();
        if(item != null){
            return item.getKey();
        }
        return null;
    }
    @Override
    public T getItem() {
        if(item == null){
            updateItem();
        }
        return item;
    }
    @Override
    public void setItem(T item) {
        if(item == this.item){
            return;
        }
        if(item == null){
            throw new NullPointerException("Can't set null for reference of: " + getSectionType().getName());
        }
        set(item.getIndex());
        this.item = item;
        updateItemUsage();
    }
    @Override
    public void updateItem(){
        this.item = getBlockItem().get(sectionType, get());
        updateItemUsage();
    }
    @Override
    public void setItem(Key key){
        setItem(getBlockItem().getOrCreate(getSectionType(), key));
    }
    @Override
    public SectionType<T> getSectionType() {
        return sectionType;
    }

    @Override
    public void set(int value) {
        Block.putInteger(getBytesInternal(), getOffset(), value);
    }
    @Override
    public int get() {
        return Block.getInteger(getBytesInternal(), getOffset());
    }

    @Override
    public void refresh() {
        T item = getItem();
        if(item != null){
            set(item.getIndex());
        }
        updateItemUsage();
    }
    private void updateItemUsage(){
        int usageType = this.usageType;
        if(usageType == UsageMarker.USAGE_NONE){
            return;
        }
        T item = this.item;
        if(item != null){
            item.addUsageType(usageType);
        }
    }
    public void unlink(){
        this.item = null;
        set(0);
    }

    @Override
    public String toString() {
        if(item != null){
            return get() + ": " + item.toString();
        }
        return sectionType.getName() + ": " + get();
    }
}
