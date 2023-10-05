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

import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.StringsUtil;

public class AnnotationKey implements Key{

    private final String defining;
    private final String name;
    private final String[] otherNames;
    private final String returnType;

    public AnnotationKey(String defining, String name, String returnType, String[] otherNames){
        this.defining = defining;
        this.name = name;
        this.returnType = returnType;
        this.otherNames = otherNames;
    }
    public AnnotationKey(String defining, String name, String returnType){
        this(defining, name, returnType, null);
    }
    public AnnotationKey(String defining, String name){
        this(defining, name, null, null);
    }

    public String getDefining() {
        return defining;
    }
    public String getName() {
        return name;
    }
    public boolean containsName(String[] names){
        if(names == null){
            return false;
        }
        for(String name : names){
            if(containsName(name)){
                return true;
            }
        }
        return false;
    }
    public boolean containsName(String name){
        if(name == null){
            return false;
        }
        if(name.equals(getName())){
            return true;
        }
        String[] otherNames = getOtherNames();
        if(otherNames != null){
            for(String other : otherNames){
                if(name.equals(other)){
                    return true;
                }
            }
        }
        return false;
    }
    public String[] getOtherNames() {
        return otherNames;
    }
    public String getReturnType() {
        return returnType;
    }

    @Override
    public int compareTo(Object obj) {
        if(obj == null){
            return -1;
        }
        if(obj == this){
            return 0;
        }
        AnnotationKey key = (AnnotationKey) obj;
        int i = CompareUtil.compare(getDefining(), key.getDefining());
        if(i != 0) {
            return i;
        }
        i = CompareUtil.compare(getName(), key.getName());
        if(i != 0) {
            return i;
        }
        i = CompareUtil.compare(getOtherNames(), key.getOtherNames());
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
        if (!(obj instanceof AnnotationKey)) {
            return false;
        }
        AnnotationKey key = (AnnotationKey) obj;
        if(!getDefining().equals(key.getDefining())){
            return false;
        }
        if(containsName(key.getName())){
            return true;
        }
        return containsName(key.getOtherNames());
    }

    @Override
    public int hashCode() {
        int hash = 1;
        String text = getDefining();
        if(text != null){
            hash = hash * 31 + text.hashCode();
        }
        text = getName();
        if(text != null){
            hash = hash * 31 + text.hashCode();
        }
        return hash;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getDefining());
        builder.append("@");
        builder.append(getName());
        builder.append('(');
        String[] parameters = getOtherNames();
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

    public static AnnotationKey parse(String text) {
        if(text == null){
            return null;
        }
        text = text.trim();
        if(text.length() < 6 || (text.charAt(0) != 'L' && text.charAt(0) != '[')){
            return null;
        }
        int i = text.indexOf(";@");
        if(i < 0){
            return null;
        }
        String defining = text.substring(0, i + 1);
        text = text.substring(i + 2);
        i = text.indexOf("()");
        if(i < 0){
            return null;
        }
        String name = text.substring(0, i);
        text = text.substring(i + 2);
        String returnType = null;
        if(!StringsUtil.isEmpty(text)){
            returnType = text;
        }
        return new AnnotationKey(defining, name, returnType);
    }
}
