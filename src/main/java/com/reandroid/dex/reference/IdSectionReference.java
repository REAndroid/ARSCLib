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

import com.reandroid.dex.base.DexException;
import com.reandroid.dex.common.SectionTool;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;

public abstract class IdSectionReference<T extends IdItem> implements
        IdReference<T>, SmaliFormat {

    private final SectionTool sectionTool;
    private T item;
    private final int usage;

    public IdSectionReference(SectionTool sectionTool, int usage) {
        this.sectionTool = sectionTool;
        this.usage = usage;
    }

    protected SectionTool getSectionTool() {
        return sectionTool;
    }

    @Override
    public void refresh() {
        T item = validateReplace(this.item);
        this.item = item;
        set(item.getIdx());
        updateUsage();
    }

    @Override
    public abstract int get();
    @Override
    public abstract void set(int value);
    @Override
    public abstract SectionType<T> getSectionType();

    @Override
    public T getItem() {
        return item;
    }

    @Override
    public void setItem(T item) {
        item = validateReplace(item);
        this.item = item;
        set(item.getIdx());
        updateUsage();
    }

    @Override
    public void setItem(Key key) {
        T item = getSectionTool().getOrCreateSection(getSectionType())
                .getOrCreate(key);
        setItem(item);
    }

    @Override
    public Key getKey() {
        T item = this.getItem();
        if (item != null) {
            return item.getKey();
        }
        return null;
    }

    @Override
    public void pullItem() {
        setItem(getSectionTool().getSectionItem(getSectionType(), get()));
    }

    private void updateUsage(){
        T item = this.getItem();
        if (item != null) {
            item.addUsageType(this.usage);
        }
    }

    public void validate() {
        validateReplace(this.getItem());
    }
    protected T validateReplace(T idItem) {
        if(idItem == null) {
            throw new DexException("null id item: " + buildTrace(getItem()));
        }
        idItem = idItem.getReplace();
        if(idItem == null) {
            throw new DexException("Invalid id item: " + buildTrace(getItem()));
        }
        return idItem;
    }
    protected String buildTrace(T currentItem) {
        StringBuilder builder = new StringBuilder();
        String key = toDebugString(currentItem);
        if(key == null) {
            key = HexUtil.toHex(get(), 1);
        }
        builder.append(", key = '");
        builder.append(key);
        builder.append('\'');
        return builder.toString();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        T item = getItem();
        if (item == null) {
            writer.appendComment("error reference = " + HexUtil.toHex(get(), 1));
        } else {
            item.append(writer);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        IdSectionReference<?> reference = (IdSectionReference<?>) obj;
        IdItem item1 = this.getItem();
        IdItem item2 = reference.getItem();
        if (item1 != null && item2 != null) {
            return ObjectsUtil.equals(item1.getKey(), item2.getKey());
        }
        return getSectionTool() == reference.getSectionTool() &&
                ObjectsUtil.equals(get(), reference.get());
    }
    @Override
    public int hashCode() {
        Key key = getKey();
        if(key == null) {
            return 0;
        }
        return ObjectsUtil.hash(key);
    }
    @Override
    public String toString() {
        T item = this.getItem();
        if(item == null) {
            return getSectionType().getName() + ": " + get();
        }
        return item.getKey().toString();
    }
    static String toDebugString(IdItem item) {
        if(item == null){
            return null;
        }
        Key key = item.getKey();
        if(key == null){
            return null;
        }
        String keyString = key.toString();
        if(keyString == null){
            return null;
        }
        if(keyString.length() > 100){
            keyString = keyString.substring(0, 100) + "...";
        }
        if(keyString.startsWith("\"")){
            keyString = keyString.substring(1);
        }
        if(keyString.endsWith("\"")){
            keyString = keyString.substring(0, keyString.length() - 1);
        }
        return keyString;
    }
}
