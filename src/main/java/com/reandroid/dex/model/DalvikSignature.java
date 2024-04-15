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

import com.reandroid.dex.data.AnnotationElement;
import com.reandroid.dex.data.AnnotationItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.value.ArrayValue;
import com.reandroid.dex.value.DexValueType;

public class DalvikSignature extends DalvikAnnotation {

    private final AnnotationItem annotationItem;

    public DalvikSignature(DexFile dexFile, AnnotationItem annotationItem) {
        super(dexFile);
        this.annotationItem = annotationItem;
    }

    public int size(){
        ArrayValue arrayValue = getArrayValue();
        if(arrayValue != null){
            return arrayValue.size();
        }
        return 0;
    }
    private ArrayValue getArrayValue(){
        AnnotationElement element = getElement();
        if(element != null){
            return element.getValue(DexValueType.ARRAY);
        }
        return null;
    }

    private AnnotationElement getElement(){
        return annotationItem.getElement("value");
    }

    @Override
    public boolean uses(Key key) {
        AnnotationElement element = getElement();
        if(element != null){
            return element.uses(key);
        }
        return false;
    }

    @Override
    public DexFile getClassRepository() {
        return (DexFile) super.getClassRepository();
    }
}
