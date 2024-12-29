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
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.key.TypeListKey;

import java.lang.annotation.ElementType;
import java.util.Iterator;

public interface ClassProgram extends AccessibleProgram {

    @Override
    TypeKey getKey();
    TypeKey getSuperClassKey();
    String getSourceFileName();

    TypeListKey getInterfacesKey();

    Iterator<? extends FieldProgram> getStaticFields();
    Iterator<? extends FieldProgram> getInstanceFields();

    Iterator<? extends MethodProgram> getDirectMethods();
    Iterator<? extends MethodProgram> getVirtualMethods();

    @Override
    default ElementType getElementType() {
        return ElementType.TYPE;
    }

    default boolean isInterface() {
        return AccessFlag.INTERFACE.isSet(getAccessFlagsValue());
    }
    default boolean isEnum() {
        return AccessFlag.ENUM.isSet(getAccessFlagsValue());
    }
    default boolean isAnnotation() {
        return AccessFlag.ANNOTATION.isSet(getAccessFlagsValue());
    }
}
