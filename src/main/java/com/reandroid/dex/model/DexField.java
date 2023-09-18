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
package com.reandroid.dex.model;

import com.reandroid.dex.index.FieldId;
import com.reandroid.dex.item.StringData;
import com.reandroid.dex.item.AnnotationSet;
import com.reandroid.dex.item.FieldDef;
import com.reandroid.dex.item.FieldDefArray;
import com.reandroid.utils.collection.EmptyList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DexField {

    private final DexClass dexClass;
    private final FieldDef fieldDef;

    public DexField(DexClass dexClass, FieldDef fieldDef){
        this.dexClass = dexClass;
        this.fieldDef = fieldDef;
    }

    public String getName(){
        return getFieldId().getName();
    }
    public void setName(String name){
        getFieldId().setName(name);
    }
    public void setName(StringData name){
        getFieldId().setName(name);
    }
    public String key(){
        return getFieldId().key();
    }
    public String getKey(){
        return getFieldId().getKey();
    }
    public FieldId getFieldId() {
        return getFieldDef().getFieldId();
    }
    public DexClass getDexClass() {
        return dexClass;
    }
    public FieldDef getFieldDef() {
        return fieldDef;
    }
    public Iterator<AnnotationSet> getAnnotations(){
        getFieldDef().setClassId(getDexClass().getClassId());
        return getFieldDef().getAnnotations();
    }

    @Override
    public String toString() {
        return key();
    }

    static DexField create(DexClass dexClass, FieldDef fieldDef){
        return new DexField(dexClass, fieldDef);
    }
    static List<DexField> create(DexClass dexClass, FieldDefArray defArray){
        int count = defArray.getChildesCount();
        if(count == 0){
            return EmptyList.of();
        }
        List<DexField> results = new ArrayList<>(count);
        Iterator<FieldDef> iterator = defArray.iterator();
        while (iterator.hasNext()){
            results.add(create(dexClass, iterator.next()));
        }
        return results;
    }

}
