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

import com.reandroid.dex.base.DexException;
import com.reandroid.dex.common.OperandType;
import com.reandroid.dex.common.RegisterFormat;
import com.reandroid.dex.data.FixedDexContainerWithTool;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.data.MethodDef;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliCodeSet;
import com.reandroid.dex.smali.model.SmaliInstruction;
import com.reandroid.dex.smali.model.SmaliLabel;
import com.reandroid.utils.ObjectsStore;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.InstanceIterator;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

public class Ins extends FixedDexContainerWithTool implements SmaliFormat {

    private final Opcode<?> opcode;
    private Object extraLineList;
    private Ins targetIns;

    Ins(int childesCount, Opcode<?> opcode) {
        super(childesCount);
        this.opcode = opcode;
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
    public MethodKey getMethodKey() {
        MethodDef methodDef = getMethodDef();
        if(methodDef != null) {
            return methodDef.getKey();
        }
        return null;
    }
    public Ins edit() {
        MethodDef methodDef = getMethodDef();
        InstructionList current = methodDef.getInstructionList();
        methodDef.edit();
        InstructionList update = methodDef.getInstructionList();
        if(current != update) {
            return update.get(getIndex());
        }
        return this;
    }
    InsBlockList getInsBlockList() {
        return getParentInstance(InsBlockList.class);
    }
    public InstructionList getInstructionList() {
        return getParentInstance(InstructionList.class);
    }

    public void updateTargetAddress() {
        if(this instanceof Label) {
            Ins target = getTargetIns();
            if(target == null) {
                throw new NullPointerException("Null target: " + this + ", " + getMethodKey());
            }
            ((Label) this).setTargetAddress(target.getAddress());
        }
    }
    public void transferExtraLinesTo(Ins destination) {
        destination.extraLineList = ObjectsStore.addAll(
                destination.extraLineList, this.getExtraLines());

        Iterator<ExtraLine> iterator = destination.getExtraLines();
        while (iterator.hasNext()) {
            ExtraLine extraLine = iterator.next();
            extraLine.setTargetIns(null);
            extraLine.setTargetIns(destination);
        }
        Ins target = this.targetIns;
        if (target != null && destination instanceof Label) {
            destination.setTargetIns(target);
        }
        this.clearExtraLines();
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
    public<T1 extends Ins> T1 replace(Opcode<T1> opcode){
        InstructionList instructionList = getInstructionList();
        if(instructionList == null){
            throw new DexException("Missing parent "
                    + InstructionList.class.getSimpleName());
        }
        return instructionList.replace(this, opcode);
    }
    public<T1 extends Ins> T1 createNext(Opcode<T1> opcode) {
        InstructionList instructionList = getInstructionList();
        if(instructionList == null){
            throw new DexException("Parent " + getClass().getSimpleName() + " == null");
        }
        return instructionList.createAt(getIndex() + 1, opcode);
    }
    public void moveTo(int index){
        InstructionList instructionList = getInstructionList();
        if(instructionList == null){
            throw new DexException("Parent " + getClass().getSimpleName() + " == null");
        }
        instructionList.moveTo(this, index);
    }

    public boolean is(Opcode<?> opcode){
        return opcode == getOpcode();
    }

    public Opcode<?> getOpcode() {
        return opcode;
    }
    public RegisterFormat getRegisterFormat() {
        return getOpcode().getRegisterFormat();
    }

    public int getCodeUnits(){
        return countBytes() / 2;
    }
    public int getOutSize(){
        return 0;
    }
    public int getAddress() {
        InsBlockList insBlockList = getInsBlockList();
        if(insBlockList != null) {
            return insBlockList.addressOf(this);
        }
        return -1;
    }
    void linkTargetIns() {
        Ins targetIns = this.targetIns;
        if(targetIns == null) {
            setTargetIns(findTargetIns());
        }
        if((this instanceof Label) && this.targetIns == null) {
            throw new NullPointerException("Missing target: " + this + ", " + getMethodKey());
        }
    }
    void unLinkTargetIns() {
        Ins targetIns = this.targetIns;
        if(targetIns != null) {
            setTargetIns(null);
        }
        clearExtraLines();
    }
    public Ins getTargetIns() {
        Ins targetIns = ensureTargetNotRemoved();
        if(targetIns == null) {
            targetIns = findTargetIns();
            setTargetIns(targetIns);
            targetIns = this.targetIns;
        }
        return targetIns;
    }
    public void setTargetIns(Ins targetIns) {
        if(targetIns == this) {
            // TODO: throw ?
            return;
        }
        if(targetIns != this.targetIns) {
            this.targetIns = targetIns;
            if(targetIns != null) {
                ((Label) this).setTargetAddress(targetIns.getAddress());
                targetIns.addExtraLine((Label) this);
            }
        }
    }

    private Ins ensureTargetNotRemoved() {
        Ins target = this.targetIns;
        if(target != null && target.isRemoved()) {
            target = null;
            this.targetIns = null;
        }
        return target;
    }
    private Ins findTargetIns() {
        if(this instanceof Label) {
            InsBlockList insBlockList = getInsBlockList();
            if(insBlockList != null) {
                int targetAddress = ((Label) this).getTargetAddress();
                Ins target = insBlockList.getAtAddress(targetAddress);
                if(targetAddress != 0 || target != this) {
                    return target;
                }
            }
        }
        return null;
    }
    public void addExtraLine(ExtraLine extraLine){
        if (extraLine != this) {
            this.extraLineList = ObjectsStore.add(this.extraLineList, extraLine);
        }
    }
    public Iterator<ExtraLine> getExtraLines() {
        ObjectsStore.sort(this.extraLineList, ExtraLine.COMPARATOR);
        return ObjectsStore.iterator(this.extraLineList);
    }
    public<T1> Iterator<T1> getExtraLines(Class<T1> instance) {
        ObjectsStore.sort(this.extraLineList, ExtraLine.COMPARATOR);
        return ObjectsStore.iterator(this.extraLineList, instance);
    }
    public void clearExtraLines() {
        extraLineList = ObjectsStore.clear(extraLineList);
    }
    public boolean hasExtraLines() {
        return !ObjectsStore.isEmpty(extraLineList);
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

    public void replaceKeys(Key search, Key replace){

    }
    public Iterator<IdItem> usedIds(){
        return EmptyIterator.of();
    }
    public boolean isRemoved() {
        return getParent() == null;
    }
    public void merge(Ins ins){
        throw new RuntimeException("merge method not implemented, opcode = " + getOpcode());
    }
    public void fromSmali(SmaliInstruction smaliInstruction) {
        throw new RuntimeException("fromSmali method not implemented, opcode = " + getOpcode());
    }

    public SmaliInstruction toSmali() {
        return toSmali(new SmaliCodeSet());
    }
    public SmaliInstruction toSmali(SmaliCodeSet smaliCodeSet) {
        SmaliInstruction instruction = smaliCodeSet.newInstruction(getOpcode());
        toSmali(instruction);
        return instruction;
    }

    private void toSmali(SmaliInstruction instruction) {
        if (instruction.getOperandType() != OperandType.NONE) {
            toSmaliOperand(instruction);
        }
        if (instruction.getRegisterFormat() != RegisterFormat.NONE) {
            toSmaliRegisters(instruction);
        }
        if (hasExtraLines() && instruction.getCodeSet() != null) {
            toSmaliExtraLines(instruction);
        }
        toSmaliOthers(instruction);
    }
    void toSmaliOperand(SmaliInstruction instruction) {
    }
    void toSmaliRegisters(SmaliInstruction instruction) {
    }
    void toSmaliExtraLines(SmaliInstruction instruction) {
        SmaliCodeSet smaliCodeSet = instruction.getCodeSet();
        Iterator<Label> iterator = InstanceIterator.of(getExtraLines(),
                Label.class, label -> !(label instanceof ExceptionLabel));
        Label extraLine;
        ExtraLine previous = null;
        int index = smaliCodeSet.indexOf(instruction);
        while (iterator.hasNext()){
            extraLine = iterator.next();
            if(extraLine.isEqualExtraLine(previous)){
                continue;
            }
            SmaliLabel smaliLabel = new SmaliLabel();
            smaliCodeSet.add(index, smaliLabel);
            smaliLabel.setLabelName(extraLine.getLabelName());
            index ++;
            previous = extraLine;
        }
    }
    void toSmaliOthers(SmaliInstruction instruction) {
    }
    void validateOpcode(SmaliInstruction smaliInstruction) {
        if(getOpcode() != smaliInstruction.getOpcode()) {
            throw new DexException("Mismatch opcode " + getOpcode()
                    + " vs " + smaliInstruction.getOpcode());
        }
    }
    @Override
    public final void append(SmaliWriter writer) throws IOException {
        appendExtraLines(writer);
        writer.newLine();
        appendCode(writer);
    }
    public void appendCode(SmaliWriter writer) throws IOException {
        writer.append(getOpcode().getName());
        writer.append(' ');
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
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
    static int toSigned(int unsigned, int width){
        int half = width / 2;
        if(unsigned <= half){
            return unsigned;
        }
        return unsigned - width - 1;
    }
}
