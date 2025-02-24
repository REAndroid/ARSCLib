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
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.base.DirectStreamReader;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;

public abstract class OffsetItem extends BlockItem implements DirectStreamReader,
        Comparable<OffsetItem> {

    public static int NO_ENTRY = ObjectsUtil.of(0xffffffff);
    public static int NO_ENTRY16 = ObjectsUtil.of(0xffff);

    public static final Creator<OffsetItem> CREATOR_OFFSET16 = Helper.init16();
    public static final Creator<OffsetItem> CREATOR_OFFSET32 = Helper.init32();
    public static final Creator<OffsetItem> CREATOR_SPARSE = Helper.initSparse();

    private int mOffset;

    protected OffsetItem(int bytesLength) {
        super(bytesLength);
    }
    protected OffsetItem() {
        this(4);
    }

    public int getOffset() {
        return mOffset;
    }
    public void setOffset(int offset) {
        if (offset != mOffset) {
            writeOffset(offset);
            mOffset = offset;
        }
    }

    public int getIdx() {
        return getIndex();
    }
    public void setIdx(int idx) {
    }

    protected abstract int readOffset();
    protected abstract void writeOffset(int offset);

    @Override
    protected void onBytesChanged() {
        super.onBytesChanged();
        this.mOffset = readOffset();
    }

    public boolean isNoEntry() {
        return getOffset() == NO_ENTRY;
    }

    public int updateOffset(Block target, int offset) {
        if (target.isNull()) {
            setOffset(NO_ENTRY);
        } else {
            setOffset(offset);
            offset = offset + target.countBytes();
        }
        return offset;
    }
    public void readTarget(BlockReader reader, Block target) throws IOException {
        readTarget(reader, target, false);
    }
    public void readTarget(BlockReader reader, Block target, boolean ignoreOutOfRange) throws IOException {
        boolean noEntry = isNoEntry();
        int offset = getOffset();
        if (!noEntry) {
            int maximumPosition = reader.getPosition() + reader.available();
            if (offset < 0 || offset > maximumPosition) {
                if (!ignoreOutOfRange) {
                    throw new IOException("Offset " + offset + " is out of range " + maximumPosition);
                } else {
                    offset = NO_ENTRY;
                    setOffset(offset);
                    noEntry = true;
                }
            }
        }
        target.setNull(noEntry);
        if (!noEntry) {
            int position = reader.getPosition();
            reader.seek(offset);
            try {
                target.readBytes(reader);
            }catch (Exception ex) {
                throw new IOException("Error at:" + toString() + ex.getMessage() , ex);
            }
            int current = reader.getPosition();
            if (current < position) {
                reader.seek(position);
            }
        }
    }

    protected void validateValueRange(int value) {
        if (value != NO_ENTRY && (value & 0xffff0000) != 0) {
            throw new NumberFormatException("Value out of range [0 - 0xffff]: " +
                    HexUtil.toHex(value, 1));
        }
    }

    public int compareOffset(OffsetItem offsetItem) {
        if (offsetItem == this) {
            return 0;
        }
        return CompareUtil.compare(this.getOffset(), offsetItem.getOffset());
    }
    public int compareIdx(OffsetItem offsetItem) {
        if (offsetItem == this) {
            return 0;
        }
        return CompareUtil.compare(this.getIdx(), offsetItem.getIdx());
    }
    @Override
    public int compareTo(OffsetItem offsetItem) {
        return compareIdx(offsetItem);
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(getIdx());
        builder.append(", ");
        if (isNoEntry()) {
            builder.append("NO_ENTRY");
        } else {
            builder.append(getOffset());
        }
        builder.append(')');
        return builder.toString();
    }

    static class Offset16 extends OffsetItem {

        public Offset16() {
            super(2);
        }

        @Override
        protected int readOffset() {
            int offset = getShortUnsigned(getBytesInternal(), 0);
            if (offset == NO_ENTRY16) {
                offset = NO_ENTRY;
            } else {
                offset = offset * 4;
            }
            return offset;
        }

        @Override
        protected void writeOffset(int offset) {
            if (offset == NO_ENTRY) {
                offset = NO_ENTRY16;
            } else {
                offset = offset / 4;
                validateValueRange(offset);
            }
            putShort(getBytesInternal(), 0, offset);
        }

        @Override
        public int compareTo(OffsetItem offsetItem) {
            return compareIdx(offsetItem);
        }

    }

    static class Offset32 extends OffsetItem {

        public Offset32() {
            super();
        }

        @Override
        protected int readOffset() {
            return getInteger(getBytesInternal(), 0);
        }

        @Override
        protected void writeOffset(int offset) {
            putInteger(getBytesInternal(), 0, offset);
        }
        @Override
        public int compareTo(OffsetItem offsetItem) {
            return compareIdx(offsetItem);
        }
    }

    static class Sparse extends OffsetItem {

        private int mIdx;

        public Sparse() {
            super();
        }

        @Override
        public int getIdx() {
            return mIdx;
        }

        @Override
        public void setIdx(int idx) {
            if (idx != mIdx) {
                validateValueRange(idx);
                this.mIdx = idx;
                putShort(getBytesInternal(), 0, idx);
            }
        }
        @Override
        public boolean isNoEntry() {
            return false;
        }

        @Override
        protected void onBytesChanged() {
            super.onBytesChanged();
            this.mIdx = getShortUnsigned(getBytesInternal(), 0);
        }

        @Override
        protected int readOffset() {
            return getShortUnsigned(getBytesInternal(), 2) * 4;
        }

        @Override
        protected void writeOffset(int offset) {
            int value = offset / 4;
            validateValueRange(value);
            putShort(getBytesInternal(), 2, value);
        }

        @Override
        public int compareTo(OffsetItem offsetItem) {
            return compareOffset(offsetItem);
        }
    }

    static class Helper {
        static Creator<OffsetItem> init16() {
            return Offset16::new;
        }
        static Creator<OffsetItem> init32() {
            return Offset32::new;
        }
        static Creator<OffsetItem> initSparse() {
            return Sparse::new;
        }
    }
}
