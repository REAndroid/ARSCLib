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
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyItemCreate;
import com.reandroid.dex.key.ProtoKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class ProtoId extends IdSectionEntry implements Comparable<ProtoId>, KeyItemCreate {

    private final StringReference shorty;
    private final ItemIdReference<TypeId> returnType;
    private final ItemOffsetReference<TypeList> parameters;

    public ProtoId() {
        super(SIZE);

        this.shorty = new StringReference(this, 0, StringId.USAGE_SHORTY);
        this.returnType = new ItemIdReference<>(SectionType.TYPE_ID, this, 4);
        this.parameters = new ItemOffsetReference<>(SectionType.TYPE_LIST, this, 8);
    }

    @Override
    public ProtoKey getKey(){
        return ProtoKey.create(this);
    }
    @Override
    public void setKey(Key key){
        if(!(key instanceof ProtoKey)){
            return;
        }
        setKey((ProtoKey) key);
    }
    public void setKey(ProtoKey key){
        if(Objects.equals(key, getKey())){
            return;
        }
        returnType.setItem(key.getReturnTypeKey());
        parameters.getOrCreate().setKey(key.getParametersKey());
        shorty.setString(key.getShorty());
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

    public int getParameterRegistersCount(){
        TypeList typeList = getTypeList();
        if(typeList == null){
            return 0;
        }
        int result = 0;
        Iterator<String> iterator = typeList.getTypeNames();
        while (iterator.hasNext()){
            String name = iterator.next();
            if("J".equals(name) || "D".equals(name)){
                result ++;
            }
            result ++;
        }
        return result;
    }

    public int getParametersCount(){
        TypeList typeList = getTypeList();
        if(typeList != null){
            return typeList.size();
        }
        return 0;
    }
    public TypeId getParameter(int index) {
        TypeList typeList = getTypeList();
        if(typeList != null){
            return typeList.getTypeId(index);
        }
        return null;
    }
    public void removeParameter(int index){
        TypeList typeList = getTypeList();
        TypeList update = getSection(SectionType.TYPE_LIST).createItem();
        int length = getParametersCount();
        for(int i = 0; i < length; i++){
            if(i == index){
                continue;
            }
            update.add(typeList.getTypeId(i));
        }
        setParameters(update);
    }
    public String[] getParameterNames(){
        TypeList typeList = getTypeList();
        if(typeList != null){
            return typeList.getNames();
        }
        return null;
    }
    public Iterator<TypeId> getParameters(){
        TypeList typeList = getTypeList();
        if(typeList != null){
            return typeList.iterator();
        }
        return EmptyIterator.of();
    }
    public TypeList getTypeList() {
        return parameters.getItem();
    }
    public void setParameters(TypeList typeList){
        parameters.setItem(typeList);
    }
    public String getReturnType(){
        TypeId typeId = getReturnTypeId();
        if(typeId != null){
            return typeId.getName();
        }
        return null;
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
    public void setShorty(String shortyString){
        shorty.setString(shortyString);
    }

    @Override
    public void refresh() {
        shorty.refresh();
        returnType.refresh();
        parameters.refresh();
    }
    @Override
    void cacheItems(){
        shorty.cacheItem();
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
