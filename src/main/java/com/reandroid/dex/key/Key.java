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


import com.reandroid.dex.common.DexUtils;
import com.reandroid.utils.ObjectsUtil;

import java.util.Iterator;

public interface Key extends Comparable<Object> {

    default TypeKey getDeclaring(){
        return TypeKey.NULL;
    }
    default Iterator<? extends Key> mentionedKeys(){
        throw new RuntimeException("Method 'mentionedKeys()' Not implemented for: " + getClass());
    }
    default Key replaceKey(Key search, Key replace){
        return this;
    }
    default boolean isPlatform(){
        return DexUtils.isPlatform(getDeclaring());
    }

    String DALVIK_accessFlags = ObjectsUtil.of("accessFlags");
    String DALVIK_name = ObjectsUtil.of("name");
    String DALVIK_value = ObjectsUtil.of("value");
}
