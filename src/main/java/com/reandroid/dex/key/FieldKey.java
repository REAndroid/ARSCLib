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

import com.reandroid.dex.index.FieldId;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.StringsUtil;

import java.util.Objects;

public class FieldKey implements Key {
    private final String defining;
    private final String name;
    private final String type;

    public FieldKey(String defining, String name, String type) {
        this.defining = defining;
        this.name = name;
        this.type = type;
    }
    public TypeKey getDefiningKey() {
        return new TypeKey(getDefining());
    }
    public StringKey getNameKey() {
        return new StringKey(getName());
    }
    public TypeKey getTypeKey() {
        return new TypeKey(getType());
    }

    public String getDefining() {
        return defining;
    }
    public String getName() {
        return name;
    }
    public String getType() {
        return type;
    }

    @Override
    public int compareTo(Object obj) {
        if(obj == null){
            return -1;
        }
        FieldKey key = (FieldKey) obj;
        int i = CompareUtil.compare(getDefining(), key.getDefining());
        if(i != 0) {
            return i;
        }
        i = CompareUtil.compare(getName(), key.getName());
        if(i != 0) {
            return i;
        }
        return CompareUtil.compare(getType(), key.getType());
    }

    @Override
    public int hashCode() {
        int hash = 1;
        String defining = getDefining();
        if(defining != null){
            hash += defining.hashCode();
        }
        return hash * 31 + getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FieldKey)) {
            return false;
        }
        FieldKey fieldKey = (FieldKey) obj;
        return Objects.equals(getDefining(), fieldKey.getDefining()) &&
                getName().equals(fieldKey.getName());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getDefining());
        builder.append("->");
        builder.append(getName());
        String type = getType();
        if(type != null){
            builder.append(':');
            builder.append(getType());
        }
        return builder.toString();
    }

    public static FieldKey parse(String text) {
        if(text == null){
            return null;
        }
        text = text.trim();
        if(text.length() < 6 || text.charAt(0) != 'L'){
            return null;
        }
        int i = text.indexOf(";->");
        if(i < 0){
            return null;
        }
        String defining = text.substring(0, i + 1);
        text = text.substring(i + 3);
        i = text.indexOf(':');
        if(i < 0){
            return null;
        }
        String name = text.substring(0, i);
        text = text.substring(i + 1);
        String type = null;
        if(!StringsUtil.isEmpty(text)){
            type = text;
        }
        return new FieldKey(defining, name, type);
    }
    public static FieldKey create(FieldId fieldId){
        String defining = fieldId.getClassName();
        if(defining == null){
            return null;
        }
        String name = fieldId.getName();
        if(name == null) {
            return null;
        }
        return new FieldKey(defining, name, fieldId.getFieldTypeName());
    }

}
