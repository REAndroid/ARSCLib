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

import com.reandroid.arsc.item.IntegerVisitor;
import com.reandroid.arsc.item.VisitableInteger;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.index.ClassId;
import com.reandroid.dex.index.FieldId;
import com.reandroid.dex.pool.DexIdPool;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.value.DexValueBlock;
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

    @Override
    public void visitIntegers(IntegerVisitor visitor) {
        DexValueBlock<?> valueBlock = getStaticInitialValue();
        if(valueBlock instanceof VisitableInteger){
            ((VisitableInteger) valueBlock).visitIntegers(visitor);
        }
    }

    public DexValueBlock<?> getStaticInitialValue(){
        if(isStatic()){
            ClassId classId = getClassId();
            if(classId != null){
                return classId.getStaticValue(getIndex());
            }
        }
        return null;
    }

    @Override
    public ClassId getClassId() {
        ClassId classId = super.getClassId();
        if(classId != null){
            return classId;
        }
        FieldId fieldId = getFieldId();
        if(fieldId == null){
            return null;
        }
        DexIdPool<ClassId> pool = getPool(SectionType.CLASS_ID);
        if(pool == null){
            return null;
        }
        String className = fieldId.getClassName();
        classId = pool.get(className);
        if(classId == null) {
            return null;
        }
        ClassData classData = getParentInstance(ClassData.class);
        if(classData == null){
            return null;
        }
        classData.setClassId(classId);
        return classId;
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
            DexValueBlock<?> value = getClassId().getStaticValue(getIndex());
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
