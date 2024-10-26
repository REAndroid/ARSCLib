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

public abstract class Checksum {

    private byte[] oneByte;

    public Checksum() {
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

    public abstract long getValue();
    public abstract void reset();
    public abstract void update(byte[] data, int offset, int length);
}
