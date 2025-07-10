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

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliSparseSwitchEntry;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;

public class SparseSwitchEntry implements SwitchEntry {

    private final InsSparseSwitchData payload;
    private final IntegerReference element;
    private final SparseSwitchEntryKey entryKey;

    public SparseSwitchEntry(InsSparseSwitchData payload,
                             IntegerReference element,
                             SparseSwitchEntryKey entryKey) {
        this.payload = payload;
        this.element = element;
        this.entryKey = entryKey;
    }

    @Override
    public InsSparseSwitchData getPayload() {
        return payload;
    }

    @Override
    public int get() {
        return element.get();
    }
    @Override
    public void set(int value) {
        if (value != element.get()) {
            element.set(value);
            this.payload.mSortRequired = true;
        }
    }
    @Override
    public int getIndex() {
        return entryKey.getIndex();
    }

    @Override
    public Ins getTargetIns() {
        Ins targetIns = this.entryKey.getTargetIns();
        if (targetIns == null) {
            setTargetIns(findTargetIns());
            targetIns = this.entryKey.getTargetIns();
        }
        return targetIns;
    }

    @Override
    public void setTargetIns(Ins targetIns) {
        Ins ins = entryKey.getTargetIns();
        if (targetIns != ins) {
            entryKey.setTargetIns(targetIns);
            if (targetIns != null) {
                targetIns.addExtraLine(this);
            }
        }
    }

    SparseSwitchEntryKey getEntryKey() {
        return entryKey;
    }
    IntegerReference getElement() {
        return element;
    }

    public int getKey() {
        return entryKey.get();
    }

    public void setKey(int value) {
        entryKey.set(value);
    }

    @Override
    public int getSortOrder() {
        return ExtraLine.ORDER_INSTRUCTION_LABEL;
    }

    @Override
    public int getAddress() {
        return payload.getAddress();
    }

    @Override
    public int getTargetAddress() {
        return getKey() + payload.getBaseAddress();
    }

    @Override
    public void setTargetAddress(int targetAddress) {
        setKey(targetAddress - payload.getBaseAddress());
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

    @Override
    public void appendExtra(SmaliWriter writer) throws IOException {
        writer.appendLabelName(getLabelName());
        writer.appendComment(HexUtil.toSignedHex(get()));
    }

    public void removeSelf() {
        payload.remove(this);
    }

    public void fromPackedSwitch(PackedSwitchEntry packedSwitchEntry) {
        this.set(packedSwitchEntry.get());
        Ins ins = packedSwitchEntry.getTargetIns();
        this.setTargetAddress(ins.getAddress());
        this.setTargetIns(ins);
        ins.addExtraLine(this);
    }

    public void merge(SparseSwitchEntry data) {
        set(data.get());
        setKey(data.getKey());
    }

    public void fromSmali(SmaliSparseSwitchEntry smaliEntry) {
        set(smaliEntry.getValue());
        setKey(smaliEntry.getRelativeOffset());
    }

    public SmaliSparseSwitchEntry toSmali() {
        SmaliSparseSwitchEntry entry = new SmaliSparseSwitchEntry();
        entry.getLabel().setLabelName(getLabelName());
        entry.setValue(get());
        return entry;
    }

    @Override
    public int hashCode() {
        return ObjectsUtil.hash(payload, element);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SparseSwitchEntry data = (SparseSwitchEntry) obj;
        return element == data.element && payload == data.payload;
    }

    @Override
    public String toString() {
        return HexUtil.toHex8(get()) + " -> " + getKey();
    }
}
