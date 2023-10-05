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

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.NumberIntegerReference;
import com.reandroid.dex.item.DexContainerItem;
import com.reandroid.dex.item.InstructionList;
import com.reandroid.dex.item.MethodDef;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

public class Ins extends DexContainerItem implements SmaliFormat {

    private final Opcode<?> opcode;
    private ExtraLineList extraLineList;
    private final IntegerReference address;

    Ins(int childesCount, Opcode<?> opcode) {
        super(childesCount);
        this.opcode = opcode;
        this.extraLineList = ExtraLineList.EMPTY;
        this.address = new NumberIntegerReference();
    }
    Ins(Opcode<?> opcode) {
        this(1, opcode);
    }

    public MethodDef getMethodDef() {
        InstructionList instructionList = getInstructionList();
        if(instructionList != null) {
            return instructionList.getMethodDef();
        }
        return null;
    }
    public InstructionList getInstructionList() {
        return getParentInstance(InstructionList.class);
    }

    public void updateLabelAddress() {
        int address = getAddress();
        Iterator<ExtraLine> iterator = getExtraLines();
        while (iterator.hasNext()) {
            ExtraLine extraLine = iterator.next();
            if(extraLine instanceof Label){
                Label label = (Label) extraLine;
                if(address != label.getTargetAddress()){
                    label.setTargetAddress(address);
                }
            }
        }
    }
    public void transferExtraLines(Ins target) {
        target.extraLineList = this.extraLineList;
        this.extraLineList = ExtraLineList.EMPTY;
    }
    public void replace(Ins ins){
        if(ins == null || ins == this){
            return;
        }
        InstructionList instructionList = getInstructionList();
        if(instructionList == null){
            return;
        }
        instructionList.replace(this, ins);
    }

    public Opcode<?> getOpcode() {
        return opcode;
    }

    public int getCodeUnits(){
        return countBytes() / 2;
    }
    public IntegerReference getAddressReference() {
        return address;
    }
    public int getAddress() {
        return address.get();
    }
    public void setAddress(int address) {
        this.address.set(address);
    }

    public void trimExtraLines(){
        this.extraLineList.trimToSize();
    }
    public void addExtraLine(ExtraLine extraLine){
        this.extraLineList = ExtraLineList.add(this.extraLineList, extraLine);
    }
    public void addExtraLine(Iterator<ExtraLine> iterator){
        this.extraLineList = ExtraLineList.add(this.extraLineList, iterator);
    }
    public Iterator<ExtraLine> getExtraLines(){
        return this.extraLineList.iterator();
    }
    public void clearExtraLines() {
        extraLineList = ExtraLineList.EMPTY;
    }
    public boolean hasExtraLines() {
        return !extraLineList.isEmpty();
    }
    private void appendExtraLines(SmaliWriter writer) throws IOException {
        Iterator<ExtraLine> iterator = getExtraLines();
        ExtraLine extraLine = null;
        boolean hasHandler = false;
        ExtraLine previous = null;
        while (iterator.hasNext()){
            extraLine = iterator.next();
            if(extraLine.isEqualExtraLine(previous)){
                continue;
            }
            writer.newLine();
            extraLine.appendExtra(writer);
            if(!hasHandler){
                hasHandler = extraLine.getSortOrder() == ExtraLine.ORDER_EXCEPTION_HANDLER;
            }
            previous = extraLine;
        }
        if(hasHandler && extraLine.getSortOrder() >= ExtraLine.ORDER_EXCEPTION_HANDLER){
            writer.newLine();
        }
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        clearExtraLines();
    }

    @Override
    public final void append(SmaliWriter writer) throws IOException {
        appendExtraLines(writer);
        appendCode(writer);
    }
    void appendCode(SmaliWriter writer) throws IOException {
        writer.newLine();
        writer.append(getOpcode().getName());
        writer.append(' ');
    }
    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        SmaliWriter smaliWriter = new SmaliWriter(writer);
        try {
            appendCode(smaliWriter);
            smaliWriter.close();
            return writer.toString().trim();
        } catch (Throwable exception) {
            return exception.toString();
        }
    }
}
