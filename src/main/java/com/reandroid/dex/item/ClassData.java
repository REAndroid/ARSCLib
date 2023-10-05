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
import com.reandroid.dex.base.*;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.index.ClassId;
import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;

public class ClassData extends DataSectionEntry
        implements SmaliFormat, VisitableInteger {

    private final Ule128Item staticFieldsCount;
    private final Ule128Item instanceFieldCount;
    private final Ule128Item directMethodCount;
    private final Ule128Item virtualMethodCount;

    private final FieldDefArray staticFields;
    private final FieldDefArray instanceFields;
    private final MethodDefArray directMethods;
    private final MethodDefArray virtualMethods;

    public ClassData() {
        super(8);
        this.staticFieldsCount = new Ule128Item();
        this.instanceFieldCount = new Ule128Item();
        this.directMethodCount = new Ule128Item();
        this.virtualMethodCount = new Ule128Item();


        this.staticFields = new StaticFieldDefArray(staticFieldsCount);
        this.instanceFields = new FieldDefArray(instanceFieldCount);
        this.directMethods = new MethodDefArray(directMethodCount);
        this.virtualMethods = new MethodDefArray(virtualMethodCount);


        addChild(0, staticFieldsCount);
        addChild(1, instanceFieldCount);
        addChild(2, directMethodCount);
        addChild(3, virtualMethodCount);

        addChild(4, staticFields);
        addChild(5, instanceFields);
        addChild(6, directMethods);
        addChild(7, virtualMethods);
    }

    @Override
    public void visitIntegers(IntegerVisitor visitor) {
        getStaticFields().visitIntegers(visitor);
        getDirectMethods().visitIntegers(visitor);
        getVirtualMethods().visitIntegers(visitor);
    }

    public void remove(Key key){
        Def<?> def = get(key);
        if(def != null){
            def.removeSelf();
        }
    }
    public void removeField(FieldKey key){
        Def<?> def = getField(key);
        if(def != null){
            def.removeSelf();
        }
    }
    public void removeMethod(MethodKey key){
        Def<?> def = getMethod(key);
        if(def != null){
            def.removeSelf();
        }
    }
    public Def<?> get(Key key){
        if(key instanceof FieldKey){
            return getField((FieldKey) key);
        }
        if(key instanceof MethodKey){
            return getMethod((MethodKey) key);
        }
        return null;
    }
    public FieldDef getField(FieldKey key){
        FieldDef fieldDef = getStaticFields().get(key);
        if(fieldDef == null){
            fieldDef = getInstanceFields().get(key);
        }
        return fieldDef;
    }
    public MethodDef getMethod(MethodKey key){
        MethodDef methodDef = getDirectMethods().get(key);
        if(methodDef == null){
            methodDef = getVirtualMethods().get(key);
        }
        return methodDef;
    }
    public FieldDefArray getStaticFields() {
        return staticFields;
    }
    public FieldDef getOrCreateStatic(FieldKey fieldKey){
        FieldDef fieldDef = getStaticFields().getOrCreate(fieldKey);
        fieldDef.addAccessFlag(AccessFlag.STATIC);
        fieldDef.addAccessFlag(AccessFlag.PUBLIC);
        fieldDef.addAccessFlag(AccessFlag.FINAL);
        return fieldDef;
    }
    public void ensureStaticConstructor(String type){
        MethodKey methodKey = new MethodKey(type, "<clinit>", null, "V");
        MethodDef methodDef = getDirectMethods().get(methodKey);
        if(methodDef != null){
            return;
        }
        methodDef = getDirectMethods().getOrCreate(methodKey);
        directMethodCount.set(getDirectMethods().getCount());
        virtualMethodCount.set(getVirtualMethods().getCount());
        methodDef.addAccessFlag(AccessFlag.STATIC);
        methodDef.addAccessFlag(AccessFlag.CONSTRUCTOR);
        InstructionList instructionList = methodDef.getCodeItem().getInstructionList();
        instructionList.add(Opcode.RETURN_VOID.newInstance());
    }
    public FieldDefArray getInstanceFields() {
        return instanceFields;
    }

    public MethodDefArray getDirectMethods() {
        return directMethods;
    }
    public MethodDefArray getVirtualMethods() {
        return virtualMethods;
    }

    public void setClassId(ClassId classId) {
        staticFields.setClassId(classId);
        instanceFields.setClassId(classId);
        directMethods.setClassId(classId);
        virtualMethods.setClassId(classId);
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        staticFieldsCount.set(staticFields.getCount());
        instanceFieldCount.set(instanceFields.getCount());
        directMethodCount.set(directMethods.getCount());
        virtualMethodCount.set(virtualMethods.getCount());
        staticFields.sort(CompareUtil.getComparableComparator());
        instanceFields.sort(CompareUtil.getComparableComparator());
        directMethods.sort(CompareUtil.getComparableComparator());
        virtualMethods.sort(CompareUtil.getComparableComparator());
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        staticFields.append(writer);
        instanceFields.append(writer);
        directMethods.append(writer);
        virtualMethods.append(writer);
    }
    @Override
    public String toString() {
        return "staticFieldsCount=" + staticFieldsCount +
                ", instanceFieldCount=" + instanceFieldCount +
                ", directMethodCount=" + directMethodCount +
                ", virtualMethodCount=" + virtualMethodCount;
    }

}
