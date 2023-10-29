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
package com.reandroid.dex.refactor;

import com.reandroid.dex.data.AnnotationElement;
import com.reandroid.dex.data.AnnotationItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.ArrayCollection;

import java.util.ArrayList;
import java.util.List;

public class RenameInfoAnnotationName extends RenameInfoName<AnnotationItem> {

    public RenameInfoAnnotationName(String typeName, String search, String replace) {
        super(typeName, null, search, replace);
    }

    @Override
    SectionType<AnnotationItem> getSectionType() {
        return SectionType.ANNOTATION_ITEM;
    }
    @Override
    void apply(Iterable<AnnotationItem> group){
        String replace = getReplace();
        for(AnnotationItem annotationItem : group){
            AnnotationElement element = annotationItem.getElement(0);
            element.setName(replace);
        }
    }
    @Override
    public Key getKey(){
        throw new IllegalArgumentException("Not implemented yet!");
    }
    @Override
    List<RenameInfo<?>> createChildRenames() {
        List<RenameInfo<?>> results = new ArrayCollection<>(1);
        RenameInfoMethodName methodName = new RenameInfoMethodName(
                getTypeName(), null, getSearch(), getReplace());
        results.add(methodName);
        return results;
    }
}
