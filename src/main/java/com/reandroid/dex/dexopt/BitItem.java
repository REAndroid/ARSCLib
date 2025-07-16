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
package com.reandroid.dex.dexopt;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.item.BlockItem;
import com.reandroid.utils.NumbersUtil;

public class BitItem extends BlockItem {

    private int bitsLength;

    public BitItem(int bitsLength) {
        super(bitsToBytes(bitsLength));
        this.bitsLength = bitsLength;
    }

    public boolean get(int bitIndex) {
        int v = readByte(byteIndex(bitIndex));
        int b = (v >> bitIndex(bitIndex)) & 0x1;
        return b == 1;
    }
    public void set(int bitIndex, boolean value) {
        int byteIndex = byteIndex(bitIndex);
        ensureSize(byteIndex + 1);
        int i = bitIndex(bitIndex);
        int v = NumbersUtil.setUInt(readByte(byteIndex), value ? 1 :0, i, 1);
        writeByte(byteIndex, v);
    }
    public int append(int offset, BitItem bitItem) {
        int length = bitItem.bitsLength();
        for (int i = 0; i < length; i++) {
            offset = offset + i;
            this.set(offset, bitItem.get(i));
        }
        return offset;
    }
    public long getValueUnsigned() {
        byte[] bytes = getBytesInternal();
        int i = bytes.length - 1;
        long low = Block.getUnsignedNumber(bytes, 0, i);
        long high = NumbersUtil.getUInt(bytes[i] & 0xff, 0, bitIndex(bitsLength()));
        high = high << (i * 8);
        return high | low;
    }
    public int readByte(int byteIndex) {
        return getBytesInternal()[byteIndex] & 0xff;
    }
    public void writeByte(int byteIndex, int byteValue) {
        getBytesInternal()[byteIndex] = (byte) byteValue;
    }
    public void ensureSize(int size) {
        if (size > size()) {
            setSize(size);
        }
    }
    public void setSize(int count) {
        setBytesLength(count, false);
    }
    public int size() {
        return countBytes();
    }
    public void ensureBitsLength(int length) {
        if (length > bitsLength()) {
            bitsLength(length);
        }
    }
    public int bitsLength() {
        return bitsLength;
    }
    public void bitsLength(int length) {
        setBytesLength(bitsToBytes(length), true);
        this.bitsLength = length;
    }


    @Override
    public String toString() {
        return "bits=" + bitsLength;
    }

    public static int bitIndex(int bitIndex) {
        return bitIndex & 0x7;
    }
    public static int wordIndex(int bitIndex) {
        return bitIndex >>> 6;
    }
    public static int byteIndex(int bitIndex) {
        return bitIndex >>> 3;
    }
    public static int bitsToBytes(int numberOfBits) {
        return ((numberOfBits + 7) & -8) / 8;
    }
}
