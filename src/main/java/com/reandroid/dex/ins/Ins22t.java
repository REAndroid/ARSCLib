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
package com.reandroid.dex.ins;

import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class Ins22t extends Size4Ins implements Label{
    public Ins22t(Opcode<?> opcode) {
        super(opcode);
    }
    @Override
    public int getRegisterA(){
        return getNibble(2);
    }
    public int getRegisterB(){
        return getNibble(3);
    }
    @Override
    public int getTargetAddress() {
        return getAddress() + getData();
    }
    @Override
    public String getLabelName() {
        return HexUtil.toHex(":cond_", getTargetAddress(), 1);
    }
    @Override
    public void appendCode(SmaliWriter writer) throws IOException {
        Opcode<?> opcode = getOpcode();
        writer.newLine();
        writer.append(opcode.getName());
        writer.append(' ');
        writer.append("v");
        writer.append(getRegisterA());
        writer.append(", ");
        writer.append("v");
        writer.append(getRegisterB());
        writer.append(", ");
        writer.append(getLabelName());
    }
    @Override
    public int getSortOrder() {
        return ExtraLine.ORDER_INSTRUCTION_LABEL;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Opcode<?> opcode = getOpcode();
        builder.append(opcode.getName());
        builder.append(' ');
        builder.append("v");
        builder.append(getRegisterA());
        builder.append(", ");
        builder.append("v");
        builder.append(getRegisterB());
        builder.append(", ");
        builder.append(getLabelName());
        return builder.toString();
    }
}