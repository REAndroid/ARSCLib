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

import com.reandroid.dex.common.HiddenApiFlag;
import com.reandroid.dex.common.Modifier;
import com.reandroid.dex.key.MemberKey;
import com.reandroid.utils.collection.CombiningIterator;

import java.util.Iterator;

public interface MemberProgram extends AccessibleProgram {

    @Override
    MemberKey getKey();

    default String getName() {
        MemberKey key = getKey();
        if (key != null) {
            return key.getName();
        }
        return null;
    }
    default int getHiddenApiFlagsValue() {
        return 0x7; //NO_RESTRICTION
    }
    default void setHiddenApiFlagsValue(int value) {
    }
    default Iterator<HiddenApiFlag> getHiddenApiFlags() {
        return HiddenApiFlag.valuesOf(getHiddenApiFlagsValue());
    }

    @Override
    default Iterator<? extends Modifier> getModifiers() {
        return CombiningIterator.two(getAccessFlags(), getHiddenApiFlags());
    }
}
