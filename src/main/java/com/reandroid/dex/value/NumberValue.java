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

    public long getNumberValue(){
        return getNumber(getBytesInternal(), 0, getSize());
    }
    public void setNumberValue(byte value){
        getBytesInternal()[0] = value;
    }
    public void setNumberValue(short value){
        putNumber(getBytesInternal(), 0, getSize(), value & 0xffffffffL);
    }
    public void setNumberValue(int value){
        putNumber(getBytesInternal(), 0, getSize(), value & 0xffffffffL);
    }
    public void setNumberValue(long value){
        putNumber(getBytesInternal(), 0, getSize(), value);
    }

    @Override
    public String toString() {
        return getSize() + ":" + getNumberValue();
    }
}
