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
import com.reandroid.dex.id.MethodId;
import com.reandroid.dex.id.TypeId;
import com.reandroid.dex.ins.Ins;
import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.data.MethodDef;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.collection.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

public class DexMethod extends DexDef {
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
                dexClass -> dexClass.getDefinedMethod(key));
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
        if(!getClassName().equals(bridgedKey.getDefining())){
            return null;
        }
        if(!getName().equals(bridgedKey.getName())){
            return null;
        }
        DexClass dexClass = getDexClass();
        dexClass.addAccessFlag(AccessFlag.SYNTHETIC);
        return dexClass.getDefinedMethod(bridgedKey);
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
                new Function<DexClass, Iterator<MethodKey>>() {
                    @Override
                    public Iterator<MethodKey> apply(DexClass dexClass) {
                        return dexClass.getOverridingKeys(DexMethod.this.getKey());
                    }
                }));
    }
    @Override
    public String getAccessFlags() {
        return AccessFlag.formatForMethod(getAccessFlagsValue());
    }
    @Override
    int getAccessFlagsValue() {
        return getMethodDef().getAccessFlagsValue();
    }
    public String getName(){
        return getMethodDef().getName();
    }
    public void setName(String name){
        getMethodDef().setName(name);
    }
    public int getParametersCount() {
        return getMethodId().getParametersCount();
    }
    public String getParameter(int index) {
        TypeId typeId = getMethodId().getParameter(index);
        if(typeId != null){
            return typeId.getName();
        }
        return null;
    }
    public Iterator<String> getParameters() {
        return ComputeIterator.of(getMethodId().getParameters(), TypeId::getName);
    }
    public String getReturnType() {
        TypeId typeId = getMethodId().getReturnTypeId();
        if(typeId != null) {
            return typeId.getName();
        }
        return null;
    }
    public Iterator<DexInstruction> getInstructions(Opcode<?> opcode) {
        return getInstructions(ins -> ins.getOpcode() == opcode);
    }
    public Iterator<DexInstruction> getInstructions(Predicate<? super Ins> filter) {
        Iterator<Ins> iterator = FilterIterator.of(getMethodDef().getInstructions(), filter);
        return ComputeIterator.of(iterator, this::create);
    }
    public Iterator<DexInstruction> getInstructions() {
        return ComputeIterator.of(getMethodDef().getInstructions(), this::create);
    }
    public DexInstruction getInstruction(int i){
        return create(getMethodDef().getInstruction(i));
    }
    public int getInstructionsCount(){
        return getMethodDef().getInstructionsCount();
    }
    private DexInstruction create(Ins ins){
        if(ins != null){
            return new DexInstruction(this, ins);
        }
        return null;
    }

    @Override
    public MethodKey getKey(){
        return getMethodId().getKey();
    }
    @Override
    public TypeKey getDefining(){
        return getKey().getDefiningKey();
    }
    public MethodId getMethodId() {
        return getMethodDef().getMethodId();
    }
    public DexClass getDexClass() {
        return dexClass;
    }
    public MethodDef getMethodDef() {
        return methodDef;
    }
    public boolean isConstructor() {
        return AccessFlag.CONSTRUCTOR.isSet(getAccessFlagsValue());
    }
    public boolean isBridge() {
        return AccessFlag.BRIDGE.isSet(getAccessFlagsValue());
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getMethodDef().append(writer);
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
        return MethodId.equals(true, getMethodId(), dexMethod.getMethodId());
    }
}
