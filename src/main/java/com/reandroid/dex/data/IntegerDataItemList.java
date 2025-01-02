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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.CountedBlockList;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.dex.base.DexPositionAlign;
import com.reandroid.dex.key.ArrayKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyList;
import com.reandroid.dex.reference.IntegerDataReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.ComputeIterator;

import java.util.Comparator;
import java.util.Iterator;


public class IntegerDataItemList<T extends DataItem> extends DataItem implements Iterable<T> {

    private final CountedBlockList<IntegerDataReference<T>> referenceList;
    private final DexPositionAlign positionAlign;

    public IntegerDataItemList(SectionType<T> sectionType, int usageType, DexPositionAlign positionAlign) {
        super(3);

        this.positionAlign = positionAlign;

        IntegerItem countReference = new IntegerItem();
        this.referenceList = new CountedBlockList<>(
                new ReferenceCreator<>(sectionType, usageType), countReference);

        addChildBlock(0, countReference);
        addChildBlock(1, referenceList);
        addChildBlock(2, positionAlign);
    }

    @Override
    public KeyList<?> getKey() {
        Key[] elements = new Key[size()];
        getItemKeys(elements);
        return ArrayKey.create(elements);
    }
    public void setKey(Key key) {
        KeyList<?> keyList = (KeyList<?>) key;
        int size = keyList.size();
        setSize(size);
        for (int i = 0; i < size; i++) {
            getReference(i).setKey(keyList.get(i));
        }
    }
    public int size() {
        return referenceList.size();
    }
    public void setSize(int size) {
        referenceList.setSize(size);
    }
    public void clear() {
        setSize(0);
    }
    private void ensureSize(int size) {
        referenceList.ensureSize(size);
    }
    private IntegerDataReference<T> createNext() {
        return referenceList.createNext();
    }
    private IntegerDataReference<T> getReference(int i) {
        return referenceList.get(i);
    }
    private IntegerDataReference<T> getOrCreateReference(int i) {
        ensureSize(i + 1);
        return referenceList.get(i);
    }
    public T addNewItem(Key key) {
        IntegerDataReference<T> item = createNext();
        item.setKey(key);
        return item.getItem();
    }
    public void addNewItem(T item) {
        IntegerDataReference<T> reference = createNext();
        reference.setItem(item);
    }
    public T addNewItem() {
        return createNext().getOrCreate();
    }
    public T getOrCreateAt(int index) {
        return getOrCreateReference(index).getOrCreate();
    }
    public Key getItemKey(int i) {
        IntegerDataReference<T> reference = getReference(i);
        if (reference != null) {
            return reference.getKey();
        }
        return null;
    }
    public T setItemKeyAt(int index, Key key) {
        IntegerDataReference<T> reference = getOrCreateReference(index);
        reference.setKey(key);
        return reference.getItem();
    }
    public void clearAt(int index) {
        IntegerDataReference<T> reference = getReference(index);
        if (reference != null) {
            reference.setItem(null);
        }
    }
    void getItemKeys(Key[] out) {
        int length = out.length;
        for (int i = 0; i < length; i++) {
            out[i] = getItemKey(i);
        }
    }

    @Override
    public void removeSelf() {
        clear();
        super.removeSelf();
    }

    public void remove(T item) {
        removeIf(t -> t == item);
    }
    public boolean removeIf(org.apache.commons.collections4.Predicate<? super T> filter) {
        return referenceList.removeIf(reference -> filter.evaluate(reference.getItem()));
    }
    void removeNulls() {
        referenceList.removeIf(reference -> reference.getItem() == null);
    }
    @Override
    public Iterator<T> iterator() {
        return ComputeIterator.of(referenceList.iterator(), IntegerDataReference::getItem);
    }
    public T getItem(int i){
        IntegerDataReference<T> reference = getReference(i);
        if (reference != null) {
            return reference.getItem();
        }
        return null;
    }
    public boolean isEmpty() {
        return !iterator().hasNext();
    }
    public boolean sort(Comparator<? super T> comparator) {
        removeNulls();
        return referenceList.sort((ref1, ref2) -> comparator.compare(ref1.getItem(), ref2.getItem()));
    }

    @Override
    protected void onPreRefresh() {
        super.onPreRefresh();
        removeNulls();
    }
    @Override
    public void editInternal(Block user) {
        user = this;
        int size = size();
        for (int i = 0; i < size; i++) {
            getReference(i).editInternal(user);
        }
    }
    public DexPositionAlign getPositionAlign() {
        return positionAlign;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        int size = size();
        for(int i = 0; i < size; i++){
            hash = hash * 31 + ObjectsUtil.hash(getItem(i));
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this){
            return true;
        }
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }
        IntegerDataItemList<?> itemList = (IntegerDataItemList<?>)obj;
        int size = size();
        if(size != itemList.size()){
            return false;
        }
        for(int i = 0; i < size; i++) {
            if(!ObjectsUtil.equals(getItem(i), itemList.getItem(i))){
                return false;
            }
        }
        return true;
    }

    static class ReferenceCreator<T extends DataItem> implements Creator<IntegerDataReference<T>> {

        private final SectionType<T> sectionType;
        private final int usageType;

        ReferenceCreator(SectionType<T> sectionType, int usageType) {
            this.sectionType = sectionType;
            this.usageType = usageType;
        }

        @SuppressWarnings("unchecked")
        @Override
        public IntegerDataReference<T>[] newArrayInstance(int length) {
            return new IntegerDataReference[length];
        }

        @Override
        public IntegerDataReference<T> newInstance() {
            return new IntegerDataReference<>(sectionType, usageType);
        }
    }
}
