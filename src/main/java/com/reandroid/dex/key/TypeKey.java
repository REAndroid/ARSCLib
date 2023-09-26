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

public class TypeKey implements Key{
    private final String type;

    public TypeKey(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public int compareTo(Object obj) {
        if(obj == null){
            return -1;
        }
        StringKey key = (StringKey) obj;
        return CompareUtil.compare(getType(), key.getString());
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TypeKey key = (TypeKey) obj;
        return getType().equals(key.getType());
    }
    @Override
    public int hashCode() {
        return getType().hashCode();
    }
    @Override
    public String toString() {
        return getType();
    }

    public TypeKey parse(String text) {
        if(text == null){
            return null;
        }
        text = text.trim();
        if(text.length() < 3){
            return null;
        }
        if(text.charAt(0) != 'L' || text.charAt(text.length() - 1) != ';'){
            return null;
        }
        if(text.indexOf('>') > 0 || text.indexOf('(') > 0 || text.indexOf('@') > 0){
            return null;
        }
        return new TypeKey(text);
    }
}
