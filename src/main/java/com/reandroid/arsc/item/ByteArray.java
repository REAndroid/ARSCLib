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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.io.BlockReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractList;
import java.util.List;

public class ByteArray extends BlockItem {

    public ByteArray(int bytesLength) {
        super(bytesLength);
    }
    public ByteArray() {
        this(0);
    }
    public final void clear() {
        setSize(0);
    }
    public final void add(byte[] values) {
        if (values == null || values.length == 0) {
            return;
        }
        int current = size();
        int valuesLength = values.length;
        int length = current + valuesLength;
        setBytesLength(length, false);
        byte[] bytes = getBytesInternal();
        System.arraycopy(values, 0, bytes, current, valuesLength);
    }
    public final void set(byte[] values) {
        super.setBytesInternal(values);
    }
    public final byte[] toArray() {
        return getBytes();
    }
    public final void fill(byte value) {
        byte[] bytes = getBytesInternal();
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            bytes[i] = value;
        }
    }
    public final void ensureArraySize(int size) {
        if (size > size()) {
            setSize(size);
        }
    }
    public final void setSize(int size) {
        if (size < 0) {
            size = 0;
        }
        setBytesLength(size);
    }
    public final int size() {
        return getBytesLength();
    }

    public byte get(int index) {
        return getBytesInternal()[index];
    }
    public int getByteUnsigned(int index) {
        return 0xff & get(index);
    }
    public final void putByte(int index, int byteValue) {
        put(index, (byte) byteValue);
    }
    public final void put(int index, byte value) {
        getBytesInternal()[index] = value;
    }
    public boolean getBit(int byteOffset, int bitIndex) {
        return getBit(getBytesInternal(), byteOffset, bitIndex);
    }
    public void putBit(int byteOffset, int bitIndex, boolean bit) {
        putBit(getBytesInternal(), byteOffset, bitIndex, bit);
    }
    public final void putShort(int offset, int value) {
        putShort(offset, (short) value);
    }
    public final void putShort(int offset, short val) {
        Block.putShort(getBytesInternal(), offset, val);
    }
    public final int getShortUnsigned(int offset) {
        return 0xffff & getShort(offset);
    }
    public final short getShort(int offset) {
        return Block.getShort(getBytesInternal(), offset);
    }
    public final void putInteger(int offset, int val) {
        byte[] bytes = getBytesInternal();
        if ((offset + 4) > bytes.length) {
            return;
        }
        Block.putInteger(bytes, offset, val);
    }
    public final int getInteger(int offset) {
        return Block.getInteger(getBytesInternal(), offset);
    }
    public final void putByteArray(int offset, byte[] array) {
        byte[] bytes = getBytesInternal();
        int available = bytes.length - offset;
        if (available <= 0) {
            return;
        }
        int length = array.length;
        if (length > available) {
            length = available;
        }
        System.arraycopy(array, 0, bytes, offset, length);
    }
    public final byte[] getByteArray(int offset, int length) {
        byte[] bts = getBytesInternal();
        byte[] result = new byte[length];
        if (result.length >= 0) {
            System.arraycopy(bts, offset, result, 0, result.length);
        }
        return result;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
    }
    @Override
    public int readBytes(InputStream inputStream) throws IOException {
        return super.readBytes(inputStream);
    }

    public final List<Byte> toByteList() {
        return new AbstractList<Byte>() {
            @Override
            public Byte get(int i) {
                return ByteArray.this.get(i);
            }
            @Override
            public int size() {
                return ByteArray.this.size();
            }
        };
    }
    public final List<Short> toShortList() {
        return new AbstractList<Short>() {
            @Override
            public Short get(int i) {
                return ByteArray.this.getShort(i);
            }
            @Override
            public int size() {
                return ByteArray.this.size()/2;
            }
        };
    }
    public final List<Integer> toIntegerList() {
        return new AbstractList<Integer>() {
            @Override
            public Integer get(int i) {
                return ByteArray.this.getInteger(i);
            }
            @Override
            public int size() {
                return ByteArray.this.size()/4;
            }
        };
    }
    @Override
    public String toString() {
        return "size="+size();
    }

    public static byte[] trimTrailZeros(byte[] bytes) {
        if (bytes == null) {
            return new byte[0];
        }
        int length = bytes.length;
        int resultLength = length;
        for (int i = length - 1; i >= 0; i--) {
            if (bytes[i] == 0) {
                resultLength --;
            } else {
                break;
            }
        }
        if (resultLength == length) {
            return bytes;
        }
        byte[] result = new byte[resultLength];
        System.arraycopy(bytes, 0, result, 0, resultLength);
        return result;
    }
    public static boolean equals(byte[] bts1, byte[] bts2) {
        if (bts1==bts2) {
            return true;
        }
        if (bts1 == null || bts1.length==0) {
            return bts2 == null || bts2.length==0;
        }
        if (bts2 == null || bts2.length==0) {
            return false;
        }
        if (bts1.length!=bts2.length) {
            return false;
        }
        for (int i=0;i<bts1.length;i++) {
            if (bts1[i]!=bts2[i]) {
                return false;
            }
        }
        return true;
    }
}
