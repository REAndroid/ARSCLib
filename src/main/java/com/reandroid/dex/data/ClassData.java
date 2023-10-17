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

import com.reandroid.arsc.item.IntegerVisitor;
import com.reandroid.arsc.item.VisitableInteger;
import com.reandroid.dex.base.*;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.ins.Ins;
import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.IterableIterator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ClassData extends DataItem
        implements SmaliFormat, VisitableInteger {

    private final Ule128Item staticFieldsCount;
    private final Ule128Item instanceFieldCount;
    private final Ule128Item directMethodCount;
    private final Ule128Item virtualMethodCount;

    private final FieldDefArray staticFields;
    private final FieldDefArray instanceFields;
    private final MethodDefArray directMethods;
    private final MethodDefArray virtualMethods;

    private ClassId mClassId;

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
        getStaticFieldsArray().visitIntegers(visitor);
        getDirectMethodsArray().visitIntegers(visitor);
        getVirtualMethodsArray().visitIntegers(visitor);
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
        FieldDef fieldDef = getStaticFieldsArray().get(key);
        if(fieldDef == null){
            fieldDef = getInstanceFieldsArray().get(key);
        }
        return fieldDef;
    }
    public MethodDef getMethod(MethodKey key){
        MethodDef methodDef = getDirectMethodsArray().get(key);
        if(methodDef == null){
            methodDef = getVirtualMethodsArray().get(key);
        }
        return methodDef;
    }
    public FieldDef getOrCreateStatic(FieldKey fieldKey){
        FieldDef fieldDef = getStaticFieldsArray().getOrCreate(fieldKey);
        fieldDef.addAccessFlag(AccessFlag.STATIC);
        fieldDef.addAccessFlag(AccessFlag.PUBLIC);
        fieldDef.addAccessFlag(AccessFlag.FINAL);
        return fieldDef;
    }
    public void ensureStaticConstructor(String type){
        MethodKey methodKey = new MethodKey(type, "<clinit>", null, "V");
        MethodDef methodDef = getDirectMethodsArray().get(methodKey);
        if(methodDef != null){
            return;
        }
        methodDef = getDirectMethodsArray().getOrCreate(methodKey);
        methodDef.addAccessFlag(AccessFlag.STATIC);
        methodDef.addAccessFlag(AccessFlag.CONSTRUCTOR);
        InstructionList instructionList = methodDef.getCodeItem().getInstructionList();
        instructionList.add(Opcode.RETURN_VOID.newInstance());
    }
    public Iterator<Ins> getInstructions(){
        return new IterableIterator<MethodDef, Ins>(getMethods()){
            @Override
            public Iterator<Ins> iterator(MethodDef element) {
                return element.getInstructions();
            }
        };
    }
    public Iterator<FieldDef> getFields(){
        return new CombiningIterator<>(getStaticFields(), getInstanceFields());
    }
    public Iterator<MethodDef> getMethods(){
        return new CombiningIterator<>(getDirectMethods(), getVirtualMethods());
    }
    public Iterator<MethodDef> getDirectMethods(){
        return getDirectMethodsArray().arrayIterator();
    }
    public Iterator<MethodDef> getVirtualMethods(){
        return getVirtualMethodsArray().arrayIterator();
    }
    public MethodDefArray getDirectMethodsArray() {
        return directMethods;
    }
    public MethodDefArray getVirtualMethodsArray() {
        return virtualMethods;
    }

    public Iterator<FieldDef> getStaticFields(){
        return getStaticFieldsArray().arrayIterator();
    }
    public Iterator<FieldDef> getInstanceFields(){
        return getInstanceFieldsArray().arrayIterator();
    }
    public FieldDefArray getStaticFieldsArray() {
        return staticFields;
    }
    public FieldDefArray getInstanceFieldsArray() {
        return instanceFields;
    }


    public void setClassId(ClassId classId) {
        if(mClassId == classId){
            return;
        }
        this.mClassId = classId;

        staticFields.setClassId(classId);
        instanceFields.setClassId(classId);
        directMethods.setClassId(classId);
        virtualMethods.setClassId(classId);
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
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
