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

import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliValue;
import com.reandroid.dex.smali.model.SmaliValueDouble;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class DoubleValue extends PrimitiveValue {

    public DoubleValue() {
        super(DexValueType.DOUBLE);
    }

    @Override
    public Double getNumber() {
        return get();
    }
    @Override
    public void setNumber(Number number) {
        this.set((Double) number);
    }
    public double get(){
        int shift = (7 - getValueSize()) * 8;
        long value = getNumberValue();
        return Double.longBitsToDouble(value << shift);
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
        int shift = (7 - getValueSize()) * 8;
        return HexUtil.toHex(getNumberValue() << shift, 8) + "L";
    }
    @Override
    public TypeKey getDataTypeKey() {
        return TypeKey.TYPE_D;
    }

    @Override
    public void fromSmali(SmaliValue smaliValue) {
        SmaliValueDouble smaliValueDouble = (SmaliValueDouble) smaliValue;
        set(smaliValueDouble.getValue());
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(get());
    }
    @Override
    public String toString() {
        return getHex() + " # " + get();
    }
}
