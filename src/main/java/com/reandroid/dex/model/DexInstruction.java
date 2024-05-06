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
package com.reandroid.dex.model;

import com.reandroid.dex.common.Register;
import com.reandroid.dex.common.RegisterFormat;
import com.reandroid.dex.common.RegisterType;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.id.FieldId;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.id.MethodId;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.ins.*;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliInstruction;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class DexInstruction extends DexCode {

    private final DexMethod dexMethod;
    private final Ins mIns;

    public DexInstruction(DexMethod dexMethod, Ins ins) {
        this.dexMethod = dexMethod;
        this.mIns = ins;
    }

    public boolean usesRegister(int register) {
        int count = getRegistersCount();
        for(int i = 0; i < count; i++) {
            if(register == getRegister(i)) {
                return true;
            }
        }
        return false;
    }
    public boolean usesRegister(int register, RegisterType type) {
        RegisterFormat format = getOpcode().getRegisterFormat();
        int count = getRegistersCount();
        for(int i = 0; i < count; i++) {
            if(register == getRegister(i) && type.is(format.get(i))) {
                return true;
            }
        }
        return false;
    }
    public int getAddress(){
        return getIns().getAddress();
    }
    public int getCodeUnits(){
        return getIns().getCodeUnits();
    }
    public List<Register> getLocalFreeRegisters(){
        return getDexMethod().getLocalFreeRegisters(getIndex());
    }
    public String getString(){
        IdItem idItem = getIdSectionEntry();
        if(idItem instanceof StringId){
            return ((StringId) idItem).getString();
        }
        return null;
    }
    public void setString(String text){
        setKey(StringKey.create(text));
    }
    public DexInstruction setStringWithJumbo(String text){
        SizeXIns sizeXIns = (SizeXIns) getIns();
        StringId stringId = sizeXIns.getOrCreateSectionItem(
                SectionType.STRING_ID, StringKey.create(text));
        if((stringId.getIdx() & 0xffff0000) == 0 || !sizeXIns.is(Opcode.CONST_STRING)){
            sizeXIns.setSectionId(stringId);
            return this;
        }
        int register = ((RegistersSet)sizeXIns).getRegister();
        InsConstStringJumbo jumbo = sizeXIns.replace(Opcode.CONST_STRING_JUMBO);
        jumbo.setRegister(register);
        jumbo.setSectionId(stringId);
        return DexInstruction.create(getDexMethod(), jumbo);
    }
    public FieldKey getFieldKey(){
        IdItem idItem = getIdSectionEntry();
        if(idItem instanceof FieldId){
            return ((FieldId) idItem).getKey();
        }
        return null;
    }
    public MethodKey getMethodKey(){
        IdItem idItem = getIdSectionEntry();
        if(idItem instanceof MethodId){
            return ((MethodId) idItem).getKey();
        }
        return null;
    }
    public Key getKey(){
        IdItem entry = getIdSectionEntry();
        if(entry != null){
            return entry.getKey();
        }
        return null;
    }
    public void setKey(Key key){
        Ins ins = getIns();
        if(ins instanceof SizeXIns){
            ((SizeXIns) ins).setSectionIdKey(key);
        }
    }
    public IdItem getIdSectionEntry(){
        Ins ins = getIns();
        if(ins instanceof SizeXIns){
            return  ((SizeXIns) ins).getSectionId();
        }
        return null;
    }
    public int getRegister(int i){
        if(i < 0){
            return -1;
        }
        Ins ins = getIns();
        if(ins instanceof RegistersSet){
            RegistersSet registersSet = (RegistersSet) ins;
            if(i >= registersSet.getRegistersCount()){
                return -1;
            }
            return registersSet.getRegister(i);
        }
        return -1;
    }
    public int getRegister(){
        return getRegister(0);
    }
    public int getRegistersCount(){
        Ins ins = getIns();
        if(ins instanceof RegistersSet){
            return ((RegistersSet) ins).getRegistersCount();
        }
        return 0;
    }
    public void setRegister(int register){
        setRegister(0, register);
    }
    public void setRegister(int i, int register){
        Ins ins = getIns();
        if(ins instanceof RegistersSet){
            ensureRegistersCount(i + 1);
            ((RegistersSet) ins).setRegister(i, register);
        }
    }
    public boolean removeRegisterAt(int index) {
        Ins ins = getIns();
        if(ins instanceof RegistersSet) {
            return ((RegistersSet) ins).removeRegisterAt(index);
        }
        return false;
    }
    private void ensureRegistersCount(int count){
        if(count > getRegistersCount()){
            if(getOpcode().getRegisterFormat().isOut()){
                setRegistersCount(count);
            }
        }
    }
    public void setRegistersCount(int count){
        Ins ins = getIns();
        if(ins instanceof RegistersSet){
            ((RegistersSet) ins).setRegistersCount(count);
        }
    }
    public boolean is(Opcode<?> opcode){
        return opcode == getOpcode();
    }
    public boolean isConstString(){
        return getIns() instanceof ConstString;
    }
    public boolean isNumber(){
        return getIns() instanceof ConstNumber;
    }
    public boolean isNumberLong(){
        return getIns() instanceof ConstNumberLong;
    }
    public int getTargetAddress(){
        Ins ins = getIns();
        if(ins instanceof Label){
            return ((Label) ins).getTargetAddress();
        }
        return -1;
    }
    public void setTargetAddress(int address){
        Ins ins = getIns();
        if(ins instanceof Label){
            ((Label) ins).setTargetAddress(address);
        }
    }
    public Integer getAsInteger(){
        Ins ins = getIns();
        if(ins instanceof ConstNumber){
            return ((ConstNumber) ins).get();
        }
        return null;
    }
    public Long getAsLong(){
        Ins ins = getIns();
        if(ins instanceof ConstNumberLong){
            return ((ConstNumberLong) ins).getLong();
        }
        return null;
    }
    public void setAsInteger(int value){
        Ins ins = getIns();
        if(ins instanceof ConstNumber){
            ((ConstNumber) ins).set(value);
        }
    }
    public void setAsLong(long value){
        Ins ins = getIns();
        if(ins instanceof ConstNumberLong){
            ((ConstNumberLong) ins).set(value);
        }
    }
    public DexInstruction replace(String smaliString) throws IOException {
        return replace(SmaliReader.of(smaliString));
    }
    public DexInstruction replace(SmaliReader reader) throws IOException {
        SmaliInstruction smaliInstruction = new SmaliInstruction();
        smaliInstruction.parse(reader);
        Ins ins = getIns().replace(smaliInstruction.getOpcode());
        ins.fromSmali(smaliInstruction);
        return DexInstruction.create(getDexMethod(), ins);
    }
    public DexInstruction createNext(String smaliString) throws IOException {
        return createNext(SmaliReader.of(smaliString));
    }
    public DexInstruction createNext(SmaliReader reader) throws IOException {
        SmaliInstruction smaliInstruction = new SmaliInstruction();
        smaliInstruction.parse(reader);
        Ins ins = getIns().createNext(smaliInstruction.getOpcode());
        ins.fromSmali(smaliInstruction);
        return DexInstruction.create(getDexMethod(), ins);
    }
    public DexInstruction replace(Opcode<?> opcode){
        return DexInstruction.create(getDexMethod(), getIns().replace(opcode));
    }
    public DexInstruction createNext(Opcode<?> opcode){
        return DexInstruction.create(getDexMethod(), getIns().createNext(opcode));
    }
    public boolean removeSelf(){
        Ins ins = getIns();
        InstructionList instructionList = ins.getInstructionList();
        if(instructionList != null){
            return instructionList.remove(ins);
        }
        return false;
    }
    public Opcode<?> getOpcode(){
        return getIns().getOpcode();
    }
    public Ins getIns() {
        return mIns;
    }

    @Override
    public boolean uses(Key key) {
        Key insKey = getKey();
        if(insKey != null){
            return insKey.uses(key);
        }
        return false;
    }
    @Override
    public DexMethod getDexMethod() {
        return dexMethod;
    }

    public DexDeclaration findDeclaration(){
        Key key = getKey();
        if(key != null){
            DexClassRepository dexClassRepository = getClassRepository();
            if(dexClassRepository != null){
                return dexClassRepository.getDexDeclaration(key);
            }
        }
        return null;
    }
    public DexInstruction getNext(){
        return getDexMethod().getInstruction(getIndex() + 1);
    }
    public DexInstruction getPrevious(){
        return getDexMethod().getInstruction(getIndex() - 1);
    }
    public int getIndex(){
        return getIns().getIndex();
    }
    public void moveBackward(){
        int index = getIndex();
        if(index != 0){
            getIns().moveTo(index - 1);
        }
    }
    public void moveForward(){
        int index = getIndex() + 1;
        if(index < getDexMethod().getInstructionsCount()){
            getIns().moveTo(index);
        }
    }
    public void moveTo(int index){
        getIns().moveTo(index);
    }
    public void merge(DexInstruction other){
        getIns().merge(other.getIns());
    }

    @Override
    public DexClassRepository getClassRepository() {
        return getDexMethod().getClassRepository();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getIns().append(writer);
    }
    public static Iterator<DexInstruction> create(DexMethod dexMethod, Iterator<Ins> iterator){
        if(dexMethod == null){
            return EmptyIterator.of();
        }
        return ComputeIterator.of(iterator, ins -> create(dexMethod, ins));
    }
    public static DexInstruction create(DexMethod dexMethod, Ins ins){
        if(dexMethod == null || ins == null){
            return null;
        }
        return new DexInstruction(dexMethod, ins);
    }
}
