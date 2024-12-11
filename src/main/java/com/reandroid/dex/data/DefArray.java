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
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.common.EditableItem;
import com.reandroid.dex.common.IdUsageIterator;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.Smali;
import com.reandroid.dex.smali.model.SmaliDef;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.IterableIterator;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

public abstract class DefArray<T extends Def<?>> extends CountedBlockList<T> implements
        Iterable<T>, EditableItem, SmaliFormat, IdUsageIterator {

    private ClassId mClassId;

    public DefArray(IntegerReference countReference, Creator<T> creator){
        super(creator, countReference);
    }

    @Override
    public void onPreRemove(T item) {
        AnnotationsDirectory directory = getUniqueAnnotationsDirectory();
        if(directory != null) {
            directory.remove(item);
        }
        resetIndex();
        super.onPreRemove(item);
        item.onRemove();
    }

    public boolean sort() {
        return sort(CompareUtil.getComparableComparator());
    }
    @Override
    public final boolean sort(Comparator<? super T> comparator) {
        if(!needsSort(comparator)){
            sortAnnotations();
            return false;
        }
        Object lock = onPreSort();
        boolean changed = super.sort(comparator);
        onPostSort(lock);
        return changed;
    }
    Object onPreSort() {
        ClassId classId = getClassId();
        if(classId != null){
            classId.getUniqueAnnotationsDirectory();
        }
        linkAnnotation();
        return null;
    }
    void onPostSort(Object lock) {
        resetIndex();
        sortAnnotations();
    }
    void sortAnnotations(){
    }

    @Override
    protected void onRemoveRequestCompleted(Object lock) {
        super.onRemoveRequestCompleted(lock);
        updateCountReference();
    }

    public T getOrCreate(Key key) {
        T item = get(key);
        if(item != null){
            return item;
        }
        item = createNext();
        item.setKey(key);
        return item;
    }
    public T get(Key key) {
        for(T def : this){
            if(key.equals(def.getKey())){
                return def;
            }
        }
        return null;
    }

    @Override
    public T createNext() {
        T item = super.createNext();
        updateCountReference();
        return item;
    }
    public void clear() {
        clearChildes();
    }

    private ClassId searchClassId() {
        ClassId classId = mClassId;
        if (classId != null) {
            return classId;
        }
        Iterator<T> iterator = iterator();
        if (iterator.hasNext()) {
            classId = iterator.next().getClassId();
        }
        return classId;
    }
    public ClassId getClassId() {
        return mClassId;
    }
    public void setClassId(ClassId classId) {
        if(mClassId == classId){
            return;
        }
        this.mClassId = classId;
        if(classId != null){
            linkAnnotation();
        }
    }
    @Override
    protected void onPreRefresh() {
        super.onPreRefresh();
        linkAnnotation();
        if (sort()) {
            resetIndex();
        }
    }

    public ClassData getClassData(){
        return getParentInstance(ClassData.class);
    }

    private void linkAnnotation(){
        int size = size();
        if (size == 0) {
            return;
        }
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if (directory == null) {
            return;
        }
        for (int i = 0; i < size; i++) {
            directory.link(get(i));
        }
    }
    AnnotationsDirectory getAnnotationsDirectory(){
        ClassId classId = searchClassId();
        if(classId == null){
            return null;
        }
        return classId.getAnnotationsDirectory();
    }
    AnnotationsDirectory getUniqueAnnotationsDirectory(){
        ClassId classId = searchClassId();
        if(classId == null){
            return null;
        }
        return classId.getUniqueAnnotationsDirectory();
    }

    private void resetIndex() {
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).resetIndex();
        }
    }
    public void replaceKeys(Key search, Key replace){
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).replaceKeys(search, replace);
        }
    }

    @Override
    public void editInternal(Block user) {
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).editInternal(user);
        }
    }

    @Override
    public Iterator<IdItem> usedIds(){
        return new IterableIterator<Def<?>, IdItem>(iterator()) {
            @Override
            public Iterator<IdItem> iterator(Def<?> element) {
                return element.usedIds();
            }
        };
    }

    @Override
    protected void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
    }

    public void merge(DefArray<T> defArray) {
        int size = defArray.size();
        setSize(size);
        for (int i = 0; i < size; i++) {
            T def = this.get(i);
            T source = defArray.get(i);
            def.merge(source);
            onMerged(def, (T) source);
        }
        updateCountReference();
        linkAnnotation();
    }
    void onMerged(T def, T source) {
    }

    public void fromSmali(Iterator<? extends Smali> iterator){
        while (iterator.hasNext()) {
            fromSmali(iterator.next());
        }
    }
    public T fromSmali(Smali smali){
        T item = createNext();
        item.fromSmali(smali);
        return item;
    }
    public Iterator<SmaliDef> toSmali() {
        return ComputeIterator.of(iterator(), Def::toSmali);
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).append(writer);
            writer.newLine();
        }
    }
}
