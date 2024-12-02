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

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.id.FieldId;
import com.reandroid.dex.data.FieldDef;
import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.key.*;
import com.reandroid.dex.program.FieldProgram;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;
import java.util.Iterator;

public class DexField extends DexDeclaration implements FieldProgram {

    private final DexClass dexClass;
    private final FieldDef fieldDef;

    public DexField(DexClass dexClass, FieldDef fieldDef) {
        this.dexClass = dexClass;
        this.fieldDef = fieldDef;
    }

    public String getName(){
        return getId().getName();
    }
    public void setName(String name){
        getId().setName(name);
    }

    @Override
    public Key getStaticValue() {
        return getDefinition().getStaticValue();
    }
    public void setStaticValue(Key value) {
        getDefinition().setStaticValue(value);
    }

    public IntegerReference getStaticValueIntegerReference() {
        if (!(getStaticValue() instanceof PrimitiveKey.IntegerKey)) {
            return null;
        }
        final DexField dexField = this;
        return new IntegerReference() {
            @Override
            public int get() {
                Key key = dexField.getStaticValue();
                if (key instanceof PrimitiveKey.IntegerKey) {
                    return ((PrimitiveKey.IntegerKey) key).value();
                }
                return 0;
            }
            @Override
            public void set(int value) {
                dexField.setStaticValue(PrimitiveKey.of(value));
            }
            @Override
            public String toString() {
                return Integer.toString(get());
            }
        };
    }
    public IntegerReference getStaticIntegerValue() {
        if(isStatic()) {
            IntegerReference reference = resolveValueFromStaticConstructor();
            if(reference == null) {
                reference = getStaticValueIntegerReference();
            }
            return reference;
        }
        return null;
    }
    private IntegerReference resolveValueFromStaticConstructor() {
        DexClass dexClass = getDexClass();
        DexMethod dexMethod = dexClass.getStaticConstructor();
        if(dexMethod == null) {
            return null;
        }
        Iterator<DexInstruction> iterator = dexMethod.getInstructions();
        FieldKey fieldKey = getKey();
        while (iterator.hasNext()) {
            DexInstruction instruction = iterator.next();
            if(!fieldKey.equals(instruction.getFieldKey())) {
                continue;
            }
            if(!instruction.is(Opcode.SPUT)) {
                return null;
            }
            DexInstruction constInstruction = instruction.getPreviousSetter(instruction.getRegister());
            if(constInstruction == null) {
                return null;
            }
            return constInstruction.getAsIntegerReference();
        }
        return null;
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
    public void removeSelf(){
        getDefinition().removeSelf();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getDefinition().append(writer);
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
