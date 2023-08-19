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

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.io.StreamUtil;

import java.io.IOException;

public class Sle128Item extends DexBlockItem implements IntegerReference {
    private int value;
    public Sle128Item() {
        super(1);
    }
    @Override
    public int get() {
        return value;
    }
    @Override
    public void set(int value) {
        if(this.value == value){
            return;
        }
        this.value = value;
        setBytesLength(5, false);
        int length = writeSleb128(getBytesInternal(), 0, value);
        setBytesLength(length, false);
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int position = reader.getPosition();
        int value = readSleb128(StreamUtil.createByteReader(reader));
        int length = reader.getPosition() - position;
        reader.seek(position);
        setBytesLength(length, false);
        reader.readFully(getBytesInternal());
        this.value = value;
    }

    @Override
    public String toString() {
        return Integer.toString(get());
    }
}
