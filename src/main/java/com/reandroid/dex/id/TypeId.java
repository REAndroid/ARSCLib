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
package com.reandroid.dex.id;

import com.reandroid.dex.key.KeyItemCreate;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.reference.IndirectStringReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;
import java.util.Objects;

public class TypeId extends IdItem implements Comparable<TypeId>, KeyItemCreate {

    private final IndirectStringReference nameReference;

    public TypeId() {
        super(4);
        this.nameReference = new IndirectStringReference(this, 0, StringId.USAGE_TYPE_NAME);
    }

    @Override
    public TypeKey getKey(){
        return checkKey(SectionType.TYPE_ID, TypeKey.create(getName()));
    }
    @Override
    public void setKey(Key key){
        TypeKey typeKey = (TypeKey) key;
        setKey(typeKey);
    }
    public void setKey(TypeKey key){
        TypeKey old = getKey();
        if(Objects.equals(key, old)){
            return;
        }
        setName(key.getType());
        keyChanged(SectionType.TYPE_ID, old);
    }
    public String getName(){
        StringId stringId = getNameId();
        if(stringId != null){
            return stringId.getString();
        }
        return null;
    }
    public void setName(String name){
        getNameReference().setString(name);
    }
    public StringId getNameId(){
        return getNameReference().getItem();
    }

    public IndirectStringReference getNameReference() {
        return nameReference;
    }

    @Override
    public void refresh() {
        nameReference.refresh();
    }
    @Override
    void cacheItems(){
        nameReference.updateItem();
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
        String name = getName();
        if(name != null){
            return name;
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
