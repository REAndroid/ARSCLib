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
package com.reandroid.dex.sections;

import com.reandroid.dex.base.IntegerPair;
import com.reandroid.dex.data.AnnotationSet;
import com.reandroid.dex.key.AnnotationSetKey;
import com.reandroid.dex.pool.DexSectionPool;

public class AnnotationSetSection extends DataSection<AnnotationSet> {

    public AnnotationSetSection(IntegerPair countAndOffset, SectionType<AnnotationSet> sectionType) {
        super(countAndOffset, sectionType);
    }

    @Override
    public boolean sort() {
        AnnotationSet annotationSet = getEmptySet();
        if (annotationSet != null && annotationSet.getIndex() != 0) {
            getItemArray().moveTo(annotationSet, 0);
            return true;
        }
        return false;
    }

    private AnnotationSet getEmptySet() {
        DataSectionArray<AnnotationSet> array = getItemArray();
        AnnotationSet annotationSet = array.getFirst();
        if (annotationSet != null && annotationSet.isEmpty()) {
            return annotationSet;
        }
        DexSectionPool<AnnotationSet> pool = getLoadedPool();
        if (pool != null) {
            return pool.get(AnnotationSetKey.empty());
        }
        int size = array.size();
        for (int i = 0; i < size; i++) {
            annotationSet = array.get(i);
            if (annotationSet.isEmpty()) {
                return annotationSet;
            }
        }
        return null;
    }
    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        ensureNotEmpty();
    }
    private void ensureNotEmpty() {
        if (!isEmpty()) {
            return;
        }
        add(new AnnotationSet.EmptyAnnotationSet());
    }
}
