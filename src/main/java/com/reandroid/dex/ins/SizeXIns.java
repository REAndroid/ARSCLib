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
import com.reandroid.dex.base.DexException;
import com.reandroid.dex.common.Register;
import com.reandroid.dex.common.RegistersTable;
import com.reandroid.dex.data.CodeItem;
import com.reandroid.dex.data.MethodDef;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.reference.InsIdSectionReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliValidateException;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliInstruction;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class SizeXIns extends Ins {

    private final ByteArray valueBytes;
    private final InsIdSectionReference sectionReference;

    public SizeXIns(Opcode<?> opcode, boolean hasSectionData) {
        super(opcode);
        this.valueBytes = new ByteArray();

        addChild(0, valueBytes);
        valueBytes.setSize(opcode.size());
        valueBytes.putShort(0, opcode.getValue());

        InsIdSectionReference sectionReference;
        if (hasSectionData) {
            sectionReference = new InsIdSectionReference(this);
        }else {
            sectionReference = null;
        }
        this.sectionReference = sectionReference;
    }

    public SizeXIns(Opcode<?> opcode) {
        this(opcode, opcode.getSectionType() != null);
    }

    public SectionType<? extends IdItem> getSectionType(){
        return getOpcode().getSectionType();
    }
    public SectionType<? extends IdItem> getSectionType2(){
        return getOpcode().getSectionType2();
    }


    final ByteArray getValueBytes() {
        return valueBytes;
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
        if((value & 0xffff0000) != 0 && value != (short) value) {
           throw new InstructionException("Short value out of range "
                    + HexUtil.toHex(value, 4) + " > 0xffff", this);
        }
        valueBytes.putShort(offset, value);
    }

    int getByteSigned(){
        return valueBytes.get(1);
    }
    void setByte(int offset, int value){
        if((value & 0xffffff00) != 0 && value != (byte) value) {
            throw new InstructionException("Byte value out of range "
                    + HexUtil.toHex(value & 0xffff, 2) + "> 0xff", this);
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
        pullSectionItem();
    }
    void pullSectionItem(){
        InsIdSectionReference sectionReference = this.sectionReference;
        if(sectionReference != null){
            sectionReference.pullItem();
        }
    }
    public IdItem getSectionId() {
        InsIdSectionReference sectionReference = this.sectionReference;
        if(sectionReference != null){
            return sectionReference.getItem();
        }
        return null;
    }
    public void setSectionId(IdItem item){
        sectionReference.setItem(item);
    }
    public Key getKey() {
        InsIdSectionReference sectionReference = this.sectionReference;
        if(sectionReference != null) {
            return sectionReference.getKey();
        }
        return null;
    }
    public void setKey(Key key) {
        sectionReference.setItem(key);
    }
    /**
     * Use setKey
     */
    @Deprecated
    public void setSectionIdKey(Key key){
        setKey(key);
    }
    /**
     * Use getKey
     */
    @Deprecated
    public Key getSectionIdKey() {
        return getKey();
    }

    public int getData(){
        return getShortUnsigned(2);
    }
    public int getSignedData(){
        return getData();
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
        InsIdSectionReference sectionItem = this.sectionReference;
        if(sectionItem != null){
            sectionItem.refresh();
        }
    }

    public RegistersIterator getRegistersIterator() {
        if(this instanceof RegistersSet){
            RegistersTable table = getRegistersTable();
            if(table != null){
                return new RegistersIterator(table, (RegistersSet) this);
            }
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
    public void appendCode(SmaliWriter writer) throws IOException {
        Opcode<?> opcode = getOpcode();
        writer.append(opcode.getName());
        writer.append(' ');
        appendRegisters(writer);
        appendOperand(writer);
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
    void appendOperand(SmaliWriter writer) throws IOException {
        writer.append(", ");
        IdItem sectionItem = getSectionId();
        if(sectionItem != null){
            sectionItem.append(writer);
        }else {
            appendHexData(writer);
        }
    }
    void appendHexData(SmaliWriter writer) throws IOException {
        writer.appendHex(getSignedData());
    }

    @Override
    public void replaceKeys(Key search, Key replace){
        Key key = getKey();
        if(key == null){
            return;
        }
        Key key2 = key.replaceKey(search, replace);
        if(key != key2){
            setKey(key2);
        }
    }
    @Override
    public Iterator<IdItem> usedIds(){
        return SingleIterator.of(getSectionId());
    }
    public void merge(Ins ins){
        SizeXIns coming = (SizeXIns) ins;
        SectionType<? extends IdItem> sectionType = coming.getSectionType();
        if(sectionType == null){
            this.valueBytes.set(coming.valueBytes.getBytes().clone());
            return;
        }
        setKey(coming.getKey());
        this.sectionReference.validate();
        RegistersSet comingSet = (RegistersSet) coming;
        RegistersSet set = (RegistersSet) this;
        int count = comingSet.getRegistersCount();
        set.setRegistersCount(count);
        for(int i = 0; i < count; i++){
            set.setRegister(i, comingSet.getRegister(i));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SizeXIns sizeXIns = (SizeXIns) obj;
        if(getIndex() != sizeXIns.getIndex()){
            return false;
        }
        if(getOpcode() != sizeXIns.getOpcode()){
            return false;
        }
        if(this instanceof RegistersSet){
            if(!areEqual((RegistersSet) this, (RegistersSet) sizeXIns)){
                return false;
            }
        }
        if(getSectionType() != null){
            return Objects.equals(getKey(), sizeXIns.getKey());
        }else {
            return getData() == sizeXIns.getData();
        }
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash + getIndex();
        hash = hash * 31 + getOpcode().getValue();
        if(this instanceof RegistersSet){
            RegistersSet set = (RegistersSet) this;
            int count = set.getRegistersCount();
            hash = hash * 31 + count;
            for(int i = 0; i < count; i++){
                hash = hash * 31 + set.getRegister(i);
            }
        }
        hash = hash * 31;
        Key key = getKey();
        if(key != null){
            hash = hash + key.hashCode();
        }else {
            hash = hash + getData();
        }
        return hash;
    }

    @Override
    public void fromSmali(SmaliInstruction smaliInstruction) throws IOException {
        try {
            validateOpcode(smaliInstruction);
            fromSmaliRegisters(smaliInstruction);
            fromSmaliKey(smaliInstruction);
            fromSmaliData(smaliInstruction);
        } catch (DexException exception) {
            throw new SmaliValidateException(exception.getMessage(), smaliInstruction);
        }
    }
    private void fromSmaliRegisters(SmaliInstruction smaliInstruction){
        if(!(this instanceof RegistersSet)){
            return;
        }
        if(smaliInstruction.getRegistersTable() == null){
            smaliInstruction.setRegistersTable(getRegistersTable());
        }
        int count = smaliInstruction.getRegistersCount();
        RegistersSet registersSet = (RegistersSet) this;
        registersSet.setRegistersCount(count);
        for(int i = 0; i < count; i++){
            Register register = smaliInstruction.getRegister(i);
            registersSet.setRegister(i, register.getValue());
        }
    }
    private void fromSmaliKey(SmaliInstruction smaliInstruction){
        if(getSectionType() == null){
            return;
        }
        setKey(smaliInstruction.getKey());
    }
    private void fromSmaliData(SmaliInstruction smaliInstruction) throws IOException {
        Number data = smaliInstruction.getData();
        if(data == null){
            return;
        }
        if(data instanceof Long){
            setLong((Long) data);
        }else {
            setData(data.intValue());
        }
    }

    private static boolean areEqual(RegistersSet set1, RegistersSet set2){
        if(set1 == set2){
            return true;
        }
        if(set1 == null){
            return false;
        }
        int count = set1.getRegistersCount();
        if(count != set2.getRegistersCount()){
            return false;
        }
        for(int i = 0; i < count; i++){
            if(set1.getRegister(i) != set2.getRegister(i)){
                return false;
            }
        }
        return true;
    }

    public static String buildTrace(SizeXIns sizeXIns, IdItem item, int data) {
        InstructionList instructionList = sizeXIns.getInstructionList();
        if(instructionList == null) {
            return "removed instruction";
        }
        CodeItem codeItem = instructionList.getCodeItem();
        if(codeItem == null){
            return "removed instruction list";
        }
        if(codeItem.getParent() == null){
            return "removed code item";
        }
        MethodDef methodDef = codeItem.getMethodDef();
        StringBuilder builder = new StringBuilder();
        if(methodDef != null){
            builder.append("method = ");
            builder.append(methodDef.getKey());
            builder.append(", ");
        }
        builder.append(sizeXIns.getOpcode());
        String key = toDebugString(item);
        if(key == null){
            key = HexUtil.toHex(data, 1);
        }
        builder.append(", key = '");
        builder.append(key);
        builder.append('\'');
        return builder.toString();
    }
    static String toDebugString(IdItem item) {
        if(item == null){
            return null;
        }
        Key key = item.getKey();
        if(key == null){
            return null;
        }
        String keyString = key.toString();
        if(keyString == null){
            return null;
        }
        if(keyString.length() > 100){
            keyString = keyString.substring(0, 100) + "...";
        }
        if(keyString.startsWith("\"")){
            keyString = keyString.substring(1);
        }
        if(keyString.endsWith("\"")){
            keyString = keyString.substring(0, keyString.length() - 1);
        }
        return keyString;
    }
}
