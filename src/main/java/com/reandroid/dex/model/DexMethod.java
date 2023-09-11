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

import com.reandroid.dex.index.MethodId;
import com.reandroid.dex.item.MethodDef;
import com.reandroid.dex.item.MethodDefArray;
import com.reandroid.utils.collection.EmptyList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DexMethod {
    private final DexClass dexClass;
    private final MethodDef methodDef;

    public DexMethod(DexClass dexClass, MethodDef methodDef){
        this.dexClass = dexClass;
        this.methodDef = methodDef;
    }

    public String getName(){
        return getMethodId().getName();
    }
    public String key(){
        return getMethodId().key();
    }
    public String getKey(){
        return getMethodId().getKey();
    }
    public MethodId getMethodId() {
        return getMethodDef().getMethodId();
    }
    public DexClass getDexClass() {
        return dexClass;
    }
    public MethodDef getMethodDef() {
        return methodDef;
    }
    @Override
    public String toString() {
        return key();
    }

    static DexMethod create(DexClass dexClass, MethodDef fieldDef){
        return new DexMethod(dexClass, fieldDef);
    }
    static List<DexMethod> create(DexClass dexClass, MethodDefArray defArray){
        int count = defArray.getChildesCount();
        if(count == 0){
            return EmptyList.of();
        }
        List<DexMethod> results = new ArrayList<>(count);
        Iterator<MethodDef> iterator = defArray.iterator();
        while (iterator.hasNext()){
            results.add(create(dexClass, iterator.next()));
        }
        return results;
    }
}
