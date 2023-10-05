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
package com.reandroid.dex.item;

import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.item.IntegerVisitor;
import com.reandroid.arsc.item.VisitableInteger;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.*;
import com.reandroid.dex.debug.DebugParameter;
import com.reandroid.dex.index.ItemOffsetReference;
import com.reandroid.dex.index.ProtoId;
import com.reandroid.dex.ins.RegistersTable;
import com.reandroid.dex.ins.TryBlock;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;
import java.util.Iterator;

public class CodeItem extends DataSectionEntry implements RegistersTable,
        SmaliFormat, VisitableInteger, PositionAlignedItem {

    private final Header header;
    private final InstructionList instructionList;
    private TryBlock tryBlock;
    private MethodDef methodDef;

    public CodeItem() {
        super(3);
        this.header = new Header(this);
        this.instructionList = new InstructionList(this);
        this.tryBlock = null;

        addChild(0, header);
        addChild(1, instructionList);
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
    }

    @Override
    public void visitIntegers(IntegerVisitor visitor) {
        getInstructionList().visitIntegers(visitor);
    }
    @Override
    public int getRegistersCount(){
        return header.registersCount.get();
    }
    @Override
    public void setRegistersCount(int count){
        header.registersCount.set(count);
    }
    @Override
    public int getParameterRegistersCount(){
        return header.parameterRegisters.get();
    }
    @Override
    public void setParameterRegistersCount(int count){
        header.parameterRegisters.set(count);
    }

    public DebugInfo getDebugInfo(){
        return header.debugInfoOffset.getItem();
    }
    public void setDebugInfo(DebugInfo debugInfo){
        header.debugInfoOffset.setItem(debugInfo);
    }
    public InstructionList getInstructionList() {
        return instructionList;
    }
    public TryBlock getTryBlock(){
        return tryBlock;
    }
    public TryBlock getOrCreateTryBlock(){
        initTryBlock();
        return tryBlock;
    }

    public MethodDef getMethodDef() {
        return methodDef;
    }
    public void setMethodDef(MethodDef methodDef) {
        this.methodDef = methodDef;
    }

    IntegerReference getInstructionCodeUnitsReference(){
        return header.instructionCodeUnits;
    }
    IntegerReference getInstructionOutsReference(){
        return header.outs;
    }
    void initTryBlock(){
        if(this.tryBlock == null){
            this.tryBlock = new TryBlock(header.tryBlockCount);
            addChild(2, this.tryBlock);
        }
    }
    public DexPositionAlign getPositionAlign(){
        if(this.tryBlock != null){
            return this.tryBlock.getPositionAlign();
        }else if(this.instructionList != null){
            return this.instructionList.getBlockAlign();
        }
        return new DexPositionAlign();
    }
    @Override
    public void removeLastAlign(){
        if(this.tryBlock != null){
            this.tryBlock.getPositionAlign().setSize(0);
        }else if(this.instructionList != null){
            this.instructionList.getBlockAlign().setSize(0);
        }
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        MethodDef methodDef = getMethodDef();
        DebugInfo debugInfo = getDebugInfo();
        ProtoId proto = methodDef.getMethodId().getProto();
        writer.newLine();
        writer.append(".locals ");
        InstructionList instructionList = getInstructionList();
        int count = getRegistersCount() - getParameterRegistersCount();
        writer.append(count);
        methodDef.appendParameterAnnotations(writer, proto);
        if(debugInfo != null){
            Iterator<DebugParameter> iterator = debugInfo.getParameters();
            while (iterator.hasNext()){
                iterator.next().append(writer);
            }
        }
        methodDef.appendAnnotations(writer);
        instructionList.append(writer);
    }
    @Override
    public String toString() {
        if(isNull()){
            return "NULL";
        }
        return header.toString()
                + "\n instructionList=" + instructionList
                + "\n tryBlock=" + tryBlock
                + "\n debug=" + getDebugInfo();
    }

    static class Header extends DexBlockItem implements BlockRefresh {

        private final CodeItem codeItem;

        final IntegerReference registersCount;
        final IntegerReference parameterRegisters;
        final IntegerReference outs;
        final IntegerReference tryBlockCount;

        final ItemOffsetReference<DebugInfo> debugInfoOffset;
        final IntegerReference instructionCodeUnits;

        public Header(CodeItem codeItem) {
            super(16);
            this.codeItem = codeItem;
            int offset = -2;
            this.registersCount = new IndirectShort(this, offset += 2);
            this.parameterRegisters = new IndirectShort(this, offset += 2);
            this.outs = new IndirectShort(this, offset += 2);
            this.tryBlockCount = new IndirectShort(this, offset += 2);
            this.debugInfoOffset = new ItemOffsetReference<>(SectionType.DEBUG_INFO,this, offset += 2);
            this.instructionCodeUnits = new IndirectInteger(this, offset += 4);
        }


        @Override
        public void refresh() {
            debugInfoOffset.refresh();
        }
        @Override
        public void onReadBytes(BlockReader reader) throws IOException {
            super.onReadBytes(reader);
            this.debugInfoOffset.getItem();
            if(this.tryBlockCount.get() != 0){
                this.codeItem.initTryBlock();
            }
        }


        @Override
        public String toString() {
            return  "registers=" + registersCount +
                    ", parameters=" + parameterRegisters +
                    ", outs=" + outs +
                    ", tries=" + tryBlockCount +
                    ", debugInfo=" + debugInfoOffset +
                    ", codeUnits=" + instructionCodeUnits;
        }
    }
}
