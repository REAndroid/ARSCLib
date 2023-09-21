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
import com.reandroid.arsc.item.ByteArray;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.CountedArray;
import com.reandroid.dex.base.DexBlockAlign;
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
    private ByteArray unknownBytes;
    private CountedArray<TryItem> tryItemArray;
    private DexBlockAlign positionAlign;

    public TryBlock(IntegerReference totalCount) {
        super(5);
        this.totalCount = totalCount;
    }

    public int getTryItemCount() {
        return tryItemArray.getCount();
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

    @Override
    protected void onRefreshed() {
        super.onRefreshed();


    }
    private int getStart(int index) {
        int start = tryItemsCount.countBytes();
        return 0;
    }

    private HandlerOffsetArray initHandlersOffset() {
        if(handlerOffsetArray == null){
            handlerOffsetArray = new HandlerOffsetArray(totalCount);
            addChild(INDEX_offsetArray, handlerOffsetArray);
        }
        return handlerOffsetArray;
    }
    private void initTryItemArray(){
        if(tryItemArray != null){
            return;
        }
        tryItemsCount = new Ule128Item();
        addChild(INDEX_itemsCount, tryItemsCount);
        tryItemArray = new CountedArray<>(totalCount, this);
        addChild(INDEX_itemArray, tryItemArray);
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
        initHandlersOffset();
        initTryItemArray();
        if(positionAlign == null){
            positionAlign = new DexBlockAlign(this);
            addChild(INDEX_positionAlign, positionAlign);
        }
    }
    private void clear(){
        if(handlerOffsetArray != null){
            handlerOffsetArray.setParent(null);
            handlerOffsetArray.setIndex(-1);
            handlerOffsetArray = null;
        }
        if(tryItemsCount != null){
            tryItemsCount.setParent(null);
            tryItemsCount.setIndex(-1);
            tryItemArray = null;
        }
        if(tryItemArray != null){
            tryItemArray.clearChildes();
            tryItemArray.setParent(null);
            tryItemArray.setIndex(-1);
            tryItemArray = null;
        }
        if(positionAlign != null){
            positionAlign.setParent(null);
            positionAlign.setIndex(-1);
            positionAlign = null;
        }
        addChild(INDEX_offsetArray, null);
        addChild(INDEX_itemsCount, null);
        addChild(INDEX_unknownBytes, null);
        addChild(INDEX_itemArray, null);
        addChild(INDEX_positionAlign, null);
    }
    @Override
    public boolean isNull(){
        return tryItemArray == null;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        boolean is_null = totalCount.get() == 0;
        setNull(is_null);
        if(is_null){
            return;
        }
        this.handlerOffsetArray.onReadBytes(reader);
        this.tryItemsCount.onReadBytes(reader);
        readUnknownBytes(reader);
        this.tryItemArray.onReadBytes(reader);
        this.positionAlign.onReadBytes(reader);
    }
    private void setUnknownBytes(int count) {
        if(count <= 0 || isNull()) {
            ByteArray unknown = this.unknownBytes;
            if(unknown != null) {
                unknown.setParent(null);
                unknown.setIndex(-1);
                unknown.setSize(0);
                this.unknownBytes = null;
            }
            return;
        }
        ByteArray unknown = this.unknownBytes;
        if(unknown == null) {
            unknown = new ByteArray(count);
            this.unknownBytes = unknown;
            unknown.setParent(this);
            addChild(INDEX_unknownBytes, unknown);
        }else {
            unknown.setSize(count);
        }
    }
    private void readUnknownBytes(BlockReader reader) throws IOException {
        int minStart = this.handlerOffsetArray.getMinStart();
        minStart = minStart - this.tryItemsCount.countBytes();
        setUnknownBytes(minStart);
        ByteArray unknown = this.unknownBytes;
        if(unknown != null){
            unknown.readBytes(reader);
        }
    }

    @Override
    public TryItem[] newInstance(int length) {
        HandlerOffsetArray offsetArray = initHandlersOffset();
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

    @Override
    public TryItem newInstance() {
        return new TryItem(initHandlersOffset());
    }

    @Override
    public String toString() {
        if(isNull()){
            return "NULL";
        }
        return "tryItems = " + tryItemArray.toString() + ", bytes="+countBytes();
    }

    private static final int INDEX_offsetArray = 0;
    private static final int INDEX_itemsCount = 1;
    private static final int INDEX_unknownBytes = 2;
    private static final int INDEX_itemArray = 3;
    private static final int INDEX_positionAlign = 4;
}
