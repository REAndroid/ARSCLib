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

public abstract class AnnotationsKey<T extends Key> extends KeyList<T> {

    protected AnnotationsKey(Key[] elements, boolean sorted) {
        super(elements, sorted);
    }

    public boolean isBlank() {
        return isEmpty();
    }

    public abstract AnnotationsKey<T> merge(AnnotationsKey<? extends T> itm);
    public abstract AnnotationsKey<T> merge(int i, T item);

    @Override
    public int compareTo(Object obj) {
        if (obj == this) {
            return 0;
        }
        if (!getClass().isInstance(obj)) {
            return 0;
        }
        return compareElements((AnnotationsKey<?>) obj);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!getClass().isInstance(obj)) {
            return false;
        }
        return equalsElements((AnnotationsKey<?>) obj);
    }
    @Override
    public int hashCode() {
        return getHashCode();
    }
}
