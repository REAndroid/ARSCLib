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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.item.IndirectItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.DexBlockItem;
import com.reandroid.dex.item.DataSectionEntry;
import com.reandroid.dex.sections.SectionType;

public class ItemOffsetReference<T extends DataSectionEntry> extends IndirectItem<DexBlockItem>
        implements IntegerReference, BlockRefresh {

    private final SectionType<T> sectionType;
    private T item;

    public ItemOffsetReference(SectionType<T> sectionType, DexBlockItem blockItem, int offset) {
        super(blockItem, offset);
        this.sectionType = sectionType;
    }

    public T getOrCreate() {
        T item = getItem();
        if(item != null) {
            return item;
        }
        item = getBlockItem().createItem(sectionType);
        setItem(item);
        return item;
    }
    public T getItem() {
        int i = get();
        if(item == null && i != 0){
            item = getBlockItem().get(sectionType, i);
        }
        return item;
    }
    public T getUniqueItem(ClassId classId) {
        T item = getItem();
        if(item == null){
            return null;
        }
        if(item.isSharedUsage()){
            item = createNewCopy();
            item.addUsage(classId);
        }
        return item;
    }
    public T getOrCreateUniqueItem(ClassId classId) {
        T item = getUniqueItem(classId);
        if(item != null) {
            return item;
        }
        item = getBlockItem().createItem(sectionType);
        setItem(item);
        addUsage(classId);
        return item;
    }
    public boolean isSharedUsage(){
        T item = getItem();
        if(item != null){
            return item.isSharedUsage();
        }
        return false;
    }
    public void addUsage(ClassId classId){
        T item = getItem();
        if(item != null){
            item.addUsage(classId);
        }
    }

    public void setItem(T item) {
        if(item == this.item){
            return;
        }
        int value = 0;
        if(item != null){
            IntegerReference reference = item.getOffsetReference();
            if(reference != null){
                value = reference.get();
            }
        }
        set(value);
        this.item = item;
    }

    @Override
    public void set(int value) {
        Block.putInteger(getBytesInternal(), getOffset(), value);
        item = null;
    }
    @Override
    public int get() {
        return Block.getInteger(getBytesInternal(), getOffset());
    }

    @Override
    public void refresh() {
        T item = getItem();
        int value = 0;
        if(item != null){
            IntegerReference reference = item.getOffsetReference();
            if(reference != null){
                value = reference.get();
            }
            if(value == 0){
                this.item = null;
            }
        }
        Block.putInteger(getBytesInternal(), getOffset(), value);
    }

    private T createNewCopy() {
        T itemNew = getBlockItem().createItem(sectionType);
        copyToIfPresent(itemNew);
        setItem(itemNew);
        return itemNew;
    }
    private void copyToIfPresent(T itemNew){
        T item = this.getItem();
        if(item != null){
            itemNew.copyFrom(item);
        }
    }
    @Override
    public String toString() {
        if(item != null){
            return get() + ":" +item.toString();
        }
        return sectionType.getName() + ": " + get();
    }
}
