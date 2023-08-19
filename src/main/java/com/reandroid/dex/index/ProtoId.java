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

import com.reandroid.dex.base.IndirectInteger;
import com.reandroid.dex.item.TypeList;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class ProtoId extends ItemId {

    private final IndirectInteger shorty;
    private final IndirectInteger returnType;
    private final IndirectInteger parameters;

    public ProtoId() {
        super(SIZE);
        int offset = -4;

        this.shorty = new IndirectInteger(this, offset += 4);
        this.returnType = new IndirectInteger(this, offset += 4);
        this.parameters = new IndirectInteger(this, offset += 4);
    }

    public TypeList getTypeList() {
        return getAt(SectionType.TYPE_LIST, parameters.get());
    }

    public IndirectInteger getShorty() {
        return shorty;
    }
    public IndirectInteger getReturnTypeIdReference() {
        return returnType;
    }
    public IndirectInteger getParametersReference() {
        return parameters;
    }

    public TypeId getReturnTypeId(){
        return getTypeId(getReturnTypeIdReference());
    }

    public int[] getParametersIndexes(){
        TypeList typeList = getTypeList();
        if(typeList != null){
            return typeList.toArray();
        }
        return null;
    }
    public TypeId[] getParameterTypes(){
        TypeList typeList = getTypeList();
        if(typeList != null){
            return typeList.toTypeIds();
        }
        return null;
    }
    public int getParametersCount(){
        TypeList typeList = getTypeList();
        if(typeList != null){
            return typeList.size();
        }
        return 0;
    }

    public String buildMethodParameters(){
        TypeId[] parameters = getParameterTypes();
        if(parameters == null){
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for(TypeId typeId :parameters){
            builder.append(typeId.getStringData().getString());
        }
        return builder.toString();
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        TypeId[] parameters = getParameterTypes();
        if(parameters == null){
            return;
        }
        for(TypeId typeId : parameters){
            typeId.append(writer);
        }
    }
    @Override
    public String toString() {
        return "(" + buildMethodParameters() +")" + getReturnTypeId().toString();
    }

    private static final int SIZE = 12;

}
