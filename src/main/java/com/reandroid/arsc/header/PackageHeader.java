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
package com.reandroid.arsc.header;

import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.FixedLengthString;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.StringReference;

import java.io.IOException;
import java.io.InputStream;

public class PackageHeader extends HeaderBlock {

    private final IntegerItem packageId;
    private final FixedLengthString packageName;

    private final IntegerItem typeStringPoolOffset;
    private final IntegerItem typeStringPoolCount;
    private final IntegerItem specStringPoolOffset;
    private final IntegerItem specStringPoolCount;
    private final IntegerItem typeIdOffset;

    public PackageHeader() {
        super(7, ChunkType.PACKAGE);
        this.packageId = new IntegerItem();
        this.packageName = new FixedLengthString(256);

        this.typeStringPoolOffset = new IntegerItem();
        this.typeStringPoolCount = new IntegerItem();
        this.specStringPoolOffset = new IntegerItem();
        this.specStringPoolCount = new IntegerItem();

        this.typeIdOffset = new TypeIdOffset(this);

        addChild(3, this.packageId);
        addChild(4, this.packageName);
        addChild(5, this.typeStringPoolOffset);
        addChild(6, this.typeStringPoolCount);
        addChild(7, this.specStringPoolOffset);
        addChild(8, this.specStringPoolCount);
        addChild(9, this.typeIdOffset);
    }

    public IntegerReference getPackageId() {
        return packageId;
    }
    public StringReference packageName() {
        return packageName;
    }
    public IntegerReference typeStringPoolOffset() {
        return typeStringPoolOffset;
    }
    public IntegerReference typeStringPoolCount() {
        return typeStringPoolCount;
    }
    public IntegerReference specStringPoolOffset() {
        return specStringPoolOffset;
    }
    public IntegerReference specStringPoolCount() {
        return specStringPoolCount;
    }
    public IntegerReference typeIdOffset() {
        return typeIdOffset;
    }
    public void setTypeIdOffset(int offset) {
        typeIdOffset.set(offset);
    }
    public int getTypeIdOffset() {
        return typeIdOffset.get();
    }

    static class TypeIdOffset extends IntegerItem {

        private final PackageHeader header;

        TypeIdOffset(PackageHeader header) {
            this.header = header;
        }

        @Override
        public int get() {
            if (isNull()) {
                return 0;
            }
            return super.get();
        }

        @Override
        public void set(int value) {
            IntegerReference headerSize = header.headerSize();
            if (value == 0) {
                if (headerSize.get() >= 288) {
                    headerSize.set(284);
                }
            } else if (headerSize.get() < 288) {
                headerSize.set(288);
            }
            super.set(value);
        }

        @Override
        public boolean isNull() {
            return header.headerSize().get() < 288;
        }

        @Override
        public void onReadBytes(BlockReader reader) throws IOException {
            if (isNull()) {
                return;
            }
            super.onReadBytes(reader);
        }

        @Override
        public int readBytes(InputStream inputStream) throws IOException {
            if (isNull()) {
                return 0;
            }
            return super.readBytes(inputStream);
        }
    }
}
