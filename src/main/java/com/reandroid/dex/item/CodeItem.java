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

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.ShortItem;

import java.io.IOException;

public class CodeItem extends BaseItem {

    private final ShortItem registersCount;
    private final ShortItem instruction;
    private final ShortItem outs;
    private final ShortItem triesSize;
    private final IntegerItem debugInfoOffset;
    private final IntegerItem instructionCount;

    public CodeItem() {
        super(6);
        registersCount = new ShortItem();
        instruction = new ShortItem();
        outs = new ShortItem();
        triesSize = new ShortItem();
        debugInfoOffset = new IntegerItem();
        instructionCount = new IntegerItem();

        addChild(0, registersCount);
        addChild(1, instruction);
        addChild(2, outs);
        addChild(3, triesSize);
        addChild(4, debugInfoOffset);
        addChild(5, instructionCount);
        setNull(true);
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
    }

    @Override
    public String toString() {
        if(isNull()){
            return "NULL";
        }
        return  "registersCount=" + registersCount +
                ", instruction=" + instruction +
                ", outs=" + outs +
                ", triesSize=" + triesSize +
                ", debugInfoOffset=" + debugInfoOffset +
                ", instructionCount=" + instructionCount;
    }
}
