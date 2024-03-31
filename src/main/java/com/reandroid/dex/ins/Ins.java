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
import com.reandroid.dex.common.RegisterFormat;
import com.reandroid.dex.debug.DebugElement;
import com.reandroid.dex.debug.DebugLineNumber;
import com.reandroid.dex.debug.DebugSequence;
import com.reandroid.dex.data.FixedDexContainerWithTool;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.data.MethodDef;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliInstruction;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.InstanceIterator;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

public class Ins extends FixedDexContainerWithTool implements SmaliFormat {

    private final Opcode<?> opcode;
    private ExtraLineList extraLineList;
    private int address;

    Ins(int childesCount, Opcode<?> opcode) {
        super(childesCount);
        this.opcode = opcode;
        this.extraLineList = ExtraLineList.EMPTY;
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
    public DebugSequence getOrCreateDebugSequence(){
        InstructionList instructionList = getInstructionList();
        if(instructionList != null){
            return instructionList.getOrCreateDebugSequence();
        }
        return null;
    }
    public DebugSequence getDebugSequence(){
        InstructionList instructionList = getInstructionList();
        if(instructionList != null){
            return instructionList.getDebugSequence();
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
            if(address != extraLine.getTargetAddress()){
                extraLine.setTargetAddress(address);
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
    public Ins[] createNext(Opcode<?>[] opcodeArray) {
        InstructionList instructionList = getInstructionList();
        if(instructionList == null){
            throw new DexException("Parent " + getClass().getSimpleName() + " == null");
        }
        return instructionList.createAt(getIndex() + 1, opcodeArray);
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
        return address;
    }
    public void setAddress(int address) {
        this.address = address;
    }

    public boolean isLonelyInTryCatch(){
        InstructionList instructionList = getInstructionList();
        if(instructionList != null){
            return instructionList.isLonelyInTryCatch(this);
        }
        return false;
    }
    public void trimExtraLines(){
        this.extraLineList.trimToSize();
    }
    public void addExtraLine(ExtraLine extraLine){
        if(extraLine != this){
            this.extraLineList = ExtraLineList.add(this.extraLineList, extraLine);
        }
    }
    public void addExtraLine(Iterator<ExtraLine> iterator){
        this.extraLineList = ExtraLineList.add(this.extraLineList, iterator);
    }
    public DebugLineNumber getDebugLineNumber(){
        return CollectionUtil.getFirst(getDebugLineNumbers());
    }
    public Iterator<DebugLineNumber> getDebugLineNumbers(){
        return InstanceIterator.of(getExtraLines(), DebugLineNumber.class);
    }
    public boolean removeDebugElement(DebugElement element){
        DebugSequence debugSequence = getDebugSequence();
        if(debugSequence != null){
            return debugSequence.remove(element);
        }
        return false;
    }
    public Iterator<DebugElement> getDebugElements(){
        return InstanceIterator.of(getExtraLines(), DebugElement.class);
    }
    public Iterator<ExtraLine> getExtraLines(){
        return this.extraLineList.iterator();
    }
    public<T1> Iterator<T1> getExtraLines(Class<T1> instance){
        return this.extraLineList.iterator(instance);
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

    public void replaceKeys(Key search, Key replace){

    }
    public Iterator<IdItem> usedIds(){
        return EmptyIterator.of();
    }
    public void merge(Ins ins){

    }
    public void fromSmali(SmaliInstruction smaliInstruction) throws IOException {

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
