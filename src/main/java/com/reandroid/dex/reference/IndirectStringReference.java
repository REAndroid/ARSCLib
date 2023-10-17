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

import com.reandroid.dex.base.DexBlockItem;
import com.reandroid.dex.data.StringData;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.pool.DexSectionPool;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.CompareUtil;

import java.util.Objects;

public class IndirectStringReference extends IdItemIndirectReference<StringId> implements
        Comparable<IndirectStringReference> {

    public IndirectStringReference(DexBlockItem blockItem, int offset, int usage) {
        super(SectionType.STRING_ID, blockItem, offset, usage);
    }

    public StringData getStringData(){
        StringId stringId = getItem();
        if(stringId != null){
            return stringId.getStringData();
        }
        return null;
    }
    public String getString(){
        StringId stringId = getItem();
        if(stringId != null){
            return stringId.getString();
        }
        return null;
    }
    public void setString(String text) {
        setItem(StringKey.create(text));
    }
    public void setString(String text, boolean overwrite) {
        StringKey key = new StringKey(text);
        StringId stringId = this.getItem();
        String oldText = null;
        if(stringId != null) {
            oldText = stringId.getString();
        }
        if(Objects.equals(text, oldText)){
            return;
        }
        DexSectionPool<StringId> pool = getBlockItem().getPool(SectionType.STRING_ID);
        if(pool == null){
            return;
        }
        if(!overwrite || stringId == null){
            stringId = pool.getOrCreate(key);
            if(stringId.getStringData()==null){
                stringId = pool.getOrCreate(key);
            }
        }
        setItem(stringId);
        if(overwrite){
            pool.update(StringKey.create(oldText));
        }
    }

    @Override
    public int compareTo(IndirectStringReference reference) {
        if(reference == null){
            return -1;
        }
        return CompareUtil.compare(getItem(), reference.getItem());
    }

    public static boolean equals(IndirectStringReference reference1, IndirectStringReference reference2) {
        if(reference1 == reference2){
            return true;
        }
        if(reference1 == null){
            return false;
        }
        return StringId.equals(reference1.getItem(), reference2.getItem());
    }
}
