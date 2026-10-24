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
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.IntegerReference;

public class XmlNodeHeader extends HeaderBlock {

    private final ChunkType chunkType;

    public XmlNodeHeader(ChunkType chunkType) {
        super(2, chunkType);
        this.chunkType = chunkType;

        setupBlock(3, new IntegerItem());         //lineNumber
        setupBlock(4, new IntegerItem(-1)); //commentReference
    }

    public IntegerReference lineNumber() {
        return block(3);
    }
    public IntegerItem commentReference() {
        return block(4);
    }

    @Override
    public String toString() {
        if (getChunkType() != chunkType) {
            return super.toString();
        }
        return getClass().getSimpleName()
                +" {lineNumber=" + lineNumber()
                +", commentReference=" + commentReference() + '}';
    }
}
