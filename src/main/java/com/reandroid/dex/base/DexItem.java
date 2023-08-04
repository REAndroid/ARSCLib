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
package com.reandroid.dex.base;

import com.reandroid.arsc.item.BlockItem;
import com.reandroid.dex.io.ByteReader;
import com.reandroid.dex.io.StreamUtil;

import java.io.IOException;
import java.io.InputStream;

public abstract class DexItem extends BlockItem {
    public DexItem(int bytesLength) {
        super(bytesLength);
    }


    public static int writeUleb128(byte[] bytes, int offset, int value) {
        int index = 0;
        while ((value & 0xffffffffL) > 0x7f) {
            bytes[offset + index] = (byte) ((value & 0x7f) | 0x80);
            value >>>= 7;
            index ++;
        }
        bytes[offset + index] = (byte) (value);
        return index + 1;
    }
    public static int readUleb128(InputStream inputStream) throws IOException {
        return readUleb128(StreamUtil.createByteReader(inputStream));
    }
    public static int readUleb128(byte[] bytes, int offset) throws IOException {
        return readUleb128(StreamUtil.createByteReader(bytes, offset));
    }
    public static int readUleb128(ByteReader reader) throws IOException{
        return readUleb128(reader, 4);
    }
    public static int readUleb128Large(ByteReader reader) throws IOException{
        return readUleb128(reader, 5);
    }
    public static int readUleb128(ByteReader reader, int size) throws IOException {
        int result = 0;
        int value = 0x80;
        int count = 0;
        while (value > 0x7f && count < size){
            value = reader.read();
            result |= ((value & 0x7f) << (count * 7));
            count ++;
        }
        if (value > 0x7f) {
            throw new IOException("Invalid uleb128 integer, size = " + size);
        } else if (count == size && ((value & 0xf) > 0x07)) {
            throw new IOException("Encountered valid uleb128 that is out of range, size = " + size);
        }
        return result;
    }

    protected static long getNumber(byte[] bytes, int offset, int size){
        if((offset + size)>bytes.length){
            return 0;
        }
        long result = 0;
        int index = offset + size - 1;
        while (index>=offset){
            result = result << 8;
            result |= (bytes[index] & 0xff);
            index --;
        }
        return result;
    }
    protected static void putNumber(byte[] bytes, int offset, int size, long value){
        if((offset + size) > bytes.length){
            return;
        }
        int index = offset;
        offset = index + size;
        while (index<offset){
            bytes[index] = (byte) (value & 0xff);
            value = value >>> 8;
            index++;
        }
    }
}
