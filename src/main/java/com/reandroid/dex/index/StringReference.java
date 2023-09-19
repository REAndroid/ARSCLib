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
import com.reandroid.dex.item.StringData;
import com.reandroid.dex.pool.DexIdPool;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.CompareUtil;

import java.util.Objects;

public class StringReference extends IndirectItem<DexBlockItem> implements
        IntegerReference, BlockRefresh, Comparable<StringReference> {

    private StringId stringId;
    private final int stringUsage;

    public StringReference(DexBlockItem blockItem, int offset, int usage) {
        super(blockItem, offset);
        this.stringUsage = usage;
    }

    public StringId getStringId() {
        StringId stringId = this.stringId;
        if(stringId == null){
            stringId = getBlockItem().get(SectionType.STRING_ID, get());
            this.stringId = stringId;
            if(stringId != null){
                stringId.addStringUsage(stringUsage);
            }
        }
        return stringId;
    }
    public void setStringId(StringId stringId) {
        this.stringId = stringId;
        int value = 0;
        if(stringId != null){
            value = stringId.getIndex();
            stringId.addStringUsage(stringUsage);
        }
        set(value);
    }

    public StringData getItem(){
        StringId stringId = getStringId();
        if(stringId != null){
            return stringId.getStringData();
        }
        return null;
    }
    public void setItem(StringData stringData){
        StringId stringId = null;
        if(stringData != null){
            stringId = stringData.getStringId();
        }
        setStringId(stringId);
    }
    public String getString(){
        StringData stringData = getItem();
        if(stringData != null){
            return stringData.getString();
        }
        return null;
    }
    public void setString(String text){
        if(Objects.equals(text, getString())){
            return;
        }
        Section<StringData> section = getBlockItem().getSection(SectionType.STRING_DATA);
        if(section == null){
            return;
        }
        DexIdPool<StringData> pool = section.getPool();
        StringData stringData = pool.getOrCreate(text);
        setStringId(stringData.getStringId());
    }

    @Override
    public void refresh() {
        StringId stringId = getStringId();
        if(stringId != null){
            Block.putInteger(getBytesInternal(), getOffset(), stringId.getIndex());
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
    @Override
    public int compareTo(StringReference reference) {
        if(reference == null){
            return -1;
        }
        return CompareUtil.compare(getStringId(), reference.getStringId());
    }

    @Override
    public String toString() {
        return get() + "{" + stringId + "}";
    }
}
