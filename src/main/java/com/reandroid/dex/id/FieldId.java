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

import com.reandroid.dex.data.StringData;
import com.reandroid.dex.key.*;
import com.reandroid.dex.reference.IdItemIndirectReference;
import com.reandroid.dex.reference.IdItemIndirectShortReference;
import com.reandroid.dex.reference.IndirectStringReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class FieldId extends IdItem implements KeyItemCreate, Comparable<FieldId>{
    private final IdItemIndirectReference<TypeId> classType;
    private final IdItemIndirectReference<TypeId> fieldType;
    private final IndirectStringReference nameReference;

    public FieldId() {
        super(8);
        this.classType = new IdItemIndirectShortReference<>(SectionType.TYPE_ID, this, 0, USAGE_FIELD_CLASS);
        this.fieldType = new IdItemIndirectShortReference<>(SectionType.TYPE_ID, this, 2, USAGE_FIELD_TYPE);
        this.nameReference = new IndirectStringReference( this, 4, USAGE_FIELD_NAME);
    }

    @Override
    public Iterator<IdItem> usedIds(){
        return CombiningIterator.three(
                SingleIterator.of(classType.getItem()),
                SingleIterator.of(fieldType.getItem()),
                SingleIterator.of(nameReference.getItem())
        );
    }
    public String getName() {
        return nameReference.getString();
    }
    public void setName(String name){
        this.nameReference.setString(name);
    }
    public StringData getNameString(){
        return nameReference.getStringData();
    }
    IndirectStringReference getNameReference() {
        return nameReference;
    }
    public String getClassName(){
        TypeId typeId = getClassType();
        if(typeId != null){
            return typeId.getName();
        }
        return null;
    }
    public TypeId getClassType(){
        return classType.getItem();
    }
    public void setClassType(TypeId typeId){
        classType.setItem(typeId);
    }
    public void setClassType(String type) {
        classType.setItem(new TypeKey(type));
    }

    public String getFieldTypeName(){
        TypeId typeId = getFieldType();
        if(typeId != null){
            return typeId.getName();
        }
        return null;
    }
    public TypeId getFieldType(){
        return fieldType.getItem();
    }
    public void setFieldType(TypeId typeId) {
        fieldType.setItem(typeId);
    }
    public void setFieldType(String type) {
        fieldType.setItem(new TypeKey(type));
    }

    @Override
    public void refresh() {
        classType.refresh();
        fieldType.refresh();
        nameReference.refresh();
    }
    @Override
    void cacheItems(){
        classType.updateItem();
        fieldType.updateItem();
        nameReference.updateItem();
    }

    @Override
    public SectionType<FieldId> getSectionType(){
        return SectionType.FIELD_ID;
    }
    @Override
    public FieldKey getKey(){
        return checkKey(FieldKey.create(this));
    }

    @Override
    public void setKey(Key key){
        setKey((FieldKey) key);
    }
    public void setKey(FieldKey key){
        FieldKey old = getKey();
        if(Objects.equals(key, old)){
            return;
        }
        classType.setItem(key.getDefiningKey());
        nameReference.setString(key.getName());
        fieldType.setItem(key.getTypeKey());
        keyChanged(old);
    }
    public String key(boolean appendFieldType) {
        StringBuilder builder = new StringBuilder();
        String type = getClassName();
        if(type == null){
            return null;
        }
        builder.append(type);
        builder.append("->");
        String name = getName();
        if(name == null){
            return null;
        }
        builder.append(name);
        if(appendFieldType) {
            builder.append(':');
            String fieldType = getFieldTypeName();
            if(fieldType == null) {
                return null;
            }
            builder.append(fieldType);
        }
        return builder.toString();
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
        FieldKey key = getKey();
        if(key != null){
            return key.toString();
        }
        return getClassType() + "->" + getNameString() + ":" + getFieldType();
    }
    public static boolean equals(FieldId fieldId1, FieldId fieldId2) {
        return equals(false, fieldId1, fieldId2);
    }
    public static boolean equals(boolean ignoreClass, FieldId fieldId1, FieldId fieldId2) {
        if(fieldId1 == fieldId2) {
            return true;
        }
        if(fieldId1 == null) {
            return false;
        }
        if(!IndirectStringReference.equals(fieldId1.getNameReference(), fieldId2.getNameReference())){
            return false;
        }
        return ignoreClass || TypeId.equals(fieldId1.getClassType(), fieldId2.getClassType());
    }
}
