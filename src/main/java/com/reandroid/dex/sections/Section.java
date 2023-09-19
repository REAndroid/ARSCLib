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
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.*;
import com.reandroid.dex.pool.DexIdPool;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Section<T extends Block>  extends FixedDexContainer
        implements DexArraySupplier<T>, OffsetSupplier,
        Iterable<T>, PreloadArray<T>{

    private final SectionType<T> sectionType;
    private final DexItemArray<T> itemArray;
    private final DexPositionAlign sectionAlign;
    private final Map<Integer, T> offsetMap;

    private DexIdPool<T> dexIdPool;

    Section(SectionType<T> sectionType, DexItemArray<T> itemArray){
        super(2);
        this.sectionType = sectionType;
        this.itemArray = itemArray;
        this.sectionAlign = new DexPositionAlign();
        this.offsetMap = new HashMap<>();
        addChild(0, sectionAlign);
        addChild(1, itemArray);
        itemArray.setPreloadArray(this);
    }
    public Section(IntegerPair countAndOffset, SectionType<T> sectionType){
        this(sectionType, new DexItemArray<>(countAndOffset, sectionType.getCreator()));
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int align = sectionAlign.getAlignment();
        sectionAlign.setAlignment(0);
        super.onReadBytes(reader);
        sectionAlign.setAlignment(align);
    }

    public DexIdPool<T> getPool(){
        DexIdPool<T> dexIdPool = this.dexIdPool;
        if(dexIdPool == null){
            dexIdPool = new DexIdPool<>(this);
            this.dexIdPool = dexIdPool;
            dexIdPool.load();
        }
        return dexIdPool;
    }
    public void add(T item){
        itemArray.add(item);
    }

    public void buildOffsetMap(){
        offsetMap.clear();
        for (T item : this) {
            if (!(item instanceof OffsetSupplier)) {
                return;
            }
            int offset = ((OffsetSupplier) item).getOffsetReference().get();
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
    public DexItemArray<T> getItemArray() {
        return itemArray;
    }
    public void sort() throws ClassCastException {
        sort(CompareUtil.getComparatorUnchecked());
    }
    public void sort(Comparator<? super T> comparator){
        getItemArray().sort(comparator);
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
    private void updateItemOffsets(int position){
        DexItemArray<T> array = getItemArray();
        int count = array.getCount();
        array.getCountAndOffset().getFirst().set(count);
        DexPositionAlign previous = null;
        for(int i = 0; i < count; i++){
            T item = array.get(i);
            if(item == null) {
                previous = null;
                continue;
            }
            DexPositionAlign itemAlign = null;
            if(item instanceof PositionAlignedItem){
                itemAlign = ((PositionAlignedItem) item).getPositionAlign();
                itemAlign.setSize(0);
                if(previous != null){
                    previous.align(position);
                    position += previous.size();
                }
            }
            if(item instanceof PositionedItem){
                ((PositionedItem) item).setPosition(position);
            }else {
                IntegerReference supplier = ((OffsetSupplier) item).getOffsetReference();
                supplier.set(position);
            }
            position += item.countBytes();
            previous = itemAlign;
        }
        updateNextSection(position);
        buildOffsetMap();
    }
    private boolean updateIdOffsets(int position){
        Section<?> idSection = getSection(getSectionType().getIdSectionType());
        if(idSection == null){
            return false;
        }
        int count = getCount();
        DexItemArray<?> idArray = idSection.getItemArray();
        idArray.setChildesCount(count);
        for(int i = 0; i < count; i++){
            T item = get(i);
            IntegerReference reference = (IntegerReference) idArray.get(i);
            reference.set(position);
            position += item.countBytes();
        }
        updateNextSection(position);
        return true;
    }
    private void updateNextSection(int position){
        Section<?> next = getNextSection();
        if(next != null){
            next.getOffsetReference().set(position);
        }
    }
    private Section<?> getNextSection(){
        SectionList sectionList = getParentInstance(SectionList.class);
        if(sectionList != null){
            return sectionList.get(getIndex() + 1);
        }
        return null;
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
    protected boolean isValidOffset(int offset){
        if(offset == 0){
            return getSectionType() == SectionType.HEADER;
        }
        return offset > 0;
    }

    @Override
    protected void onRefreshed(){
        int position = getOffset();
        sectionAlign.align(position);
        position += sectionAlign.size();
        getOffsetReference().set(position);
        boolean hasId = updateIdOffsets(position);
        if(!hasId){
            if(sectionType.isOffsetType()){
                updateItemOffsets(position);
            }else {
                position += getItemArray().countBytes();
                updateNextSection(position);
            }
        }
    }
    @Override
    public String toString() {
        return getSectionType() +", offset = " + getOffset()
                + ", count = " + getCount();
    }
}
