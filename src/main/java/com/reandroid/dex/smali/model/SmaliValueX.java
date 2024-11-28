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
package com.reandroid.dex.smali.model;

import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.PrimitiveKey;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.value.DexValueType;
import com.reandroid.utils.NumberX;
import com.reandroid.utils.NumbersUtil;

import java.io.IOException;

public class SmaliValueX extends SmaliValueNumber<NumberX> {

    private int width;
    private long value;

    public SmaliValueX(int width, long value) {
        super();
        this.width = width;
        this.value = value;
    }
    public SmaliValueX() {
        this(1, 0);
    }
    public SmaliValueX(NumberX numberX) {
        this(numberX.width(), numberX.longValue());
    }

    @Override
    public DexValueType<?> getValueType() {
        return null;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(NumberX.toHexString(getWidth(), getValueAsLong()));
    }
    @Override
    public void parse(SmaliReader reader) throws IOException {
        reader.skipSpaces();
        SmaliValueNumber<?> number = SmaliValueNumber.createNumber(reader);
        number.parse(reader);
        long l = number.getValueAsLong();
        int width = NumbersUtil.max(this.getWidth(), NumberX.widthOfSigned(l));
        set(width, l);
    }

    @Override
    public NumberX getNumber() {
        return NumberX.valueOf(getWidth(), getValueAsLong());
    }

    @Override
    public void setNumber(NumberX number) {
        set(number.width(), number.longValue());
    }
    public void set(int width, long value) {
        this.width = width;
        this.value = value;
    }

    @Override
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public long getValueAsLong() {
        return value;
    }

    @Override
    public PrimitiveKey getKey() {
        return PrimitiveKey.of(getWidth(), getValueAsLong());
    }
    @Override
    public void setKey(Key key) {
        PrimitiveKey primitiveKey = (PrimitiveKey) key;
        if (primitiveKey.isNumber()) {
            set(primitiveKey.width(), primitiveKey.getValueAsLong());
        } else {
            throw new NumberFormatException("Incompatible key: "
                    + key.getClass() + ", " + key);
        }
    }
}
