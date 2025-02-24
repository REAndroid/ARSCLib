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
import com.reandroid.arsc.item.IntegerReference;

public class XmlIDMapHeader extends HeaderBlock {

    private final IntegerReference idsCount;

    public XmlIDMapHeader() {
        super(ChunkType.XML_RESOURCE_MAP);
        this.idsCount = new CountReference(this);
    }

    public IntegerReference getIdsCount() {
        return idsCount;
    }

    static class CountReference implements IntegerReference {

        private final XmlIDMapHeader header;

        CountReference(XmlIDMapHeader header) {
            this.header = header;
        }

        @Override
        public int get() {
            return (header.getChunkSize() - header.getHeaderSize()) / 4;
        }
        @Override
        public void set(int value) {
            header.setChunkSize(value * 4 + header.getHeaderSize());
        }
        @Override
        public String toString() {
            return Integer.toString(get());
        }
    }
}
