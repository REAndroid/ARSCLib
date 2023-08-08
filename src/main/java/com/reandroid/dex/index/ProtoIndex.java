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
import com.reandroid.dex.item.TypeList;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class ProtoIndex extends ItemIndex {
    private final TypeList typeList;

    private final IndirectInteger shorty;
    private final IndirectInteger returnType;
    private final IndirectInteger parameters;

    public ProtoIndex() {
        super(SIZE);
        int offset = -4;

        this.shorty = new IndirectInteger(this, offset += 4);
        this.returnType = new IndirectInteger(this, offset += 4);
        this.parameters = new IndirectInteger(this, offset += 4);

        typeList = new TypeList(parameters);
    }

    public IndirectInteger getShorty() {
        return shorty;
    }
    public IndirectInteger getReturnTypeIndexReference() {
        return returnType;
    }
    public IndirectInteger getParametersReference() {
        return parameters;
    }

    public TypeIndex getReturnTypeIndex(){
        return getTypeIndex(getReturnTypeIndexReference());
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        typeList.readBytes(reader);
    }
    public int[] getParametersIndexes(){
        TypeList typeList = this.typeList;
        if(typeList != null){
            return typeList.toArray();
        }
        return null;
    }
    public TypeIndex[] getParameterTypes(){
        TypeList typeList = this.typeList;
        if(typeList != null){
            return typeList.toTypes(getTypeSection());
        }
        return null;
    }

    public String buildMethodParameters(){
        TypeIndex[] parameters = getParameterTypes();
        if(parameters == null){
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for(TypeIndex typeIndex:parameters){
            builder.append(typeIndex.getStringIndex().getString());
        }
        return builder.toString();
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        TypeIndex[] parameters = getParameterTypes();
        if(parameters == null){
            return;
        }
        for(TypeIndex typeIndex : parameters){
            typeIndex.append(writer);
        }
    }
    @Override
    public String toString() {
        return "(" + buildMethodParameters() +")" + getReturnTypeIndex().toString();
    }

    private static final int SIZE = 12;

}
