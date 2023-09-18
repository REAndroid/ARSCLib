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
package com.reandroid.dex.item;

import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.index.FieldId;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.value.DexValue;
import com.reandroid.dex.value.DexValueType;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;

public class FieldDef extends Def<FieldId> implements Comparable<FieldDef>{
    public FieldDef() {
        super(0, SectionType.FIELD_ID);
    }
    public FieldId getFieldId(){
        return getItem();
    }
    @Override
    public Iterator<AnnotationSet> getAnnotations(){
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if(directory == null){
            return EmptyIterator.of();
        }
        return directory.getFieldsAnnotation(getIdIndex());
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.newLine();
        writer.append(".field ");
        AccessFlag[] accessFlags = AccessFlag.getForField(getAccessFlagsValue());
        for(AccessFlag af:accessFlags){
            writer.append(af.toString());
            writer.append(' ');
        }
        FieldId fieldId = getFieldId();
        writer.append(fieldId.getNameString().getString());
        writer.append(':');
        fieldId.getFieldType().append(writer);
        if(isStatic()){
            DexValue<?> value = getClassId().getStaticValue(getIndex());
            if(value != null && value.getValueType() != DexValueType.NULL){
                writer.append(" = ");
                value.append(writer);
            }
        }
        writer.indentPlus();
        boolean hasAnnotation = appendAnnotations(writer);
        writer.indentMinus();
        if(hasAnnotation){
            writer.newLine();
            writer.append(".end field");
        }
    }
    @Override
    public int compareTo(FieldDef fieldDef) {
        if(fieldDef == null){
            return -1;
        }
        return CompareUtil.compare(getFieldId(), fieldDef.getFieldId());
    }
    @Override
    public String toString() {
        FieldId fieldId = getFieldId();
        if(fieldId != null){
            return ".field " + AccessFlag.formatForField(getAccessFlagsValue())
                    + " " + fieldId.toString();
        }
        return ".field " + AccessFlag.formatForField(getAccessFlagsValue())
                + " " + getRelativeIdValue();
    }

}
