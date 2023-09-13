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
package com.reandroid.dex.header;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Adler32;

public class Alder32OutputStream extends OutputStream {

    private final Adler32 adler32;

    public Alder32OutputStream() {
        adler32 = new Adler32();
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        if (length != 0) {
            adler32.update(bytes, offset, length);
        }
    }
    @Override
    public void write(byte[] bytes) throws IOException {
        this.write(bytes, 0, bytes.length);
    }
    @Override
    public void write(int i) throws IOException {
        this.write(new byte[]{(byte) i}, 0, 1);
    }
    public int getValue() {
        return (int) adler32.getValue();
    }
}
