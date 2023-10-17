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
import com.reandroid.arsc.item.IntegerVisitor;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.debug.DebugParameter;
import com.reandroid.dex.id.*;
import com.reandroid.dex.ins.Ins;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.reference.DataItemUle128Reference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Objects;

public class MethodDef extends Def<MethodId> implements Comparable<MethodDef>{

    private final DataItemUle128Reference<CodeItem> codeOffset;

    public MethodDef() {
        super(1, SectionType.METHOD_ID);
        this.codeOffset = new DataItemUle128Reference<>(SectionType.CODE, UsageMarker.USAGE_DEFINITION);
        addChild(2, codeOffset);
    }

    public boolean isConstructor(){
        return AccessFlag.CONSTRUCTOR.isSet(getAccessFlagsValue());
    }
    public boolean isBridge(){
        return AccessFlag.BRIDGE.isSet(getAccessFlagsValue());
    }
    @Override
    public void visitIntegers(IntegerVisitor visitor) {
        CodeItem codeItem = getCodeItem();
        if(codeItem != null){
            codeItem.visitIntegers(visitor);
        }
    }
    public String getName() {
        MethodId methodId = getMethodId();
        if(methodId != null) {
            return methodId.getName();
        }
        return null;
    }
    public void setName(String name) {
        if(Objects.equals(getName(), name)){
            return;
        }
        getMethodId().setName(name);
    }
    @Override
    public String getClassName(){
        MethodId methodId = getMethodId();
        if(methodId != null){
            return methodId.getClassName();
        }
        return null;
    }
    public int getParametersCount(){
        ProtoId protoId = getProtoId();
        if(protoId != null){
            return protoId.getParametersCount();
        }
        return 0;
    }
    public Parameter getParameter(int index){
        if(index < 0 || index >= getParametersCount()){
            return null;
        }
        return new Parameter(this, index);
    }
    @Override
    public MethodKey getKey(){
        return (MethodKey) super.getKey();
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
        MethodId methodId = getMethodId();
        if(methodId != null){
            return methodId.getProto();
        }
        return null;
    }
    public MethodId getMethodId(){
        return getItem();
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
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        linkCodeItem();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.newLine();
        writer.append(".method ");
        AccessFlag[] accessFlags = AccessFlag.getForMethod(getAccessFlagsValue());
        for(AccessFlag af:accessFlags){
            writer.append(af.toString());
            writer.append(' ');
        }
        MethodId methodId = getMethodId();
        writer.append(methodId.getNameString().getString());
        writer.append('(');
        ProtoId protoId = methodId.getProto();
        protoId.append(writer);
        writer.append(')');
        methodId.getProto().getReturnTypeId().append(writer);
        writer.indentPlus();
        CodeItem codeItem = getCodeItem();
        if(codeItem != null){
            codeItem.append(writer);
        }else {
            appendAnnotations(writer);
        }
        writer.indentMinus();
        writer.newLine();
        writer.append(".end method");
    }
    void appendParameterAnnotations(SmaliWriter writer, ProtoId protoId) throws IOException {
        if(protoId == null || protoId.getParametersCount() == 0){
            return;
        }
        TypeList typeList = protoId.getTypeList();
        TypeId[] parameters = typeList.getTypeIds();
        if(parameters == null){
            return;
        }
        for(int i = 0; i < parameters.length; i++){
            appendParameterAnnotations(writer, parameters[i], i);
        }
    }
    private void appendParameterAnnotations(SmaliWriter writer, TypeId typeId, int index) throws IOException {
        if(typeId == null){
            return;
        }
        Iterator<AnnotationSet> iterator = getParameterAnnotations(index);
        boolean appendOnce = false;
        while (iterator.hasNext()){
            if(!appendOnce){
                int param = isStatic() ? 0 : 1;
                writer.newLine();
                writer.append(".param p");
                writer.append(index + param);
                writer.appendComment(typeId.getName());
                writer.indentPlus();
            }
            iterator.next().append(writer);
            appendOnce = true;
        }
        if(appendOnce){
            writer.indentMinus();
            writer.newLine();
            writer.append(".end param");
        }
    }

    @Override
    public int compareTo(MethodDef methodDef) {
        if(methodDef == null){
            return -1;
        }
        return CompareUtil.compare(getMethodId(), methodDef.getMethodId());
    }
    @Override
    public String toString() {
        MethodId methodId = getMethodId();
        if(methodId != null){
            return ".method " + AccessFlag.formatForMethod(getAccessFlagsValue())
                    + " " + methodId.toString();
        }
        return ".method " + AccessFlag.formatForMethod(getAccessFlagsValue())
                + " " + getRelativeIdValue();
    }

    public static class Parameter implements DefIndex, SmaliFormat {

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
            TypeId typeId = getTypeId();
            if(typeId == null){
                return;
            }
            Iterator<AnnotationSet> iterator = getAnnotations();
            boolean appendOnce = false;
            while (iterator.hasNext()){
                if(!appendOnce){
                    int param = this.methodDef.isStatic() ? 0 : 1;
                    writer.newLine();
                    writer.append(".param p");
                    writer.append(getDefinitionIndex() + param);
                    writer.appendComment(typeId.getName());
                    writer.indentPlus();
                }
                iterator.next().append(writer);
                appendOnce = true;
            }
            if(appendOnce){
                writer.indentMinus();
                writer.newLine();
                writer.append(".end param");
            }
        }
        private String getDebugString() throws IOException {
            StringWriter writer = new StringWriter();
            SmaliWriter smaliWriter = new SmaliWriter(writer);
            TypeId typeId = getTypeId();
            int param = this.methodDef.isStatic() ? 0 : 1;
            param += getDefinitionIndex();
            smaliWriter.newLine();
            smaliWriter.append("p");
            smaliWriter.append(param);
            smaliWriter.append(", ");
            String typeName = null;
            if(typeId != null){
                typeName = typeId.getName();
            }
            if(typeName != null){
                smaliWriter.append(typeName);
            }else {
                smaliWriter.append("null");
            }
            Iterator<AnnotationSet> iterator = getAnnotations();
            boolean appendOnce = false;
            while (iterator.hasNext()){
                if(!appendOnce){
                    smaliWriter.newLine();
                    smaliWriter.indentPlus();
                    appendOnce = true;
                }
                iterator.next().append(smaliWriter);
            }
            if(appendOnce){
                smaliWriter.indentMinus();
            }
            smaliWriter.close();
            return writer.toString();
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
        public String toString() {
            try {
                return getDebugString();
            } catch (IOException exception) {
                return exception.toString();
            }
        }
    }

}
