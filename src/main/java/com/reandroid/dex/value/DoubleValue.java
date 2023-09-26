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

import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class DoubleValue extends PrimitiveValue {

    public DoubleValue() {
        super(DexValueType.DOUBLE);
    }

    public double get(){
        return Double.longBitsToDouble(getNumberValue());
    }
    public void set(double value){
        setNumberValue(Double.doubleToLongBits(value));
    }
    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.DOUBLE;
    }
    @Override
    public String getHex() {
        return HexUtil.toHex(getNumberValue(), getValueSize()) + "L";
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        super.append(writer);
        writer.appendComment(Double.toString(get()));
    }
    @Override
    public String toString() {
        return getHex() + " # " + get();
    }
}
