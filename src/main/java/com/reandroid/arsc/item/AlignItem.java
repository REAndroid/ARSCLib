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
package com.reandroid.arsc.item;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class AlignItem extends BlockItem {

    private byte fill;
    private int alignment;
    private final boolean readable;

    public AlignItem(int alignment, boolean readable) {
        super(0);
        this.alignment = alignment;
        this.readable = readable;
    }
    public AlignItem(int alignment) {
        this(alignment, false);
    }
    public AlignItem(boolean readable) {
        this(ALIGNMENT, readable);
    }
    public AlignItem() {
        this(ALIGNMENT, false);
    }

    public int align(Block block) {
        clear();
        if (getAlignment() <= 0) {
            return 0;
        }
        return align(block.countBytes());
    }
    public int align(long count) {
        int size = align(getAlignment(), count);
        setSize(size);
        return size;
    }
    public int align(int count) {
        int size = align(getAlignment(), count);
        setSize(size);
        return size;
    }
    public void alignSafe(BlockReader reader) throws IOException {
        align(reader.getPosition());
        int size = size();
        int available = reader.available();
        if (size != 0 && available >= size) {
            reader.readFully(getBytesInternal(), 0, size);
        }
    }
    public void clear() {
        setBytesLength(0, false);
    }
    public int size() {
        return countBytes();
    }
    public void ensureSize(int size) {
        if (size > size()) {
            setSize(size);
        }
    }
    public void setSize(int size) {
        setBytesLength(size, false);
        setFill(this.fill);
    }
    public int getAlignment() {
        return alignment;
    }
    public void setAlignment(int alignment) {
        this.alignment = alignment;
        if (alignment <= 0) {
            setBytesLength(0, false);
        }
    }

    public byte getFill() {
        return fill;
    }
    public void setFill(byte fill) {
        this.fill = fill;
        byte[] bytes = getBytesInternal();
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            bytes[i] = fill;
        }
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        if (readable) {
            alignSafe(reader);
        } else {
            super.onReadBytes(reader);
        }
    }

    @Override
    public String toString() {
        int alignment = getAlignment();
        if (alignment <= 0) {
            return "OFF";
        }
        int size = size();
        byte fill = this.fill;
        StringBuilder builder = new StringBuilder();
        if (alignment != ALIGNMENT) {
            builder.append("alignment=");
            builder.append(alignment);
            builder.append(", ");
        }
        if (fill != 0) {
            builder.append("fill=");
            builder.append(HexUtil.toHex2(fill));
            builder.append(", ");
        }
        builder.append("align=");
        builder.append(size);
        return builder.toString();
    }


    public static int align(int alignment, long value) {
        if (alignment <= 1) {
            return 0;
        }
        return (alignment - (int) (value % alignment)) % alignment;
    }
    public static int align(int alignment, int value) {
        if (alignment <= 1) {
            return 0;
        }
        return (alignment - (value % alignment)) % alignment;
    }

    private static final int ALIGNMENT = 4;
}
