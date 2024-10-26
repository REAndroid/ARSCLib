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

// implemented from http://www.libpng.org/pub/png/spec/1.2/PNG-CRCAppendix.html

public class Crc32 extends Checksum {

    private static final long[] CRC_TABLE;

    static {
        long[] table = new long[256];
        CRC_TABLE = table;
        for (int i = 0; i < 256; i++) {
            long c = i;
            for (int j = 0; j < 8; j++) {
                if ((c & 1) == 1) {
                    c = 0xedb88320L ^ (c >> 1);
                } else {
                    c = c >> 1;
                }
            }
            table[i] = c;
        }
    }

    private long mCrc;
    private long mLength;

    public Crc32() {
        super();
        this.mCrc = 0xffffffffL;
    }

    @Override
    public long getValue() {
        return mCrc ^ 0xffffffffL;
    }

    public long getLength() {
        return mLength;
    }

    @Override
    public void reset() {
        this.mCrc = 0xffffffffL;
        this.mLength = 0;
    }

    @Override
    public void update(byte[] data, int offset, int length) {
        long c = mCrc;
        int end = offset + length;
        long[] table = CRC_TABLE;
        for (int i = offset; i < end; i++) {
            int b = data[i] & 0xff;
            c = table[(int)((c ^ b) & 0xff)] ^ (c >> 8);
        }
        this.mCrc = c;
        this.mLength += length;
    }
}
