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
package com.reandroid.dex.value;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class DexValueBlock<T extends Block> extends FixedBlockContainer implements SmaliFormat {

    private final ByteItem valueTypeItem;
    private final T valueContainer;

    DexValueBlock(T value, DexValueType<?> type){
        super(2);
        valueTypeItem = new ByteItem();
        valueContainer = value;
        addChild(0, valueTypeItem);
        addChild(1, valueContainer);
        valueTypeItem.set((byte) type.getFlag());
    }
    DexValueBlock(DexValueType<?> type){
        this(null, type);
    }

    T getValueContainer(){
        return valueContainer;
    }
    ByteItem getValueTypeItem(){
        return valueTypeItem;
    }
    public DexValueType<?> getValueType(){
        return getValueTypeReal();
    }
    private DexValueType<?> getValueTypeReal(){
        return DexValueType.fromFlag(valueTypeItem.unsignedInt());
    }
    public String getTypeName(){
        return getValueType().getTypeName();
    }
    int getValueSize(){
        return DexValueType.decodeSize(valueTypeItem.unsignedInt());
    }
    void setValueSize(int size){
        int flag = getValueType().getFlag(size);
        valueTypeItem.set((byte) flag);
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        T value = getValueContainer();
        if(value instanceof SmaliFormat){
            ((SmaliFormat)value).append(writer);
        }
    }
    public String getAsString() {
        return String.valueOf(getValueContainer());
    }
    @Override
    public String toString() {
        return String.valueOf(getValueContainer());
    }
}
