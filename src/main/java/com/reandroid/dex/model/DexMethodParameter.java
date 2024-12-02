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

import com.reandroid.dex.data.MethodParameter;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.program.MethodParameterProgram;
import com.reandroid.dex.program.ProgramElement;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;
import java.util.Iterator;

public class DexMethodParameter extends Dex implements AnnotatedDex, MethodParameterProgram {

    private final DexMethod dexMethod;
    private final MethodParameter parameter;

    public DexMethodParameter(DexMethod dexMethod, MethodParameter parameter) {
        this.dexMethod = dexMethod;
        this.parameter = parameter;
    }

    @Override
    public TypeKey getKey() {
        return getParameter().getKey();
    }

    @Override
    public String getDebugName() {
        return getParameter().getDebugName();
    }
    @Override
    public void setDebugName(String name) {
        getParameter().setDebugName(name);
    }
    public DexClass getTypeClass(){
        return getClassRepository().getDexClass(getType());
    }
    public TypeKey getType(){
        return getParameter().getType();
    }
    public int getIndex(){
        return getParameter().getDefinitionIndex();
    }
    public DexMethod getDexMethod() {
        return dexMethod;
    }
    public MethodParameter getParameter() {
        return parameter;
    }

    @Override
    public boolean uses(Key key) {
        if(ObjectsUtil.equals(getType(), key)) {
            return true;
        }
        Iterator<DexAnnotation> iterator = getDexAnnotations();
        while (iterator.hasNext()){
            DexAnnotation dexAnnotation = iterator.next();
            if(dexAnnotation.uses(key)){
                return true;
            }
        }
        return false;
    }

    @Override
    public DexClassRepository getClassRepository() {
        return getDexMethod().getClassRepository();
    }

    @Override
    public void removeSelf() {
        getDexMethod().removeParameter(getIndex());
    }

    @Override
    public ProgramElement getProgramElement() {
        return getParameter();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getParameter().append(writer);
    }

    public static DexMethodParameter create(DexMethod dexMethod, MethodParameter parameter){
        if(dexMethod != null && parameter != null){
            return new DexMethodParameter(dexMethod, parameter);
        }
        return null;
    }
}
