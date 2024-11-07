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
package com.reandroid.dex.data;

import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.ArrayKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.IterableIterator;

import java.util.Iterator;

public class AnnotationGroup extends IntegerDataItemList<AnnotationSet> implements KeyReference {

    public AnnotationGroup() {
        super(SectionType.ANNOTATION_SET, UsageMarker.USAGE_ANNOTATION, null);
    }

    @Override
    public ArrayKey getKey() {
        return (ArrayKey) checkKey(super.getKey());
    }
    @Override
    public void setKey(Key key){
        super.setKey(key);
    }
    @Override
    public SectionType<AnnotationGroup> getSectionType() {
        return SectionType.ANNOTATION_GROUP;
    }
    @Override
    void removeNulls() {
    }
    public void replaceKeys(Key search, Key replace){
        for(AnnotationSet annotationSet : this){
            annotationSet.replaceKeys(search, replace);
        }
    }
    public Iterator<IdItem> usedIds(){
        return new IterableIterator<AnnotationSet, IdItem>(iterator()) {
            @Override
            public Iterator<IdItem> iterator(AnnotationSet element) {
                return element.usedIds();
            }
        };
    }

    public void merge(AnnotationGroup annotationGroup) {
        int size = annotationGroup.size();
        for (int i = 0; i < size; i++) {
            addNewItem(annotationGroup.getItemKey(i));
        }
    }

    @Override
    public String toString() {
        return getKey().toString("\n");
    }
}
