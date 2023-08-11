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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.OffsetSupplier;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Section<T extends Block>  extends FixedDexContainer
        implements DexArraySupplier<T>, OffsetSupplier,
        Iterable<T>, PreloadArray<T>{

    private final SectionType<T> sectionType;
    private final DexItemArray<T> itemArray;
    private final DexAlign sectionAlign;
    private final Map<Integer, T> offsetMap;

    public Section(SectionType<T> sectionType, DexItemArray<T> itemArray){
        super(2);
        this.sectionType = sectionType;
        this.itemArray = itemArray;
        this.sectionAlign = new DexAlign();
        this.offsetMap = new HashMap<>();
        addChild(0, itemArray);
        addChild(1, sectionAlign);
        itemArray.setPreloadArray(this);
    }
    public Section(IntegerPair countAndOffset, SectionType<T> sectionType){
        this(sectionType, new DexItemArray<>(countAndOffset, sectionType.getCreator()));
    }
    public void add(T item){
        itemArray.add(item);
    }
    public void buildOffsetMap(){
        offsetMap.clear();
        Iterator<T> iterator = iterator();
        while (iterator.hasNext()){
            T item = iterator.next();
            if(!(item instanceof OffsetSupplier)){
                return;
            }
            int offset = ((OffsetSupplier)item).getOffsetReference().get();
            offsetMap.put(offset, item);
        }
    }

    public SectionType<T> getSectionType() {
        return sectionType;
    }

    @Override
    public T get(int i){
        return getItemArray().get(i);
    }
    public T[] get(int[] indexes){
        if(indexes == null || indexes.length == 0){
            return null;
        }
        DexItemArray<T> itemArray = getItemArray();
        int length = indexes.length;
        T[] results = itemArray.newInstance(indexes.length);
        for(int i = 0; i < length; i++){
            results[i] = itemArray.get(indexes[i]);
        }
        return results;
    }
    public T getAt(int offset){
        return offsetMap.get(offset);
    }
    public T[] getAt(int[] offsets){
        if(offsets == null || offsets.length == 0){
            return null;
        }
        Map<Integer, T> offsetMap = this.offsetMap;
        int length = offsets.length;
        T[] results = getItemArray().newInstance(offsets.length);
        for(int i = 0; i < length; i++){
            results[i] = offsetMap.get(offsets[i]);
        }
        return results;
    }
    @Override
    public int getCount(){
        return getItemArray().getCount();
    }
    public int getOffset(){
        return getOffsetReference().get();
    }
    @Override
    public IntegerReference getOffsetReference(){
        return getItemArray().getOffsetReference();
    }
    DexItemArray<T> getItemArray() {
        return itemArray;
    }

    @Override
    public Iterator<T> iterator() {
        return getItemArray().iterator();
    }
    @Override
    public void onPreload(T[] elements) {
        Section<?> idSection = getSection(getSectionType().getIdSectionType());
        if(idSection == null){
            return;
        }
        for(int i = 0; i < elements.length; i++){
            IntegerReference reference = (IntegerReference) idSection.get(i);
            OffsetReceiver receiver = (OffsetReceiver) elements[i];
            receiver.setOffsetReference(reference);
        }

    }
    private Section<?> getSection(SectionType<?> sectionType){
        if(sectionType == null){
            return null;
        }
        SectionList sectionList = getParentInstance(SectionList.class);
        if(sectionList == null){
            return null;
        }
        return sectionList.get(sectionType);
    }
    @Override
    protected void onPreRefreshRefresh(){
        sectionAlign.setSize(0);
    }
    @Override
    protected void onRefreshed(){
        sectionAlign.align(this);
    }
    @Override
    public String toString() {
        return getSectionType() +", offset = " + getOffset()
                + ", count = " + getCount();
    }
}
