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
package com.reandroid.dex.smali.model;

import com.reandroid.dex.common.Register;
import com.reandroid.dex.common.RegisterFormat;
import com.reandroid.dex.common.RegistersTable;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public class SmaliRegister extends Smali{

    private boolean parameter;
    private int number;

    public SmaliRegister() {
        super();
    }

    public Register toRegister() {
        return toRegister(null);
    }
    public Register toRegister(RegistersTable registersTable) {
        return new Register(getNumber(), isParameter(), registersTable);
    }
    public boolean isParameter() {
        return parameter;
    }
    public void setParameter(boolean parameter) {
        this.parameter = parameter;
    }
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        if (isParameter()) {
            writer.append('p');
        } else {
            writer.append('v');
        }
        writer.appendInteger(getNumber());
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        reader.skipSpaces();
        int position = reader.position();
        char ch = SmaliParseException.expect(reader, 'v', 'p');
        setParameter(ch == 'p');
        setNumber(reader.readInteger());
        if (reader.isValidateRegisters()) {
            validate(reader, position);
        }
    }
    private void validate(SmaliReader reader, int position) throws IOException {
        SmaliRegisterSet registerSet = getParentInstance(SmaliRegisterSet.class);
        if (registerSet == null) {
            return;
        }
        RegistersTable registersTable = registerSet.getRegistersTable();
        if (registersTable == null) {
            return;
        }
        int number = getNumber();
        if (number < 0) {
            throw new IOException(reader.getOrigin(position)
                    + " Negative register " + this.toString());
        }
        if (number > 0xffff) {
            throw new IOException(reader.getOrigin(position)
                    + " Register " + this.toString() + " out of range");
        }
        int registersCount = registersTable.getRegistersCount();
        int localRegistersCount = registersTable.getLocalRegistersCount();
        boolean is_parameter = isParameter();
        int r = number;
        if (is_parameter) {
            r = r + localRegistersCount;
        }
        if (r >= registersCount) {
            throw new IOException(reader.getOrigin(position)
                    + " Register " + this.toString() + "(r" + r
                    + ") is out of bounds (check .local/.registers definition)");
        }
        if (r < localRegistersCount && is_parameter) {
            throw new IOException(reader.getOrigin(position)
                    + " Register " + this.toString() + "(r" + r
                    + ") is NOT parameter (p) (check .locals definition)");
        }
        if (r >= localRegistersCount && !is_parameter) {
            throw new IOException(reader.getOrigin(position)
                    + " Register " + this.toString() + "(r" + r
                    + ") is NOT local register (v) (check .locals definition)");
        }
        RegisterFormat registerFormat = registerSet.getFormat();
        int index = registerSet.indexOfIdentity(this);
        if (registerFormat.isOut() && index > 4) {
            throw new IOException(reader.getOrigin(position)
                    + " A list of registers can only have a maximum of 5 registers." +
                    " Use the <op>/range alternate opcode instead.");
        }
        int limit = registerFormat.limit(index);
        if (r > limit) {
            throw new IOException(reader.getOrigin(position)
                    + " Invalid register: " + this.toString() +
                    ". Must be between v0 and v" + limit + ", inclusive.");
        }
        if (registerFormat.isWide(index)) {
            int rHigh = r + 1;
            if (rHigh >= registersCount) {
                throw new IOException(reader.getOrigin(position)
                        + " Wide high register " + this.toString() + "(r" + r + ", r" + rHigh
                        + ") is out of bounds (check .local/.registers definition)");
            }
            if (!is_parameter && rHigh >= localRegistersCount) {
                throw new IOException(reader.getOrigin(position)
                        + " Wide register " + this.toString() + "(r" + r + ", r" + rHigh
                        + ") low part is v but high part is p  (check .local/.registers definition)");
            }
        }
    }

    @Override
    public String toString() {
        String name = isParameter() ? "p" : "v";
        return name + getNumber();
    }
}
