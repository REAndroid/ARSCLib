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
package com.reandroid.dex.ins;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.CountedArray;
import com.reandroid.dex.base.DexBlockAlign;
import com.reandroid.dex.base.DexPositionAlign;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.item.DexContainerItem;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.ExpandIterator;

import java.io.IOException;
import java.util.Iterator;

public class TryBlock extends DexContainerItem implements
        Creator<TryItem>, Iterable<TryItem>, LabelList {
    private final IntegerReference totalCount;

    private HandlerOffsetArray handlerOffsetArray;
    private Ule128Item tryItemsCount;
    private CountedArray<TryItem> tryItemArray;
    private DexBlockAlign positionAlign;

    public TryBlock(IntegerReference totalCount) {
        super(4);
        this.totalCount = totalCount;
    }

    @Override
    public Iterator<? extends Label> getLabels() {
        return new ExpandIterator<>(iterator());
    }

    @Override
    public Iterator<TryItem> iterator(){
        if(isNull()){
            return EmptyIterator.of();
        }
        return tryItemArray.iterator();
    }

    private HandlerOffsetArray getHandlersOffset() {
        if(handlerOffsetArray == null){
            handlerOffsetArray = new HandlerOffsetArray(totalCount);
            addChild(0, handlerOffsetArray);
        }
        return handlerOffsetArray;
    }
    private void initTryItemArray(){
        if(tryItemArray != null){
            return;
        }
        tryItemsCount = new Ule128Item();
        addChild(1, tryItemsCount);
        tryItemArray = new CountedArray<>(totalCount, this);
        addChild(2, tryItemArray);
    }
    @Override
    public void setNull(boolean is_null){
        if(is_null == this.isNull()){
            return;
        }
        if(is_null){
            clear();
        }else {
            initialize();
        }
    }
    private void initialize(){
        getHandlersOffset();
        initTryItemArray();
        if(positionAlign == null){
            positionAlign = new DexBlockAlign(this);
            addChild(3, positionAlign);
        }
    }
    private void clear(){
        if(handlerOffsetArray != null){
            handlerOffsetArray.setParent(null);
            handlerOffsetArray.setIndex(-1);
            handlerOffsetArray = null;
            addChild(0, null);
        }
        if(tryItemsCount != null){
            tryItemsCount.setParent(null);
            tryItemsCount.setIndex(-1);
            tryItemArray = null;
            addChild(1, null);
        }
        if(tryItemArray != null){
            tryItemArray.clearChildes();
            tryItemArray.setParent(null);
            tryItemArray.setIndex(-1);
            tryItemArray = null;
            addChild(2, null);
        }
        if(positionAlign != null){
            positionAlign.setParent(null);
            positionAlign.setIndex(-1);
            positionAlign = null;
            addChild(3, null);
        }
    }
    @Override
    public boolean isNull(){
        return tryItemArray == null;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        boolean is_null = totalCount.get() == 0;
        setNull(is_null);
        if(!is_null){
            super.onReadBytes(reader);
        }
    }

    @Override
    public TryItem[] newInstance(int length) {
        HandlerOffsetArray offsetArray = getHandlersOffset();
        int count = offsetArray.size();
        TryItem[] results = new TryItem[length];
        if(length < 2 || length != count || tryItemArray.getCount() != 0){
            return results;
        }
        for(int i = 0; i < count; i++){
            int offset = offsetArray.getOffset(i);
            int index = offsetArray.indexOf(offset);
            TryItem tryItem;
            if(index >= 0 && index < i){
                tryItem = results[index].newCopy();
            }else {
                tryItem = new TryItem(offsetArray);
            }
            tryItem.setIndex(i);
            results[i] = tryItem;
        }
        return results;
    }

    public TryItem newInstance() {
        return new TryItem(getHandlersOffset());
    }
    public TryItem newInstance1() {
        HandlerOffsetArray offsetArray = getHandlersOffset();
        if(offsetArray.size() < 2){
            return new TryItem(offsetArray);
        }
        int index = 0;
        TryItem[] childes = tryItemArray.getChildes();
        for(int i = 0; i < childes.length; i++){
            if(childes[i] == null){
                index = i;
                break;
            }
        }
        if(index == 0){
            return new TryItem(offsetArray);
        }
        int offset = offsetArray.getOffset(index);
        int i = offsetArray.indexOf(offset);
        if(i >= 0 && i < index){
            return childes[i].newCopy();
        }
        return new TryItem(offsetArray);
    }

    @Override
    public String toString() {
        if(isNull()){
            return "NULL";
        }
        return "tryItems = " + tryItemArray.toString() + ", bytes="+countBytes();
    }
}
