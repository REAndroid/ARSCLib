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
package com.reandroid.dex.model;

import com.reandroid.dex.key.ArrayKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.utils.collection.SingleIterator;

import java.util.Iterator;

public class DalvikUtil {

    public static int cleanMissingMembers(DexClassRepository repository) {
        int result = 0;
        Iterator<DexClass> iterator = repository.getDexClasses();
        while (iterator.hasNext()) {
            result += cleanMissingMembers(iterator.next());
        }
        return result;
    }
    public static int cleanMissingMembers(DexClass dexClass) {
        int result = 0;
        Iterator<DexAnnotation> iterator = SingleIterator.of(dexClass.getAnnotation(TypeKey.DALVIK_MemberClass));
        while (iterator.hasNext()) {
            DexAnnotation annotation = iterator.next();
            DexAnnotationElement element = annotation.get("value");
            if(element == null) {
                continue;
            }
            Key value = element.getValue();
            if(value instanceof ArrayKey) {
                ArrayKey valueArray = (ArrayKey) value;
                DexClassRepository repository = dexClass.getClassRepository();
                ArrayKey changedKey = valueArray.removeIf(key -> !repository.containsClass((TypeKey) key));
                if(changedKey != valueArray) {
                    result ++;
                    if(changedKey.isEmpty()) {
                        element.removeSelf();
                    } else {
                        element.setValue(changedKey);
                    }
                }
            }
            if(annotation.size() == 0) {
                annotation.removeSelf();
            }
        }
        return result;
    }
}
