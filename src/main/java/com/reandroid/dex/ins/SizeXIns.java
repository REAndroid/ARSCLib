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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteArray;
import com.reandroid.dex.index.IndexItemEntry;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class SizeXIns extends Ins {
    private final ByteArray valueBytes;
    private IndexItemEntry mItemId;
    public SizeXIns(Opcode<?> opcode) {
        super(opcode);
        this.valueBytes = new ByteArray();
        addChild(0, valueBytes);
        valueBytes.setSize(opcode.size());
        valueBytes.putShort(0, opcode.getValue());
    }
    public int getInteger(int offset){
        return valueBytes.getInteger(2 + offset);
    }
    public int getShortUnsigned(int offset){
        return valueBytes.getShortUnsigned(2 + offset);
    }

    public int getByte(int offset){
        return valueBytes.get(offset);
    }
    public int getByteUnsigned(int offset){
        return valueBytes.get(offset) & 0xff;
    }
    public int getShort(int offset){
        return valueBytes.getShort(2 + offset);
    }
    public int getNibble(int index){
        int i = getByteUnsigned(index / 2);
        index = (index + 1) % 2;
        return (i >> index * 4) & 0x0f;
    }

    public ByteArray getValueBytes() {
        return valueBytes;
    }
    public int getRegisterA(){
        return getNibble(3);
    }

    @Override
    public int countBytes(){
        return valueBytes.countBytes();
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        valueBytes.onReadBytes(reader);
        SectionType<? extends IndexItemEntry> sectionType = getOpcode().getSectionType();
        if(sectionType == null){
            return;
        }
        int data = getData();
        this.mItemId = get(sectionType, data);
    }
    public int getData(){
        return getValueBytes().getShortUnsigned(2);
    }
    public void setData(int data){
        getValueBytes().putShort(2, data);
    }

    @Override
    protected void onRefreshed() {
        IndexItemEntry itemId = this.mItemId;
        if(itemId != null){
            setData(itemId.getIndex());
        }
    }

    @Override
    void appendCode(SmaliWriter writer) throws IOException {
        Opcode<?> opcode = getOpcode();
        writer.newLine();
        writer.append(opcode.getName());
        writer.append(' ');
        boolean method = opcode.getSectionType() == SectionType.METHOD_ID;
        if(method){
            writer.append('{');
        }
        writer.append("v");
        writer.append(Integer.toString(getRegisterA()));
        if(method){
            writer.append('}');
        }
        writer.append(", ");
        int data = getData();
        SectionType<? extends IndexItemEntry> sectionType = opcode.getSectionType();
        if(sectionType != null){
            IndexItemEntry sectionData = get(sectionType, data);
            if(sectionData != null){
                sectionData.append(writer);
            }
        }else {
            writer.append(HexUtil.toHex(data, 1));
        }
    }
    @Override
    public String toString() {
        Opcode<?> opcode = getOpcode();
        if(opcode.size() < 4){
            return super.toString();
        }
        StringBuilder builder = new StringBuilder();
        builder.append(opcode);
        builder.append(" v");
        builder.append(getRegisterA());
        builder.append(", ");
        int data = getValueBytes().getShortUnsigned(2);
        SectionType<?> sectionType = opcode.getSectionType();
        if(sectionType != null){
            Block sectionData = get(sectionType, data);
            builder.append(sectionData);
        }else {
            builder.append(HexUtil.toHex(data, 2));
        }
        return builder.toString();
    }
}
