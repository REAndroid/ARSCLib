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

import java.io.IOException;
import java.io.OutputStream;

public class Alder32OutputStream extends OutputStream {

    private final ALDER32 alder32;

    public Alder32OutputStream() {
        super();
        this.alder32 = new ALDER32();
    }

    public long getValue() {
        return alder32.getValue();
    }
    public void reset() {
        alder32.reset();
    }
    @Override
    public void write(int i) throws IOException {
        alder32.update((byte) i);
    }

    @Override
    public void write(byte[] b) throws IOException {
        alder32.update(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        alder32.update(b, off, len);
    }

    @Override
    public String toString() {
        return alder32.toString();
    }
}
