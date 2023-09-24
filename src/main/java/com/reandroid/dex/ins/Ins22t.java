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

public class Ins22t extends Size4Ins implements Label {

    public Ins22t(Opcode<?> opcode) {
        super(opcode);
    }

    @Override
    public int getData(){
        return getShort(2);
    }
    @Override
    public void setData(int data){
        setShort(2, data);
    }
    @Override
    public int getRegistersCount() {
        return 2;
    }
    @Override
    public int getRegister(int index) {
        return getNibble(2 + index);
    }
    @Override
    public void setRegister(int index, int value) {
        setNibble(2 + index, value);
    }
    @Override
    public int getTargetAddress() {
        return getAddress() + getData();
    }
    @Override
    public void setTargetAddress(int targetAddress){
        setData(targetAddress - getAddress());
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
        getRegisters().append(writer);
        writer.append(", ");
        writer.append(getLabelName());
    }
    @Override
    public int getSortOrder() {
        return ExtraLine.ORDER_INSTRUCTION_LABEL;
    }
}