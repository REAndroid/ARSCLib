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

import com.reandroid.dex.item.StringData;
import com.reandroid.dex.pool.DexIdPool;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;

public class FieldId extends IndexItemEntry implements Comparable<FieldId>{
    private final ItemIndexReference<TypeId> classType;
    private final ItemIndexReference<TypeId> fieldType;
    private final StringReference nameReference;

    public FieldId() {
        super(8);
        this.classType = new ItemShortReference<>(SectionType.TYPE_ID, this, 0);
        this.fieldType = new ItemShortReference<>(SectionType.TYPE_ID, this, 2);
        this.nameReference = new StringReference( this, 4);
    }

    public StringReference getNameReference() {
        return nameReference;
    }
    public String getName(){
        StringData stringData = getNameString();
        if(stringData != null){
            return stringData.getString();
        }
        return null;
    }
    public void setName(String name){
        DexIdPool<StringData> stringPool = getPool(SectionType.STRING_DATA);
        StringData stringData = stringPool.getOrCreate(name);
        setName(stringData);
    }
    public void setName(StringData stringData){
        this.nameReference.setItem(stringData);
    }
    @Override
    public String getKey(){
        StringBuilder builder = new StringBuilder();
        TypeId type = getClassType();
        if(type == null){
            return null;
        }
        StringData stringData = type.getNameData();
        if(stringData == null){
            return null;
        }
        builder.append(stringData.getString());
        builder.append("->");
        stringData = getNameString();
        if(stringData == null){
            return null;
        }
        builder.append(stringData.getString());
        return builder.toString();
    }
    public String key(){
        StringBuilder builder = new StringBuilder();
        StringData stringData = getNameString();
        if(stringData == null){
            return null;
        }
        builder.append(stringData.getString());
        builder.append(':');
        TypeId type = getFieldType();
        if(type == null){
            return null;
        }
        stringData = type.getNameData();
        if(stringData == null){
            return null;
        }
        builder.append(stringData.getString());
        return builder.toString();
    }
    public TypeId getClassType(){
        return classType.getItem();
    }
    public StringData getNameString(){
        return nameReference.getItem();
    }
    public TypeId getFieldType(){
        return fieldType.getItem();
    }

    @Override
    public void refresh() {
        classType.refresh();
        fieldType.refresh();
        nameReference.refresh();
    }
    @Override
    void cacheItems(){
        classType.getItem();
        fieldType.getItem();
        nameReference.getStringId();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getClassType().append(writer);
        writer.append("->");
        writer.append(getNameString().getString());
        writer.append(':');
        getFieldType().append(writer);
    }

    @Override
    public int compareTo(FieldId fieldId) {
        if(fieldId == null){
            return -1;
        }
        int i = CompareUtil.compare(getClassType(), fieldId.getClassType());
        if(i != 0){
            return i;
        }
        i = CompareUtil.compare(getNameReference(), fieldId.getNameReference());
        if(i != 0){
            return i;
        }
        return CompareUtil.compare(getFieldType(), fieldId.getFieldType());
    }

    @Override
    public String toString(){
        String key = getKey();
        if(key != null){
            return key;
        }
        return getClassType() + "->" + getNameString() + ":" + getFieldType();
    }
}
