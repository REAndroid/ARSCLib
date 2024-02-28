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
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;
import java.util.List;

public class DexInstruction extends Dex {

    private final DexMethod dexMethod;
    private final Ins mIns;

    public DexInstruction(DexMethod dexMethod, Ins ins) {
        this.dexMethod = dexMethod;
        this.mIns = ins;
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
        return new DexInstruction(getDexMethod(), jumbo);
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
    public DexInstruction replace(Opcode<?> opcode){
        return new DexInstruction(getDexMethod(), getIns().replace(opcode));
    }
    public DexInstruction createNext(Opcode<?> opcode){
        return new DexInstruction(getDexMethod(), getIns().createNext(opcode));
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
    public DexMethod getDexMethod() {
        return dexMethod;
    }
    public DexClass getDexClass(){
        return getDexMethod().getDexClass();
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
}
