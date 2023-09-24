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
package com.reandroid.dex.model;

import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.index.FieldId;
import com.reandroid.dex.item.AnnotationSet;
import com.reandroid.dex.item.FieldDef;
import com.reandroid.dex.pool.DexIdPool;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class DexField extends DexDef {

    private final DexClass dexClass;
    private final FieldDef fieldDef;

    public DexField(DexClass dexClass, FieldDef fieldDef){
        this.dexClass = dexClass;
        this.fieldDef = fieldDef;
    }

    public FieldId getOrCreate(DexFile dexFile){
        Section<FieldId> section = dexFile.get(SectionType.FIELD_ID);
        DexIdPool<FieldId> pool = section.getPool();
        FieldId fieldId = pool.get(this.getFieldId().getKey());
        if(fieldId != null){
            return fieldId;
        }
        fieldId = section.createIdItem();
        fieldId.setName(getName());
        fieldId.setClassType(getClassName());
        fieldId.setFieldType(getFieldType());
        pool.add(fieldId);
        System.err.println("Created: " + fieldId);
        return fieldId;
    }
    public String getAccessFlags() {
        return AccessFlag.formatForField(getFieldDef().getAccessFlagsValue());
    }
    @Override
    int getAccessFlagsValue() {
        return getFieldDef().getAccessFlagsValue();
    }
    public String getName(){
        return getFieldId().getName();
    }
    public void setName(String name){
        getFieldId().setName(name);
    }
    public String getFieldType(){
        return getFieldId().getFieldTypeName();
    }
    public void setFieldType(String type){
        getFieldId().setFieldType(type);
    }

    public DexValue getInitialValue() {
        DexValueBlock<?> dexValueBlock = getFieldDef().getStaticInitialValue();
        if(dexValueBlock != null){
            return new DexValue(dexValueBlock);
        }
        return null;
    }

    @Override
    public String getKey(){
        return getFieldId().getName();
    }
    @Override
    public String getClassName() {
        return getFieldId().getClassName();
    }
    public FieldId getFieldId() {
        return getFieldDef().getFieldId();
    }
    public DexClass getDexClass() {
        return dexClass;
    }
    public FieldDef getFieldDef() {
        return fieldDef;
    }
    public Iterator<AnnotationSet> getAnnotations(){
        getFieldDef().setClassId(getDexClass().getClassId());
        return getFieldDef().getAnnotations();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DexField dexField = (DexField) obj;
        return FieldId.equals(getFieldId(), dexField.getFieldId());
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getFieldDef().append(writer);
    }
}
