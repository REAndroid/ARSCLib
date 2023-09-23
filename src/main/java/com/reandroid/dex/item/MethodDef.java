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

import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.index.MethodId;
import com.reandroid.dex.index.ProtoId;
import com.reandroid.dex.index.TypeId;
import com.reandroid.dex.ins.Ins;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class MethodDef extends Def<MethodId> implements Comparable<MethodDef>{
    private final OffsetUle128Item<CodeItem> codeOffset;

    public MethodDef() {
        super(1, SectionType.METHOD_ID);
        this.codeOffset = new OffsetUle128Item<>(SectionType.CODE);
        addChild(2, codeOffset);
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
    public MethodId getMethodId(){
        return getItem();
    }

    public Iterator<Ins> getInstructions() {
        InstructionList instructionList = getInstructionList();
        if(instructionList != null) {
            return instructionList.iterator();
        }
        return EmptyIterator.of();
    }

    public InstructionList getInstructionList(){
        CodeItem codeItem = getCodeItem();
        if(codeItem != null){
            return codeItem.getInstructionList();
        }
        return null;
    }
    public CodeItem getCodeItem(){
        CodeItem codeItem = codeOffset.getItem();
        if(codeItem != null){
            codeItem.setMethodDef(this);
        }
        return codeItem;
    }
    @Override
    public Iterator<AnnotationSet> getAnnotations(){
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if(directory == null){
            return EmptyIterator.of();
        }
        return directory.getMethodAnnotation(getIdIndex());
    }
    public Iterator<AnnotationSet> getParameterAnnotations(int parameterIndex){
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if(directory == null){
            return EmptyIterator.of();
        }
        return directory.getParameterAnnotation(getIdIndex(), parameterIndex);
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

}
