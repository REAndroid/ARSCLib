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

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.dex.base.DexException;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.common.SectionTool;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.HexUtil;

import java.io.IOException;


public class ShortIdReference<T extends IdItem> extends ShortItem
        implements IdReference<T> {

    private final SectionType<T> sectionType;
    private final int usageType;
    private T item;

    public ShortIdReference(SectionType<T> sectionType, int usageType) {
        super();
        this.sectionType = sectionType;
        this.usageType = usageType;
        super.set(-1);
    }

    @Override
    public void set(int value) {
        if((value & 0xffff0000) != 0){
            throw new DexException("Short value out of range "
                    + HexUtil.toHex(value, 4) + " > 0xffff");
        }
        super.set(value);
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
    public T getItem() {
        return item;
    }
    @Override
    public void setItem(T item) {
        if(item == this.item){
            return;
        }
        int index = getItemIndex(item);
        set(index);
        this.item = item;
        updateItemUsage();
    }
    @Override
    public void pullItem(){
        this.item = getSectionTool().getSectionItem(getSectionType(), get());
        updateItemUsage();
    }
    @Override
    public void setKey(Key key) {
        setItem(getSectionTool().getOrCreateSectionItem(getSectionType(), key));
    }
    @Override
    public SectionType<T> getSectionType() {
        return sectionType;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        pullItem();
    }

    @Override
    public void refresh() {
        T item = getItem();
        if(item != null){
            item = item.getReplace();
        }
        checkNonNullItem(item);
        if(item != null){
            set(item.getIdx());
        }
        this.item = item;
        updateItemUsage();
    }

    @Override
    public void checkNonNullItem(T item) {
        if (item != null) {
            return;
        }
        throw new NullPointerException(buildMessage());
    }

    private String buildMessage(){
        return "Parent = " + getParent() + ", section = " + getSectionType().getName();
    }

    protected int getItemIndex(T item) {
        if (item == null) {
            throw new NullPointerException("Can't set null for reference of: " + getSectionType().getName());
        }
        return item.getIdx();
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

    public void replaceKeys(Key search, Key replace){
        Key key = getKey();
        if (key == null) {
            return;
        }
        Key key2 = key.replaceKey(search, replace);
        if(key != key2) {
            setKey(key2);
        }
    }

    @Override
    public String toString() {
        if(item != null) {
            return get() + ": " + item.toString();
        }
        return getSectionType().getName() + ": " + get();
    }

    private SectionTool getSectionTool() {
        return getParentInstance(SectionTool.class);
    }
}
