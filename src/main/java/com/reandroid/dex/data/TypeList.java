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
package com.reandroid.dex.data;

import com.reandroid.dex.base.PositionAlignedItem;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.id.TypeId;
import com.reandroid.dex.key.*;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.ComputeIterator;

import java.io.IOException;
import java.util.Iterator;

public class TypeList extends ShortIdList<TypeId> implements KeyReference, PositionAlignedItem,
        SmaliFormat, Iterable<TypeId> {

    public TypeList() {
        super(SectionType.TYPE_ID, UsageMarker.USAGE_DEFINITION);
    }

    @Override
    public TypeListKey getKey() {
        TypeListKey lastKey = getLastKey();
        if (lastKey != null && equalsKey(lastKey)) {
            return lastKey;
        }
        TypeKey[] elements = new TypeKey[size()];
        getItemKeys(elements);
        return checkKey(TypeListKey.create(elements));
    }

    @Override
    public SectionType<TypeList> getSectionType() {
        return SectionType.TYPE_LIST;
    }

    public Iterator<TypeKey> getTypeKeys() {
        return ComputeIterator.of(iterator(), TypeId::getKey);
    }

    @Override
    public int size() {
        return super.size();
    }
    public TypeId get(TypeKey typeKey){
        if(typeKey != null){
            for(TypeId typeId : this){
                if(typeKey.equals(typeId.getKey())){
                    return typeId;
                }
            }
        }
        return null;
    }

    public TypeId getTypeIdForRegister(int register) {
        int size = size();
        int count = 0;
        for (int i = 0; i < size; i++) {
            TypeId typeId = getItem(i);
            if (count == register) {
                return typeId;
            }
            count++;
            if (typeId.isWide()) {
                count++;
            }
        }
        return null;
    }
    public int getParameterRegistersCount() {
        int size = size();
        int count = 0;
        for (int i = 0; i < size; i++) {
            TypeId typeId = getItem(i);
            count ++;
            if (typeId.isWide()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        for(TypeId typeId : this){
            typeId.append(writer);
        }
    }
    public void appendInterfaces(SmaliWriter writer) throws IOException {
        SmaliDirective smaliDirective = null;
        for(TypeId typeId : this){
            if(smaliDirective == null){
                writer.appendCommentNewLine("interfaces");
                smaliDirective = SmaliDirective.IMPLEMENTS;
            }
            writer.newLine();
            smaliDirective.append(writer);
            typeId.append(writer);
        }
        if(smaliDirective != null){
            writer.newLine();
        }
    }

    @Override
    public boolean isBlank() {
        return isRemoved() || isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(TypeId typeId : this){
            builder.append(typeId);
        }
        return builder.toString();
    }
}
