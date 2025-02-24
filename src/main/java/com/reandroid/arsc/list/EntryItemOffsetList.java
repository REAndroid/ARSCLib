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

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.header.TypeHeader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.OffsetItem;
import com.reandroid.arsc.value.Entry;
import com.reandroid.utils.collection.ArrayCollection;

import java.util.List;

public class EntryItemOffsetList extends OffsetReferenceList<OffsetItem> {

    public EntryItemOffsetList(IntegerReference countReference) {
        super(countReference, OffsetItem.CREATOR_OFFSET32);
    }

    public int getHighestIdx() {
        int result = -1;
        int size = size();
        for (int i = 0; i < size; i++) {
            int idx = get(i).getIdx();
            if (idx > result) {
                result = idx;
            }
        }
        return result;
    }
    public int indexOfIdx(int idx) {
        int result = -1;
        int size = size();
        for (int i = 0; i < size; i++) {
            if (idx == get(i).getIdx()) {
                return i;
            }
        }
        return result;
    }
    public int findSortPoint(int idx) {
        int size = size();
        for (int i = 0; i < size; i++) {
            if (idx > get(i).getIdx()) {
                return i;
            }
        }
        return size;
    }
    public boolean isSparse() {
        return getCreator() == OffsetItem.CREATOR_SPARSE;
    }
    public int getOffsetType() {
        Creator<? extends OffsetItem> creator = getCreator();
        if (creator == OffsetItem.CREATOR_OFFSET16) {
            return TypeHeader.OFFSET_16;
        }
        if (creator == OffsetItem.CREATOR_SPARSE) {
            return TypeHeader.OFFSET_SPARSE;
        }
        return TypeHeader.OFFSET_32;
    }
    public boolean setOffsetType(int offsetType, EntryItemList entryItemList) {
        if (offsetType != getOffsetType()) {
            if (offsetType == TypeHeader.OFFSET_SPARSE) {
                changeToSparse(entryItemList);
            } else if (isSparse()) {
                if (offsetType == TypeHeader.OFFSET_16) {
                    changeFromSparse(entryItemList, OffsetItem.CREATOR_OFFSET16);
                } else {
                    changeFromSparse(entryItemList, OffsetItem.CREATOR_OFFSET32);
                }
            } else {
                if (offsetType == TypeHeader.OFFSET_16) {
                    setCreator(OffsetItem.CREATOR_OFFSET16);
                } else {
                    setCreator(OffsetItem.CREATOR_OFFSET32);
                }
                if (!entryItemList.isEmpty()) {
                    clear();
                }
            }
            return !entryItemList.isEmpty();
        }
        return false;
    }
    private void changeFromSparse(EntryItemList entryItemList, Creator<? extends OffsetItem> creator) {
        if (entryItemList.isEmpty()) {
            this.setCreator(creator);
        } else {
            entryItemList.sort();
            List<Integer> idList = listIds(entryItemList);
            int size = idList.size();
            int largest = idList.get(size - 1);
            this.clear();
            this.setCreator(creator);
            int index = 0;
            for (int i = 0; i < size; i++) {
                int id = idList.get(i);
                for (int j = index + 1; j < id; j++) {
                    entryItemList.createAt(j);
                }
                index = id;
            }
            entryItemList.setSize(largest + 1);
            this.setSize(entryItemList.size());
        }
    }
    private void changeToSparse(EntryItemList entryItemList) {
        if (entryItemList.isEmpty()) {
            this.setCreator(OffsetItem.CREATOR_SPARSE);
        } else {
            List<Integer> idList = listIds(entryItemList);
            int size = idList.size();
            this.clear();
            this.setCreator(OffsetItem.CREATOR_SPARSE);
            setSize(size);
            for (int i = 0; i < size; i++) {
                Integer id = idList.get(i);
                OffsetItem offsetItem = get(i);
                offsetItem.setIdx(id);
            }
            entryItemList.removeIf(Entry::isNull);
        }
    }
    private List<Integer> listIds(EntryItemList entryItemList) {
        int size = entryItemList.size();
        List<Integer> list = new ArrayCollection<>(size);
        for (int i = 0; i < size; i ++) {
            Entry entry = entryItemList.get(i);
            if (!entry.isNull()) {
                list.add(entry.getId());
            }
        }
        return list;
    }
}
