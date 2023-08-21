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

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.DexBlockList;
import com.reandroid.dex.debug.DebugElement;
import com.reandroid.dex.ins.*;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;
import java.util.Iterator;

public class InstructionList extends DexBlockList<Ins> implements SmaliFormat {
    private final IntegerReference codeUnits;
    private final TryBlock tryBlock;

    public InstructionList(IntegerReference codeUnits, TryBlock tryBlock){
        super();
        this.codeUnits = codeUnits;
        this.tryBlock = tryBlock;
    }

    private Ins getAtAddress(int address){
        for (Ins ins : this) {
            if(ins.getAddress() == address){
                return ins;
            }
        }
        return null;
    }
    private void buildLabels(){
        for(Ins ins : this){
            if(ins instanceof Label){
                Label label = (Label) ins;
                Ins target = getAtAddress(label.getTargetAddress());
                if(target != null){
                    target.addExtraLine(label);
                }
            }else if(ins instanceof LabelList){
                Iterator<? extends Label> iterator = ((LabelList) ins).getLabels();
                while (iterator.hasNext()){
                    Label label = iterator.next();
                    Ins target = getAtAddress(label.getTargetAddress());
                    if(target != null){
                        target.addExtraLine(label);
                    }
                }
            }
        }
    }
    private void addExceptionHandler(ExceptionHandler handler){
        if(handler == null){
            return;
        }
        Label start = handler.getStartLabel();
        Ins target = getAtAddress(start.getTargetAddress());
        target.addExtraLine(start);

        Label label = handler.getEndLabel();

        target = getAtAddress(label.getTargetAddress());
        if(target != null){
            target.addExtraLine(label);
        }

        label = handler.getCatchLabel();
        target = getAtAddress(label.getTargetAddress());
        target.addExtraLine(label);

        target = getAtAddress(handler.getAddress());
        if(target != null){
            target.addExtraLine(handler);
        }
    }
    private void buildTryBlock(){
        TryBlock tryBlock = this.tryBlock;
        if(tryBlock.isNull()){
            return;
        }
        for(TryItem tryItem : tryBlock){
            for(TryHandler handler : tryItem){
                addExceptionHandler(handler);
            }
            addExceptionHandler(tryItem.getCatchAllHandler());
        }
    }

    public void buildDebugInfo(DebugInfo debugInfo){
        if(debugInfo == null){
            return;
        }
        Iterator<DebugElement> iterator = debugInfo.getExtraLines();
        while (iterator.hasNext()){
            DebugElement element = iterator.next();
            Ins target = getAtAddress(element.getAddress());
            if(target != null){
                target.addExtraLine(element);
            }
        }
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        buildTryBlock();
        for (Ins ins : this) {
            writer.newLine();
            ins.append(writer);
        }
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int position = reader.getPosition() + codeUnits.get() * 2;
        int zeroPosition = reader.getPosition();
        while (reader.getPosition() < position){
            Opcode<?> opcode = Opcode.read(reader);
            Ins ins = opcode.newInstance();
            ins.setAddress((reader.getPosition() - zeroPosition) / 2);
            add(ins);
            ins.readBytes(reader);
        }
        if(position != reader.getPosition()){
            // should not reach here
            reader.seek(position);
        }
        getDexPositionAlign().readBytes(reader);
        buildLabels();
    }
}
