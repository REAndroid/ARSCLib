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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.common.ArraySupplier;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.Modifier;
import com.reandroid.dex.id.*;
import com.reandroid.dex.ins.Ins;
import com.reandroid.dex.ins.TryBlock;
import com.reandroid.dex.key.*;
import com.reandroid.dex.reference.DataItemUle128Reference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.Smali;
import com.reandroid.dex.smali.model.SmaliMethod;
import com.reandroid.dex.smali.model.SmaliMethodParameter;
import com.reandroid.utils.collection.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class MethodDef extends Def<MethodId>{

    private final DataItemUle128Reference<CodeItem> codeOffset;

    public MethodDef() {
        super(1, SectionType.METHOD_ID);
        this.codeOffset = new DataItemUle128Reference<>(SectionType.CODE, UsageMarker.USAGE_DEFINITION);
        addChild(2, codeOffset);
    }
    public boolean isBridge(){
        return AccessFlag.BRIDGE.isSet(getAccessFlagsValue());
    }

    @Override
    public boolean isDirect(){
        return isConstructor() || isPrivate() || isStatic();
    }
    public String getName() {
        MethodId methodId = getId();
        if(methodId != null) {
            return methodId.getName();
        }
        return null;
    }
    public void setName(String name) {
        if(Objects.equals(getName(), name)){
            return;
        }
        getId().setName(name);
    }
    public int getParametersCount(){
        MethodId methodId = getId();
        if(methodId != null){
            return methodId.getParametersCount();
        }
        return 0;
    }
    public int getParameterRegistersCount(){
        MethodId methodId = getId();
        if(methodId != null){
            int count = methodId.getParameterRegistersCount();
            if(!isStatic()){
                count = count + 1;
            }
            return count;
        }
        return 0;
    }
    public MethodParameter getParameter(int index){
        if(index < 0 || index >= getParametersCount()){
            return null;
        }
        return new MethodParameter(this, index);
    }
    public Iterator<MethodParameter> getParameters(){
        return getParameters(false);
    }
    public Iterator<MethodParameter> getParameters(boolean skipEmpty){
        if(getParametersCount() == 0){
            return EmptyIterator.of();
        }
        Iterator<MethodParameter> iterator = ArraySupplierIterator.of(new ArraySupplier<MethodParameter>() {
            @Override
            public MethodParameter get(int i) {
                return MethodDef.this.getParameter(i);
            }

            @Override
            public int getCount() {
                return MethodDef.this.getParametersCount();
            }
        });
        if(!skipEmpty) {
            return iterator;
        }
        return FilterIterator.of(iterator, parameter -> !parameter.isEmpty());
    }
    @Override
    public MethodKey getKey(){
        return (MethodKey) super.getKey();
    }

    @Override
    void onRemove() {
        CodeItem codeItem = codeOffset.getItem();
        if(codeItem != null){
            codeItem.setMethodDef(null);
            this.codeOffset.setItem((CodeItem) null);
        }
        super.onRemove();
    }

    public void removeParameter(int index){
        MethodParameter parameter = getParameter(index);
        if(parameter == null){
            return;
        }
        parameter.removeSelf();
        MethodKey methodKey = getKey();
        if(methodKey == null){
            return;
        }
        methodKey = methodKey.removeParameter(index);
        setItem(methodKey);
    }
    public void setDebugInfo(DebugInfo debugInfo){
        CodeItem codeItem = getCodeItem();
        if(codeItem != null){
            codeItem.setDebugInfo(debugInfo);
        }
    }
    public DebugInfo getDebugInfo(){
        CodeItem codeItem = getCodeItem();
        if(codeItem != null){
            return codeItem.getDebugInfo();
        }
        return null;
    }
    public DebugInfo getUniqueDebugInfo(){
        CodeItem codeItem = getCodeItem();
        if(codeItem != null){
            return codeItem.getDebugInfo();
        }
        return null;
    }
    public DebugInfo getOrCreateDebugInfo(){
        return getOrCreateCodeItem().getOrCreateDebugInfo();
    }
    public ProtoId getProtoId(){
        MethodId methodId = getId();
        if(methodId != null){
            return methodId.getProtoId();
        }
        return null;
    }
    public Iterator<Ins> getInstructions() {
        return new ArraySupplierIterator<>(new ArraySupplier<Ins>() {
            @Override
            public Ins get(int i) {
                return getInstruction(i);
            }
            @Override
            public int getCount() {
                return getInstructionsCount();
            }
        });
    }
    public Ins getInstruction(int i) {
        InstructionList instructionList = getInstructionList();
        if(instructionList != null) {
            return instructionList.get(i);
        }
        return null;
    }
    public Ins getInstructionAt(int address) {
        InstructionList instructionList = getInstructionList();
        if(instructionList != null) {
            return instructionList.getAtAddress(address);
        }
        return null;
    }
    public int getInstructionsCount() {
        InstructionList instructionList = getInstructionList();
        if(instructionList != null) {
            return instructionList.getCount();
        }
        return 0;
    }

    public InstructionList getOrCreateInstructionList(){
        return getOrCreateCodeItem().getInstructionList();
    }
    public InstructionList getInstructionList(){
        CodeItem codeItem = getCodeItem();
        if(codeItem != null){
            return codeItem.getInstructionList();
        }
        return null;
    }
    public TryBlock getTryBlock(){
        CodeItem codeItem = getCodeItem();
        if(codeItem != null){
            return codeItem.getTryBlock();
        }
        return null;
    }
    public TryBlock getOrCreateTryBlock(){
        return getOrCreateCodeItem().getOrCreateTryBlock();
    }
    public CodeItem getOrCreateCodeItem(){
        CodeItem current = codeOffset.getItem();
        CodeItem codeItem = codeOffset.getOrCreateUniqueItem(this);
        if(current == null){
            codeItem.setMethodDef(this);
            int registers = getParameterRegistersCount();
            codeItem.setRegistersCount(registers);
            codeItem.setParameterRegistersCount(registers);
        }
        return codeItem;
    }
    public CodeItem getCodeItem(){
        CodeItem codeItem = codeOffset.getItem();
        if(codeItem != null){
            codeItem.setMethodDef(this);
        }
        return codeItem;
    }
    public void clearCode(){
        codeOffset.setItem((CodeItem) null);
    }
    public void clearDebug(){
        CodeItem codeItem = getCodeItem();
        if(codeItem != null){
            codeItem.removeDebugInfo();
        }
    }
    private void linkCodeItem(){
        CodeItem codeItem = codeOffset.getItem();
        if(codeItem != null){
            codeItem.addUniqueUser(this);
            codeItem.setMethodDef(this);
        }
    }

    public Iterator<AnnotationGroup> getParameterAnnotations(){
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if(directory != null){
            return directory.getParameterAnnotation(this);
        }
        return EmptyIterator.of();
    }
    public Iterator<AnnotationSet> getParameterAnnotations(int parameterIndex){
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if(directory == null){
            return EmptyIterator.of();
        }
        return directory.getParameterAnnotation(getDefinitionIndex(), parameterIndex);
    }
    @Override
    public Iterator<? extends Modifier> getAccessFlags(){
        return AccessFlag.valuesOfMethod(getAccessFlagsValue());
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        linkCodeItem();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.onWriteMethod(getKey());
        writer.newLine();
        getSmaliDirective().append(writer);

        writer.appendModifiers(getModifiers());

        getId().append(writer, false);
        writer.indentPlus();
        if(!writer.appendOptional(getCodeItem())) {
            writer.appendAllWithDoubleNewLine(this.getParameters(true));
            writer.appendAllWithDoubleNewLine(this.getAnnotationSets(true));
        }
        writer.indentMinus();
        getSmaliDirective().appendEnd(writer);
    }
    @Override
    public void replaceKeys(Key search, Key replace){
        super.replaceKeys(search, replace);
        CodeItem codeItem = getCodeItem();
        if(codeItem != null){
            codeItem.replaceKeys(search, replace);
        }
    }

    @Override
    public void edit(){
        CodeItem shared = codeOffset.getItem();
        CodeItem unique = codeOffset.getUniqueItem(this);
        if(unique != null) {
            unique.setMethodDef(this);
            if(shared != unique) {
                unique.edit();
                shared.getInstructionList()
                        .onEditing(unique.getInstructionList());
            }
            unique.flattenTryItems();
        }
    }
    @Override
    public void editInternal(Block user) {
        this.edit();
    }

    @Override
    public Iterator<IdItem> usedIds(){
        Iterator<IdItem> iterator;
        CodeItem codeItem = getCodeItem();
        if(codeItem == null){
            iterator = EmptyIterator.of();
        }else {
            iterator = codeItem.usedIds();
        }
        return CombiningIterator.singleOne(getId(), iterator);
    }
    @Override
    public void merge(Def<?> def){
        super.merge(def);
        MethodDef comingMethod = (MethodDef) def;
        CodeItem comingCode = comingMethod.getCodeItem();
        if(comingCode != null){
            this.codeOffset.setKey(comingCode.getKey());
        }
    }
    @Override
    public void fromSmali(Smali smali) {
        SmaliMethod smaliMethod = (SmaliMethod) smali;
        setKey(smaliMethod.getKey());
        setAccessFlagsValue(smaliMethod.getAccessFlagsValue());
        addHiddenApiFlags(smaliMethod.getHiddenApiFlags());
        if(smaliMethod.hasInstructions()){
            getOrCreateCodeItem().fromSmali(smaliMethod);
        }
        if(smaliMethod.hasAnnotation()){
            addAnnotationSet(smaliMethod.getAnnotationSetKey());
        }
        Iterator<SmaliMethodParameter> iterator = smaliMethod.getParameters();
        while (iterator.hasNext()){
            SmaliMethodParameter smaliMethodParameter = iterator.next();
            int index = smaliMethodParameter.getDefinitionIndex();
            if(index < 0){
                MethodKey methodKey = smaliMethod.getKey();
                throw new RuntimeException("Parameter out of range, class = " +
                        methodKey.getDeclaring() + ", method = " + methodKey.getName() +
                        methodKey.getProto() + "\n" + smaliMethodParameter);
            }
            MethodParameter parameter = getParameter(index);
            parameter.fromSmali(smaliMethodParameter);
        }
        linkCodeItem();
    }

    @Override
    public SmaliMethod toSmali() {
        SmaliMethod smaliMethod = new SmaliMethod();
        smaliMethod.setKey(getKey());
        smaliMethod.setAccessFlags(AccessFlag.valuesOfField(getAccessFlagsValue()));
        smaliMethod.setAnnotation(getAnnotationKeys());
        CodeItem codeItem = getCodeItem();
        if (codeItem != null) {
            codeItem.toSmali(smaliMethod);
        }
        return smaliMethod;
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.METHOD;
    }

    @Override
    public String toString() {
        if(isReading()){
            return getSmaliDirective() + " " +
                    Modifier.toString(getModifiers()) +
                    getKey();
        }
        MethodId methodId = getId();
        if (methodId != null) {
            return getSmaliDirective() + " " +
                    Modifier.toString(getModifiers()) +
                    methodId.toString();
        }
        return getSmaliDirective() + " " +
                Modifier.toString(getAccessFlags()) +
                " " + getRelativeIdValue();
    }
}
