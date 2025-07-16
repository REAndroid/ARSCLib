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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.BlockItem;
import com.reandroid.arsc.item.BooleanReference;
import com.reandroid.arsc.item.IntegerReference;

import java.io.IOException;

public class ByteOrShortItem extends BlockItem implements IntegerReference {

    private final BooleanReference is_byte;

    public ByteOrShortItem(BooleanReference is_byte) {
        super(2);
        this.is_byte = is_byte;
    }

    @Override
    public int get() {
        byte[] bytes = getBytesInternal();
        if (is_byte.get()) {
            return bytes[0] & 0xff;
        }
        return Block.getShortUnsigned(bytes, 0);
    }

    @Override
    public void set(int value) {
        boolean isByte = this.is_byte.get();
        setBytesLength(isByte ? 1 : 2, false);
        byte[] bytes = getBytesInternal();
        if (isByte) {
            bytes[0] = (byte) value;
        } else {
            Block.putShort(bytes, 0, value);
        }
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        setBytesLength(is_byte.get() ? 1 : 2, false);
        super.onReadBytes(reader);
    }

    @Override
    public String toString() {
        return Integer.toString(get());
    }
}
