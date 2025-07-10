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
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliInstruction;
import com.reandroid.dex.smali.model.SmaliInstructionOperand;
import com.reandroid.dex.smali.model.SmaliPayloadPackedSwitch;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;
import java.util.Iterator;

public class InsPackedSwitchData extends InsSwitchPayload<PackedSwitchEntry> {

    private final IntegerItem firstKey;
    private final PackedSwitchDataList elements;

    private InsSparseSwitchData mReplacement;

    public InsPackedSwitchData() {
        super(3, Opcode.PACKED_SWITCH_PAYLOAD);
        ShortItem elementCount = new ShortItem();
        this.firstKey = new IntegerItem();
        this.elements = new PackedSwitchDataList(this, elementCount);

        addChild(1, elementCount);
        addChild(2, firstKey);
        addChild(3, elements);
    }

    public int getFirstKey() {
        return firstKey.get();
    }
    public void setFirstKey(int firstKey) {
        this.firstKey.set(firstKey);
    }

    @Override
    public Iterator<PackedSwitchEntry> iterator() {
        return ObjectsUtil.cast(elements.iterator());
    }
    @Override
    public PackedSwitchEntry get(int index) {
        return elements.get(index);
    }
    @Override
    public int size() {
        return elements.size();
    }
    @Override
    public void setSize(int size) {
        Object lock = requestLock();
        elements.setSize(size);
        releaseLock(lock);
    }

    void onDataChange(int index, int value) {
        replaceBySparse().get(index).set(value);
    }
    public InsSparseSwitchData replaceBySparse() {
        InsSparseSwitchData sparseData = this.mReplacement;
        if (sparseData != null) {
            return sparseData;
        }
        Object lock = requestLock();

        InsPackedSwitch packed = getSwitch();
        InsSparseSwitch sparse = packed.getSparseSwitchReplacement();

        sparseData = Opcode.SPARSE_SWITCH_PAYLOAD.newInstance();
        this.mReplacement = sparseData;

        sparseData.setSwitch(sparse);
        sparseData.fromPackedSwitchData(this.elements);

        this.replace(sparseData);

        releaseLock(lock);

        return sparseData;
    }

    @Override
    public Opcode<InsPackedSwitch> getSwitchOpcode() {
        return Opcode.PACKED_SWITCH;
    }

    @Override
    public InsPackedSwitch getSwitch() {
        return (InsPackedSwitch) super.getSwitch();
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.PACKED_SWITCH;
    }

    @Override
    public Iterator<PackedSwitchEntry> getLabels() {
        return elements.getLabels();
    }

    @Override
    public void merge(Ins ins) {
        InsPackedSwitchData switchData = (InsPackedSwitchData) ins;
        setFirstKey(switchData.getFirstKey());
        elements.merge(switchData.elements);
    }

    @Override
    public void fromSmali(SmaliInstruction smaliInstruction) {
        validateOpcode(smaliInstruction);
        SmaliPayloadPackedSwitch smaliPayloadPackedSwitch =
                (SmaliPayloadPackedSwitch) smaliInstruction;
        setFirstKey(smaliPayloadPackedSwitch.getFirstKey());
        this.elements.fromSmali(smaliPayloadPackedSwitch);
    }

    @Override
    void toSmaliOperand(SmaliInstruction instruction) {
        super.toSmaliOperand(instruction);
        SmaliInstructionOperand.SmaliHexOperand operand =
                (SmaliInstructionOperand.SmaliHexOperand) instruction.getOperand();
        operand.setNumber(getFirstKey());
    }
    @Override
    void toSmaliEntries(SmaliInstruction instruction) {
        super.toSmaliEntries(instruction);
        this.elements.toSmali((SmaliPayloadPackedSwitch) instruction);
    }

    @Override
    public void appendCode(SmaliWriter writer) throws IOException {
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
                ", firstKey=" + firstKey +
                ", elements=" + elements +
                '}';
    }

    public static final Creator<PackedSwitchEntry> CREATOR = PackedSwitchEntry::new;
}