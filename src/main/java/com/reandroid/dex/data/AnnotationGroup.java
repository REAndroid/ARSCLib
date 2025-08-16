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
import com.reandroid.dex.key.*;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.IterableIterator;

import java.util.Iterator;

public class AnnotationGroup extends AnnotationsList<AnnotationSet> implements KeyReference {

    public AnnotationGroup() {
        super(SectionType.ANNOTATION_SET, UsageMarker.USAGE_ANNOTATION, null);
    }

    @Override
    public AnnotationGroupKey getKey() {
        AnnotationGroupKey groupKey;
        int size = size();
        if (size == 0) {
            groupKey = AnnotationGroupKey.empty();
        } else {
            AnnotationSetKey[] elements = new AnnotationSetKey[size];
            getItemKeys(elements);
            groupKey = AnnotationGroupKey.of(elements);
        }
        return checkKey(groupKey);
    }

    @Override
    public void setKey(Key key) {
        super.setKey(key);
    }

    @Override
    public AnnotationSetKey getItemKey(int i) {
        return (AnnotationSetKey) super.getItemKey(i);
    }

    @Override
    public AnnotationSet setItemKeyAt(int index, Key key) {
        if (key != null && ((AnnotationSetKey) key).isEmpty()) {
            key = null;
        }
        return super.setItemKeyAt(index, key);
    }

    @Override
    public boolean isBlank() {
        int size = size();
        for (int i = 0; i < size; i++) {
            AnnotationSet item = getItem(i);
            if (item != null && !item.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    public boolean isEmptyAt(int i) {
        AnnotationSet item = getItem(i);
        return item == null || item.isEmpty();
    }

    @Override
    public SectionType<AnnotationGroup> getSectionType() {
        return SectionType.ANNOTATION_GROUP;
    }
    @Override
    void removeNulls() {
    }
    public void replaceKeys(Key search, Key replace) {
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
    protected boolean elementsAreEqual(AnnotationSet t1, AnnotationSet t2) {
        if (t1 == null) {
            return t2 == null || t2.isEmpty();
        }
        if (t2 == null) {
            return t1.isEmpty();
        }
        return t1.equals(t2);
    }

    @Override
    public String toString() {
        return size() + " [" + StringsUtil.join(iterator(), ", ") + "]";
    }
}
