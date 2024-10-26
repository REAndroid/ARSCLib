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

public class ALDER32 extends Checksum {

    private static final int MOD_ADLER = 0xfff1;

    private int a;
    private int b;

    public ALDER32() {
        super();
        this.a = 1;
        this.b = 0;
    }

    @Override
    public void update(byte[] data, int offset, int length) {
        if (length != 0) {
            int mod = MOD_ADLER;
            int a = this.a;
            int b = this.b;
            int end = offset + length;
            for (int i = offset; i < end; i++) {
                int v = data[i] & 0xFF;
                a = (a + v) % mod;
                b = (b + a) % mod;
            }
            this.a = a;
            this.b = b;
        }
    }

    @Override
    public long getValue() {
        int i = (this.b << 16) | this.a;
        return (long) i & 0xffffffffL;
    }

    @Override
    public void reset() {
        this.a = 1;
        this.b = 0;
    }

    @Override
    public String toString() {
        return HexUtil.toHex8(getValue());
    }
}
