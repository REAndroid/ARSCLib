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

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.DexPositionAlign;
import com.reandroid.dex.base.IntegerPair;
import com.reandroid.dex.base.PositionAlignedItem;
import com.reandroid.dex.data.DataItem;
import com.reandroid.dex.pool.DataSectionPool;


public class DataSection<T extends DataItem> extends Section<T> {

    public DataSection(IntegerPair countAndOffset, SectionType<T> sectionType) {
        super(sectionType, new DataSectionArray<>(countAndOffset, sectionType.getCreator()));
    }
    DataSection(SectionType<T> sectionType, DataSectionArray<T> itemArray){
        super(sectionType, itemArray);
    }

    @Override
    public T get(int offset){
        return getItemArray().getAt(offset);
    }
    @Override
    public T[] get(int[] offsets){
        return getItemArray().getAt(offsets);
    }
    public T createItem() {
        int position = estimateLastOffset();
        T item = getItemArray().createNext();
        item.setPosition(position);
        return item;
    }

    @Override
    public DataSectionPool<T> getPool() {
        return (DataSectionPool<T>) super.getPool();
    }
    @Override
    DataSectionPool<T> createPool(){
        return new DataSectionPool<>(this);
    }
    @Override
    public DataSectionArray<T> getItemArray() {
        return (DataSectionArray<T>) super.getItemArray();
    }

    private int estimateLastOffset() {
        int offset;
        T last = getItemArray().getLast();
        if(last != null) {
            IntegerReference supplier = last.getOffsetReference();
            offset = supplier.get();
            offset += last.countBytes();
        }else {
            offset = getOffset() + countBytes();
            if(offset == 0){
                offset = estimateMainOffset();
            }
        }
        return offset;
    }
    private int estimateMainOffset(){
        int offset = getOffset();
        if(offset != 0){
            return offset;
        }
        Section<?> section = getNextSection();
        if(section == null){
            section = getPreviousSection();
            if(section != null){
                offset = section.countBytes();
            }
        }
        if(section != null){
            offset += section.getOffset();
        }
        getOffsetReference().set(offset);
        return offset;
    }
    @Override
    void onRefreshed(int position){
        updateItemOffsets(position);
    }
    @Override
    void alignSection(DexPositionAlign positionAlign, int position){
        if(isPositionAlignedItem()){
            positionAlign.setAlignment(4);
            positionAlign.align(position);
        }
    }
    private boolean isPositionAlignedItem(){
        return getItemArray().get(0) instanceof PositionAlignedItem;
    }

    private void updateItemOffsets(int position){
        DataSectionArray<T> array = getItemArray();
        position = array.updateItemOffsets(position);
        updateNextSection(position);
    }
    @Override
    int getDiffCount(Section<T> section){
        return getCount();
    }
}
