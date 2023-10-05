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

    public int getNumber() {
        int register = getRegister();
        int local = getLocalRegistersCount();
        if(register >= local){
            register = register - local;
        }
        return register;
    }
    public int getRegister(){
        return registersSet.getRegister(getIndex());
    }
    public int getIndex() {
        return index;
    }
    public boolean isParameter() {
        return getRegister() >= getLocalRegistersCount();
    }

    public int getLocalRegistersCount(){
        return registersTable.getRegistersCount() - registersTable.getParameterRegistersCount();
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
}
