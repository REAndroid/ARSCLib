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
package com.reandroid.dex.id;

import com.reandroid.dex.key.KeyItemCreate;
import com.reandroid.dex.data.StringData;
import com.reandroid.dex.data.TypeList;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.reference.IdItemIndirectReference;
import com.reandroid.dex.reference.IdItemIndirectShortReference;
import com.reandroid.dex.reference.IndirectStringReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class MethodId extends IdItem implements Comparable<MethodId>, KeyItemCreate {

    private final IdItemIndirectReference<TypeId> classType;
    private final IdItemIndirectReference<ProtoId> proto;
    private final IndirectStringReference nameReference;

    public MethodId() {
        super(SIZE);
        this.classType = new IdItemIndirectShortReference<>(SectionType.TYPE_ID, this, 0, USAGE_METHOD);
        this.proto = new IdItemIndirectShortReference<>(SectionType.PROTO_ID, this, 2, USAGE_METHOD);
        this.nameReference = new IndirectStringReference(this, 4, StringId.USAGE_METHOD_NAME);
    }

    @Override
    public Iterator<IdItem> usedIds(){
        return CombiningIterator.three(
                SingleIterator.of(classType.getItem()),
                proto.getItem().usedIds(),
                SingleIterator.of(nameReference.getItem())
        );
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
    public StringData getNameString(){
        return nameReference.getStringData();
    }
    IndirectStringReference getNameReference(){
        return nameReference;
    }

    public TypeId getClassType(){
        return classType.getItem();
    }
    public void setClassType(TypeId typeId){
        classType.setItem(typeId);
    }
    public String getClassName() {
        TypeId typeId = getClassType();
        if(typeId != null){
            return typeId.getName();
        }
        return null;
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
    public String[] getParameterNames(){
        ProtoId protoId = getProto();
        if(protoId != null){
            return protoId.getParameterNames();
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
    public TypeList getParameterTypes(){
        ProtoId protoId = getProto();
        if(protoId != null){
            return protoId.getTypeList();
        }
        return null;
    }
    public ProtoId getProto(){
        return proto.getItem();
    }
    public void setProto(ProtoId protoId) {
        proto.setItem(protoId);
    }

    public String getReturnType() {
        TypeId typeId = getReturnTypeId();
        if(typeId != null){
            return typeId.getName();
        }
        return null;
    }
    public TypeId getReturnTypeId() {
        ProtoId protoId = getProto();
        if(protoId != null){
            return protoId.getReturnTypeId();
        }
        return null;
    }

    @Override
    public SectionType<MethodId> getSectionType(){
        return SectionType.METHOD_ID;
    }
    @Override
    public MethodKey getKey() {
        return checkKey(MethodKey.create(this));
    }
    @Override
    public void setKey(Key key){
        setKey((MethodKey) key);
    }
    public void setKey(MethodKey key){
        MethodKey old = getKey();
        if(Objects.equals(key, old)){
            return;
        }
        classType.setItem(key.getDefiningKey());
        nameReference.setString(key.getName());
        proto.setItem(key.getProtoKey());
        keyChanged(old);
    }
    public String getKey(boolean appendType, boolean appendReturnType){
        StringBuilder builder = new StringBuilder();
        String type = getClassName();
        if(type == null && appendType){
            return null;
        }
        if(appendType){
            builder.append(type);
            builder.append("->");
        }
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
        classType.updateItem();
        proto.updateItem();
        nameReference.updateItem();
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

    public static boolean equals(MethodId methodId1, MethodId methodId2) {
        return equals(false, methodId1, methodId2);
    }
    public static boolean equals(boolean ignoreClass, MethodId methodId1, MethodId methodId2) {
        if(methodId1 == methodId2){
            return true;
        }
        if(methodId1 == null){
            return false;
        }
        if(!IndirectStringReference.equals(methodId1.getNameReference(), methodId1.getNameReference())){
            return false;
        }
        if(!ignoreClass) {
            if(!TypeId.equals(methodId1.getClassType(), methodId2.getClassType())){
                return false;
            }
        }
        return TypeList.equals(methodId1.getParameterTypes(), methodId2.getParameterTypes());
    }

    private static final int SIZE = 8;

}
