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
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;

public class TypeId extends IndexItemEntry implements Comparable<TypeId>{

    private final StringReference nameReference;

    private TypeName typeName;
    public TypeId() {
        super(4);
        this.nameReference = new StringReference(this, 0, StringData.USAGE_TYPE);
    }

    @Override
    public String getKey(){
        return getName();
    }
    @Override
    public void setKey(String key){
        setName(key);
    }
    public String getName(){
        StringData stringData = getNameData();
        if(stringData != null){
            return stringData.getString();
        }
        return null;
    }
    public void setName(String name){
        getNameReference().setString(name);
        clearTypeName();
    }
    public StringData getNameData(){
        return getNameReference().getItem();
    }

    public StringReference getNameReference() {
        return nameReference;
    }

    public void setName(StringData name){
        nameReference.setItem(name);
    }

    public TypeName getTypeName(){
        TypeName typeName = this.typeName;
        if(typeName != null){
            return typeName;
        }
        synchronized (this){
            StringData stringData = getNameData();
            if(stringData == null){
                return null;
            }
            typeName = TypeName.createOrDefault(stringData);
            this.typeName = typeName;
            return typeName;
        }
    }
    private void clearTypeName(){
        if(this.typeName == null){
            return;
        }
        synchronized (this){
            this.typeName = null;
        }
    }

    @Override
    public void refresh() {
        nameReference.refresh();
    }
    @Override
    void cacheItems(){
        nameReference.getItem();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getName());
    }

    @Override
    public int compareTo(TypeId typeId) {
        if(typeId == null){
            return -1;
        }
        return CompareUtil.compare(this.getNameReference(), typeId.getNameReference());
    }

    @Override
    public String toString(){
        StringData stringData = getNameData();
        if(stringData != null){
            return stringData.getString();
        }
        return getIndex() + ":string-index=" + nameReference.get();
    }

    public static boolean equals(TypeId typeId1, TypeId typeId2) {
        if(typeId1 == typeId2){
            return true;
        }
        if(typeId1 == null){
            return false;
        }
        return CompareUtil.compare(typeId1.getName(), typeId2.getName()) == 0;
    }
}
