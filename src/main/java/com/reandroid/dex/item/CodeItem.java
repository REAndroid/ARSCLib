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

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.*;
import com.reandroid.dex.instruction.TryBlock;
import com.reandroid.dex.sections.SectionType;

public class CodeItem extends DexItem {

    private final Header header;
    private final InstructionList instructionList;
    private final TryBlock tryBlock;

    public CodeItem() {
        super(3);
        this.header = new Header();
        this.instructionList = new InstructionList(header.instructionCodeUnits);
        this.tryBlock = new TryBlock(header.tryBlockCount);

        addChild(0, header);
        addChild(1, instructionList);
        addChild(2, tryBlock);
    }

    public DebugInfo getDebugInfo(){
        return getAt(SectionType.DEBUG_INFO,
                header.debugInfoOffset.get());
    }
    public InstructionList getInstructionList() {
        return instructionList;
    }
    public TryBlock getTryBlock(){
        return tryBlock;
    }
    @Override
    public String toString() {
        if(isNull()){
            return "NULL";
        }
        return header.toString()
                + "\n instructionList=" + instructionList
                + "\n tryBlock=" + tryBlock
                + "\n debug=" + getDebugInfo();
    }

    static class Header extends DexBlockItem {

        final IntegerReference registersCount;
        final IntegerReference instruction;
        final IntegerReference outs;
        final IntegerReference tryBlockCount;

        final IntegerReference debugInfoOffset;
        final IntegerReference instructionCodeUnits;

        public Header() {
            super(16);
            int offset = -2;
            this.registersCount = new IndirectShort(this, offset += 2);
            this.instruction = new IndirectShort(this, offset += 2);
            this.outs = new IndirectShort(this, offset += 2);
            this.tryBlockCount = new IndirectShort(this, offset += 2);
            this.debugInfoOffset = new IndirectInteger(this, offset += 2);
            this.instructionCodeUnits = new IndirectInteger(this, offset += 4);

        }

        @Override
        public String toString() {
            return  "registers=" + registersCount +
                    ", instruction=" + instruction +
                    ", outs=" + outs +
                    ", tries=" + tryBlockCount +
                    ", debugInfo=" + debugInfoOffset +
                    ", codeUnits=" + instructionCodeUnits;
        }
    }
}
