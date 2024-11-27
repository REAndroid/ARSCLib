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

import com.reandroid.arsc.item.*;
import com.reandroid.dex.base.DexBlockAlign;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliInstruction;
import com.reandroid.dex.smali.model.SmaliInstructionOperand;
import com.reandroid.dex.smali.model.SmaliPayloadArray;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.InstanceIterator;

import java.io.IOException;
import java.util.Iterator;

public class InsArrayData extends PayloadData implements SmaliRegion {

    private final DexBlockAlign blockAlign;
    private final InsArrayDataList entryList;

    public InsArrayData() {
        super(4, Opcode.ARRAY_PAYLOAD);

        ShortItem widthReference = new ShortItem();
        IntegerItem countReference = new IntegerItem();
        this.entryList = new InsArrayDataList(widthReference, countReference);

        this.blockAlign = new DexBlockAlign(this.entryList);
        this.blockAlign.setAlignment(2);

        addChild(1, widthReference);
        addChild(2, countReference);
        addChild(3, this.entryList);
        addChild(4, this.blockAlign);
    }

    public Iterator<InsFillArrayData> getInsFillArrayData() {
        InsBlockList insBlockList = getInsBlockList();
        if (insBlockList == null) {
            return EmptyIterator.of();
        }
        insBlockList.link();
        return InstanceIterator.of(getExtraLines(), InsFillArrayData.class);
    }
    public int size(){
        return getEntryList().size();
    }
    public void setSize(int size) {
        getEntryList().setSize(size);
        refreshAlignment();
    }
    public void clear() {
        getEntryList().clear();
        refreshAlignment();
    }
    public boolean isEmpty() {
        return getEntryList().isEmpty();
    }
    public LongReference get(int i) {
        return getEntryList().get(i);
    }
    public Iterator<LongReference> iterator() {
        return ObjectsUtil.cast(getEntryList().iterator());
    }
    public int getWidth() {
        return getEntryList().getWidth();
    }
    public void setWidth(int width) {
        getEntryList().setWidth(width);
        refreshAlignment();
    }
    public void put(int index, long value) {
        int changed = size();
        getEntryList().put(index, value);
        if (changed != this.size()) {
            refreshAlignment();
        }
    }
    public void addValue(long value) {
        getEntryList().addValue(value);
        refreshAlignment();
    }
    public void set(long[] values) {
        getEntryList().clear();
        addValues(values);
    }
    public void addValues(long[] values) {
        getEntryList().addValues(values);
        refreshAlignment();
    }
    public void addValues(int[] values) {
        getEntryList().addValues(values);
        refreshAlignment();
    }
    public void addValues(short[] values) {
        getEntryList().addValues(values);
        refreshAlignment();
    }
    public void addValues(byte[] values) {
        getEntryList().addValues(values);
        refreshAlignment();
    }
    public void addValues(char[] values) {
        getEntryList().addValues(values);
        refreshAlignment();
    }
    public void addValues(float[] values) {
        getEntryList().addValues(values);
        refreshAlignment();
    }
    public void addValues(double[] values) {
        getEntryList().addValues(values);
        refreshAlignment();
    }
    public long[] getValuesAsLong() {
        return getEntryList().getValues();
    }
    public int[] getValuesAsInt() {
        return getEntryList().getValuesAsInt();
    }
    public short[] getValuesAsShort() {
        return getEntryList().getValuesAsShort();
    }
    public byte[] getValuesAsByte() {
        return getEntryList().getValuesAsByte();
    }
    public char[] getValuesAsChar() {
        return getEntryList().getValuesAsChar();
    }
    public float[] getValuesAsFloat() {
        return getEntryList().getValuesAsFloat();
    }
    public double[] getValuesAsDouble() {
        return getEntryList().getValuesAsDouble();
    }

    @Override
    public Iterator<IntegerReference> getReferences() {
        return ObjectsUtil.cast(getEntryList().iterator());
    }
    InsArrayDataList getEntryList() {
        return entryList;
    }

    public void refreshAlignment() {
        this.blockAlign.align(this);
    }

    private TypeKey findNewArrayType() {
        Iterator<InsFillArrayData> iterator = getInsFillArrayData();
        while (iterator.hasNext()) {
            InsFillArrayData fillArrayData = iterator.next();
            Ins22c ins22c = fillArrayData.findNewArrayLazy();
            if (ins22c != null) {
                return (TypeKey) ins22c.getKey();
            }
        }
        return null;
    }
    @Override
    public void appendCode(SmaliWriter writer) throws IOException {
        getSmaliDirective().append(writer);
        writer.appendInteger(getWidth());
        writer.indentPlus();
        getEntryList().append(findNewArrayType(), writer);
        writer.indentMinus();
        getSmaliDirective().appendEnd(writer);
    }

    @Override
    public void merge(Ins ins) {
        InsArrayData coming = (InsArrayData) ins;
        getEntryList().merge(coming.getEntryList());
        refreshAlignment();
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.ARRAY_DATA;
    }

    @Override
    public void fromSmali(SmaliInstruction smaliInstruction) {
        validateOpcode(smaliInstruction);
        getEntryList().fromSmali((SmaliPayloadArray) smaliInstruction);
        refreshAlignment();
    }
    @Override
    void toSmaliOperand(SmaliInstruction instruction) {
        super.toSmaliOperand(instruction);
        SmaliInstructionOperand.SmaliDecimalOperand operand =
                (SmaliInstructionOperand.SmaliDecimalOperand) instruction.getOperand();
        operand.setNumber(getWidth());
    }
    @Override
    void toSmaliEntries(SmaliInstruction instruction) {
        super.toSmaliEntries(instruction);
        getEntryList().toSmali((SmaliPayloadArray) instruction);
    }
}