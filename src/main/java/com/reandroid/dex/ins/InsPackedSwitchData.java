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

import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.*;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;
import java.util.Iterator;

public class InsPackedSwitchData extends PayloadData implements LabelsSet, SmaliRegion {
    private final ShortItem elementCount;
    private final IntegerItem firstKey;
    private final PackedSwitchDataList elements;
    private InsPackedSwitch insPackedSwitch;

    private InsSparseSwitchData mReplacement;

    public InsPackedSwitchData() {
        super(3, Opcode.PACKED_SWITCH_PAYLOAD);
        this.elementCount = new ShortItem();
        this.firstKey = new IntegerItem();
        this.elements = new PackedSwitchDataList(this, elementCount);

        addChild(1, elementCount);
        addChild(2, firstKey);
        addChild(3, elements);
    }

    public int getFirstKey() {
        return firstKey.get();
    }
    public void setFirstKey(int firstKey){
        this.firstKey.set(firstKey);
    }

    @Override
    public Iterator<IntegerReference> getReferences() {
        return ObjectsUtil.cast(getLabels());
    }


    void onDataChange(int index, int value) {
        InsSparseSwitchData sparseSwitchData = this.mReplacement;
        if(sparseSwitchData == null) {
            sparseSwitchData = replaceBySparse();
        }
        sparseSwitchData.get(index).set(value);
    }
    private InsSparseSwitchData replaceBySparse() {
        int[][] copy = elements.makeCopy();
        InsPackedSwitch packedSwitch = getParentPackedSwitch();
        InsSparseSwitch sparseSwitch = packedSwitch.getReplacement();
        InsSparseSwitchData sparseSwitchData = this.replace(Opcode.SPARSE_SWITCH_PAYLOAD);
        this.mReplacement = sparseSwitchData;
        this.clearExtraLines();
        sparseSwitch.clearExtraLines();
        sparseSwitchData.clearExtraLines();
        int size = copy.length;
        sparseSwitchData.setCount(size);
        for(int i = 0; i < size; i++) {
            InsSparseSwitchData.SSData data = sparseSwitchData.get(i);
            int[] c = copy[i];
            data.set(c[0]);
            data.setTargetAddress(c[1]);
        }
        return sparseSwitchData;
    }

    public InsPackedSwitch getParentPackedSwitch() {
        InsPackedSwitch packedSwitch = this.insPackedSwitch;
        if(packedSwitch == null){
            packedSwitch = findOnExtraLines();
            if(packedSwitch == null){
                packedSwitch = findByAddress();
            }
            this.insPackedSwitch = packedSwitch;
        }
        return insPackedSwitch;
    }
    private InsPackedSwitch findOnExtraLines() {
        Iterator<ExtraLine> iterator = getExtraLines();
        while (iterator.hasNext()){
            ExtraLine extraLine = iterator.next();
            if(extraLine instanceof InsPackedSwitch){
                return (InsPackedSwitch) extraLine;
            }
        }
        return null;
    }
    private InsPackedSwitch findByAddress() {
        InstructionList instructionList = getInstructionList();
        if(instructionList != null){
            Iterator<InsPackedSwitch> iterator = instructionList.iterator(Opcode.PACKED_SWITCH);
            int address = getAddress();
            while (iterator.hasNext()){
                InsPackedSwitch packedSwitch = iterator.next();
                if(packedSwitch.getTargetAddress() == address){
                    return packedSwitch;
                }
            }
        }
        return null;
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.PACKED_SWITCH;
    }

    @Override
    public Iterator<PackedSwitchDataList.PSData> getLabels() {
        return elements.getLabels();
    }

    @Override
    public void merge(Ins ins){
        InsPackedSwitchData switchData = (InsPackedSwitchData) ins;
        setFirstKey(switchData.getFirstKey());
        elements.merge(switchData.elements);
    }

    @Override
    public void fromSmali(SmaliInstruction smaliInstruction) throws IOException {
        validateOpcode(smaliInstruction);
        SmaliPayloadPackedSwitch smaliPayloadPackedSwitch = (SmaliPayloadPackedSwitch) smaliInstruction;
        setFirstKey(smaliPayloadPackedSwitch.getFirstKey());
        SmaliSet<SmaliLabel> entries = smaliPayloadPackedSwitch.getEntries();
        int size = entries.size();
        PackedSwitchDataList dataList = this.elements;
        dataList.setSize(size);
        for(int i = 0; i < size; i++){
            SmaliLabel smaliLabel = entries.get(i);
            PackedSwitchDataList.PSData data = dataList.get(i);
            data.setTargetAddress(smaliLabel.getAddress());
        }
    }

    @Override
    void appendCode(SmaliWriter writer) throws IOException {
        writer.newLine();
        getSmaliDirective().append(writer);
        writer.append(HexUtil.toHex(firstKey.get(), 1));
        writer.indentPlus();
        elements.append(writer);
        writer.indentMinus();
        getSmaliDirective().appendEnd(writer);
    }

    @Override
    public String toString() {
        return "InsPackedSwitchData{" +
                "elementCount=" + elementCount +
                ", firstKey=" + firstKey +
                ", elements=" + elements +
                '}';
    }
}