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
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.dex.base.DexException;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.common.SectionTool;
import com.reandroid.dex.data.DataItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.sections.SectionType;

import java.io.IOException;

public class IntegerDataReference<T extends DataItem> extends IntegerItem
        implements DataReference<T> {

    private final SectionType<T> sectionType;
    private final int usageType;
    private T item;

    public IntegerDataReference(SectionType<T> sectionType, int usageType) {
        super();
        this.sectionType = sectionType;
        this.usageType = usageType;
    }

    @Override
    public T getItem() {
        T item = this.item;
        if(item != null) {
            T replace = item.getReplace();
            if(replace != item) {
                setItem(replace);
                item = this.item;
            }
        }
        return item;
    }

    @Override
    public void setItem(T item) {
        int offset = 0;
        if(item != null) {
            offset = item.getOffset();
        }
        this.item = item;
        set(offset);
        updateItemUsage();
    }

    @Override
    public void setItem(Key key) {
        setItem(getSectionTool().getOrCreateSectionItem(getSectionType(), key));
    }

    @Override
    public Key getKey() {
        T item = getItem();
        if (item != null) {
            return item.getKey();
        }
        return null;
    }

    @Override
    public SectionType<T> getSectionType() {
        return sectionType;
    }

    @Override
    public void pullItem() {
        this.item = getSectionTool().getSectionItem(getSectionType(),
                get());
        updateItemUsage();
    }
    SectionTool getSectionTool() {
        return getParentInstance(SectionTool.class);
    }

    @Override
    public void refresh() {
        int value = 0;
        T item = getItem();
        if(item != null) {
            value = item.getIdx();
            if(value == 0){
                throw new DexException("Invalid reference: " + item);
            }
        }
        this.item = item;
        set(value);
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

    public void replaceKeys(Key search, Key replace){
        Key key = getKey();
        if(key == null){
            return;
        }
        Key key2 = key.replaceKey(search, replace);
        if(key != key2){
            setItem(key2);
        }
    }

    @Override
    public void editInternal(Block user) {
        T item = getUniqueItem(user);
        if(item != null){
            item.editInternal(user);
        }
    }

    public T getUniqueItem(Block user) {
        T item = getItem();
        if(item == null){
            return null;
        }
        if(item.isSharedItem(user)){
            item = createNewCopy();
        }
        item.addUniqueUser(user);
        return item;
    }

    private T createNewCopy() {
        T itemNew = getSectionTool().createSectionItem(getSectionType());
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
    public T getOrCreate() {
        T item = getItem();
        if(item != null) {
            return item;
        }
        item = getSectionTool().createSectionItem(getSectionType());
        setItem(item);
        return item;
    }

    @Override
    public void unlink() {
        this.item = null;
        set(0);
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        pullItem();
    }

    @Override
    public String toString() {
        T item = this.item;
        if(item != null) {
            return get() + ":" + item.toString();
        }
        return getSectionType().getName() + ": " + get();
    }
}
