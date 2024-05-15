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

import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class InsGoto extends SizeXIns implements Label {

    public InsGoto(Opcode<?> opcode) {
        super(opcode);
    }
    @Override
    public int getData() {
        int size = getOpcode().size();
        if(size == 2){
            return getByteSigned();
        }
        if(size == 4){
            return getShortSigned();
        }
        return getInteger();
    }
    @Override
    public void setData(int data) {
        int size = getOpcode().size();
        if(size == 2){
            setByte(1, data);
        }
        if(size == 4){
            setShort(2, data);
        }
        setInteger(data);
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
        return HexUtil.toHex(":goto_", getTargetAddress(), 1);
    }
    @Override
    void appendCode(SmaliWriter writer) throws IOException {
        writer.newLine();
        writer.append(getOpcode().getName());
        writer.append(' ');
        writer.appendLabelName(getLabelName());
    }
    @Override
    public int getSortOrder() {
        return ExtraLine.ORDER_INSTRUCTION_LABEL;
    }
}