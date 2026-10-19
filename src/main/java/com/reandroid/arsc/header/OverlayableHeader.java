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
import com.reandroid.arsc.item.FixedLengthString;
import com.reandroid.arsc.item.StringReference;

public class OverlayableHeader extends HeaderBlock {

    private final StringReference name;
    private final StringReference actor;

    public OverlayableHeader() {
        super(2, ChunkType.OVERLAYABLE);

        FixedLengthString name = new FixedLengthString(512);
        FixedLengthString actor = new FixedLengthString(512);
        this.name = name;
        this.actor = actor;

        addChild(3, name);
        addChild(4, actor);
    }

    public StringReference name() {
        return name;
    }
    public StringReference actor() {
        return actor;
    }

    @Override
    public String toString() {
        if (getChunkType() != ChunkType.OVERLAYABLE) {
            return super.toString();
        }
        return getClass().getSimpleName()
                +" {count=" + name()
                +", actor=" + actor() + '}';
    }
}
