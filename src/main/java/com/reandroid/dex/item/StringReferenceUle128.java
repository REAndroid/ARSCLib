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
package com.reandroid.dex.item;

import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.index.StringId;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.pool.DexIdPool;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;
import java.util.Objects;

public class StringReferenceUle128 extends Ule128Item implements
        BlockRefresh, Comparable<StringReferenceUle128> {

    private final int stringUsage;
    private StringId stringId;

    public StringReferenceUle128(int stringUsage) {
        super();
        this.stringUsage = stringUsage;
    }

    public StringId getStringId() {
        StringId stringId = this.stringId;
        if(stringId == null){
            stringId = get(SectionType.STRING_ID, get());
            this.stringId = stringId;
            stringId.addUsageType(stringUsage);
        }
        return stringId;
    }
    public void setStringId(StringId stringId) {
        this.stringId = stringId;
        int value = 0;
        if(stringId != null){
            value = stringId.getIndex();
            stringId.addUsageType(stringUsage);
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
        Section<StringId> section = getSection(SectionType.STRING_ID);
        if(section == null){
            return;
        }
        DexIdPool<StringId> pool = section.getPool();
        StringId stringId = pool.getOrCreate(new StringKey(text));
        setStringId(stringId);
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        getItem();
    }

    @Override
    public void refresh() {
        StringId stringId = getStringId();
        int value = 0;
        if(stringId != null){
            value = stringId.getIndex();
            stringId.addUsageType(stringUsage);
        }
        set(value);
    }
    @Override
    public int compareTo(StringReferenceUle128 reference) {
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
