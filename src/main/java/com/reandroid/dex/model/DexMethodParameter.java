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

import com.reandroid.dex.data.MethodParameterDef;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.program.MethodParameter;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;

public class DexMethodParameter extends Dex implements AnnotatedDex, MethodParameter {

    private final DexMethod dexMethod;
    private final MethodParameterDef parameterDef;

    public DexMethodParameter(DexMethod dexMethod, MethodParameterDef parameterDef) {
        this.dexMethod = dexMethod;
        this.parameterDef = parameterDef;
    }

    @Override
    public TypeKey getKey() {
        return getProgramElement().getKey();
    }

    @Override
    public String getDebugName() {
        return getProgramElement().getDebugName();
    }
    @Override
    public void setDebugName(String name) {
        getProgramElement().setDebugName(name);
    }
    public TypeKey getType() {
        return getKey();
    }
    @Override
    public int getDefinitionIndex() {
        return getProgramElement().getDefinitionIndex();
    }
    @Override
    public int getRegister() {
        return getProgramElement().getRegister();
    }

    public DexMethod getDexMethod() {
        return dexMethod;
    }

    @Override
    public boolean uses(Key key) {
        if (ObjectsUtil.equals(getType(), key)) {
            return true;
        }
        return getAnnotation().uses(key);
    }

    @Override
    public DexClassRepository getClassRepository() {
        return getDexMethod().getClassRepository();
    }

    @Override
    public void removeSelf() {
        getDexMethod().removeParameter(getDefinitionIndex());
    }
    @Override
    public boolean isRemoved() {
        DexMethod dexMethod = getDexMethod();
        return dexMethod.isRemoved() || !dexMethod.hasParameter(getDefinitionIndex());
    }

    @Override
    public MethodParameterDef getProgramElement() {
        return parameterDef;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getProgramElement().append(writer);
    }

    public static DexMethodParameter create(DexMethod dexMethod, MethodParameterDef parameter) {
        if (dexMethod != null && parameter != null) {
            return new DexMethodParameter(dexMethod, parameter);
        }
        return null;
    }
}
