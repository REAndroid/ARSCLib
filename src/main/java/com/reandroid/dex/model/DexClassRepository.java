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

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.data.AnnotationElement;
import com.reandroid.dex.data.AnnotationItem;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.FilterIterator;
import com.reandroid.utils.collection.IterableIterator;
import com.reandroid.utils.collection.UniqueIterator;

import java.util.Iterator;
import java.util.function.Predicate;

public interface DexClassRepository {

    int getDexClassesCount();
    DexClass getDexClass(TypeKey typeKey);
    Iterator<DexClass> getDexClasses(Predicate<? super TypeKey> filter);
    Iterator<DexClass> getDexClassesCloned(Predicate<? super TypeKey> filter);
    <T extends SectionItem> Iterator<T> getItems(SectionType<T> sectionType);
    <T extends SectionItem> Iterator<T> getClonedItems(SectionType<T> sectionType);
    <T1 extends SectionItem> Iterator<T1> getItems(SectionType<T1> sectionType, Key key);
    <T1 extends SectionItem> T1 getItem(SectionType<T1> sectionType, Key key);
    <T1 extends SectionItem> int removeEntries(SectionType<T1> sectionType, Predicate<T1> filter);

    default <T extends SectionItem> Iterator<T> getClonedItems(SectionType<T> sectionType, Predicate<? super T> filter) {
        return FilterIterator.of(getClonedItems(sectionType), filter);
    }

    default Iterator<DexClass> findUserClasses(Key key){
        return new UniqueIterator<>(getDexClasses(),
                dexClass -> dexClass.uses(key));
    }
    default Iterator<DexClass> getDexClasses(){
        return getDexClasses(null);
    }
    default Iterator<DexClass> getDexClassesCloned(){
        return getDexClassesCloned(null);
    }
    default Iterator<DexClass> getPackageClasses(String packageName){
        return getPackageClasses(packageName, true);
    }
    default Iterator<DexClass> getPackageClasses(String packageName, boolean includeSubPackages){
        return getDexClasses(key -> key.isPackage(packageName, includeSubPackages));
    }
    default DexMethod getDeclaredMethod(MethodKey methodKey){
        DexClass dexClass = getDexClass(methodKey.getDeclaring());
        if(dexClass != null){
            DexMethod dexMethod = dexClass.getDeclaredMethod(methodKey, false);
            if(dexMethod == null) {
                dexMethod = dexClass.getDeclaredMethod(methodKey, true);
            }
            return dexMethod;
        }
        return null;
    }
    default DexMethod getDeclaredMethod(MethodKey methodKey, boolean ignoreReturnType){
        DexClass dexClass = getDexClass(methodKey.getDeclaring());
        if(dexClass != null){
            return dexClass.getDeclaredMethod(methodKey, ignoreReturnType);
        }
        return null;
    }
    default DexField getDeclaredField(FieldKey fieldKey){
        DexClass dexClass = getDexClass(fieldKey.getDeclaring());
        if(dexClass != null){
            return dexClass.getDeclaredField(fieldKey);
        }
        return null;
    }
    default DexDeclaration getDexDeclaration(Key key){
        if(key instanceof TypeKey){
            return getDexClass((TypeKey) key);
        }
        if(key instanceof MethodKey){
            return getDeclaredMethod((MethodKey) key);
        }
        if(key instanceof FieldKey){
            return getDeclaredField((FieldKey) key);
        }
        return null;
    }
    default Iterator<DexMethod> getDeclaredMethods(){
        return new IterableIterator<DexClass, DexMethod>(getDexClasses()) {
            @Override
            public Iterator<DexMethod> iterator(DexClass dexClass) {
                return dexClass.getDeclaredMethods();
            }
        };
    }
    default Iterator<DexField> getDeclaredFields(){
        return new IterableIterator<DexClass, DexField>(getDexClasses()) {
            @Override
            public Iterator<DexField> iterator(DexClass dexClass) {
                return dexClass.getDeclaredFields();
            }
        };
    }
    default Iterator<IntegerReference> visitIntegers(){
        return new DexIntegerVisitor(this);
    }

    default int removeAnnotations(TypeKey typeKey) {
        return removeEntries(SectionType.ANNOTATION_ITEM,
                annotationItem -> typeKey.equals(annotationItem.getTypeKey()));
    }
    default int removeAnnotationElements(MethodKey methodKey) {
        int removeCount = 0;

        TypeKey typeKey = methodKey.getDeclaring();
        Predicate<AnnotationElement> elementFilter = element -> element.is(methodKey);

        Iterator<AnnotationItem> iterator = getClonedItems(SectionType.ANNOTATION_ITEM);
        while (iterator.hasNext()) {
            AnnotationItem annotationItem = iterator.next();
            if(typeKey.equals(methodKey.getDeclaring())) {
                int count = annotationItem.remove(elementFilter);
                if(count != 0){
                    if(annotationItem.isEmpty()) {
                        annotationItem.removeSelf();
                    }
                    removeCount += count;
                }
            }
        }
        return removeCount;
    }
}
