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

import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public class SmaliInstruction extends SmaliCode{

    private Opcode<?> opcode;
    private SmaliRegisterSet registerSet;
    private SmaliInstructionOperand operand;

    private int address;

    public SmaliInstruction(){
        super();
        this.operand = SmaliInstructionOperand.NO_OPERAND;
    }

    public int getAddress() {
        return address;
    }
    public void setAddress(int address) {
        this.address = address;
    }

    public int getCodeUnits(){
        return getOpcode().size() / 2;
    }
    public SmaliRegisterSet getRegisterSet() {
        return registerSet;
    }
    public void setRegisterSet(SmaliRegisterSet registerSet) {
        this.registerSet = registerSet;
        if(registerSet != null){
            registerSet.setParent(this);
        }
    }
    public SmaliInstructionOperand getOperand() {
        return operand;
    }
    public void setOperand(SmaliInstructionOperand operand) {
        this.operand = operand;
        if(operand != null){
            operand.setParent(this);
        }
    }
    public boolean hasLabel(SmaliLabel label){
        SmaliInstructionOperand operand = getOperand();
        if(!(operand instanceof SmaliInstructionOperand.LabelOperand)){
            return false;
        }
        SmaliInstructionOperand.LabelOperand labelOperand = (SmaliInstructionOperand.LabelOperand) operand;
        return label.equals(labelOperand.getLabel());
    }

    public Opcode<?> getOpcode() {
        return opcode;
    }
    public void setOpcode(Opcode<?> opcode) {
        this.opcode = opcode;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        Opcode<?> opcode = getOpcode();
        if(opcode == null){
            return;
        }
        writer.newLine();
        opcode.append(writer);
        if(opcode.isMethodInvoke()){
            writer.append('{');
        }
        SmaliRegisterSet registerSet = getRegisterSet();
        if(registerSet != null){
            registerSet.append(writer);
        }
        if(opcode.isMethodInvoke()){
            writer.append('}');
        }
        SmaliInstructionOperand operand = getOperand();
        if(operand != SmaliInstructionOperand.NO_OPERAND){
            writer.append(", ");
        }
        operand.append(writer);
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        parseOpcode(reader);
        Opcode<?> opcode = getOpcode();
        while (!reader.isLineEnd()){
            if(getOperand() != SmaliInstructionOperand.NO_OPERAND || opcode == Opcode.NOP){
                throw new SmaliParseException("Unrecognized state", reader);
            }
            parseNext(reader);
            reader.skipSpaces();
        }
    }
    private void parseOpcode(SmaliReader reader){
        reader.skipWhitespaces();
        Opcode<?> opcode = Opcode.parseSmali(reader, true);
        setOpcode(opcode);
        reader.skipSpaces();
        if(opcode == Opcode.NOP){
            setOperand(SmaliInstructionOperand.NO_OPERAND);
        }
    }
    private void parseNext(SmaliReader reader) throws IOException {
        byte b = reader.get();
        switch (b){
            case '{':
            case 'v':
            case 'p':
                parseRegisterSet(reader);
                break;
            case ':':
                parseLabel(reader);
                break;
            case '-':
            case '0':
                parseHex(reader);
                break;
            default:
                if(getOpcode().getSectionType() != null){
                    parseKey(reader);
                }else {
                    throw new SmaliParseException("Unrecognized register/operand", reader);
                }
        }
    }
    private void parseRegisterSet(SmaliReader reader) throws IOException {
        SmaliRegisterSet registerSet = new SmaliRegisterSet();
        setRegisterSet(registerSet);
        registerSet.parse(reader);
    }
    private void parseLabel(SmaliReader reader) throws IOException {
        SmaliInstructionOperand operand = new SmaliInstructionOperand.LabelOperand();
        setOperand(operand);
        operand.parse(getOpcode(), reader);
    }
    private void parseKey(SmaliReader reader) throws IOException {
        SmaliInstructionOperand operand = new SmaliInstructionOperand.KeyOperand();
        setOperand(operand);
        operand.parse(getOpcode(), reader);
    }
    private void parseHex(SmaliReader reader) throws IOException {
        SmaliInstructionOperand operand = new SmaliInstructionOperand.HexOperand();
        setOperand(operand);
        operand.parse(getOpcode(), reader);
    }
}
