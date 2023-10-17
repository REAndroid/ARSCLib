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

import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.ins.Ins;
import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.ins.RegistersSet;
import com.reandroid.dex.ins.SizeXIns;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class DexInstruction extends DexModel{
    private final DexMethod dexMethod;
    private final Ins mIns;

    public DexInstruction(DexMethod dexMethod, Ins ins) {
        this.dexMethod = dexMethod;
        this.mIns = ins;
    }

    public String getString(){
        StringId stringId = (StringId) getIdSectionEntry();
        if(stringId != null){
            return stringId.getString();
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
    public IdItem getIdSectionEntry(){
        Ins ins = getIns();
        if(ins instanceof SizeXIns){
            return  ((SizeXIns) ins).getSectionItem();
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
    public int getRegistersCount(){
        Ins ins = getIns();
        if(ins instanceof RegistersSet){
            return ((RegistersSet) ins).getRegistersCount();
        }
        return 0;
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

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getIns().append(writer);
    }
}
