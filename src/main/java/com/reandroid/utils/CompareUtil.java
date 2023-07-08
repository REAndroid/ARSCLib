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
package com.reandroid.utils;

import java.util.Comparator;

public class CompareUtil {

    @SuppressWarnings("unchecked")
    public static <E, T extends Comparable<E>> Comparator<T> getComparableComparator(){
        return (Comparator<T>) COMPARATOR;
    }
    @SuppressWarnings("unchecked")
    static <T1> Comparator<T1> getToStringComparator(){
        return (Comparator<T1>) TO_STRING_COMPARATOR;
    }

    public static final Comparator<String> STRING_COMPARATOR = new Comparator<String>() {
        @Override
        public int compare(String s1, String s2) {
            return StringsUtil.compareStrings(s1, s2);
        }
    };
    private static final Comparator<?> TO_STRING_COMPARATOR = new Comparator<Object>() {
        @Override
        public int compare(Object obj1, Object obj2) {
            return StringsUtil.compareToString(obj1, obj2);
        }
    };

    private static final Comparator<Comparable<?>> COMPARATOR = new Comparator<Comparable<?>>() {
        @Override
        public int compare(Comparable comparable1, Comparable comparable2) {
            return comparable1.compareTo(comparable2);
        }
    };
}
