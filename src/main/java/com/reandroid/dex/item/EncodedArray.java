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
package com.reandroid.dex.item;

import com.reandroid.arsc.base.BlockArray;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.CreatorArray;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.value.DexValueType;
import com.reandroid.dex.value.NullValue;

import java.io.IOException;
import java.util.Iterator;

public class EncodedArray extends DataItemEntry implements Iterable<DexValueBlock<?>> {

    private final Ule128Item elementCount;
    private final BlockArray<DexValueBlock<?>> elementsArray;

    public EncodedArray() {
        super(2);
        this.elementCount = new Ule128Item();
        this.elementsArray = new CreatorArray<>(CREATOR);
        addChild(0, elementCount);
        addChild(1, elementsArray);
    }

    public DexValueBlock<?> get(int i){
        return getElementsArray().get(i);
    }
    @SuppressWarnings("unchecked")
    public<T1 extends DexValueBlock<?>> T1 getOrCreate(DexValueType<T1> valueType, int i){
        BlockArray<DexValueBlock<?>> array = getElementsArray();
        array.ensureSize(i + 1);
        DexValueBlock<?> value = array.get(i);
        if(value.getValueType() != valueType){
            value = valueType.newInstance();
            array.setItem(i, value);
        }
        return (T1) value;
    }
    public int size(){
        return getElementsArray().getCount();
    }
    public void add(DexValueBlock<?> value){
       getElementsArray().add(value);
    }
    public void set(int i, DexValueBlock<?> value){
        getElementsArray().setItem(i, value);
    }
    public void trimNull(){
        getElementsArray().removeAllNull(0);
    }


    public void setSize(int size) {
        getElementsArray().setChildesCount(size);
    }
    @Override
    public Iterator<DexValueBlock<?>> iterator(){
        return getElementsArray().iterator();
    }

    private BlockArray<DexValueBlock<?>> getElementsArray() {
        return elementsArray;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        this.elementCount.onReadBytes(reader);
        BlockArray<DexValueBlock<?>> array = getElementsArray();
        int count = this.elementCount.get();
        array.setChildesCount(count);
        for(int i = 0; i < count; i++){
            DexValueBlock<?> dexValue = DexValueType.create(reader);
            array.setItem(i, dexValue);
            dexValue.onReadBytes(reader);
        }
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        this.elementCount.set(size());
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        Iterator<DexValueBlock<?>> iterator = iterator();
        boolean appendOnce = false;
        while (iterator.hasNext()){
            if(appendOnce){
                builder.append(',');
            }
            builder.append('\n');
            builder.append("    ");
            builder.append(iterator.next());
            appendOnce = true;
        }
        if(appendOnce){
            builder.append('\n');
        }
        builder.append('}');
        return builder.toString();
    }
    private static final Creator<DexValueBlock<?>> CREATOR = new Creator<DexValueBlock<?>>() {
        @Override
        public DexValueBlock<?>[] newInstance(int length) {
            if(length == 0){
                return EncodedArray.EMPTY;
            }
            return new DexValueBlock[length];
        }
        @Override
        public DexValueBlock<?> newInstance() {
            return NullValue.getInstance();
        }
    };
    static final DexValueBlock<?>[] EMPTY = new DexValueBlock<?>[0];
}
