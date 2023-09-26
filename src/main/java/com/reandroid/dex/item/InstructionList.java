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

import com.reandroid.arsc.base.BlockArray;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.CreatorArray;
import com.reandroid.dex.base.DexBlockAlign;
import com.reandroid.dex.base.DexBlockList;
import com.reandroid.arsc.item.IntegerVisitor;
import com.reandroid.arsc.item.VisitableInteger;
import com.reandroid.dex.base.FixedDexContainer;
import com.reandroid.dex.debug.DebugElement;
import com.reandroid.dex.ins.*;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.collection.CollectionUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

public class InstructionList extends FixedBlockContainer implements Iterable<Ins>, SmaliFormat, VisitableInteger {
    private final CodeItem codeItem;
    private final BlockArray<Ins> insArray;
    private final DexBlockAlign blockAlign;
    private final RegisterFactory registerFactory;

    public InstructionList(CodeItem codeItem){
        super(2);
        this.codeItem = codeItem;

        this.insArray = new CreatorArray<>(CREATOR);
        this.blockAlign = new DexBlockAlign(this);

        this.registerFactory = new RegisterFactory(codeItem);

        addChild(0, insArray);
        addChild(1, blockAlign);
    }

    public MethodDef getMethodDef() {
        return getCodeItem().getMethodDef();
    }
    public CodeItem getCodeItem() {
        return codeItem;
    }

    @Override
    public Iterator<Ins> iterator() {
        return getInsArray().iterator();
    }

    private BlockArray<Ins> getInsArray() {
        return insArray;
    }

    public Ins get(int i){
        return getInsArray().get(i);
    }
    public void add(Ins ins){
        getInsArray().add(ins);
    }
    public void add(int index, Ins item) {
        reBuildExtraLines();
        getInsArray().insertItem(index, item);
        updateAddresses();
        updateLabelAddress();
        reBuildExtraLines();
    }
    public boolean contains(Ins item){
        return getInsArray().contains(item);
    }
    public boolean remove(Ins item) {
        if(!contains(item)){
            return false;
        }
        reBuildExtraLines();
        if(item.hasExtraLines()){
            Ins next = get(item.getIndex() + 1);
            if(next != null) {
                item.transferExtraLines(next);
            }
        }
        getInsArray().remove(item);
        updateAddresses();
        updateLabelAddress();
        return true;
    }
    public void replace(Ins old, Ins item) {
        if(old == item){
            return;
        }
        reBuildExtraLines();
        int index = old.getIndex();
        old.transferExtraLines(item);
        item.setAddress(old.getAddress());
        getInsArray().setItem(index, item);
        old.setParent(null);
        old.setIndex(-1);
        updateAddresses();
        updateLabelAddress();
    }
    private void updateLabelAddress() {
        for(Ins ins : this) {
            ins.updateLabelAddress();
        }
    }
    private void updateAddresses() {
        int address = 0;
        for(Ins ins : this) {
            ins.setAddress(address);
            address += ins.getCodeUnits();
        }
    }

    @Override
    public void visitIntegers(IntegerVisitor visitor) {
        List<Ins> insList = CollectionUtil.toList(this.iterator());
        for(Ins ins : insList) {
            if(ins instanceof VisitableInteger){
                ((VisitableInteger)ins).visitIntegers(visitor);
            }
        }
    }

    public RegisterFactory getRegisterFactory() {
        return registerFactory;
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        this.codeItem.getInstructionCodeUnitsReference().set(getCodeUnits());
        this.blockAlign.align();
    }
    public int getCodeUnits() {
        int result = 0;
        for (Ins ins : this) {
            result += ins.getCodeUnits();
        }
        return result;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        buildExtraLines();
        for (Ins ins : this) {
            writer.newLine();
            ins.append(writer);
        }
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {

        int insCodeUnits = codeItem.getInstructionCodeUnitsReference().get();
        int position = reader.getPosition() + insCodeUnits * 2;
        int zeroPosition = reader.getPosition();

        int count = (insCodeUnits + 1) / 2;
        BlockArray<Ins> insBlockArray = getInsArray();
        insBlockArray.setChildesCount(count);
        int index = 0;

        while (reader.getPosition() < position){
            Opcode<?> opcode = Opcode.read(reader);
            Ins ins = opcode.newInstance();
            ins.setAddress((reader.getPosition() - zeroPosition) / 2);
            insBlockArray.setItem(index, ins);
            index ++;
            ins.readBytes(reader);
        }
        insBlockArray.setChildesCount(index);
        if(position != reader.getPosition()){
            // should not reach here
            reader.seek(position);
        }
        int totalRead = reader.getPosition() - zeroPosition;
        blockAlign.align(totalRead);
        reader.offset(blockAlign.size());
    }

    private void clearExtraLines() {
        for (Ins ins : this) {
            ins.clearExtraLines();
        }
    }
    private boolean haveExtraLines() {
        for (Ins ins : this) {
            if(ins.hasExtraLines()){
                return true;
            }
        }
        return false;
    }
    public void buildExtraLines(){
        if(haveExtraLines()){
            return;
        }
        buildLabels();
        buildTryBlock();
        buildDebugInfo();
    }
    public void reBuildExtraLines(){
        clearExtraLines();
        buildExtraLines();
    }
    private Ins getAtAddress(int address){
        for (Ins ins : this) {
            if(ins.getAddress() == address){
                return ins;
            }
        }
        return null;
    }
    private void addLabel(Label label){
        Ins target = getAtAddress(label.getTargetAddress());
        if(target != null){
            target.addExtraLine(label);
        }
    }
    private void addLabels(Iterator<? extends Label> iterator){
        while (iterator.hasNext()){
            addLabel(iterator.next());
        }
    }
    private void buildLabels(){
        for(Ins ins : this){
            if(ins instanceof Label){
                addLabel((Label) ins);
            }else if(ins instanceof LabelList){
                addLabels(((LabelList) ins).getLabels());
            }
            ins.sortExtraLines();
        }
    }
    private void buildTryBlock(){
        TryBlock tryBlock = this.codeItem.getTryBlock();
        if(tryBlock == null || tryBlock.isNull()){
            return;
        }
        addLabels(tryBlock.getLabels());
    }
    private void buildDebugInfo(){
        DebugInfo debugInfo = codeItem.getDebugInfo();
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
    public String toString() {
        StringWriter writer = new StringWriter();
        SmaliWriter smaliWriter = new SmaliWriter(writer);
        smaliWriter.indentPlus();
        try {
            append(smaliWriter);
            smaliWriter.close();
        } catch (IOException exception) {
            return exception.toString();
        }
        return writer.toString();
    }
    private static final Creator<Ins> CREATOR = new Creator<Ins>() {
        @Override
        public Ins[] newInstance(int length) {
            return new Ins[length];
        }
        @Override
        public Ins newInstance() {
            return PLACE_HOLDER;
        }
    };

    private static final Ins PLACE_HOLDER = Opcode.NOP.newInstance();
}
