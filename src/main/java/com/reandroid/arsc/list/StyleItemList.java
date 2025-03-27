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
package com.reandroid.arsc.list;

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.StringItem;
import com.reandroid.arsc.item.StyleItem;
import com.reandroid.arsc.pool.StringPool;
import com.reandroid.utils.CompareUtil;

import java.util.Comparator;
import java.util.Iterator;

public class StyleItemList extends OffsetBlockList<StyleItem> {

    private boolean stringsLinked;

    public StyleItemList(IntegerReference start, OffsetReferenceList<?> offsetReferenceList) {
        super(start, offsetReferenceList, StyleItem.CREATOR);
    }

    public boolean sort() {
        return sort(CompareUtil.getComparableComparator());
    }
    @Override
    public boolean sort(Comparator<? super StyleItem> comparator) {
        if (!isSortingAllowed()) {
            return false;
        }
        boolean sorted = super.sort(comparator);
        if (adjustIndexes()) {
            if (super.sort(comparator)) {
                sorted = true;
            }
        }
        trimLastIf(StyleItem::isEmpty);
        return sorted;
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        sort();
    }

    @Override
    public void onPreRemove(StyleItem item) {
        item.onRemoved();
        super.onPreRemove(item);
    }

    public void linkStyleStringsInternal() {
        if (!stringsLinked) {
            stringsLinked = true;
            int size = size();
            for (int i = 0; i < size; i++) {
                get(i).linkStringsInternal();
            }
        }
    }
    private boolean isSortingAllowed() {
        StringPool<?> stringPool = getParentInstance(StringPool.class);
        if (stringPool != null) {
            return !stringPool.getStringsArray().isSortRequired();
        }
        return false;
    }

    private boolean adjustIndexes() {
        Iterator<StyleItem> iterator = clonedIterator();
        boolean adjusted = false;
        while (iterator.hasNext()) {
            StyleItem styleItem = iterator.next();
            StringItem stringItem = styleItem.getStringItemInternal();
            if (stringItem != null) {
                int index = stringItem.getIndex();
                if (index != styleItem.getIndex()) {
                    moveTo(styleItem, index);
                    adjusted = true;
                }
            }
        }
        if (adjusted) {
            getParentInstance(StringPool.class).linkStylesInternal();
        }
        return adjusted;
    }
}
