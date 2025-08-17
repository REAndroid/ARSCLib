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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.model.SmaliInstruction;

import java.util.Iterator;

public abstract class PayloadData<T extends PayloadEntry> extends Ins
        implements SmaliRegion, Iterable<T> {

    public PayloadData(int childesCount, Opcode<?> opcode) {
        super(childesCount + 1, opcode);
        ShortItem opcodeItem = new ShortItem();
        opcodeItem.set(opcode.getValue());
        addChild(0, opcodeItem);
    }

    @Override
    public abstract Iterator<T> iterator();
    public abstract T get(int index);
    public abstract int size();
    public abstract void setSize(int size);
    public boolean isEmpty() {
        return size() == 0;
    }

    Object requestLock() {
        InsBlockList insBlockList = getInsBlockList();
        if (insBlockList != null) {
            return insBlockList.linkLocked();
        }
        return null;
    }
    void releaseLock(Object lock) {
        if (lock != null) {
            getInsBlockList().unlinkLocked(lock);
        }
    }
    void updateNopAlignment() {
        InstructionList instructionList = getInstructionList();
        if(instructionList == null) {
            return;
        }
        int position = instructionList.countUpTo(this);
        if(position % 4 == 0) {
            return;
        }
        InsNop insNop = getNopAlignment();
        if(insNop != null) {
            instructionList.remove(insNop);
        } else {
            instructionList.add(false, getIndex(), Opcode.NOP.newInstance());
        }
    }
    private InsNop getNopAlignment() {
        InstructionList instructionList = getInstructionList();
        if(instructionList != null) {
            Ins ins = instructionList.get(getIndex() - 1);
            if(ins instanceof InsNop) {
                return (InsNop) ins;
            }
        }
        return null;
    }

    @Override
    protected void onPreRefresh() {
        updateNopAlignment();
        super.onPreRefresh();
    }

    public abstract Iterator<IntegerReference> getReferences();

    @Override
    public abstract void fromSmali(SmaliInstruction smaliInstruction);

    @Override
    void toSmaliOperand(SmaliInstruction instruction) {
        super.toSmaliOperand(instruction);
    }
    @Override
    void toSmaliOthers(SmaliInstruction instruction) {
        super.toSmaliOthers(instruction);
        toSmaliEntries(instruction);
    }
    void toSmaliEntries(SmaliInstruction instruction) {
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this){
            return true;
        }
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }
        PayloadData<?> payloadData = (PayloadData<?>) obj;
        if(getIndex() != payloadData.getIndex()){
            return false;
        }
        return Block.areEqual(getChildes(), payloadData.getChildes());
    }

    @Override
    public int hashCode() {
        return Block.hashCodeOf(getChildes()) + getIndex();
    }

}