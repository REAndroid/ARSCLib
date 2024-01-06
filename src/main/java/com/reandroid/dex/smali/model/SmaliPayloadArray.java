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
package com.reandroid.dex.smali.model;

import com.reandroid.dex.ins.InsArrayData;
import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.smali.*;

import java.io.IOException;

public class SmaliPayloadArray extends SmaliInstructionPayload<SmaliValue> {

    public SmaliPayloadArray(){
        super(new SmaliInstructionOperand.IntegerOperand());
    }

    @Override
    public SmaliInstructionOperand.IntegerOperand getOperand() {
        return (SmaliInstructionOperand.IntegerOperand) super.getOperand();
    }

    public int getWidth() {
        return getOperand().getNumber();
    }
    public void setWidth(int width) {
        getOperand().setNumber(width);
    }

    @Override
    public int getCodeUnits() {
        int count = getEntries().size();
        int width = getWidth();

        int size = 2 // opcode bytes
                + 2  // width short reference
                + 4  // count integer reference
                + width * count;

        int align = (4 - (size % 4)) % 4;

        size += align;
        return size / 2;
    }
    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.ARRAY_DATA;
    }
    @Override
    public Opcode<InsArrayData> getOpcode() {
        return Opcode.ARRAY_PAYLOAD;
    }
    @Override
    public SmaliValue newEntry(SmaliReader reader) throws IOException {
        return SmaliValue.create(reader);
    }
}
