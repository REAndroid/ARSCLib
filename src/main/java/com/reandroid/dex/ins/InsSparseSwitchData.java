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
import com.reandroid.arsc.io.BlockLoad;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.*;
import com.reandroid.common.ArraySupplier;
import com.reandroid.utils.collection.ArraySupplierIterator;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class InsSparseSwitchData extends PayloadData implements BlockLoad,
        ArraySupplier<InsSparseSwitchData.Data>, LabelsSet, SmaliFormat, VisitableInteger {

    private final ShortItem elementCount;
    final IntegerArrayBlock elements;
    final IntegerArrayBlock keys;
    private InsSparseSwitch insSparseSwitch;

    public InsSparseSwitchData() {
        super(3, Opcode.SPARSE_SWITCH_PAYLOAD);
        this.elementCount = new ShortItem();

        this.elements = new IntegerArrayBlock();
        this.keys = new IntegerArrayBlock();

        addChild(1, elementCount);
        addChild(2, elements);
        addChild(3, keys);

        this.elementCount.setBlockLoad(this);
    }

    @Override
    public void visitIntegers(IntegerVisitor visitor) {
        int size = getCount();
        for(int i = 0; i < size; i++){
            visitor.visit(this, get(i));
        }
    }
    @Override
    public int getCount(){
        return keys.size();
    }
    @Override
    public Data get(int i){
        return new Data(this, i);
    }
    @Override
    public Iterator<InsSparseSwitchData.Data> getLabels() {
        return new ArraySupplierIterator<>(this);
    }

    public int getBaseAddress(){
        InsSparseSwitch sparseSwitch = getParentSparseSwitch();
        if(sparseSwitch == null){
            return 0;
        }
        return sparseSwitch.getAddress();
    }
    public InsSparseSwitch getParentSparseSwitch() {
        if(insSparseSwitch == null){
            Iterator<ExtraLine> iterator = getExtraLines();
            while (iterator.hasNext()){
                ExtraLine extraLine = iterator.next();
                if(extraLine instanceof InsSparseSwitch){
                    insSparseSwitch = (InsSparseSwitch) extraLine;
                    break;
                }
            }
        }
        return insSparseSwitch;
    }
    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender == elementCount){
            this.keys.setSize(elementCount.get());
            this.elements.setSize(elementCount.get());
        }
    }
    @Override
    public void merge(Ins ins){
        InsSparseSwitchData switchData = (InsSparseSwitchData) ins;
        int size = switchData.elements.size();
        this.elements.setSize(size);
        for(int i = 0; i < size; i++){
            this.elements.put(i, switchData.elements.get(i));
        }
        size = switchData.keys.size();
        this.keys.setSize(size);
        for(int i = 0; i < size; i++){
            this.keys.put(i, switchData.keys.get(i));
        }
        this.elementCount.set(switchData.elementCount.get());
    }
    @Override
    void appendCode(SmaliWriter writer) throws IOException {
        writer.newLine();
        writer.append('.');
        String name = "sparse-switch";
        writer.append(name);
        int size = getCount();
        writer.indentPlus();
        for(int i = 0; i < size; i++){
            get(i).append(writer);
        }
        writer.indentMinus();
        writer.newLine();
        writer.append(".end ");
        writer.append(name);
    }

    public static class Data implements IntegerReference, Label, SmaliFormat {

        private final InsSparseSwitchData switchData;
        private final int index;

        Data(InsSparseSwitchData switchData, int index){
            this.switchData = switchData;
            this.index = index;
        }

        public int getKey(){
            return switchData.keys.get(index);
        }
        public void setKey(int value){
            switchData.keys.put(index, value);
        }
        @Override
        public int get(){
            return switchData.elements.get(index);
        }
        @Override
        public void set(int value){
            switchData.elements.put(index, value);
        }

        @Override
        public int getSortOrder() {
            return ExtraLine.ORDER_INSTRUCTION_LABEL;
        }
        @Override
        public int getAddress() {
            return switchData.getAddress();
        }
        @Override
        public int getTargetAddress() {
            return getKey() + switchData.getBaseAddress();
        }
        @Override
        public void setTargetAddress(int targetAddress){
            setKey(targetAddress - switchData.getBaseAddress());
        }
        @Override
        public String getLabelName() {
            return HexUtil.toHex(":sswitch_", getTargetAddress(), 1);
        }
        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.newLine();
            writer.append(HexUtil.toHex8(get()));
            writer.append(" -> ");
            writer.append(getLabelName());
        }
        @Override
        public int hashCode() {
            return Objects.hash(switchData, index);
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Data data = (Data) obj;
            return index == data.index && switchData == data.switchData;
        }
        @Override
        public String toString() {
            return HexUtil.toHex8(get());
        }

    }
}