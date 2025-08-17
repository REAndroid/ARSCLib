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

import com.reandroid.arsc.container.CountedBlockList;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.common.ArraySupplier;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.model.SmaliInstruction;
import com.reandroid.dex.smali.model.SmaliPayloadSparseSwitch;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.ArraySupplierIterator;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

public class InsSparseSwitchData extends InsSwitchPayload<SparseSwitchEntry> {

    private final ShortItem elementCount;
    final CountedBlockList<IntegerItem> elements;
    final CountedBlockList<SparseSwitchEntryKey> keys;

    boolean mSortRequired;

    public InsSparseSwitchData() {
        super(3, Opcode.SPARSE_SWITCH_PAYLOAD);
        this.elementCount = new ShortItem();

        this.elements = new CountedBlockList<>(IntegerItem.CREATOR, elementCount);
        this.keys = new CountedBlockList<>(SparseSwitchEntryKey.CREATOR, elementCount);

        addChild(1, elementCount);
        addChild(2, elements);
        addChild(3, keys);
    }

    @Override
    public Iterator<SparseSwitchEntry> iterator() {
        return ObjectsUtil.cast(getLabels());
    }
    @Override
    public SparseSwitchEntry get(int index) {
        if (index < 0 || index >= size()) {
            return null;
        }
        return new SparseSwitchEntry(this, elements.get(index), keys.get(index));
    }
    @Override
    public int size() {
        return elements.size();
    }
    @Override
    public void setSize(int size) {
        Object lock = requestLock();
        elements.setSize(size);
        keys.setSize(size);
        elementCount.set(size);
        releaseLock(lock);
    }
    public boolean remove(SwitchEntry entry) {
        if (!(entry instanceof SparseSwitchEntry)) {
            return false;
        }
        SparseSwitchEntry switchEntry = (SparseSwitchEntry) entry;
        Object lock = requestLock();
        boolean removed = this.elements.remove((IntegerItem) switchEntry.getElement());
        if (removed) {
            this.keys.remove(switchEntry.getEntryKey());
        }
        releaseLock(lock);
        return removed;
    }
    public void sort() {
        if (!mSortRequired) {
            return;
        }
        this.mSortRequired = false;
        Comparator<IntegerItem> comparator = (item1, item2) -> CompareUtil.compare(item1.get(), item2.get());
        if (!elements.needsSort(comparator)) {
            return;
        }
        this.elements.sort(comparator, keys);
    }
    @Override
    public Iterator<SparseSwitchEntry> getLabels() {
        return new ArraySupplierIterator<>(new ArraySupplier<SparseSwitchEntry>() {
            @Override
            public SparseSwitchEntry get(int i) {
                return InsSparseSwitchData.this.get(i);
            }
            @Override
            public int getCount() {
                return InsSparseSwitchData.this.size();
            }
        });
    }

    public int getBaseAddress() {
        InsSparseSwitch sparseSwitch = getSwitch();
        if (sparseSwitch == null) {
            return 0;
        }
        return sparseSwitch.getAddress();
    }

    @Override
    public Opcode<InsSparseSwitch> getSwitchOpcode() {
        return Opcode.SPARSE_SWITCH;
    }
    @Override
    public InsSparseSwitch getSwitch() {
        return (InsSparseSwitch) super.getSwitch();
    }
    @Override
    protected void onPreRefresh() {
        sort();
        super.onPreRefresh();
    }
    void fromPackedSwitchData(PackedSwitchDataList packedSwitchDataList) {
        int length = packedSwitchDataList.size();
        setSize(length);
        for(int i = 0; i < length; i++) {
            get(i).fromPackedSwitch(packedSwitchDataList.get(i));
        }
    }
    @Override
    public void merge(Ins ins) {
        InsSparseSwitchData switchData = (InsSparseSwitchData) ins;
        int size = switchData.size();
        this.setSize(size);
        for(int i = 0; i < size; i++) {
            get(i).merge(switchData.get(i));
        }
    }

    @Override
    public void fromSmali(SmaliInstruction smaliInstruction) {
        validateOpcode(smaliInstruction);
        SmaliPayloadSparseSwitch smaliPayload = (SmaliPayloadSparseSwitch) smaliInstruction;
        int count = smaliPayload.getCount();
        this.setSize(count);
        for(int i = 0; i < count; i++) {
            get(i).fromSmali(smaliPayload.getEntry(i));
        }
        mSortRequired = true;
    }
    @Override
    void toSmaliEntries(SmaliInstruction instruction) {
        super.toSmaliEntries(instruction);
        SmaliPayloadSparseSwitch smaliPayload = (SmaliPayloadSparseSwitch) instruction;
        int size = size();
        for (int i = 0; i < size; i++) {
            smaliPayload.addEntry(get(i).toSmali());
        }
    }

    @Override
    public void appendCode(SmaliWriter writer) throws IOException {
        writer.append('.');
        writer.append(getSmaliDirective().getName());
        int size = size();
        writer.indentPlus();
        for(int i = 0; i < size; i++) {
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

}