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
package com.reandroid.dex.program;

import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.Modifier;

import java.lang.annotation.ElementType;
import java.util.Iterator;

public interface AccessibleItem {

    int getAccessFlagsValue();
    void setAccessFlagsValue(int value);
    ElementType getElementType();

    default boolean isPublic() {
        return AccessFlag.PUBLIC.isSet(getAccessFlagsValue());
    }
    default boolean isPrivate() {
        return AccessFlag.PRIVATE.isSet(getAccessFlagsValue());
    }
    default boolean isInternal() {
        return (getAccessFlagsValue() & 0x7) == 0;
    }
    default boolean isStatic() {
        return AccessFlag.STATIC.isSet(getAccessFlagsValue());
    }
    default boolean isFinal() {
        return AccessFlag.FINAL.isSet(getAccessFlagsValue());
    }
    default boolean isProtected() {
        return AccessFlag.PROTECTED.isSet(getAccessFlagsValue());
    }
    default boolean isNative() {
        return AccessFlag.NATIVE.isSet(getAccessFlagsValue());
    }
    default boolean isSynthetic() {
        return AccessFlag.SYNTHETIC.isSet(getAccessFlagsValue());
    }
    default boolean isAbstract() {
        return AccessFlag.ABSTRACT.isSet(getAccessFlagsValue());
    }


    default Iterator<? extends Modifier> getModifiers(){
        return getAccessFlags();
    }
    default void addAccessFlag(AccessFlag flag) {
        int current = getAccessFlagsValue();
        int value = flag.getValue();
        if((value & 0x7) != 0){
            current = current & ~0x7;
        }
        setAccessFlagsValue(current | value);
    }
    default Iterator<? extends Modifier> getAccessFlags() {
        return AccessFlag.valuesOf(getElementType(), getAccessFlagsValue());
    }
    default void removeAccessFlag(AccessFlag flag) {
        setAccessFlagsValue(getAccessFlagsValue() & ~flag.getValue());
    }
}
