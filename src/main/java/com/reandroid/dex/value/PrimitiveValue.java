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

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public abstract class PrimitiveValue extends DexValueBlock<NumberValue> {

    public PrimitiveValue(DexValueType<?> type) {
        super(new NumberValue(), type);
    }

    public long getNumberValue(){
        return getValueContainer().getNumberValue();
    }
    public void setNumberValue(int value){
        setNumberValue((long)value);
    }
    public void setNumberValue(long value){
        NumberValue container = getValueContainer();
        container.setNumberValue(value);
        setValueSize(container.getSize() - 1);
    }

    public abstract String getHex();
    @Override
    public String getAsString() {
        return getHex();
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        getValueTypeItem().onReadBytes(reader);
        NumberValue container = getValueContainer();
        container.setSize(getValueSize() + 1);
        container.readBytes(reader);
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getHex());
    }
    @Override
    public String toString() {
        return getHex();
    }
}
