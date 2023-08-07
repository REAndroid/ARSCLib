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

import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.IndirectInteger;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.item.*;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;
import java.util.Iterator;

public class ClassIndex extends ItemIndex{

    private final IndirectInteger typeIndex;
    private final IndirectInteger accessFlagValue;
    private final IndirectInteger superClassIndex;
    private final IndirectInteger interfacesOffset;
    private final IndirectInteger sourceFileIndex;
    private final IndirectInteger annotationsOffset;
    private final IndirectInteger classDataOffset;
    private final IndirectInteger staticValuesOffset;
    
    private TypeList interfaceList;
    private AnnotationsDirectoryItem annotationsDirectory;
    private ClassDataItem classDataItem;
    public ClassIndex() {
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
        
        this.annotationsDirectory = new AnnotationsDirectoryItem();
        this.annotationsDirectory.setParent(this);
        classDataItem = new ClassDataItem(classDataOffset);
        classDataItem.setParent(this);
    }

    public TypeIndex getType(){
        return getTypeIndex(getTypeIndex().get());
    }
    public AccessFlag[] getAccessFlags(){
        return AccessFlag.getAccessFlagsForClass(getAccessFlagValue().get());
    }
    public TypeIndex getSuperClass(){
        return getTypeIndex(getSuperClassIndex().get());
    }
    public StringIndex getSourceFile(){
        return getStringIndex(getSourceFileIndex().get());
    }


    public TypeIndex[] getInterfaces(){
        TypeList interfaceList = this.interfaceList;
        if(interfaceList != null){
            return interfaceList.toTypes(getTypeSection());
        }
        return null;
    }
    public AnnotationsDirectoryItem getAnnotationsDirectory() {
        return annotationsDirectory;
    }
    public ClassDataItem getClassDataItem(){
        return classDataItem;
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
        int position = reader.getPosition();
        interfaceList = new TypeList(getInterfacesOffset());
        interfaceList.readBytes(reader);
        int offset = getAnnotationsOffset().get();
        if(offset > 0){
            reader.seek(offset);
            AnnotationsDirectoryItem directoryItem = this.annotationsDirectory;
            directoryItem.readBytes(reader);
        }

        ClassDataItem classDataItem = getClassDataItem();
        classDataItem.readBytes(reader);

        reader.seek(position);
        //TODO: read  static values, hidden api ...
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
        StringIndex sourceFile = getSourceFile();
        if(sourceFile != null){
            writer.append(".source ");
            sourceFile.append(writer);
        }
        writer.newLine();
        TypeIndex[] interfaces = getInterfaces();
        if(interfaces != null){
            writer.newLine();
            writer.append("# interfaces");
            for(TypeIndex typeIndex : interfaces){
                writer.newLine();
                writer.append(".implements ");
                typeIndex.append(writer);
            }
        }
        writer.newLine();
        annotationsDirectory.append(writer);
        getClassDataItem().append(writer);
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
        StringIndex sourceFile = getSourceFile();
        if(sourceFile != null){
            builder.append("\n.source \"").append(sourceFile.getString()).append("\"");
        }
        builder.append("\n");
        TypeIndex[] interfaces = getInterfaces();
        if(interfaces != null){
            builder.append("\n# interfaces");
            for(TypeIndex typeIndex : interfaces){
                builder.append("\n.implements ").append(typeIndex);
            }
        }
        BlockList<AnnotationItem> annotations = annotationsDirectory.getClassAnnotations();
        if(annotations.size() > 0){
            builder.append("\n\n# annotations");
            Iterator<AnnotationItem> iterator = annotations.iterator();
            while (iterator.hasNext()){
                builder.append("\n");
                builder.append(iterator.next());
                builder.append("\n");
            }
        }
        return builder.toString();
    }


    private static final int SIZE = 32;
}
