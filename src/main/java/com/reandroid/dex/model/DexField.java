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
import com.reandroid.dex.id.FieldId;
import com.reandroid.dex.data.AnnotationSet;
import com.reandroid.dex.data.FieldDef;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.value.DexValueType;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.StringsUtil;

import java.io.IOException;
import java.util.Iterator;

public class DexField extends DexDef {

    private final DexClass dexClass;
    private final FieldDef fieldDef;

    public DexField(DexClass dexClass, FieldDef fieldDef){
        this.dexClass = dexClass;
        this.fieldDef = fieldDef;
    }

    public FieldId getOrCreate(DexFile dexFile){
        Section<FieldId> section = dexFile.get(SectionType.FIELD_ID);
        return section.getOrCreate(getFieldKey());
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
    public<T1 extends DexValueBlock<?>> T1 getOrCreateInitialValue(DexValueType<T1> dexValueType) {
        return getFieldDef().getOrCreateStaticValue(dexValueType);
    }

    @Override
    public FieldKey getKey(){
        return getFieldId().getKey();
    }
    public FieldKey getFieldKey(){
        return getFieldId().getKey();
    }
    @Override
    public TypeKey getDefining(){
        return getFieldKey().getDefiningKey();
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String flags = getAccessFlags();
        if(!StringsUtil.isEmpty(flags)){
            builder.append(flags);
            builder.append(' ');
        }
        builder.append(getKey());
        return builder.toString();
    }
}
