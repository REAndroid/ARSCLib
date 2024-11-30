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
package com.reandroid.dex.common;

import com.reandroid.dex.key.AnnotationItemKey;
import com.reandroid.dex.key.AnnotationSetKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;

import java.util.Iterator;
import java.util.function.Predicate;

public interface AnnotatedItem {

    AnnotationSetKey getAnnotation();
    void setAnnotation(AnnotationSetKey annotationSet);
    void clearAnnotations();

    default void addAnnotation(AnnotationItemKey annotation) {
        AnnotationSetKey key = getAnnotation()
                .remove(annotation.getType())
                .add(annotation);
        setAnnotation(key);
    }
    default boolean removeAnnotationIf(Predicate<? super AnnotationItemKey> predicate) {
        AnnotationSetKey key = getAnnotation();
        AnnotationSetKey update = key.removeIf(predicate);
        if (key.equals(update)) {
            return false;
        }
        setAnnotation(update);
        return true;
    }

    default Iterator<AnnotationItemKey> getAnnotations() {
        return getAnnotation().iterator();
    }
    default boolean hasAnnotations() {
        return !getAnnotation().isEmpty();
    }
    default AnnotationItemKey getAnnotation(TypeKey typeKey) {
        Iterator<AnnotationItemKey> iterator = getAnnotations();
        while (iterator.hasNext()) {
            AnnotationItemKey key = iterator.next();
            if (typeKey.equals(key.getType())) {
                return key;
            }
        }
        return null;
    }
    default Key getAnnotationValue(TypeKey typeKey, String name) {
        AnnotationItemKey annotationItemKey = getAnnotation(typeKey);
        if (annotationItemKey != null) {
            return annotationItemKey.get(name);
        }
        return null;
    }
    default boolean removeAnnotation(TypeKey typeKey) {
        return removeAnnotationIf(key -> key.equalsType(typeKey));
    }
}
