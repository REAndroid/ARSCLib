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
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.*;
import com.reandroid.dex.debug.DebugParameter;
import com.reandroid.dex.index.ItemOffsetReference;
import com.reandroid.dex.index.ProtoId;
import com.reandroid.dex.ins.TryBlock;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;
import java.util.Iterator;

public class CodeItem extends DexItem implements SmaliFormat {

    private final Header header;
    private final InstructionList instructionList;
    private final TryBlock tryBlock;
    private MethodDef methodDef;

    public CodeItem() {
        super(3);
        this.header = new Header();
        this.tryBlock = new TryBlock(header.tryBlockCount);
        this.instructionList = new InstructionList(header.instructionCodeUnits, tryBlock);

        addChild(0, header);
        addChild(1, instructionList);
        addChild(2, tryBlock);
    }

    public DebugInfo getDebugInfo(){
        return header.debugInfoOffset.getItem();
    }
    public InstructionList getInstructionList() {
        return instructionList;
    }
    public TryBlock getTryBlock(){
        return tryBlock;
    }

    public MethodDef getMethodDef() {
        return methodDef;
    }
    public void setMethodDef(MethodDef methodDef) {
        this.methodDef = methodDef;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        MethodDef methodDef = getMethodDef();
        DebugInfo debugInfo = getDebugInfo();
        ProtoId proto = methodDef.getMethodId().getProto();
        writer.newLine();
        writer.append(".locals ");
        InstructionList instructionList = getInstructionList();
        instructionList.buildDebugInfo(debugInfo);
        int count = header.registersCount.get() - proto.getParametersCount();
        if(!methodDef.isStatic()){
            count = count - 1;
        }
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

        final IntegerReference registersCount;
        final IntegerReference instruction;
        final IntegerReference outs;
        final IntegerReference tryBlockCount;

        final ItemOffsetReference<DebugInfo> debugInfoOffset;
        final IntegerReference instructionCodeUnits;

        public Header() {
            super(16);
            int offset = -2;
            this.registersCount = new IndirectShort(this, offset += 2);
            this.instruction = new IndirectShort(this, offset += 2);
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
            debugInfoOffset.getItem();
        }

        @Override
        public String toString() {
            return  "registers=" + registersCount +
                    ", instruction=" + instruction +
                    ", outs=" + outs +
                    ", tries=" + tryBlockCount +
                    ", debugInfo=" + debugInfoOffset +
                    ", codeUnits=" + instructionCodeUnits;
        }
    }
}
