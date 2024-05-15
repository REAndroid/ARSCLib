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

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.item.*;
import com.reandroid.common.ArraySupplier;
import com.reandroid.dex.base.CountedList;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.model.SmaliInstruction;
import com.reandroid.dex.smali.model.SmaliPayloadSparseSwitch;
import com.reandroid.dex.smali.model.SmaliSet;
import com.reandroid.dex.smali.model.SmaliSparseSwitchEntry;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.ArraySupplierIterator;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

public class InsSparseSwitchData extends PayloadData implements
        ArraySupplier<InsSparseSwitchData.SSData>, LabelsSet, SmaliFormat {

    private final ShortItem elementCount;
    final CountedList<IntegerItem> elements;
    final CountedList<IntegerItem> keys;
    private InsSparseSwitch insSparseSwitch;

    boolean mSortRequired;

    public InsSparseSwitchData() {
        super(3, Opcode.SPARSE_SWITCH_PAYLOAD);
        this.elementCount = new ShortItem();

        Creator<IntegerItem> creator = new Creator<IntegerItem>() {
            @Override
            public IntegerItem[] newArrayInstance(int length) {
                return new IntegerItem[length];
            }
            @Override
            public IntegerItem newInstance() {
                return new IntegerItem();
            }
        };

        this.elements = new CountedList<>(elementCount, creator);
        this.keys = new CountedList<>(elementCount, creator);

        addChild(1, elementCount);
        addChild(2, elements);
        addChild(3, keys);
    }

    public SSData newEntry() {
        int index = getCount();
        setCount(index + 1);
        return get(index);
    }
    @Override
    public SSData get(int i){
        if(i < 0 || i >= getCount()){
            return null;
        }
        return new SSData(this, elements.get(i), keys.get(i));
    }
    @Override
    public int getCount(){
        return elements.size();
    }
    public void setCount(int count){
        elements.setSize(count);
        keys.setSize(count);
        elementCount.set(count);
    }
    public void sort() {
        this.mSortRequired = false;
        Comparator<IntegerItem> comparator = (item1, item2) -> CompareUtil.compare(item1.get(), item2.get());
        if(!elements.needsSort(comparator)) {
            return;
        }
        this.elements.sort(comparator, keys);
    }
    @Override
    public Iterator<IntegerReference> getReferences() {
        return ObjectsUtil.cast(getLabels());
    }
    @Override
    public Iterator<SSData> getLabels() {
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
        InsSparseSwitch sparseSwitch = this.insSparseSwitch;
        if(sparseSwitch == null){
            sparseSwitch = findOnExtraLines();
            if(sparseSwitch == null){
                sparseSwitch = findByAddress();
            }
            this.insSparseSwitch = sparseSwitch;
        }
        return insSparseSwitch;
    }
    public void setParentSparseSwitch(InsSparseSwitch sparseSwitch) {
        this.insSparseSwitch = sparseSwitch;
        addExtraLine(sparseSwitch);
        sparseSwitch.setTargetAddress(getAddress());
    }
    private InsSparseSwitch findOnExtraLines() {
        Iterator<ExtraLine> iterator = getExtraLines();
        while (iterator.hasNext()){
            ExtraLine extraLine = iterator.next();
            if(extraLine instanceof InsSparseSwitch){
                return (InsSparseSwitch) extraLine;
            }
        }
        return null;
    }
    private InsSparseSwitch findByAddress() {
        InstructionList instructionList = getInstructionList();
        if(instructionList != null){
            Iterator<InsSparseSwitch> iterator = instructionList.iterator(Opcode.SPARSE_SWITCH);
            int address = getAddress();
            while (iterator.hasNext()){
                InsSparseSwitch sparseSwitch = iterator.next();
                if(sparseSwitch.getTargetAddress() == address){
                    return sparseSwitch;
                }
            }
        }
        return null;
    }

    @Override
    protected void onPreRefresh() {
        sort();
        super.onPreRefresh();
    }
    @Override
    public void merge(Ins ins){
        InsSparseSwitchData switchData = (InsSparseSwitchData) ins;
        int size = switchData.getCount();
        this.setCount(size);
        for(int i = 0; i < size; i++){
            get(i).merge(switchData.get(i));
        }
    }

    @Override
    public void fromSmali(SmaliInstruction smaliInstruction) throws IOException {
        validateOpcode(smaliInstruction);
        SmaliPayloadSparseSwitch smaliPayloadSparseSwitch = (SmaliPayloadSparseSwitch) smaliInstruction;
        SmaliSet<SmaliSparseSwitchEntry> entries = smaliPayloadSparseSwitch.getEntries();
        int count = entries.size();
        this.setCount(count);
        for(int i = 0; i < count; i++){
            SmaliSparseSwitchEntry smaliEntry = entries.get(i);
            SSData data = get(i);
            data.fromSmali(smaliEntry);
        }
    }

    @Override
    void appendCode(SmaliWriter writer) throws IOException {
        writer.newLine();
        getSmaliDirective().append(writer);
        int size = getCount();
        writer.indentPlus();
        for(int i = 0; i < size; i++){
            get(i).append(writer);
        }
        writer.indentMinus();
        writer.newLine();
        getSmaliDirective().appendEnd(writer);
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.SPARSE_SWITCH;
    }

    public static class SSData implements IntegerReference, Label, SmaliFormat {

        private final InsSparseSwitchData switchData;
        private final IntegerReference element;
        private final IntegerReference key;

        public SSData(InsSparseSwitchData switchData, IntegerReference element, IntegerReference key){
            this.switchData = switchData;
            this.element = element;
            this.key = key;
        }

        @Override
        public int get(){
            return element.get();
        }
        @Override
        public void set(int value){
            if(value != element.get()) {
                element.set(value);
                this.switchData.mSortRequired = true;
            }
        }

        public int getKey(){
            return key.get();
        }
        public void setKey(int value){
            key.set(value);
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
            int value = get();
            writer.appendHex(value);
            writer.append(" -> ");
            writer.appendLabelName(getLabelName());
            writer.appendResourceIdComment(value);
        }
        public void merge(SSData data) {
            set(data.get());
            setKey(data.getKey());
        }
        public void fromSmali(SmaliSparseSwitchEntry smaliEntry) throws IOException{
            set(smaliEntry.getIntegerValue());
            setTargetAddress(smaliEntry.getLabel().getIntegerData());
        }
        @Override
        public int hashCode() {
            return Objects.hash(switchData, element);
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            SSData data = (SSData) obj;
            return element == data.element && switchData == data.switchData;
        }
        @Override
        public String toString() {
            return HexUtil.toHex8(get()) + " -> " + getKey();
        }
    }
}