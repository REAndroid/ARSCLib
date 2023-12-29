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

import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.sections.SectionType;

import java.util.Iterator;
import java.util.function.Predicate;

public interface DexClassRepository {

    int getDexClassesCount();
    DexClass getDexClass(TypeKey typeKey);
    Iterator<DexClass> getDexClasses(Predicate<? super TypeKey> filter);
    <T extends SectionItem> Iterator<T> getItems(SectionType<T> sectionType);
    <T1 extends SectionItem> Iterator<T1> getItems(SectionType<T1> sectionType, Key key);
    <T1 extends SectionItem> T1 getItem(SectionType<T1> sectionType, Key key);

    default Iterator<DexClass> getDexClasses(){
        return getDexClasses((Predicate<? super TypeKey>)null);
    }
    default DexMethod getDeclaredMethod(MethodKey methodKey){
        DexClass dexClass = getDexClass(methodKey.getDeclaring());
        if(dexClass != null){
            return dexClass.getDeclaredMethod(methodKey);
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

}
