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
import com.reandroid.dex.index.TypeId;
import com.reandroid.dex.item.AnnotationSet;
import com.reandroid.dex.item.FieldDef;
import com.reandroid.dex.item.StringData;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;
import java.util.Iterator;

public class DexField extends DexModel {

    private final DexClass dexClass;
    private final FieldDef fieldDef;

    public DexField(DexClass dexClass, FieldDef fieldDef){
        this.dexClass = dexClass;
        this.fieldDef = fieldDef;
    }

    public String getAccessFlags() {
        return AccessFlag.formatForField(getFieldDef().getAccessFlagsValue());
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
    public String getInitialValue() {
        DexValueBlock<?> dexValueBlock = getFieldDef().getStaticInitialValue();
        if(dexValueBlock != null){
            return dexValueBlock.getAsString();
        }
        return null;
    }

    public String getKey(){
        return getFieldId().getKey();
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(".field");
        String flags = getAccessFlags();
        if(flags.length() != 0){
            builder.append(' ');
            builder.append(flags);
        }
        builder.append(' ');
        builder.append(getName());
        builder.append(":");
        builder.append(getFieldType());
        String value = getInitialValue();
        if(value != null){
            builder.append(" = ");
            builder.append(value);
        }
        return builder.toString();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getFieldDef().append(writer);
    }
}
