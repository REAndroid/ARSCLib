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

import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.utils.StringsUtil;

public abstract class DexDef extends DexModel {

    public boolean isAccessibleTo(TypeKey typeKey) {
        if(this.getDefining().equals(typeKey)){
            return true;
        }
        if(isInternal()) {
            return this.getPackageName().equals(typeKey.getPackageName());
        }
        return !isPrivate();
    }
    public boolean isInternal() {
        return (getAccessFlagsValue() & 0x7) == 0;
    }
    public boolean isPublic() {
        return AccessFlag.PUBLIC.isSet(getAccessFlagsValue());
    }
    public boolean isProtected() {
        return AccessFlag.PROTECTED.isSet(getAccessFlagsValue());
    }
    public boolean isPrivate() {
        return AccessFlag.PRIVATE.isSet(getAccessFlagsValue());
    }
    public boolean isNative() {
        return AccessFlag.NATIVE.isSet(getAccessFlagsValue());
    }
    public boolean isStatic() {
        return AccessFlag.STATIC.isSet(getAccessFlagsValue());
    }

    public abstract String getAccessFlags();
    abstract int getAccessFlagsValue();
    abstract Key getKey();
    public String getClassName(){
        TypeKey typeKey = getDefining();
        if(typeKey != null){
            return typeKey.getType();
        }
        return null;
    }
    public abstract TypeKey getDefining();
    public String getPackageName() {
        return getDefining().getPackageName();
    }
    @Override
    public int hashCode() {
        Key key = getKey();
        if(key != null){
            return key.hashCode();
        }
        return 0;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String flags = getAccessFlags();
        if(!StringsUtil.isEmpty(flags)){
            builder.append(flags);
            builder.append(' ');
        }
        String className = getClassName();
        builder.append(className);
        Key key = getKey();
        if(!key.toString().contains(className)){
            builder.append("->");
            builder.append(getKey());
        }
        return builder.toString();
    }
}
