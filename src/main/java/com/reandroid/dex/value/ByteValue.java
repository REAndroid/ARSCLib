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

import com.reandroid.utils.HexUtil;

public class ByteValue extends PrimitiveValue {

    public ByteValue() {
        super(DexValueType.BYTE);
    }

    public byte get(){
        return (byte) (getNumberValue() & 0xff);
    }
    public void set(byte b){
        getValueContainer().setNumberValue(b);
    }
    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.BYTE;
    }
    @Override
    public String getHex() {
        return HexUtil.toHex(getNumberValue(), 1) + "t";
    }
}
