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
import com.reandroid.dex.pool.DexIdPool;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;

public class MethodId extends IndexItemEntry implements Comparable<MethodId>{

    private final ItemIndexReference<TypeId> classType;
    private final ItemIndexReference<ProtoId> proto;
    private final StringReference nameReference;

    public MethodId() {
        super(SIZE);
        this.classType = new ItemShortReference<>(SectionType.TYPE_ID, this, 0);
        this.proto = new ItemShortReference<>(SectionType.PROTO_ID, this, 2);
        this.nameReference = new StringReference(this, 4, StringData.USAGE_METHOD);
    }

    public String getName(){
        StringData stringData = getNameString();
        if(stringData != null){
            return stringData.getString();
        }
        return null;
    }
    public void setName(String name){
        nameReference.setString(name);
    }
    public void setName(StringData stringData){
        this.nameReference.setItem(stringData);
    }

    public StringData getNameString(){
        return nameReference.getItem();
    }
    StringReference getNameReference(){
        return nameReference;
    }

    public TypeId getClassType(){
        return classType.getItem();
    }
    public void setClassType(TypeId typeId){
        classType.setItem(typeId);
    }
    public String getClassName() {
        return classType.getKey();
    }
    public int getParametersCount() {
        ProtoId protoId = getProto();
        if(protoId != null){
            return protoId.getParametersCount();
        }
        return 0;
    }
    public TypeId getParameter(int index) {
        ProtoId protoId = getProto();
        if(protoId != null){
            return protoId.getParameter(index);
        }
        return null;
    }
    public Iterator<TypeId> getParameters(){
        ProtoId protoId = getProto();
        if(protoId != null){
            return protoId.getParameters();
        }
        return EmptyIterator.of();
    }
    public ProtoId getProto(){
        return proto.getItem();
    }
    public void setProto(ProtoId protoId) {
        proto.setItem(protoId);
    }

    public TypeId getReturnTypeId() {
        ProtoId protoId = getProto();
        if(protoId != null){
            return protoId.getReturnTypeId();
        }
        return null;
    }

    @Override
    public String getKey() {
        return getKey(false);
    }
    public String getKey(boolean appendReturnType){
        StringBuilder builder = new StringBuilder();
        String type = getClassName();
        if(type == null){
            return null;
        }
        builder.append(type);
        builder.append("->");
        builder.append(getName());
        ProtoId protoId = getProto();
        if(protoId != null){
            builder.append(protoId.getKey(appendReturnType));
        }
        return builder.toString();
    }
    @Override
    public void refresh() {
        classType.refresh();
        proto.refresh();
        nameReference.refresh();
    }
    @Override
    void cacheItems(){
        classType.getItem();
        proto.getItem();
        nameReference.getItem();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getClassType().append(writer);
        writer.append("->");
        writer.append(getNameString().getString());
        writer.append('(');
        getProto().append(writer);
        writer.append(')');
        getProto().getReturnTypeId().append(writer);
    }

    @Override
    public int compareTo(MethodId methodId) {
        if(methodId == null){
            return -1;
        }
        int i = CompareUtil.compare(getClassType(), methodId.getClassType());
        if(i != 0){
            return i;
        }
        i = CompareUtil.compare(getNameReference(), methodId.getNameReference());
        if(i != 0){
            return i;
        }
        return CompareUtil.compare(getProto(), methodId.getProto());
    }

    @Override
    public String toString() {
        return getClassType() + "->" + getNameString() + getProto();
    }

    private static final int SIZE = 8;

}
