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
import com.reandroid.dex.smali.model.SmaliValueFloat;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class FloatValue extends PrimitiveValue {

    public FloatValue() {
        super(DexValueType.FLOAT);
    }

    @Override
    public Float getNumber() {
        return get();
    }
    @Override
    public void setNumber(Number number) {
        this.set((Float) number);
    }
    public float get(){
        int shift = (3 - getValueSize()) * 8;
        int value = (int) getNumberValue();
        return Float.intBitsToFloat(value << shift);
    }
    public void set(float value){
        setNumberValue(Float.floatToIntBits(value));
    }
    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.FLOAT;
    }
    @Override
    public String getHex() {
        return HexUtil.toHex(getNumberValue(), getValueSize()) + "L";
    }
    @Override
    public TypeKey getDataTypeKey() {
        return TypeKey.TYPE_F;
    }

    @Override
    public void fromSmali(SmaliValue smaliValue) {
        SmaliValueFloat smaliValueFloat = (SmaliValueFloat) smaliValue;
        set(smaliValueFloat.getValue());
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
