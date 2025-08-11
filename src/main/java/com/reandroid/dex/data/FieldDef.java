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
package com.reandroid.dex.data;

import com.reandroid.dex.base.DexException;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.Modifier;
import com.reandroid.dex.id.FieldId;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.AnnotationSetKey;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.NullValueKey;
import com.reandroid.dex.key.PrimitiveKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.program.FieldProgram;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.model.Smali;
import com.reandroid.dex.smali.model.SmaliField;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;

public class FieldDef extends Def<FieldId> implements FieldProgram {

    private Key cachedStaticValue;

    public FieldDef() {
        super(0, SectionType.FIELD_ID);
    }

    @Override
    public FieldKey getKey() {
        FieldId id = getId();
        if (id != null) {
            return id.getKey();
        }
        return null;
    }

    @Override
    public Key getStaticValue() {
        StaticFieldDefArray fieldDefArray = getParentInstance(
                StaticFieldDefArray.class);
        if (fieldDefArray != null) {
            return fieldDefArray.getStaticValue(this);
        }
        return null;
    }
    public void setStaticValue(Key staticValue) {
        StaticFieldDefArray fieldDefArray = getParentInstance(
                StaticFieldDefArray.class);
        if (fieldDefArray == null) {
            throw new DexException("Not a member of StaticFieldDefArray: "
                    + Modifier.toString(getModifiers()) + getKey());
        }
        fieldDefArray.setStaticValue(this, staticValue);
        validateStaticValue();
    }

    Key cachedStaticValue() {
        return cachedStaticValue;
    }
    void cachedStaticValue(Key staticValue) {
        this.cachedStaticValue = staticValue;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        FieldKey key = getKey();
        if (key == null) {
            throw new IOException("Null FieldKey");
        }

        writer.newLine();

        getSmaliDirective().append(writer);
        writer.appendModifiers(getModifiers());
        key.appendDefinition(writer);

        appendStaticValue(writer);

        AnnotationSetKey annotations = getAnnotation();
        if (annotations.isEmpty()) {
            return;
        }
        writer.indentPlus();
        annotations.append(writer);
        writer.indentMinus();

        getSmaliDirective().appendEnd(writer);
    }
    private void appendStaticValue(SmaliWriter writer) throws IOException {
        Key value = getStaticValue();
        if (value == null) {
            return;
        }
        if (isNonDefaultValue(value) || !isInitializedInStaticConstructor()) {
            writer.append(" = ");
            value.append(writer);
        }
    }
    private boolean isNonDefaultValue(Key key) {
        if (key instanceof PrimitiveKey) {
            PrimitiveKey primitiveKey = (PrimitiveKey) key;
            return primitiveKey.getValueAsLong() != 0;
        }
        return !(key instanceof NullValueKey);
    }
    public boolean isInitializedInStaticConstructor() {
        StaticFieldDefArray fieldDefArray = getParentInstance(
                StaticFieldDefArray.class);
        if (fieldDefArray != null) {
            return fieldDefArray.isInitializedInStaticConstructor(this);
        }
        return false;
    }

    @Override
    public Iterator<IdItem> usedIds() {
        return SingleIterator.of(getId());
    }
    @Override
    public boolean uses(Key key) {
        return key.equals(getStaticValue());
    }
    @Override
    public Iterator<Key> usedKeys() {
        return CombiningIterator.singleOne(getKey(), SingleIterator.of(getStaticValue()));
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        validateStaticValue();
    }

    // TODO: Move this function to central dex file validator
    public void validateStaticValue() {
        Key staticValue = getStaticValue();
        if (staticValue == null) {
            return;
        }
        FieldKey fieldKey = getKey();
        if (!isStatic()) {
            throw new DexException("Instance field could not have initial value: "
                    + Modifier.toString(getAccessFlags()) + " " + fieldKey + " = "
                    + SmaliWriter.toStringSafe(staticValue));
        }
        TypeKey typeKey = fieldKey.getType();
        if (typeKey.isPrimitive() != (staticValue instanceof PrimitiveKey)) {
            throw new DexException("Mismatch in type object vs primitive for value: "
                    + SmaliWriter.toStringSafe(staticValue) + ", in field: " + fieldKey + "\n");
        }
        if (staticValue instanceof PrimitiveKey) {
            TypeKey valueType = ((PrimitiveKey) staticValue).valueType();
            if (!typeKey.equals(valueType)) {
                throw new DexException("Mismatch in type: " + typeKey
                        + " vs " + valueType
                        + ", for value: "
                        + SmaliWriter.toStringSafe(staticValue) + ", in field: " + fieldKey);
            }
        } else if (typeKey.isPrimitive()) {
            throw new DexException("Mismatch in type: " + typeKey
                    + " vs L " +  ", for value: "
                    + SmaliWriter.toStringSafe(staticValue) + ", in field: " + fieldKey);
        }
    }
    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.FIELD;
    }

    @Override
    public void merge(Def<?> def) {
        super.merge(def);
        FieldDef comingField = (FieldDef) def;
        Key staticValue = comingField.getStaticValue();
        if (staticValue != null) {
            setStaticValue(staticValue);
        }
    }

    @Override
    public void fromSmali(Smali smali) {
        SmaliField smaliField = (SmaliField) smali;
        setKey(smaliField.getKey());
        setAccessFlagsValue(smaliField.getAccessFlagsValue());
        addHiddenApiFlags(smaliField.getHiddenApiFlags());
        if (smaliField.hasAnnotation()) {
            setAnnotation(smaliField.getAnnotationSetKey());
        }
        Key value = smaliField.getStaticValue();
        if (value != null) {
            setStaticValue(value);
        }
    }

    @Override
    public SmaliField toSmali() {
        SmaliField smaliField = new SmaliField();
        smaliField.setKey(getKey());
        smaliField.setAccessFlags(AccessFlag.valuesOfField(getAccessFlagsValue()));
        smaliField.setStaticValue(getStaticValue());
        smaliField.setAnnotation(getAnnotation());
        return smaliField;
    }

    @Override
    public String toString() {
        return SmaliWriter.toStringSafe(this);
    }
}
