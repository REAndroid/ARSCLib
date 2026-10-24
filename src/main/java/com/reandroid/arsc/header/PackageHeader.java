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

    public PackageHeader() {
        super(7, ChunkType.PACKAGE);

        setupBlock(3, new IntegerItem());                      // packageId
        setupBlock(4, new FixedLengthString(256));  // packageName
        setupBlock(5, new IntegerItem());                      // typeStringPoolOffset
        setupBlock(6, new IntegerItem());                      // typeStringPoolCount
        setupBlock(7, new IntegerItem());                      // specStringPoolOffset
        setupBlock(8, new IntegerItem());                      // specStringPoolCount
        setupBlock(9, new TypeIdOffset());                     // typeIdOffset
    }

    public IntegerReference packageId() {
        return block(3);
    }
    public StringReference packageName() {
        return block(4);
    }
    public IntegerReference typeStringPoolOffset() {
        return block(5);
    }
    public IntegerReference typeStringPoolCount() {
        return block(6);
    }
    public IntegerReference specStringPoolOffset() {
        return block(7);
    }
    public IntegerReference specStringPoolCount() {
        return block(8);
    }
    public IntegerReference typeIdOffset() {
        return block(9);
    }
    public void setTypeIdOffset(int offset) {
        typeIdOffset().set(offset);
    }
    public int getTypeIdOffset() {
        return typeIdOffset().get();
    }

    static class TypeIdOffset extends IntegerItem {

        TypeIdOffset() {
        }

        private PackageHeader header() {
            return getParentInstance(PackageHeader.class);
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
            IntegerReference headerSize = header().headerSize();
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
            return header().headerSize().get() < 288;
        }

        @Override
        public void readBytes(BlockReader reader) throws IOException {
            if (isNull()) {
                return;
            }
            super.readBytes(reader);
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
