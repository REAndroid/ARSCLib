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

import com.reandroid.dex.base.DexItem;
import com.reandroid.utils.HexUtil;

public class NumberValue extends DexItem {
    public NumberValue(int bytesLength) {
        super(bytesLength);
    }
    public NumberValue() {
        super(1);
    }
    public int getSize(){
        return countBytes();
    }
    public void setSize(int size){
        setBytesLength(size, false);
    }

    public long getSignedValue(){
        int size = getSize();
        long value = getNumberValue();
        if(size == 1){
            return (byte)value;
        }
        if(size == 2){
            return (short)value;
        }
        if(size < 5){
            return (int)value;
        }
        return value;
    }
    public long getNumberValue(){
        return getNumber(getBytesInternal(), 0, getSize());
    }
    public void setNumberValue(byte value){
        setSize(1);
        getBytesInternal()[0] = value;
    }
    public void setNumberValue(short value){
        if(value < 0){
            byte b = (byte) (value & 0xff);
            if(value == b){
                setNumberValue(b);
                return;
            }
        }
        setNumberValue(value & 0xffffL);
    }
    public void setNumberValue(int value){
        if(value < 0){
            short s = (short) (value & 0xffff);
            if(value == s){
                setNumberValue(s);
                return;
            }
        }
        setNumberValue(value & 0xffffffffL);
    }
    public void setNumberValue(long value){
        int size = calculateSize(value);
        setSize(size);
        putNumber(getBytesInternal(), 0, size, value);
    }

    public String toHex(){
        return HexUtil.toHex(getNumberValue(), getSize());
    }
    @Override
    public String toString() {
        return getSize() + ":" + toHex() + ":" + getSignedValue();
    }
    private static int calculateSize(long value){
        if(value == 0){
            return 1;
        }
        int i = 0;
        while (value != 0){
            value = value >>> 8;
            i++;
        }
        return i;
    }
}
