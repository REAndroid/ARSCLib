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

import com.reandroid.dex.data.TypeList;
import com.reandroid.utils.CompareUtil;

public class TypeListKey implements Key {

    private final String[] parameters;

    public TypeListKey(String[] parameters){
        this.parameters = parameters;
    }


    public TypeListKey removeParameter(int index){
        String[] parameters = getParameters();
        if(parameters == null || parameters.length < 2){
            return null;
        }
        int length = parameters.length;
        if(index < 0 || index >= length){
            return this;
        }
        String[] results = new String[length - 1];
        int count = 0;
        for(int i = 0; i < length; i++){
            if(i != index){
                results[count] = parameters[i];
                count ++;
            }
        }
        return new TypeListKey(results);
    }

    public String[] getParameters() {
        return parameters;
    }
    public int getParametersCount() {
        String[] parameters = getParameters();
        if(parameters != null){
            return parameters.length;
        }
        return 0;
    }
    public String getParameter(int i){
        return getParameters()[i];
    }
    public TypeKey getParameterType(int i){
        return TypeKey.create(getParameter(i));
    }
    @Override
    public int compareTo(Object obj) {
        if(obj == null){
            return -1;
        }
        TypeListKey key = (TypeListKey) obj;
        return CompareUtil.compare(getParameters(), key.getParameters());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TypeListKey)) {
            return false;
        }
        TypeListKey key = (TypeListKey) obj;
        return CompareUtil.compare(getParameters(), key.getParameters()) == 0;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        String[] parameters = getParameters();
        if(parameters != null){
            for(String param : parameters){
                hash = hash * 31 + param.hashCode();
            }
        }
        return hash;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        String[] parameters = getParameters();
        if(parameters != null){
            for (String parameter : parameters) {
                builder.append(parameter);
            }
        }
        builder.append(')');
        return builder.toString();
    }


    public static TypeListKey create(TypeList typeList){
        return create(typeList.getNames());
    }
    public static TypeListKey create(String[] parameters){
        if(parameters == null || parameters.length == 0){
            return null;
        }
        return new TypeListKey(parameters);
    }
}
