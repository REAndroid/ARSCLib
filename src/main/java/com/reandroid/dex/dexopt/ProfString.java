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
package com.reandroid.dex.dexopt;

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.StringBlock;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ProfString extends StringBlock {

    private final IntegerReference lengthReference;

    public ProfString(IntegerReference lengthReference) {
        super();
        this.lengthReference = lengthReference;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        setBytesLength(lengthReference.get(), false);
        super.onReadBytes(reader);
    }

    @Override
    protected void onStringChanged(String old, String text) {
        super.onStringChanged(old, text);
        lengthReference.set(countBytes());
    }
    @Override
    protected String decodeString(byte[] bytes) {
        return new String(bytes, 0, bytes.length, StandardCharsets.UTF_8);
    }
    @Override
    protected byte[] encodeString(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }
}
