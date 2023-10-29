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
import com.reandroid.dex.data.DataItem;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.sections.SectionType;

public class DataItemIndirectReference<T extends DataItem> extends IndirectItem<DexBlockItem>
        implements DataReference<T> {

    private final SectionType<T> sectionType;
    private final int usageType;

    private T item;

    public DataItemIndirectReference(SectionType<T> sectionType, DexBlockItem blockItem, int offset, int usageType) {
        super(blockItem, offset);
        this.sectionType = sectionType;
        this.usageType = usageType;
    }
    public DataItemIndirectReference(SectionType<T> sectionType, DexBlockItem blockItem, int offset) {
        this(sectionType, blockItem, offset, UsageMarker.USAGE_NONE);
    }

    @Override
    public T getItem() {
        return item;
    }
    @Override
    public T getOrCreate() {
        T item = getItem();
        if(item != null) {
            return item;
        }
        item = getBlockItem().createItem(getSectionType());
        setItem(item);
        return item;
    }
    @Override
    public void setItem(T item) {
        if(item == this.item){
            return;
        }
        int value = 0;
        if(item != null){
            value = item.getOffset();
        }
        set(value);
        this.item = item;
        updateItemUsage();
    }
    @Override
    public void setItem(Key key){
        setItem(getBlockItem().getOrCreate(getSectionType(), key));
    }
    @Override
    public Key getKey() {
        T item = getItem();
        if(item != null){
            return item.getKey();
        }
        return null;
    }
    @Override
    public SectionType<T> getSectionType() {
        return sectionType;
    }
    @Override
    public void updateItem(){
        int i = get();
        T item;
        if(i == 0){
            item = null;
        }else {
            item = getBlockItem().get(getSectionType(), i);
        }
        this.item = item;
        updateItemUsage();
    }
    @Override
    public void refresh() {
        T item = getItem();
        int value = 0;
        if(item != null){
            item = item.getReplace();
        }
        if(item != null){
            value = item.getOffset();
            if(value == 0){
                item = null;
            }
        }
        this.item = item;
        set(value);
        updateItemUsage();
    }
    @Override
    public void unlink(){
        this.item = null;
        set(0);
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
    public T getUniqueItem(ClassId classId) {
        T item = getItem();
        if(item == null){
            return null;
        }
        if(item.isSharedUsage()){
            item = createNewCopy();
            item.addClassUsage(classId);
        }
        return item;
    }
    public T getOrCreateUniqueItem(ClassId classId) {
        T item = getUniqueItem(classId);
        if(item != null) {
            return item;
        }
        item = getBlockItem().createItem(getSectionType());
        setItem(item);
        addClassUsage(classId);
        return item;
    }
    public boolean isSharedUsage(){
        T item = getItem();
        if(item != null){
            return item.isSharedUsage();
        }
        return false;
    }
    public void addClassUsage(ClassId classId){
        T item = getItem();
        if(item != null){
            item.addClassUsage(classId);
        }
    }


    @Override
    public void set(int value) {
        Block.putInteger(getBytesInternal(), getOffset(), value);
    }
    @Override
    public int get() {
        return Block.getInteger(getBytesInternal(), getOffset());
    }

    private T createNewCopy() {
        T itemNew = getBlockItem().createItem(getSectionType());
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
        return getSectionType().getName() + ": " + get();
    }
}
