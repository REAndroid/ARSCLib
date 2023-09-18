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

import com.reandroid.dex.index.TypeId;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.ArrayIterator;

import java.io.IOException;
import java.util.Iterator;

public class TypeList extends ShortList implements SmaliFormat, Iterable<TypeId>, Comparable<TypeList> {
    private TypeId[] typeIds;

    public TypeList() {
        super();
    }
    public boolean add(TypeId typeId){
        if(typeId != null){
            return addIfAbsent(typeId.getIndex());
        }
        return false;
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
    @Override
    void onChanged(){
        updateTypeIds();
    }
    private void updateTypeIds(){
        typeIds = get(SectionType.TYPE_ID, toArray());
    }

    @Override
    protected void onRefreshed() {
        TypeId[] typeIds = getTypeIds();
        if(typeIds == null){
            setSize(0);
            return;
        }
        int length = typeIds.length;
        setSize(length, false);
        for(int i = 0; i < length; i++){
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
}
