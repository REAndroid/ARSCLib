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
import com.reandroid.dex.key.Key;
import com.reandroid.dex.pool.DexIdPool;
import com.reandroid.dex.pool.StringIdPool;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.FilterIterator;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

public class Section<T extends Block>  extends FixedDexContainer
        implements DexArraySupplier<T>, OffsetSupplier,
        Iterable<T>{

    private final SectionType<T> sectionType;
    private final DexPositionAlign sectionAlign;
    private final DexItemArray<T> itemArray;

    private DexIdPool<T> dexIdPool;

    Section(SectionType<T> sectionType, DexItemArray<T> itemArray){
        super(2);
        this.sectionType = sectionType;
        this.itemArray = itemArray;
        this.sectionAlign = new DexPositionAlign();
        addChild(0, sectionAlign);
        addChild(1, itemArray);
    }
    public Section(IntegerPair countAndOffset, SectionType<T> sectionType){
        this(sectionType, new DexItemArray<>(countAndOffset, sectionType.getCreator()));
    }

    public Iterator<T> getWithUsage(int usage){
        if(!hasUsageMarker()){
            return EmptyIterator.of();
        }
        return FilterIterator.of(iterator(), item -> ((UsageMarker) item).containsUsage(usage));
    }
    public void clearUsageTypes(){
        if(hasUsageMarker()){
            UsageMarker.clearUsageTypes(iterator());
        }
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        sectionAlign.setAlignment(0);
        super.onReadBytes(reader);
    }

    public T get(Key key) {
        return getPool().get(key);
    }
    public DexIdPool<T> getPool(){
        DexIdPool<T> dexIdPool = this.dexIdPool;
        if(dexIdPool == null){
            dexIdPool = createPool();
            this.dexIdPool = dexIdPool;
            dexIdPool.load();
        }
        return dexIdPool;
    }
    public boolean isPoolLoaded(){
        return dexIdPool != null;
    }
    @SuppressWarnings("unchecked")
    private DexIdPool<T> createPool(){
        return new DexIdPool<>(this);
    }
    public void add(T item){
        itemArray.add(item);
    }

    public SectionType<T> getSectionType() {
        return sectionType;
    }

    @Override
    public T get(int i){
        return null;
    }
    public T[] get(int[] indexes){
        return null;
    }
    public T getOrCreate(Key key) {
        return getPool().getOrCreate(key);
    }
    public T createItem() {
        return getItemArray().createNext();
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
    void updateNextSection(int position){
        Section<?> next = getNextSection();
        if(next != null){
            next.getOffsetReference().set(position);
        }
    }
    Section<?> getNextSection(){
        SectionList sectionList = getParentInstance(SectionList.class);
        if(sectionList != null){
            int i = sectionList.indexOf(this);
            if(i >= 0){
                return sectionList.get(i + 1);
            }
        }
        return null;
    }
    Section<?> getPreviousSection(){
        SectionList sectionList = getParentInstance(SectionList.class);
        if(sectionList != null){
            int i = sectionList.indexOf(this);
            if(i >= 0){
                return sectionList.get(i - 1);
            }
        }
        return null;
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
        alignSection(sectionAlign, position);
        position += sectionAlign.size();
        getOffsetReference().set(position);
        onRefreshed(position);
    }
    void alignSection(DexPositionAlign positionAlign, int position){
        if(isPositionAlignedItem()){
            positionAlign.setAlignment(4);
            positionAlign.align(position);
        }
    }
    private boolean isPositionAlignedItem(){
        return getItemArray().get(0) instanceof PositionAlignedItem;
    }
    void onRefreshed(int position){
    }
    public boolean hasUsageMarker() {
        return get(0) instanceof UsageMarker;
    }
    void onRemoving(T item){
        DexIdPool<T> dexIdPool = this.dexIdPool;
        if(dexIdPool != null){
            dexIdPool.onRemoving(item);
        }
    }
    @Override
    public String toString() {
        return getSectionType() +", offset = " + getOffset()
                + ", count = " + getCount();
    }
}
