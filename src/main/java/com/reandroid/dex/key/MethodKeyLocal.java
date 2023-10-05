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
package com.reandroid.dex.key;

import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.index.MethodId;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.StringsUtil;

import java.util.Objects;

public class MethodKeyLocal implements Key{

    private final String name;
    private final String[] parameters;
    private final String returnType;

    public MethodKeyLocal(String name, String[] parameters, String returnType){
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
    }

    public StringKey getNameKey() {
        return new StringKey(getName());
    }
    public ProtoKey getProtoKey() {
        return new ProtoKey(getParameters(), getReturnType());
    }
    public TypeKey getReturnTypeKey() {
        return new TypeKey(getReturnType());
    }

    public String getName() {
        return name;
    }
    public String[] getParameters() {
        return parameters;
    }
    public String getReturnType() {
        return returnType;
    }

    @Override
    public int compareTo(Object obj) {
        if(obj == null){
            return -1;
        }
        MethodKeyLocal key = (MethodKeyLocal) obj;
        int i = CompareUtil.compare(getName(), key.getName());
        if(i != 0) {
            return i;
        }
        i = CompareUtil.compare(getParameters(), key.getParameters());
        if(i != 0) {
            return i;
        }
        return CompareUtil.compare(getReturnType(), key.getReturnType());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MethodKey)) {
            return false;
        }
        MethodKey methodKey = (MethodKey) obj;
        return  Objects.equals(getName(), methodKey.getName()) &&
                CompareUtil.compare(getParameters(), methodKey.getParameters()) == 0 &&
                Objects.equals(getName(), methodKey.getName());
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + getName().hashCode();
        String[] parameters = getParameters();
        if(parameters != null){
            for(String param : parameters){
                hash = hash * 31 + param.hashCode();
            }
        }
        String returnType = getReturnType();
        if (returnType != null){
            hash = hash * 31 + returnType.hashCode();
        }
        return hash;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        builder.append('(');
        String[] parameters = getParameters();
        if(parameters != null){
            for (String parameter : parameters) {
                builder.append(parameter);
            }
        }
        builder.append(')');
        String type = getReturnType();
        if(type != null){
            builder.append(type);
        }
        return builder.toString();
    }

    public static MethodKeyLocal parse(String text) {
        if(text == null){
            return null;
        }
        text = text.trim();
        if(text.length() < 4 ){
            return null;
        }
        int i = text.indexOf('(');
        if(i < 0){
            return null;
        }
        String name = text.substring(0, i);
        text = text.substring(i + 1);
        i = text.indexOf(')');
        if(i < 0){
            return null;
        }
        String[] parameters = DexUtils.splitParameters(text.substring(0, i));
        text = text.substring(i + 1);
        String returnType = null;
        if(!StringsUtil.isEmpty(text)){
            returnType = text;
        }
        return new MethodKeyLocal(name, parameters, returnType);
    }

    public static MethodKeyLocal create(MethodId methodId){
        String name = methodId.getName();
        if(name == null){
            return null;
        }
        return new MethodKeyLocal(name, methodId.getParameterNames(), methodId.getReturnType());
    }
}
