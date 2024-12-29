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
package com.reandroid.common;

import com.reandroid.dex.key.TypeKey;

public class ReflectionUtil {

    public static boolean isInstanceReflection(TypeKey type, TypeKey test) {
        try {
            return isInstance(Class.forName(type.getSourceName()),
                    Class.forName(test.getSourceName()));
        } catch (Throwable ignored) {
            return false;
        }
    }
    public static boolean isInstance(Class<?> type, Class<?> test) {
        if (type.equals(test) || test == Object.class) {
            return true;
        }
        if (type == Object.class) {
            return false;
        }
        if (!type.isInterface()) {
            Class<?> superType = type.getSuperclass();
            if (isInstance(superType, test)) {
                return true;
            }
        }
        Class<?>[] typeInterfaces = type.getInterfaces();
        for (Class<?> c : typeInterfaces) {
            if (isInstance(c, test)) {
                return true;
            }
        }
        return false;
    }
}
