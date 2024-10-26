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

import java.io.OutputStream;

public class Crc32OutputStream extends OutputStream {

    private final Crc32 crc32;

    public Crc32OutputStream() {
        super();
        this.crc32 = new Crc32();
    }

    public long getValue() {
        return crc32.getValue();
    }
    public long getLength() {
        return crc32.getLength();
    }

    @Override
    public void write(int i) {
        crc32.update((byte) i);
    }
    @Override
    public void write(byte[] buffer) {
        crc32.update(buffer, 0, buffer.length);
    }
    @Override
    public void write(byte[] buffer, int offset, int length) {
        crc32.update(buffer, offset, length);
    }

    public void update(byte[] buffer, int offset, int length) {
        write(buffer, offset, length);
    }

    @Override
    public String toString() {
        return HexUtil.toHex(null, getValue(), 8);
    }
}
