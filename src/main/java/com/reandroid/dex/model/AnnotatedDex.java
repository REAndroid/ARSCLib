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

import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;

import java.util.Iterator;

public interface AnnotatedDex {

    Iterator<DexAnnotation> getAnnotations();
    DexAnnotation getAnnotation(TypeKey typeKey);
    DexAnnotation getOrCreateAnnotation(TypeKey typeKey);

    default DexAnnotationElement getAnnotationElement(TypeKey typeKey, String name){
        DexAnnotation dexAnnotation = getAnnotation(typeKey);
        if(dexAnnotation != null) {
            return dexAnnotation.get(name);
        }
        return null;
    }
    default DexAnnotationElement getOrCreateAnnotationElement(TypeKey typeKey, String name) {
        DexAnnotation annotation = getOrCreateAnnotation(typeKey);
        return annotation.getOrCreate(name);
    }
    default Key getAnnotationValue(TypeKey typeKey, String name) {
        DexAnnotationElement element = getAnnotationElement(typeKey, name);
        if (element != null) {
            return element.getValue();
        }
        return null;
    }
}
