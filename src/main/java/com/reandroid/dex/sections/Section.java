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
import com.reandroid.dex.pool.DexSectionPool;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Predicate;

public class Section<T extends Block>  extends FixedDexContainer
        implements DexArraySupplier<T>, OffsetSupplier,
        Iterable<T>{

    private final SectionType<T> sectionType;
    private final DexPositionAlign sectionAlign;
    private final SectionArray<T> itemArray;

    private DexSectionPool<T> dexSectionPool;

    Section(SectionType<T> sectionType, SectionArray<T> itemArray){
        super(2);
        this.sectionType = sectionType;
        this.itemArray = itemArray;
        this.sectionAlign = new DexPositionAlign();
        addChild(0, sectionAlign);
        addChild(1, itemArray);
    }
    public Section(IntegerPair countAndOffset, SectionType<T> sectionType){
        this(sectionType, new SectionArray<>(countAndOffset, sectionType.getCreator()));
    }

    public Iterator<T> getWithUsage(int usage){
        if(!hasUsageMarker()){
            return EmptyIterator.of();
        }
        return iterator(item -> ((UsageMarker) item).containsUsage(usage));
    }
    public void clearUsageTypes(){
        if(hasUsageMarker()){
            UsageMarker.clearUsageTypes(iterator());
        }
    }
    public void removeSelf(){
        SectionList sectionList = getSectionList();
        if(sectionList != null){
            sectionList.remove(this);
        }
    }
    void onRemove(SectionList sectionList){
        clear();
        sectionList.getMapList().remove(getSectionType());
        setParent(null);
        setIndex(-1);
    }
    public void clear(){
        clearPool();
        getItemArray().clear();
    }
    public void clearPool(){
        DexSectionPool<T> dexSectionPool = this.getLoadedPool();
        if(dexSectionPool != null){
            dexSectionPool.clear();
            this.dexSectionPool = null;
        }
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        sectionAlign.setAlignment(0);
        super.onReadBytes(reader);
    }

    public boolean contains(Key key){
        return getPool().contains(key);
    }
    public Iterator<T> getAll(Key key) {
        return getPool().getAll(key);
    }
    public T get(Key key) {
        return getPool().get(key);
    }
    boolean keyChanged(Key key){
        DexSectionPool<T> dexSectionPool = this.getLoadedPool();
        if(dexSectionPool != null){
            return dexSectionPool.update(key);
        }
        return false;
    }
    public DexSectionPool<T> getPool(){
        DexSectionPool<T> dexSectionPool = this.dexSectionPool;
        if(dexSectionPool == null){
            dexSectionPool = createPool();
            this.dexSectionPool = dexSectionPool;
            dexSectionPool.load();
        }
        return dexSectionPool;
    }
    public DexSectionPool<T> getLoadedPool(){
        return dexSectionPool;
    }

    DexSectionPool<T> createPool(){
        return new DexSectionPool<>(this);
    }
    public void add(T item){
        getItemArray().add(item);
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
    public SectionArray<T> getItemArray() {
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
    public Iterator<T> iterator(Predicate<? super T> filter) {
        return getItemArray().iterator(filter);
    }
    public Iterator<T> arrayIterator() {
        return getItemArray().arrayIterator();
    }
    void updateNextSection(int position){
        Section<?> next = getNextSection();
        if(next != null){
            next.getOffsetReference().set(position);
        }
    }
    Section<?> getNextSection(){
        SectionList sectionList = getSectionList();
        if(sectionList != null){
            int i = sectionList.indexOf(this);
            if(i >= 0){
                return sectionList.get(i + 1);
            }
        }
        return null;
    }
    Section<?> getPreviousSection(){
        SectionList sectionList = getSectionList();
        if(sectionList != null){
            int i = sectionList.indexOf(this);
            if(i >= 0){
                return sectionList.get(i - 1);
            }
        }
        return null;
    }
    public SectionList getSectionList(){
        return getParent(SectionList.class);
    }
    int compareOffset(Section<?> section){
        if(section == null){
            return 1;
        }
        return Integer.compare(getOffset(), section.getOffset());
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
        clearPool();
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
        DexSectionPool<T> dexSectionPool = this.dexSectionPool;
        if(dexSectionPool != null){
            dexSectionPool.remove(item);
        }
    }
    int getDiffCount(Section<T> section){
        return getCount();
    }
    @Override
    public String toString() {
        return getSectionType() +", offset = " + getOffset()
                + ", count = " + getCount();
    }
}
