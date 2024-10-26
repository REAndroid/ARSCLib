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
package com.reandroid.utils;

public abstract class ByteDigest {

    private byte[] oneByte;

    public ByteDigest() {
    }

    public void update(byte data) {
        byte[] oneByte = this.oneByte;
        if (oneByte == null) {
            oneByte = new byte[1];
            this.oneByte = oneByte;
        }
        oneByte[0] = data;
        this.update(oneByte, 0, 1);
    }
    public void update(byte[] data) {
        update(data, 0, data.length);
    }
    public abstract void update(byte[] data, int offset, int length);
    public abstract int getDigestLength();

    public byte[] digest() {
        byte[] out = new byte[getDigestLength()];
        digest(out, 0);
        return out;
    }
    public abstract void digest(byte[] out, int outOffset);

    public abstract void reset();


    static int getBigEndianInteger(byte[] bytes, int offset){
        return bytes[offset + 3] & 0xff |
                (bytes[offset + 2] & 0xff) << 8 |
                (bytes[offset + 1] & 0xff) << 16 |
                (bytes[offset] & 0xff) << 24;
    }
    static void putBigEndianInteger(byte[] bytes, int offset, int value){
        bytes[offset] = (byte) (value >>> 24 );
        bytes[offset + 1] = (byte) (value >>> 16);
        bytes[offset + 2] = (byte) (value >>> 8 );
        bytes[offset + 3] = (byte) (value & 0xff);
    }
    static void fillZero(int[] arr) {
        int length = arr.length;
        for(int i = 0; i < length; i++){
            arr[i] = 0;
        }
    }
    static void fillZero(byte[] arr) {
        int length = arr.length;
        for(int i = 0; i < length; i++){
            arr[i] = (byte) 0;
        }
    }
}
