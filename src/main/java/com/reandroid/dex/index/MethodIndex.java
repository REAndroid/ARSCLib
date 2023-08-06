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

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.DexFile;
import com.reandroid.dex.base.DexItem;
import com.reandroid.dex.sections.DexSection;
import com.reandroid.dex.sections.DexStringPool;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class MethodIndex extends ItemIndex {
    public MethodIndex() {
        super(SIZE);
    }

    public TypeIndex getClassType(){
        return getTypeIndex(getClassIndex());
    }
    public StringIndex getNameString(){
        return getStringIndex(getNameIndex());
    }
    public ProtoIndex getProto(){
        DexFile dexFile = getDexFile();
        if(dexFile != null){
            return dexFile.getProtoSection().get(getProtoIndex());
        }
        return null;
    }

    public int getClassIndex(){
        return getShort(getBytesInternal(), OFFSET_CLASS) & 0xffff;
    }
    public void setClassIndex(int index){
        putShort(getBytesInternal(), OFFSET_CLASS, (short) index);;
    }
    public int getProtoIndex(){
        return getShort(getBytesInternal(), OFFSET_PROTO) & 0xffff;
    }
    public void setProtoIndex(int index){
        putShort(getBytesInternal(), OFFSET_PROTO, (short) index);;
    }
    public int getNameIndex(){
        return getInteger(getBytesInternal(), OFFSET_NAME);
    }
    public void setNameIndex(int index){
        putInteger(getBytesInternal(), OFFSET_NAME, index);
    }

    TypeIndex getTypeIndex(IntegerReference reference){
        return getTypeIndex(reference.get());
    }
    TypeIndex getTypeIndex(int index){
        if(index < 0){
            return null;
        }
        DexSection<TypeIndex> stringPool = getTypeSection();
        if(stringPool != null){
            return stringPool.get(index);
        }
        return null;
    }
    StringIndex getStringIndex(int index){
        if(index < 0){
            return null;
        }
        DexStringPool stringPool = getStringPool();
        if(stringPool != null){
            return stringPool.get(index);
        }
        return null;
    }
    DexStringPool getStringPool(){
        DexFile dexFile = getDexFile();
        if(dexFile != null){
            return dexFile.getStringPool();
        }
        return null;
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        getClassType().append(writer);
        writer.append("->");
        writer.append(getNameString().getString());
        writer.append('(');
        getProto().append(writer);
        writer.append(')');
        getProto().getReturnTypeIndex().append(writer);
    }
    @Override
    public String toString() {
        return getClassType() + "->" + getNameString() + getProto();
    }

    private static final int OFFSET_CLASS = 0;
    private static final int OFFSET_PROTO = 2;
    private static final int OFFSET_NAME = 4;
    private static final int SIZE = 8;

}
