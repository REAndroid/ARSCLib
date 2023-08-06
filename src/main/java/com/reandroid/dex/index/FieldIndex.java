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
import com.reandroid.dex.sections.DexSection;
import com.reandroid.dex.sections.DexStringPool;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class FieldIndex extends ItemIndex implements SmaliFormat {
    public FieldIndex() {
        super(8);
    }

    public String getKey(){
        StringBuilder builder = new StringBuilder();
        TypeIndex type = getClassType();
        if(type == null){
            return null;
        }
        StringIndex stringIndex = type.getStringIndex();
        if(stringIndex == null){
            return null;
        }
        builder.append(stringIndex.getString());
        builder.append("->");
        stringIndex = getNameString();
        if(stringIndex == null){
            return null;
        }
        builder.append(stringIndex.getString());
        builder.append(':');
        type = getFieldType();
        if(type == null){
            return null;
        }
        stringIndex = type.getStringIndex();
        if(stringIndex == null){
            return null;
        }
        builder.append(stringIndex.getString());
        return builder.toString();
    }
    public TypeIndex getClassType(){
        return getTypeIndex(getClassIndex());
    }
    public StringIndex getNameString(){
        return getStringIndex(getNameIndex());
    }
    public TypeIndex getFieldType(){
        return getTypeIndex(getTypeIndex());
    }

    public int getClassIndex(){
        return getShort(getBytesInternal(), 0) & 0xffff;
    }
    public void setClassIndex(int index){
        putShort(getBytesInternal(), 0, (short) index);
    }
    public int getTypeIndex(){
        return getShort(getBytesInternal(), 2) & 0xffff;
    }
    public void setTypeIndex(int index){
        putShort(getBytesInternal(), 2, (short) index);
    }
    public int getNameIndex(){
        return getInteger(getBytesInternal(), 4);
    }
    public void setNameIndex(int index){
        putInteger(getBytesInternal(), 4, index);
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
    DexSection<TypeIndex> getTypeSection(){
        DexFile dexFile = getDexFile();
        if(dexFile != null){
            return dexFile.getTypeSection();
        }
        return null;
    }
    DexFile getDexFile(){
        return getParentInstance(DexFile.class);
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        getClassType().append(writer);
        writer.append("->");
        writer.append(getNameString().getString());
        writer.append(':');
        getFieldType().append(writer);
    }
    @Override
    public String toString(){
        String key = getKey();
        if(key != null){
            return key;
        }
        return getClassIndex() + "->" + getNameString() + ":" + getFieldType();
    }
}
