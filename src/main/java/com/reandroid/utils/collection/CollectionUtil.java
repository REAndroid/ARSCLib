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
package com.reandroid.utils.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CollectionUtil {

    public static<T> T getFirst(Iterator<T> iterator){
        if(iterator == null || !iterator.hasNext()){
            return null;
        }
        return iterator.next();
    }
    public static int count(Iterable<?> iterable){
        if(iterable == null || iterable instanceof EmptyItem){
            return 0;
        }
        if(iterable instanceof Collection){
            return ((Collection<?>)iterable).size();
        }
        if(iterable instanceof SizedItem){
            if(((SizedItem)iterable).size() == 0){
                return 0;
            }
        }
        Iterator<?> iterator = iterable.iterator();
        int result = 0;
        while (iterator.hasNext()){
            iterator.next();
            result ++;
        }
        return result;
    }
    public static int count(Iterator<?> iterator){
        if(iterator == null || iterator instanceof EmptyItem){
            return 0;
        }
        if(iterator instanceof SizedItem){
            if(((SizedItem)iterator).size() == 0){
                return 0;
            }
        }
        int result = 0;
        while (iterator.hasNext()){
            iterator.next();
            result ++;
        }
        return result;
    }
    public static boolean isEmpty(Iterable<?> iterable){
        if(iterable == null || iterable instanceof EmptyItem){
            return true;
        }
        if(iterable instanceof Collection){
            return ((Collection<?>)iterable).isEmpty();
        }
        if(iterable instanceof SizedItem){
            if(((SizedItem)iterable).size() == 0){
                return true;
            }
        }
        return !iterable.iterator().hasNext();
    }
    public static boolean isEmpty(Iterator<?> iterator){
        if(iterator == null || iterator instanceof EmptyItem){
            return true;
        }
        if(iterator instanceof SizedItem){
            if(((SizedItem)iterator).size() == 0){
                return true;
            }
        }
        return !iterator.hasNext();
    }
    public static<T> List<T> toList(Iterator<? extends T> iterator){
        if(!iterator.hasNext()){
            return EmptyList.of();
        }
        List<T> results = new ArrayList<>(2);
        while (iterator.hasNext()){
            results.add(iterator.next());
        }
        return results;
    }
}
