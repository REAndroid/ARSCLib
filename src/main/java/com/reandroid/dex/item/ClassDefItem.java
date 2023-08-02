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

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.common.AccessFlag;

import java.io.IOException;

public class ClassDefItem extends BaseItem {
    private TypeList interfaceList;
    public ClassDefItem() {
        super(SIZE);
    }

    public TypeIndex getType(){
        return getTypeIndex(getClassIndex());
    }
    public AccessFlag[] getAccessFlags(){
        return AccessFlag.getAccessFlagsForClass(getAccessFlagsValue());
    }
    public TypeIndex getSuperClass(){
        return getTypeIndex(getSuperClassIndex());
    }
    public StringIndex getSourceFile(){
        return getStringIndex(getSourceFileIndex());
    }

    public int getClassIndex(){
        return getInteger(getBytesInternal(), OFFSET_CLASS);
    }
    public void setClassIndex(int index){
        putInteger(getBytesInternal(), OFFSET_CLASS, index);
    }
    public int getAccessFlagsValue(){
        return getInteger(getBytesInternal(), OFFSET_ACCESS_FLAGS);
    }
    public void setAccessFlagsValue(int value){
        putInteger(getBytesInternal(), OFFSET_CLASS, value);
    }
    public int getSuperClassIndex(){
        return getInteger(getBytesInternal(), OFFSET_SUPERCLASS);
    }
    public void setSuperClassIndex(int index){
        putInteger(getBytesInternal(), OFFSET_SUPERCLASS, index);
    }
    public int getInterfacesOffset(){
        return getInteger(getBytesInternal(), OFFSET_INTERFACES);
    }
    public void setInterfacesIndex(int index){
        putInteger(getBytesInternal(), OFFSET_INTERFACES, index);
    }
    public int getSourceFileIndex(){
        return getInteger(getBytesInternal(), OFFSET_SOURCE_FILE);
    }
    public void setSourceFileIndex(int index){
        putInteger(getBytesInternal(), OFFSET_SOURCE_FILE, index);
    }
    public int getAnnotationsIndex(){
        return getInteger(getBytesInternal(), OFFSET_ANNOTATIONS);
    }
    public void setAnnotationsIndex(int index){
        putInteger(getBytesInternal(), OFFSET_ANNOTATIONS, index);
    }
    public int getClassDataIndex(){
        return getInteger(getBytesInternal(), OFFSET_CLASS_DATA);
    }
    public void setClassDataIndex(int index){
        putInteger(getBytesInternal(), OFFSET_CLASS_DATA, index);
    }
    public int getStaticValuesIndex(){
        return getInteger(getBytesInternal(), OFFSET_STATIC_VALUES);
    }
    public void getStaticValuesIndex(int index){
        putInteger(getBytesInternal(), OFFSET_STATIC_VALUES, index);
    }


    public TypeIndex[] getInterfaces(){
        TypeList interfaceList = this.interfaceList;
        if(interfaceList != null){
            return interfaceList.toTypes(getTypeSection());
        }
        return null;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        interfaceList = TypeList.read(reader, getInterfacesOffset());
        //TODO: read annotation, data ...
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
        builder.append(AccessFlag.format(accessFlags));
        builder.append(getType());
        builder.append("\n.super ").append(getSuperClass());
        StringIndex sourceFile = getSourceFile();
        if(sourceFile != null){
            builder.append("\n.source \"").append(getSuperClass()).append("\"");
        }
        builder.append("\n");
        TypeIndex[] interfaces = getInterfaces();
        if(interfaces != null){
            builder.append("\n# interfaces");
            for(TypeIndex typeIndex : interfaces){
                builder.append("\n.implements ").append(typeIndex);
            }
        }
        return builder.toString();
    }

    private static final int OFFSET_CLASS = 0;
    private static final int OFFSET_ACCESS_FLAGS = 4;
    private static final int OFFSET_SUPERCLASS = 8;
    private static final int OFFSET_INTERFACES = 12;
    private static final int OFFSET_SOURCE_FILE = 16;
    private static final int OFFSET_ANNOTATIONS = 20;
    private static final int OFFSET_CLASS_DATA = 24;
    private static final int OFFSET_STATIC_VALUES = 28;

    private static final int SIZE = 32;
}
