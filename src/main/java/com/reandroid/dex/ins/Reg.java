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

import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class Reg implements SmaliFormat {

    private final RegistersTable registersTable;
    private final RegistersSet registersSet;
    private final int index;

    public Reg(RegistersTable registersTable, RegistersSet registersSet, int index){
        this.registersTable = registersTable;
        this.registersSet = registersSet;
        this.index = index;
    }

    public Editor toEditor(){
        return new Editor(this);
    }

    public int getNumber() {
        int register = getRegister();
        int local = getLocalRegistersCount();
        if(register >= local){
            register = register - local;
        }
        return register;
    }
    public int getRegister(){
        return getRegistersSet().getRegister(getIndex());
    }
    public void setRegister(int register){
        getRegistersSet().setRegister(getIndex(), register);
    }
    public int getIndex() {
        return index;
    }
    public boolean isParameter() {
        return getRegister() >= getLocalRegistersCount();
    }

    public int getLocalRegistersCount(){
        RegistersTable table = getRegistersTable();
        return table.getRegistersCount() - table.getParameterRegistersCount();
    }
    public RegistersSet getRegistersSet() {
        return registersSet;
    }
    public RegistersTable getRegistersTable() {
        return registersTable;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        if(isParameter()){
            writer.append('p');
        }else {
            writer.append('v');
        }
        writer.append(getNumber());
    }

    @Override
    public String toString() {
        if(isParameter()){
            return "p" + getNumber();
        }
        return "v" + getNumber();
    }

    public static class Editor extends Reg{

        private final Reg mBaseReg;
        private final int number;
        private final boolean parameter;

        public Editor(Reg reg) {
            super(null, null, reg.getIndex());
            this.mBaseReg = reg;
            this.number = reg.getNumber();
            this.parameter = reg.isParameter();
        }

        public void apply(){
            Reg baseReg = getBaseReg();
            baseReg.setRegister(getRegister());
        }
        private boolean isChanged(){
            Reg baseReg = getBaseReg();
            return this.isParameter() == baseReg.isParameter() &&
                    this.getNumber() == baseReg.getNumber() &&
                    this.getRegister() == baseReg.getRegister();
        }
        @Override
        public int getRegister() {
            int register = getNumber();
            if(isParameter()){
                register += getLocalRegistersCount();
            }
            return register;
        }

        @Override
        public int getNumber() {
            return this.number;
        }
        @Override
        public boolean isParameter() {
            return this.parameter;
        }
        @Override
        public int getIndex() {
            return getBaseReg().getIndex();
        }
        @Override
        public RegistersTable getRegistersTable() {
            return getBaseReg().getRegistersTable();
        }
        @Override
        public RegistersSet getRegistersSet() {
            return getBaseReg().getRegistersSet();
        }
        public Reg getBaseReg() {
            return mBaseReg;
        }
    }
}
