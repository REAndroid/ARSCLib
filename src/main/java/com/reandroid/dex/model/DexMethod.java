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
import com.reandroid.dex.common.Register;
import com.reandroid.dex.common.RegistersTable;
import com.reandroid.dex.data.*;
import com.reandroid.dex.id.MethodId;
import com.reandroid.dex.ins.Ins;
import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class DexMethod extends DexDeclaration {
    private final DexClass dexClass;
    private final MethodDef methodDef;

    public DexMethod(DexClass dexClass, MethodDef methodDef){
        this.dexClass = dexClass;
        this.methodDef = methodDef;
    }

    public DexMethod getDeclared(){
        DexClass dexClass = getDexClass().getSuperClass();
        if(dexClass != null){
            DexMethod dexMethod = dexClass.getMethod(getKey());
            if(dexMethod != null){
                return dexMethod.getDeclared();
            }
        }
        dexClass = getDexClass();
        Iterator<DexClass> iterator = dexClass.getInterfaceClasses();
        while (iterator.hasNext()){
            dexClass = iterator.next();
            DexMethod dexMethod = dexClass.getMethod(getKey());
            if(dexMethod != null){
                return dexMethod.getDeclared();
            }
        }
        return this;
    }

    public Iterator<DexMethod> getSuperMethods() {
        MethodKey key = getKey();
        return ComputeIterator.of(getDexClass().getSuperTypes(),
                dexClass -> dexClass.getDeclaredMethod(key));
    }
    public Iterator<DexMethod> getOverriding() {
        return CombiningIterator.two(getExtending(), getImplementations());
    }
    public DexMethod getBridged(){
        if(!isBridge()){
            return null;
        }
        MethodKey bridgedKey = null;
        Iterator<DexInstruction> iterator = getInstructions();
        while (iterator.hasNext()){
            DexInstruction instruction = iterator.next();
            Key key = instruction.getKey();
            if(!(key instanceof MethodKey)){
                continue;
            }
            MethodKey methodKey = (MethodKey) key;
            if(bridgedKey != null){
                return null;
            }
            bridgedKey = methodKey;
        }
        if(bridgedKey == null){
            return null;
        }
        if(!getDefining().equals(bridgedKey.getDeclaring())){
            return null;
        }
        if(!getName().equals(bridgedKey.getName())){
            return null;
        }
        DexClass dexClass = getDexClass();
        dexClass.addAccessFlag(AccessFlag.SYNTHETIC);
        return dexClass.getDeclaredMethod(bridgedKey);
    }
    public Iterator<DexMethod> getExtending() {
        return new MergingIterator<>(ComputeIterator.of(getDexClass().getExtending(),
                dexClass -> dexClass.getExtending(getKey())));
    }
    public Iterator<DexMethod> getImplementations() {
        return new MergingIterator<>(ComputeIterator.of(getDexClass().getImplementations(),
                dexClass -> dexClass.getImplementations(getKey())));
    }
    public Iterator<MethodKey> getOverridingKeys() {
        return new MergingIterator<>(ComputeIterator.of(getDexClass().getOverriding(),
                dexClass -> dexClass.getOverridingKeys(DexMethod.this.getKey())));
    }

    public String getName(){
        return getDefinition().getName();
    }
    public void setName(String name){
        getDefinition().setName(name);
    }

    @Override
    public Iterator<DexAnnotation> getAnnotations(){
        return ComputeIterator.of(ExpandIterator.of(getDefinition().getAnnotations()),
                annotationItem -> DexAnnotation.create(DexMethod.this, annotationItem));
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
    public Iterator<DexInstruction> getInstructions(Opcode<?> opcode) {
        return getInstructions(ins -> ins.getOpcode() == opcode);
    }
    public Iterator<DexInstruction> getInstructions(Predicate<? super Ins> filter) {
        Iterator<Ins> iterator = FilterIterator.of(getDefinition().getInstructions(), filter);
        return ComputeIterator.of(iterator, this::create);
    }
    public Iterator<DexInstruction> getInstructions() {
        return ComputeIterator.of(getDefinition().getInstructions(), this::create);
    }
    public DexInstruction getInstruction(int i){
        return create(getDefinition().getInstruction(i));
    }
    public DexInstruction addInstruction(Opcode<?> opcode){
        return create(getDefinition().getOrCreateInstructionList().createNext(opcode));
    }
    public DexInstruction createInstruction(int index, Opcode<?> opcode){
        return create(getDefinition().getOrCreateInstructionList().createAt(index, opcode));
    }
    public int getInstructionsCount(){
        return getDefinition().getInstructionsCount();
    }
    public RegistersTable getRegistersTable(){
        return getDefinition().getCodeItem();
    }
    public RegistersTable getOrCreateRegistersTable(){
        return getDefinition().getOrCreateCodeItem();
    }
    public List<Register> getLocalFreeRegisters(int instructionIndex){
        InstructionList instructionList = getInstructionList();
        if(instructionList != null){
            return instructionList.getLocalFreeRegisters(instructionIndex);
        }
        return EmptyList.of();
    }
    public void ensureLocalRegistersCount(int locals){
        if(locals == 0){
            return;
        }
        RegistersTable registersTable = getRegistersTable();
        if(registersTable != null){
            if(locals <= registersTable.getLocalRegistersCount()){
                return;
            }
        }
        registersTable = getOrCreateRegistersTable();
        int params = refreshParameterRegistersCount();
        int current = registersTable.getLocalRegistersCount();
        int diff = locals - current;
        InstructionList instructionList = getInstructionList();
        if(diff > 0){
            instructionList.addLocalRegisters(diff);
        }
        registersTable.setRegistersCount(locals + params);
    }
    public int refreshParameterRegistersCount(){
        RegistersTable registersTable = getRegistersTable();
        if(registersTable == null){
            return 0;
        }
        int parameterCount = getKey().getParameterRegistersCount();
        if(!isStatic()){
            parameterCount = parameterCount + 1;
        }
        int locals = registersTable.getLocalRegistersCount();
        registersTable.setParameterRegistersCount(parameterCount);
        registersTable.setRegistersCount(locals + parameterCount);
        return parameterCount;
    }
    private InstructionList getInstructionList(){
        return getDefinition().getInstructionList();
    }
    public int getLocalRegistersCount(){
        RegistersTable registersTable = getRegistersTable();
        if(registersTable != null){
            return registersTable.getLocalRegistersCount();
        }
        return 0;
    }
    public void setParameterRegistersCount(int count){
        CodeItem codeItem = getDefinition().getOrCreateCodeItem();
        if(codeItem != null){
            codeItem.setParameterRegistersCount(count);
        }
    }
    public void setLocalRegistersCount(int count){
        CodeItem codeItem = getDefinition().getOrCreateCodeItem();
        if(codeItem != null){
            codeItem.setRegistersCount(codeItem.getParameterRegistersCount() + count);
        }
    }
    private DexInstruction create(Ins ins){
        if(ins != null){
            return new DexInstruction(this, ins);
        }
        return null;
    }

    @Override
    public MethodKey getKey(){
        return getId().getKey();
    }
    @Override
    public MethodId getId() {
        return getDefinition().getId();
    }
    @Override
    public DexClass getDexClass() {
        return dexClass;
    }
    @Override
    public MethodDef getDefinition() {
        return methodDef;
    }
    public boolean isConstructor() {
        return AccessFlag.CONSTRUCTOR.isSet(getAccessFlagsValue());
    }
    public boolean isBridge() {
        return AccessFlag.BRIDGE.isSet(getAccessFlagsValue());
    }
    public boolean isDirect(){
        return isConstructor() || isPrivate() || isStatic();
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
        DexMethod dexMethod = (DexMethod) obj;
        return MethodId.equals(true, getId(), dexMethod.getId());
    }
}
