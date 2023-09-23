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
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class PrimitiveValue extends DexValueBlock<NumberValue> implements IntegerReference {

    public PrimitiveValue() {
        super(new NumberValue());
    }

    @Override
    public int get() {
        return getAsInteger(0);
    }
    @Override
    public void set(int value) {
        setNumberValue(value);
    }
    public long getNumberValue(){
        return getValue().getNumberValue();
    }
    public void setNumberValue(int value){
        getValue().setNumberValue(value);
    }
    public void setNumberValue(long value){
        getValue().setNumberValue(value);
    }
    @Override
    public int getAsInteger(int def) {
        if(getValueType() == DexValueType.INT){
            return (int) getNumberValue();
        }
        return def;
    }
    public long getAsNumber(long def) {
        return getNumberValue();
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        getValueTypeItem().onReadBytes(reader);
        NumberValue numberValue = getValue();
        numberValue.setSize(getValueSize() + 1);
        numberValue.readBytes(reader);
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(HexUtil.toHex(getNumberValue(), getValueSize()));
    }
    @Override
    public String getAsString() {
        return HexUtil.toHex(getNumberValue(), getValueSize());
    }
    @Override
    public String toString() {
        return getAsString();
    }
}
