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
package com.reandroid.dex.item;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.CountedArray;
import com.reandroid.dex.base.DexAlign;
import com.reandroid.dex.base.Ule128Item;

import java.io.IOException;

public class DebugInfo extends BaseItem{

    private final Ule128Item lineStart;
    private final Ule128Item parameterCount;
    private final CountedArray<DebugString> elements;
    private final DebugOpcodes opcodes;
    private final DexAlign dexAlign;
    public DebugInfo() {
        super(4);
        this.lineStart = new Ule128Item(true);
        this.parameterCount = new Ule128Item();
        this.elements = new CountedArray<>(parameterCount, CREATOR);
        this.opcodes = new DebugOpcodes(lineStart);
        this.dexAlign = new DexAlign();

        addChild(0, lineStart);
        addChild(1, parameterCount);
        addChild(2, elements);
        addChild(3, opcodes);
        //addChild(4, dexAlign);

    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
    }
    @Override
    public String toString() {
        return "DebugInfo{" +
                "lineStart=" + lineStart.get() +
                ", parameterCount=" + parameterCount.get() +
                ", elements=" + elements.getChildesCount() +
                '}';
    }

    private static final Creator<DebugString> CREATOR = new Creator<DebugString>() {
        @Override
        public DebugString[] newInstance(int length) {
            return new DebugString[length];
        }
        @Override
        public DebugString newInstance() {
            return new DebugString();
        }
    };

}
