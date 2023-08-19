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
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.DexBlockList;
import com.reandroid.dex.instruction.Instruction;
import com.reandroid.dex.instruction.Opcode;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;
import java.util.Iterator;

public class InstructionList extends DexBlockList<Instruction> implements SmaliFormat {
    private final IntegerReference codeUnits;
    public InstructionList(IntegerReference codeUnits){
        super();
        this.codeUnits = codeUnits;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        Iterator<Instruction> iterator = iterator();
        while (iterator.hasNext()){
            writer.newLine();
            iterator.next().append(writer);
        }
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int position = reader.getPosition() + codeUnits.get() * 2;
        while (reader.getPosition() < position){
            Opcode<?> opcode = Opcode.read(reader);
            Instruction instruction = opcode.newInstance();
            add(instruction);
            instruction.readBytes(reader);
        }
        if(position != reader.getPosition()){
            // should not reach here
            reader.seek(position);
        }
        getDexPositionAlign().readBytes(reader);
    }
}
