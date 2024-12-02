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

import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.Key;

import java.lang.annotation.ElementType;

public interface FieldProgram extends AccessibleProgram {

    @Override
    FieldKey getKey();
    Key getStaticValue();

    @Override
    default ElementType getElementType() {
        return ElementType.FIELD;
    }
    default boolean isInstance() {
        return !isStatic();
    }
}
