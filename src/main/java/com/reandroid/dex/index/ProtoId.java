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

import com.reandroid.dex.item.StringData;
import com.reandroid.dex.item.TypeList;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;

public class ProtoId extends IndexItemEntry implements Comparable<ProtoId> {

    private final StringReference shorty;
    private final ItemIndexReference<TypeId> returnType;
    private final ItemOffsetReference<TypeList> parameters;

    public ProtoId() {
        super(SIZE);

        this.shorty = new StringReference(this, 0);
        this.returnType = new ItemIndexReference<>(SectionType.TYPE_ID, this, 4);
        this.parameters = new ItemOffsetReference<>(SectionType.TYPE_LIST, this, 8);
    }

    @Override
    public String getKey(){
        return getKey(true);
    }
    public String getKey(boolean appendReturnType){
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(buildMethodParameters());
        builder.append(')');
        if(appendReturnType){
            builder.append(getReturnTypeId().getName());
        }
        return builder.toString();
    }

    public TypeList getTypeList() {
        return parameters.getItem();
    }
    public void setParameters(TypeList typeList){
        parameters.setItem(typeList);
    }
    public TypeId getReturnTypeId(){
        return returnType.getItem();
    }
    public void setReturnTypeId(TypeId typeId){
        returnType.setItem(typeId);
    }
    public StringData getShorty(){
        return shorty.getItem();
    }
    public void setShorty(StringData stringData){
        shorty.setItem(stringData);
    }

    public int getParametersCount(){
        TypeList typeList = getTypeList();
        if(typeList != null){
            return typeList.size();
        }
        return 0;
    }

    @Override
    public void refresh() {
        shorty.refresh();
        returnType.refresh();
        parameters.refresh();
    }
    @Override
    void cacheItems(){
        shorty.getItem();
        returnType.getItem();
        parameters.getItem();
    }

    public String buildMethodParameters(){
        TypeList typeList = getTypeList();
        if(typeList == null || typeList.size() == 0){
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for(TypeId typeId : typeList){
            builder.append(typeId.getNameData().getString());
        }
        return builder.toString();
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        TypeList typeList = getTypeList();
        if(typeList == null || typeList.size() == 0){
            return;
        }
        for(TypeId typeId : typeList){
            typeId.append(writer);
        }
    }

    @Override
    public int compareTo(ProtoId protoId) {
        if(protoId == null) {
            return -1;
        }
        int i = CompareUtil.compare(getReturnTypeId(), protoId.getReturnTypeId());
        if(i != 0){
            return i;
        }
        return CompareUtil.compare(getTypeList(), protoId.getTypeList());
    }

    @Override
    public String toString() {
        return getKey(true);
    }

    private static final int SIZE = 12;

}
