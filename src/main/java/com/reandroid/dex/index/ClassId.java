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
package com.reandroid.dex.index;

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.IndirectInteger;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.item.*;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class ClassId extends ItemId {

    private final IndirectInteger typeIndex;
    private final IndirectInteger accessFlagValue;
    private final IndirectInteger superClassIndex;
    private final IndirectInteger interfacesOffset;
    private final IndirectInteger sourceFileIndex;
    private final IndirectInteger annotationsOffset;
    private final IndirectInteger classDataOffset;
    private final IndirectInteger staticValuesOffset;
    
    private TypeList interfaceList;
    private final ClassData classData;

    public ClassId() {
        super(SIZE);
        int offset = -4;
        
        this.typeIndex = new IndirectInteger(this, offset += 4);
        this.accessFlagValue = new IndirectInteger(this, offset += 4);
        this.superClassIndex = new IndirectInteger(this, offset += 4);
        this.interfacesOffset = new IndirectInteger(this, offset += 4);
        this.sourceFileIndex = new IndirectInteger(this, offset += 4);
        this.annotationsOffset = new IndirectInteger(this, offset += 4);
        this.classDataOffset = new IndirectInteger(this, offset += 4);
        this.staticValuesOffset = new IndirectInteger(this, offset += 4);

        classData = new ClassData();
        classData.setParent(this);
    }

    public TypeId getType(){
        return getTypeId(getTypeIndex().get());
    }
    public AccessFlag[] getAccessFlags(){
        return AccessFlag.getAccessFlagsForClass(getAccessFlagValue().get());
    }
    public TypeId getSuperClass(){
        return getTypeId(getSuperClassIndex().get());
    }
    public StringData getSourceFile(){
        return getStringData(getSourceFileIndex().get());
    }


    public TypeId[] getInterfaceTypeIds(){
        TypeList interfaceList = getInterfaceList();
        if(interfaceList != null){
            return interfaceList.toTypeIds();
        }
        return null;
    }
    public TypeList getInterfaceList(){
        return interfaceList;
    }
    public ClassData getClassData(){
        return classData;
    }

    public IndirectInteger getTypeIndex() {
        return typeIndex;
    }
    public IndirectInteger getAccessFlagValue() {
        return accessFlagValue;
    }
    public IndirectInteger getSuperClassIndex() {
        return superClassIndex;
    }
    public IndirectInteger getInterfacesOffset() {
        return interfacesOffset;
    }
    public IndirectInteger getSourceFileIndex() {
        return sourceFileIndex;
    }
    public IndirectInteger getAnnotationsOffset() {
        return annotationsOffset;
    }
    public IndirectInteger getClassDataOffset() {
        return classDataOffset;
    }
    public IndirectInteger getStaticValuesOffset() {
        return staticValuesOffset;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        interfaceList = getSectionList().getAt(SectionType.TYPE_LIST, getInterfacesOffset().get());

    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.newLine();
        writer.append(".class ");
        AccessFlag[] accessFlags = getAccessFlags();
        for(AccessFlag af:accessFlags){
            writer.append(af.toString());
            writer.append(' ');
        }
        getType().append(writer);
        writer.newLine();
        writer.append(".super ");
        getSuperClass().append(writer);
        writer.newLine();
        StringData sourceFile = getSourceFile();
        if(sourceFile != null){
            writer.append(".source ");
            sourceFile.append(writer);
        }
        writer.newLine();
        TypeId[] interfaces = getInterfaceTypeIds();
        if(interfaces != null){
            writer.newLine();
            writer.append("# interfaces");
            for(TypeId typeId : interfaces){
                writer.newLine();
                writer.append(".implements ");
                typeId.append(writer);
            }
        }
        writer.newLine();
        appendAnnotations(writer);
        getClassData().append(writer);
    }
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("\n.class ");
        AccessFlag[] accessFlags = getAccessFlags();
        for(AccessFlag af:accessFlags){
            builder.append(af);
            builder.append(" ");
        }
        builder.append(getType());
        builder.append("\n.super ").append(getSuperClass());
        StringData sourceFile = getSourceFile();
        if(sourceFile != null){
            builder.append("\n.source \"").append(sourceFile.getString()).append("\"");
        }
        builder.append("\n");
        TypeId[] interfaces = getInterfaceTypeIds();
        if(interfaces != null){
            builder.append("\n# interfaces");
            for(TypeId typeId : interfaces){
                builder.append("\n.implements ").append(typeId);
            }
        }
        return builder.toString();
    }


    private static final int SIZE = 32;
}
