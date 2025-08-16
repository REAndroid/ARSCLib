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
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.ProtoKey;

import java.lang.annotation.ElementType;
import java.util.Iterator;

public interface MethodProgram extends AccessibleProgram {

    @Override
    MethodKey getKey();
    MethodParameter getParameter(int i);
    Iterator<? extends MethodParameter> getParameters();
    @Override
    default ElementType getElementType() {
        return ElementType.METHOD;
    }
    default int getParametersCount() {
        ProtoKey protoKey = getProtoKey();
        if (protoKey != null) {
            return protoKey.getParametersCount();
        }
        return 0;
    }
    default ProtoKey getProtoKey() {
        MethodKey key = getKey();
        if (key != null) {
            return key.getProto();
        }
        return null;
    }
    default String getName() {
        MethodKey key = getKey();
        if (key != null) {
            return key.getName();
        }
        return null;
    }

    default boolean isConstructor() {
        return AccessFlag.CONSTRUCTOR.isSet(getAccessFlagsValue());
    }
    default boolean isBridge() {
        return AccessFlag.BRIDGE.isSet(getAccessFlagsValue());
    }
    default boolean isDirect() {
        return isConstructor() || isStatic() || isPrivate();
    }
    default boolean isVirtual(){
        return !isDirect();
    }
    default boolean isVarArgs() {
        return AccessFlag.VARARGS.isSet(getAccessFlagsValue());
    }
}
