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

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.common.ArraySupplier;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.Modifier;
import com.reandroid.dex.debug.DebugParameter;
import com.reandroid.dex.id.*;
import com.reandroid.dex.ins.Ins;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.reference.DataItemUle128Reference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.ArraySupplierIterator;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.EmptyIterator;

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
    public Parameter getParameter(int index){
        if(index < 0 || index >= getParametersCount()){
            return null;
        }
        return new Parameter(this, index);
    }
    public Iterator<Parameter> getParameters(){
        if(getParametersCount() == 0){
            return EmptyIterator.of();
        }
        return ArraySupplierIterator.of(new ArraySupplier<Parameter>() {
            @Override
            public Parameter get(int i) {
                return MethodDef.this.getParameter(i);
            }
            @Override
            public int getCount() {
                return MethodDef.this.getParametersCount();
            }
        });
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
        Parameter parameter = getParameter(index);
        if(parameter == null){
            return;
        }
        parameter.remove();
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
    public DebugInfo getOrCreateDebugInfo(){
        return getOrCreateCodeItem().getOrCreateDebugInfo();
    }
    public ProtoId getProtoId(){
        MethodId methodId = getId();
        if(methodId != null){
            return methodId.getProto();
        }
        return null;
    }
    public Iterator<Ins> getInstructions() {
        InstructionList instructionList = getInstructionList();
        if(instructionList != null) {
            return instructionList.clonedIterator();
        }
        return EmptyIterator.of();
    }
    public Ins getInstruction(int i) {
        InstructionList instructionList = getInstructionList();
        if(instructionList != null) {
            return instructionList.get(i);
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
    public CodeItem getOrCreateCodeItem(){
        CodeItem codeItem = codeOffset.getItem();
        if(codeItem == null){
            codeItem = codeOffset.getOrCreate();
            codeItem.setMethodDef(this);
            int parametersCount = getParametersCount();
            int registers = parametersCount;
            if(!isStatic()){
                registers = registers + 1;
            }
            codeItem.setRegistersCount(registers);
            codeItem.setParameterRegistersCount(parametersCount);
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
    private void linkCodeItem(){
        CodeItem codeItem = codeOffset.getItem();
        if(codeItem != null){
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
        if(!writer.appendOptional(getCodeItem())){
            writer.appendAll(getParameters());
            writer.appendAll(getAnnotations());
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
            this.codeOffset.setItem(comingCode.getKey());
        }
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.METHOD;
    }

    @Override
    public String toString() {
        if(isReading()){
            return getSmaliDirective() + " " + getKey();
        }
        MethodId methodId = getId();
        if(methodId != null){
            return getSmaliDirective() + " " + Modifier.toString(getAccessFlags())
                    + " " + methodId.toString();
        }
        return getSmaliDirective() + " " + Modifier.toString(getAccessFlags())
                + " " + getRelativeIdValue();
    }
    public static class Parameter implements DefIndex, SmaliRegion {

        private final MethodDef methodDef;
        private final int index;

        public Parameter(MethodDef methodDef, int index){
            this.methodDef = methodDef;
            this.index = index;
        }

        public void remove(){
            clearAnnotations();
            clearDebugParameter();
        }

        public void clearAnnotations(){
            AnnotationsDirectory directory = this.methodDef.getUniqueAnnotationsDirectory();
            if(directory == null || !hasAnnotations()){
                return;
            }
            Iterator<DirectoryEntry<MethodDef, AnnotationGroup>> iterator =
                    directory.getParameterEntries(this.methodDef);
            int index = getDefinitionIndex();
            while (iterator.hasNext()){
                DirectoryEntry<MethodDef, AnnotationGroup> entry = iterator.next();
                AnnotationGroup group = entry.getValue();
                if(group == null || group.getItem(index) == null){
                    continue;
                }
                AnnotationGroup update = group.getSection(SectionType.ANNOTATION_GROUP)
                        .createItem();
                entry.setValue(update);
                update.put(index, 0);
                update.refresh();
            }
        }
        public boolean hasAnnotations(){
            return getAnnotations().hasNext();
        }
        public Iterator<AnnotationSet> getAnnotations(){
            AnnotationsDirectory directory = this.methodDef.getAnnotationsDirectory();
            if(directory != null){
                return directory.getParameterAnnotation(this.methodDef, getDefinitionIndex());
            }
            return EmptyIterator.of();
        }
        public TypeId getTypeId() {
            ProtoId protoId = this.methodDef.getProtoId();
            if(protoId != null){
                return protoId.getParameter(getDefinitionIndex());
            }
            return null;
        }
        @Override
        public int getDefinitionIndex() {
            return index;
        }
        public int getRegister() {
            MethodDef methodDef = this.methodDef;
            int reg;
            if(methodDef.isStatic()){
                reg = 0;
            }else {
                reg = 1;
            }
            reg += methodDef.getKey().getRegister(getDefinitionIndex());
            return reg;
        }
        public void clearDebugParameter(){
            DebugInfo debugInfo = methodDef.getDebugInfo();
            if(debugInfo != null){
                debugInfo.removeDebugParameter(getDefinitionIndex());
            }
        }
        public String getDebugName(){
            DebugParameter debugParameter = getDebugParameter();
            if(debugParameter != null){
                return debugParameter.getName();
            }
            return null;
        }
        public void setDebugName(String name){
            DebugInfo debugInfo = methodDef.getDebugInfo();
            if(debugInfo == null){
                return;
            }
            if(name == null){
                debugInfo.removeDebugParameter(getDefinitionIndex());
                return;
            }
            DebugParameter parameter = debugInfo.getOrCreateDebugParameter(
                    getDefinitionIndex());
            parameter.setName(name);
        }
        public DebugParameter getDebugParameter(){
            DebugInfo debugInfo = methodDef.getDebugInfo();
            if(debugInfo != null){
                return debugInfo.getDebugParameter(getDefinitionIndex());
            }
            return null;
        }
        @Override
        public Key getKey() {
            TypeId typeId = getTypeId();
            if(typeId != null){
                return typeId.getKey();
            }
            return null;
        }
        @Override
        public void append(SmaliWriter writer) throws IOException {
            DebugParameter debugParameter = getDebugParameter();
            boolean has_debug = debugParameter != null &&
                    debugParameter.getNameId() != null;
            Iterator<AnnotationSet> annotations = getAnnotations();
            boolean has_annotation = annotations.hasNext();
            if(!has_debug && !has_annotation){
                return;
            }
            getSmaliDirective().append(writer);
            writer.append('p');
            writer.append(getRegister());
            if(has_debug){
                debugParameter.append(writer);
            }
            writer.appendComment(getTypeId().getName());
            if(!has_annotation){
                return;
            }
            writer.indentPlus();
            writer.appendAll(annotations);
            writer.indentMinus();
            getSmaliDirective().appendEnd(writer);
        }
        @Override
        public int hashCode() {
            return methodDef.hashCode() * 31 + index;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Parameter parameter = (Parameter) obj;
            return index == parameter.index && this.methodDef == parameter.methodDef;
        }

        @Override
        public SmaliDirective getSmaliDirective() {
            return SmaliDirective.PARAM;
        }
        @Override
        public String toString() {
            return SmaliWriter.toStringSafe(this);
        }
    }
}
