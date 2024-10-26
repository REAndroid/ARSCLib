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

public class Sha1OutputStream extends OutputStream {

    private final SHA1 sha1;

    public Sha1OutputStream() {
        super();
        this.sha1 = new SHA1();
    }

    public byte[] digest() {
        return sha1.digest();
    }
    public void digest(byte[] out, int outOffset) {
        sha1.digest(out, outOffset);
    }
    public void reset() {
        sha1.reset();
    }
    @Override
    public void write(int i) throws IOException {
        sha1.update((byte) i);
    }

    @Override
    public void write(byte[] b) throws IOException {
        sha1.update(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        sha1.update(b, off, len);
    }

    @Override
    public String toString() {
        return sha1.toString();
    }
}
