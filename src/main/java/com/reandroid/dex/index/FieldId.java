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
        this.nameReference = new StringReference( this, 4, StringData.USAGE_FIELD);
    }

    public String getName() {
        return nameReference.getString();
    }
    public void setName(String name){
        this.nameReference.setString(name);
    }
    public void setName(StringData stringData){
        this.nameReference.setItem(stringData);
    }
    public StringData getNameString(){
        return nameReference.getItem();
    }
    StringReference getNameReference() {
        return nameReference;
    }
    public String getClassName(){
        return classType.getKey();
    }
    public TypeId getClassType(){
        return classType.getItem();
    }
    public void setClassType(TypeId typeId){
        classType.setItem(typeId);
    }
    public void setClassType(String type) {
        classType.setItem(type);
    }

    public String getFieldTypeName(){
        return fieldType.getKey();
    }
    public TypeId getFieldType(){
        return fieldType.getItem();
    }
    public void setFieldType(TypeId typeId) {
        fieldType.setItem(typeId);
    }
    public void setFieldType(String type) {
        fieldType.setItem(type);
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
    public String getKey(){
        return key(false);
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
        String key = getKey();
        if(key != null){
            return key;
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
        if(!StringReference.equals(fieldId1.getNameReference(), fieldId2.getNameReference())){
            return false;
        }
        return ignoreClass || TypeId.equals(fieldId1.getClassType(), fieldId2.getClassType());
    }
}
