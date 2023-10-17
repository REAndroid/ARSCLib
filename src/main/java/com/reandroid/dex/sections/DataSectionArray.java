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

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.DexPositionAlign;
import com.reandroid.dex.base.IntegerPair;
import com.reandroid.dex.base.PositionAlignedItem;
import com.reandroid.dex.data.DataItem;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DataSectionArray<T extends DataItem> extends SectionArray<T> {

    private Map<Integer, T> offsetMap = new HashMap<>();

    public DataSectionArray(IntegerPair countAndOffset, Creator<T> creator) {
        super(countAndOffset, creator);
    }


    @Override
    public void onPreRemove(T item) {
        super.onPreRemove(item);
        unRegisterOffset(item);
    }

    public T getAt(int offset){
        T item = offsetMap.get(offset);
        if(item != null && item.getParent() == null){
            offsetMap.remove(offset);
            item = null;
        }
        return item;
    }
    public T[] getAt(int[] offsets){
        if(offsets == null || offsets.length == 0){
            return null;
        }
        int length = offsets.length;
        T[] results = newInstance(offsets.length);
        for(int i = 0; i < length; i++){
            results[i] = getAt(offsets[i]);
        }
        return results;
    }

    @Override
    protected void readChildes(BlockReader reader) throws IOException {
        T[] childes = getChildes();
        clearOffsetMap();
        int length = childes.length;
        for(int i = 0; i < length; i++){
            T item = childes[i];
            int offset = reader.getPosition();
            setReadPosition(item, offset);
            registerOffset(item, offset);
            item.onReadBytes(reader);
        }
    }
    void setReadPosition(T item, int offset){
        item.setPosition(offset);
    }

    private void unRegisterOffset(T item){
        if (item == null) {
            return;
        }
        Integer offset = item.getOffset();
        T exist = this.offsetMap.get(offset);
        if(exist == item){
            this.offsetMap.remove(offset);
        }
    }
    void registerOffset(T item){
        if (item == null) {
            return;
        }
        int offset = item.getOffset();
        registerOffset(item, offset);
    }
    private void registerOffset(T item, int offset){
        offsetMap.put(offset, item);
    }
    private void clearOffsetMap(){
        offsetMap = new HashMap<>(getCount());
    }
    int updateItemOffsets(int position){
        int count = this.getCount();
        this.getCountAndOffset().getFirst().set(count);
        DexPositionAlign previous = null;
        for(int i = 0; i < count; i++){
            T item = this.get(i);
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
            if(i == count-1){
                item.removeLastAlign();
            }
            item.setPosition(position);
            registerOffset(item, position);
            position += item.countBytes();
            previous = itemAlign;
        }
        return position;
    }
}
