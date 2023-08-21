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
import com.reandroid.dex.base.DexPositionAlign;
import com.reandroid.dex.item.DexContainerItem;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;

public class TryBlock extends DexContainerItem implements
        Creator<TryItem>, Iterable<TryItem> {
    private final IntegerReference itemCount;

    private HandlerOffsetArray handlerOffsetArray;
    private CountedArray<TryItem> tryItemArray;
    private DexPositionAlign positionAlign;

    public TryBlock(IntegerReference itemCount) {
        super(3);
        this.itemCount = itemCount;
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
            handlerOffsetArray = new HandlerOffsetArray(itemCount);
            addChild(0, handlerOffsetArray);
        }
        return handlerOffsetArray;
    }
    private void initTryItemArray(){
        if(tryItemArray != null){
            return;
        }
        tryItemArray = new CountedArray<>(itemCount, this);
        addChild(1, tryItemArray);
        tryItemArray.setChildesCount(itemCount.get());
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
            positionAlign = new DexPositionAlign();
            addChild(2, positionAlign);
        }
    }
    private void clear(){
        if(handlerOffsetArray != null){
            handlerOffsetArray.setParent(null);
            handlerOffsetArray.setIndex(-1);
            handlerOffsetArray = null;
            addChild(0, null);
        }
        if(tryItemArray != null){
            tryItemArray.clearChildes();
            tryItemArray.setParent(null);
            tryItemArray.setIndex(-1);
            tryItemArray = null;
            addChild(1, null);
        }
        if(positionAlign != null){
            positionAlign.setParent(null);
            positionAlign.setIndex(-1);
            positionAlign = null;
            addChild(2, null);
        }
    }
    @Override
    public boolean isNull(){
        return tryItemArray == null;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        boolean is_null = itemCount.get() == 0;
        setNull(is_null);
        if(!is_null){
            super.onReadBytes(reader);
        }
    }

    @Override
    public TryItem[] newInstance(int length) {
        return new TryItem[length];
    }
    @Override
    public TryItem newInstance() {
        return new TryItem(getHandlersOffset());
    }

    @Override
    public String toString() {
        if(isNull()){
            return "NULL";
        }
        return "tryItems = " + tryItemArray.toString();
    }
}
