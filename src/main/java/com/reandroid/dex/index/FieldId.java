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

import com.reandroid.dex.pool.DexIdPool;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class FieldId extends ItemId {
    private final ItemIndexReference<TypeId> classType;
    private final ItemIndexReference<TypeId> fieldType;
    private final ItemIndexReference<StringData> name;

    public FieldId() {
        super(8);
        this.classType = new ItemShortReference<>(SectionType.TYPE_ID, this, 0);
        this.fieldType = new ItemShortReference<>(SectionType.TYPE_ID, this, 2);
        this.name = new ItemIndexReference<>(SectionType.STRING_DATA, this, 4);
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
    public void setName(StringData name){
        this.name.setItem(name);
    }
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
        return name.getItem();
    }
    public TypeId getFieldType(){
        return fieldType.getItem();
    }

    @Override
    public void refresh() {
        classType.refresh();
        fieldType.refresh();
        name.refresh();
    }
    @Override
    void cacheItems(){
        classType.getItem();
        fieldType.getItem();
        name.getItem();
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
    public String toString(){
        String key = getKey();
        if(key != null){
            return key;
        }
        return getClassType() + "->" + getNameString() + ":" + getFieldType();
    }
}
