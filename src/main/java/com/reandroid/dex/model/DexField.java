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

import com.reandroid.dex.id.FieldId;
import com.reandroid.dex.data.FieldDef;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.value.DexValueType;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.ExpandIterator;
import com.reandroid.utils.collection.FilterIterator;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.Iterator;

public class DexField extends DexDeclaration {

    private final DexClass dexClass;
    private final FieldDef fieldDef;

    public DexField(DexClass dexClass, FieldDef fieldDef){
        this.dexClass = dexClass;
        this.fieldDef = fieldDef;
    }

    public FieldId getOrCreate(DexFile dexFile){
        Section<FieldId> section = dexFile.getSection(SectionType.FIELD_ID);
        return section.getOrCreate(getKey());
    }

    public String getName(){
        return getId().getName();
    }
    public void setName(String name){
        getId().setName(name);
    }

    public DexValue getInitialValue() {
        return DexValue.create(this, getDefinition().getStaticInitialValue());
    }
    public<T1 extends DexValueBlock<?>> T1 getOrCreateInitialValue(DexValueType<T1> dexValueType) {
        return getDefinition().getOrCreateStaticValue(dexValueType);
    }

    @Override
    public FieldKey getKey(){
        return getId().getKey();
    }
    @Override
    public FieldId getId() {
        return getDefinition().getId();
    }
    @Override
    public DexClass getDexClass() {
        return dexClass;
    }
    @Override
    public FieldDef getDefinition() {
        return fieldDef;
    }

    @Override
    public Iterator<DexAnnotation> getAnnotations(){
        return ComputeIterator.of(ExpandIterator.of(getDefinition().getAnnotations()),
                annotationItem -> DexAnnotation.create(DexField.this, annotationItem));
    }
    @Override
    public Iterator<DexAnnotation> getAnnotations(TypeKey typeKey){
        return FilterIterator.of(getAnnotations(),
                item -> typeKey.equals(item.getType()));
    }
    @Override
    public DexAnnotation getAnnotation(TypeKey typeKey){
        return CollectionUtil.getFirst(getAnnotations(typeKey));
    }
    @Override
    public DexAnnotation getOrCreateAnnotation(TypeKey typeKey){
        return DexAnnotation.create(this,
                getDefinition().getOrCreateAnnotationSet().getOrCreate(typeKey));
    }
    @Override
    public DexAnnotation newAnnotation(TypeKey typeKey){
        return DexAnnotation.create(this,
                getDefinition().getOrCreateAnnotationSet().addNewItem(typeKey));
    }
    @Override
    public void removeSelf(){
        getDefinition().removeSelf();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getDefinition().append(writer);
    }

    @Override
    public ElementType getElementType(){
        return ElementType.FIELD;
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
        return FieldId.equals(getId(), dexField.getId());
    }
}
