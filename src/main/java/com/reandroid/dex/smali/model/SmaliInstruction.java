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

import com.reandroid.dex.common.OperandType;
import com.reandroid.dex.common.RegisterFormat;
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
        this.opcode = Opcode.NOP;
        this.registerSet = SmaliRegisterSet.NO_REGISTER_SET;
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
    public OperandType getOperandType(){
        return getOperand().getOperandType();
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
    public void initializeOpcode(Opcode<?> opcode) {
        this.opcode = opcode;
        initRegisterSet(opcode);
        initOperand(opcode);
    }
    private void initRegisterSet(Opcode<?> opcode) {
        RegisterFormat format = opcode.getRegisterFormat();
        SmaliRegisterSet registerSet;
        if(format == RegisterFormat.NONE){
            registerSet = SmaliRegisterSet.NO_REGISTER_SET;
        }else {
            registerSet = new SmaliRegisterSet(format);
        }
        setRegisterSet(registerSet);
    }
    private void initOperand(Opcode<?> opcode) {
        OperandType operandType = opcode.getOperandType();
        SmaliInstructionOperand operand;
        if(operandType == OperandType.NONE){
            operand = SmaliInstructionOperand.NO_OPERAND;
        }else if(operandType == OperandType.HEX){
            operand = new SmaliInstructionOperand.HexOperand();
        }else if(operandType == OperandType.KEY){
            operand = new SmaliInstructionOperand.KeyOperand();
        }else if(operandType == OperandType.LABEL){
            operand = new SmaliInstructionOperand.LabelOperand();
        }else if(operandType == OperandType.DECIMAL){
            operand = new SmaliInstructionOperand.DecimalOperand();
        }else {
            throw new RuntimeException("Unknown operand type: " + operandType
                    + ", opcode = " + opcode);
        }
        setOperand(operand);
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        Opcode<?> opcode = getOpcode();
        if(opcode == null){
            return;
        }
        writer.newLine();
        opcode.append(writer);
        SmaliRegisterSet registerSet = getRegisterSet();
        if(registerSet != null){
            registerSet.append(writer);
        }
        if(opcode.getRegisterFormat() != RegisterFormat.NONE &&
                opcode.getOperandType() != OperandType.NONE){
            writer.append(", ");
        }
        getOperand().append(writer);
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        Opcode<?> opcode = parseOpcode(reader);
        getRegisterSet().parse(reader);

        if(opcode.getRegisterFormat() != RegisterFormat.NONE &&
                opcode.getOperandType() != OperandType.NONE){
            reader.skipWhitespacesOrComment();
            SmaliParseException.expect(reader, ',');
            reader.skipWhitespacesOrComment();
        }
        getOperand().parse(opcode, reader);
    }
    private Opcode<?> parseOpcode(SmaliReader reader){
        reader.skipWhitespaces();
        Opcode<?> opcode = Opcode.parseSmali(reader, true);
        initializeOpcode(opcode);
        reader.skipSpaces();
        return opcode;
    }
}
