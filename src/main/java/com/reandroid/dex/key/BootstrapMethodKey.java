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
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.CombiningIterator;

import java.util.Iterator;

public class BootstrapMethodKey implements Key {

    private final ArrayKey<?> arguments;
    private final MethodHandleKey methodHandle;

    private BootstrapMethodKey(ArrayKey<?> arguments, MethodHandleKey methodHandle) {
        this.arguments = arguments;
        this.methodHandle = methodHandle;
    }

    public ArrayKey<?> getArguments() {
        return arguments;
    }
    public MethodHandleKey getMethodHandle() {
        return methodHandle;
    }

    public BootstrapMethodKey changeArguments(ArrayKey<?> arguments) {
        if (arguments.equals(getArguments())) {
            return this;
        }
        return create(arguments, getMethodHandle());
    }
    public BootstrapMethodKey changeMethodHandle(MethodHandleKey methodHandle) {
        if (methodHandle.equals(getMethodHandle())) {
            return this;
        }
        return create(getArguments(), methodHandle);
    }

    @Override
    public Iterator<? extends Key> contents() {
        return CombiningIterator.singleTwo(this,
                getArguments().contents(),
                getMethodHandle().contents());
    }

    @Override
    public BootstrapMethodKey replaceKey(Key search, Key replace) {
        BootstrapMethodKey result = this;
        if (result.equals(search)) {
            return (BootstrapMethodKey) replace;
        }
        result = result.changeArguments(getArguments().replaceKey(search, replace));
        result = result.changeMethodHandle(getMethodHandle().replaceKey(search, replace));
        return result;
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == this) {
            return 0;
        }
        if (!(obj instanceof BootstrapMethodKey)) {
            return StringsUtil.compareToString(this, obj);
        }
        BootstrapMethodKey key = (BootstrapMethodKey) obj;
        int i = CompareUtil.compare(this.getMethodHandle(), key.getMethodHandle());
        if (i == 0) {
            i = CompareUtil.compare(this.getArguments(), key.getArguments());
        }
        return i;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BootstrapMethodKey)) {
            return false;
        }
        BootstrapMethodKey other = (BootstrapMethodKey) obj;
        return ObjectsUtil.equals(getMethodHandle(), other.getMethodHandle()) &&
                ObjectsUtil.equals(getArguments(), other.getArguments());
    }

    @Override
    public int hashCode() {
        return ObjectsUtil.hash(getMethodHandle(), getArguments());
    }

    @Override
    public String toString() {
        //TODO
        return getArguments() + " @" + getMethodHandle();
    }

    public static BootstrapMethodKey create(ArrayKey<?> arguments, MethodHandleKey methodHandle) {
        if (methodHandle == null) {
            return null;
        }
        if (arguments == null) {
            arguments = ArrayKey.empty();
        }
        return new BootstrapMethodKey(arguments, methodHandle);
    }
    public static BootstrapMethodKey create(MethodHandleKey methodHandle, Key ... arguments) {
        if (methodHandle == null) {
            return null;
        }
        return create(ArrayKey.create(arguments), methodHandle);
    }
}
