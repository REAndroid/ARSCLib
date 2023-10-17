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
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class SizeXIns extends Ins {

    private final ByteArray valueBytes;
    private IdItem mSectionItem;

    public SizeXIns(Opcode<?> opcode) {
        super(opcode);
        this.valueBytes = new ByteArray();
        addChild(0, valueBytes);
        valueBytes.setSize(opcode.size());
        valueBytes.putShort(0, opcode.getValue());
    }

    public SectionType<? extends IdItem> getSectionType(){
        return getOpcode().getSectionType();
    }


    long getLong(){
        return getLong(valueBytes.getBytes(), 2);
    }
    void setLong(long value){
        putLong(valueBytes.getBytes(), 2, value);
    }

    int getInteger(){
        return valueBytes.getInteger(2);
    }
    void setInteger(int value){
        valueBytes.putInteger(2, value);
    }

    int getShortUnsigned(int offset){
        return valueBytes.getShortUnsigned(offset);
    }
    int getShortSigned(){
        return valueBytes.getShort(2);
    }
    void setShort(int offset, int value){
        if(value != (value & 0xffff) && (value & 0xffff0000) != 0xffff0000){
           throw new InstructionException("Short value out of range "
                    + HexUtil.toHex(value, 4) + " > 0xffff", this);
        }
        valueBytes.putShort(offset, value);
    }

    int getByteSigned(){
        return valueBytes.get(1);
    }
    void setByte(int offset, int value){
        if(value != (value & 0xff) && (value & 0xffffff00) != 0xffffff00){
            throw new InstructionException("Byte value out of range "
                    + HexUtil.toHex(value, 2) + "> 0xff", this);
        }
        valueBytes.put(offset, (byte) value);
    }
    int getByteUnsigned(int offset){
        return valueBytes.getByteUnsigned(offset);
    }
    int getNibble(int index){
        int i = valueBytes.getByteUnsigned(index / 2);
        int shift = (index % 2) * 4;
        return (i >> shift) & 0x0f;
    }
    void setNibble(int index, int value){
        if((value & 0x0f) != value){
            throw new InstructionException("Nibble value out of range "
                    + HexUtil.toHex(value, 1) + " > 0xf", this);
        }
        int i = index / 2;
        int half = valueBytes.getByteUnsigned(i);
        int shift = (index % 2) * 4;
        int mask = 0x0f;
        if(shift == 0){
            mask = 0xf0;
        }
        int result = (value << shift) | (half & mask);
        valueBytes.put(i, (byte) result);
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
    void cacheSectionItem(){
        SectionType<? extends IdItem> sectionType = getSectionType();
        if(sectionType == null){
            return;
        }
        int data = getData();
        this.mSectionItem = get(sectionType, data);
        if(this.mSectionItem != null){
            this.mSectionItem.addUsageType(IdItem.USAGE_INSTRUCTION);
        }
    }
    public IdItem getSectionItem() {
        return mSectionItem;
    }
    public Key getSectionItemKey() {
        IdItem entry = getSectionItem();
        if(entry != null){
            return entry.getKey();
        }
        return null;
    }
    public void setSectionItem(Key key){
        Section<? extends IdItem> section = getSection(getSectionType());
        IdItem item = section.getOrCreate(key);
        setSectionItem(item);
    }
    public void setSectionItem(IdItem item){
        this.mSectionItem = item;
        setData(item.getIndex());
    }
    public void setSectionIndex(int index){
        setData(index);
        cacheSectionItem();
    }

    public int getData(){
        return getShortUnsigned(2);
    }
    public void setData(int data){
        setShort(2, data);
    }
    @Override
    public int getOutSize(){
        if(getOpcode().hasOutRegisters()){
            return ((RegistersSet) this).getRegistersCount();
        }
        return 0;
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        updateSectionItem();
    }
    void updateSectionItem(){
        IdItem itemId = this.mSectionItem;
        if(itemId != null){
            setData(itemId.getIndex());
            itemId.addUsageType(IdItem.USAGE_INSTRUCTION);
        }
    }

    public RegistersIterator getRegistersIterator() {
        if(this instanceof RegistersSet){
            return new RegistersIterator(getRegistersTable(), (RegistersSet) this);
        }
        return null;
    }
    private RegistersTable getRegistersTable() {
        InstructionList instructionList = getParentInstance(InstructionList.class);
        if(instructionList != null){
            return instructionList.getCodeItem();
        }
        return null;
    }
    @Override
    void appendCode(SmaliWriter writer) throws IOException {
        Opcode<?> opcode = getOpcode();
        writer.newLine();
        writer.append(opcode.getName());
        writer.append(' ');
        appendRegisters(writer);
        appendCodeData(writer);
    }
    void appendRegisters(SmaliWriter writer) throws IOException {
        RegistersIterator iterator = getRegistersIterator();
        if(iterator == null){
            return;
        }
        boolean out = getOpcode().hasOutRegisters();
        if(out){
            writer.append('{');
        }
        iterator.append(writer);
        if(out){
            writer.append('}');
        }
    }
    void appendCodeData(SmaliWriter writer) throws IOException {
        writer.append(", ");
        int data = getData();
        IdItem sectionItem = getSectionItem();
        if(sectionItem != null){
            sectionItem.append(writer);
        }else {
            writer.append(HexUtil.toHex(data, 1));
        }
    }
}
