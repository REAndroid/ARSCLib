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

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteArray;
import com.reandroid.dex.index.IndexItemEntry;
import com.reandroid.dex.index.StringId;
import com.reandroid.dex.item.InstructionList;
import com.reandroid.dex.item.StringData;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class SizeXIns extends Ins implements RegisterNumber{

    private final ByteArray valueBytes;
    private IndexItemEntry mSectionItem;

    public SizeXIns(Opcode<?> opcode) {
        super(opcode);
        this.valueBytes = new ByteArray();
        addChild(0, valueBytes);
        valueBytes.setSize(opcode.size());
        valueBytes.putShort(0, opcode.getValue());
    }

    public int getInteger(int offset){
        return valueBytes.getInteger(offset);
    }
    public void setInteger(int offset, int value){
        valueBytes.putInteger(offset, value);
    }
    public int getShortUnsigned(int offset){
        return valueBytes.getShortUnsigned(2 + offset);
    }

    public int getByte(int offset){
        return valueBytes.get(offset);
    }
    public void setByte(int offset, int value){
        valueBytes.put(offset, (byte) value);
    }
    public int getByteUnsigned(int offset){
        return valueBytes.get(offset) & 0xff;
    }
    public int getShort(int offset){
        return valueBytes.getShort(offset);
    }
    public void setShort(int offset, int value){
        valueBytes.putShort(2 + offset, value);
    }
    public int getNibble(int index){
        int i = getByteUnsigned(index / 2);
        index = index % 2;
        return (i >> index * 4) & 0x0f;
    }
    public void setNibble(int index, int value){
        int i = index / 2;
        int half = getByteUnsigned(i);
        int shift1 = ((index) % 2) * 4;
        int shift2 = ((index + 1) % 2) * 4;
        int result = (value << shift1) | ((half >> shift2) & 0x0f);
        setByte(i, result);
    }

    public ByteArray getValueBytes() {
        return valueBytes;
    }

    @Override
    public int getRegistersCount() {
        return 1;
    }
    @Override
    public int getRegister(int index) {
        return getByteUnsigned(1);
    }
    @Override
    public void setRegister(int index, int value) {
        setByte(1, value);
    }

    @Override
    public int countBytes(){
        return valueBytes.countBytes();
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        valueBytes.onReadBytes(reader);
        cacheSectionItem();
    }
    private void cacheSectionItem(){
        SectionType<? extends IndexItemEntry> sectionType = getOpcode().getSectionType();
        if(sectionType == null){
            return;
        }
        int data = getData();
        this.mSectionItem = get(sectionType, data);
        if(mSectionItem instanceof StringId){
            ((StringId) mSectionItem).addStringUsage(StringData.USAGE_INSTRUCTION);
        }
    }
    public IndexItemEntry getSectionItem() {
        return mSectionItem;
    }

    public int getData(){
        return getValueBytes().getShortUnsigned(2);
    }
    public void setData(int data){
        getValueBytes().putShort(2, data);
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        IndexItemEntry itemId = this.mSectionItem;
        if(itemId != null){
            setData(itemId.getIndex());
        }
    }

    public Registers getRegisters() {
        return new Registers(getRegisterFactory(), this);
    }
    private RegisterFactory getRegisterFactory() {
        InstructionList instructionList = getParentInstance(InstructionList.class);
        if(instructionList != null){
            return instructionList.getRegisterFactory();
        }
        return null;
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
        getRegisters().append(writer);
        if(method){
            writer.append('}');
        }
        appendCodeData(writer);
    }
    void appendCodeData(SmaliWriter writer) throws IOException {
        writer.append(", ");
        int data = getData();
        IndexItemEntry sectionItem = getSectionItem();
        if(sectionItem != null){
            sectionItem.append(writer);
        }else {
            writer.append(HexUtil.toHex(data, 1));
        }
    }
}
