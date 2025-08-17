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
package com.reandroid.arsc.item;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.base.DirectStreamReader;
import com.reandroid.utils.HexUtil;

public class ByteItem extends BlockItem implements IntegerReference, DirectStreamReader {

    public ByteItem() {
        super(1);
    }

    public boolean getBit(int index){
        return ((getByte()>>index) & 0x1) == 1;
    }
    public void putBit(int index, boolean bit){
        int b = getByte();
        int one = 1 << index;
        b &= ~one;
        if(bit){
            b |= one;
        }
        set((byte) b);
    }
    public void set(byte value) {
        getBytesInternal()[0] = value;
    }
    public byte getByte() {
        return getBytesInternal()[0];
    }
    @Override
    public int get() {
        return getByte() & 0xff;
    }
    @Override
    public void set(int value) {
        set((byte) value);
    }
    public String toHex(){
        return HexUtil.toHex2(getByte());
    }
    @Override
    public String toString(){
        return String.valueOf(getByte());
    }

    public static final Creator<ByteItem> CREATOR = ByteItem::new;
}
