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
package com.reandroid.dex.data;

import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.id.TypeId;
import com.reandroid.dex.key.*;
import com.reandroid.dex.pool.DexSectionPool;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.ArrayIterator;
import com.reandroid.utils.collection.ComputeIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class TypeList extends ShortList implements KeyItemCreate, SmaliFormat, Iterable<TypeId>, Comparable<TypeList> {

    private TypeId[] typeIds;

    public TypeList() {
        super();
    }

    @Override
    public TypeListKey getKey(){
        return checkKey(SectionType.TYPE_LIST, TypeListKey.create(this));
    }
    @Override
    public void setKey(Key key){
        setKey((TypeListKey) key);
    }
    public void setKey(TypeListKey key){
        TypeListKey old = getKey();
        if(Objects.equals(old, key)){
            return;
        }
        String[] names = key.getParameters();;
        if(names == null){
            setSize(0);
            return;
        }
        setSize(0);
        DexSectionPool<TypeId> pool = getPool(SectionType.TYPE_ID);
        if(pool == null) {
            return;
        }
        for(String name : names){
            TypeId typeId = pool.getOrCreate(TypeKey.create(name));
            add(typeId);
        }
        keyChanged(SectionType.TYPE_LIST, old);
    }
    public void addAll(Iterator<String> iterator) {
        if(!iterator.hasNext()) {
            return;
        }
        DexSectionPool<TypeId> pool = getPool(SectionType.TYPE_ID);
        if(pool == null) {
            return;
        }
        while (iterator.hasNext()){
            TypeId typeId = pool.getOrCreate(new TypeKey(iterator.next()));
            add(typeId);
        }
    }
    public void add(String typeName) {
        if(typeName == null){
            return;
        }
        DexSectionPool<TypeId> pool = getPool(SectionType.TYPE_ID);
        if(pool != null){
            add(pool.getOrCreate(new TypeKey(typeName)));
        }
    }
    public void add(TypeId typeId){
        if(typeId != null) {
            add(typeId.getIndex());
        }else {
            add(0);
        }
    }
    public void remove(TypeId typeId){
        if(typeId != null){
            remove(indexOf(typeId.getIndex()));
        }
    }
    public Iterator<TypeKey> getTypeKeys() {
        return ComputeIterator.of(iterator(), TypeId::getKey);
    }
    public Iterator<String> getTypeNames() {
        return ComputeIterator.of(iterator(), TypeId::getName);
    }
    @Override
    public Iterator<TypeId> iterator() {
        return ArrayIterator.of(getTypeIds());
    }
    @Override
    public int size() {
        return super.size();
    }
    public TypeId[] getTypeIds(){
        return typeIds;
    }
    public String[] getNames() {
        TypeId[] typeIds = getTypeIds();
        if(typeIds == null){
            return null;
        }
        int length = typeIds.length;
        String[] results = new String[length];
        for(int i = 0; i < length; i++){
            results[i] = typeIds[i].getName();
        }
        return results;
    }
    public TypeId getTypeId(int index){
        TypeId[] typeIds = getTypeIds();
        if(typeIds != null && index >= 0 && index < typeIds.length){
            return typeIds[index];
        }
        return null;
    }
    @Override
    void onChanged(){
        updateTypeIds();
    }
    private void updateTypeIds(){
        typeIds = get(SectionType.TYPE_ID, toArray());
        if(typeIds == null){
            return;
        }
        for(TypeId typeId : typeIds){
            typeId.addUsageType(UsageMarker.USAGE_INTERFACE);
        }
    }

    @Override
    protected void onPreRefresh() {
        refreshTypeIds();
    }
    private void refreshTypeIds() {
        TypeId[] typeIds = getTypeIds();
        if(typeIds == null){
            setSize(0);
            return;
        }
        int length = typeIds.length;
        setSize(length, false);
        for(int i = 0; i < length; i++){
            TypeId typeId = typeIds[i];
            typeId.addUsageType(UsageMarker.USAGE_INTERFACE);
            put(i, typeIds[i].getIndex());
        }
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        for(TypeId typeId : this){
            typeId.append(writer);
        }
    }

    @Override
    public int compareTo(TypeList typeList) {
        if(typeList == null){
            return -1;
        }
        return CompareUtil.compare(getTypeIds(), typeList.getTypeIds());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(TypeId typeId : this){
            builder.append(typeId);
        }
        return builder.toString();
    }

    public static boolean equals(TypeList typeList1, TypeList typeList2) {
        if(typeList1 == typeList2) {
            return true;
        }
        if(typeList1 == null){
            return false;
        }
        return CompareUtil.compare(typeList1.getNames(), typeList2.getNames()) == 0;
    }
}
