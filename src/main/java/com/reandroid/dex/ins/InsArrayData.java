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
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.program.InstructionLabelType;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliInstruction;
import com.reandroid.dex.smali.model.SmaliInstructionOperand;
import com.reandroid.dex.smali.model.SmaliPayloadArray;
import com.reandroid.utils.NumberX;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.InstanceIterator;

import java.io.IOException;
import java.util.Iterator;

public class InsArrayData extends PayloadData<ArrayDataEntry> implements SmaliRegion {

    public InsArrayData() {
        super(4, Opcode.ARRAY_PAYLOAD);

        ShortItem widthReference = new ShortItem();
        IntegerItem countReference = new IntegerItem();
        InsArrayDataList entryList = new InsArrayDataList(widthReference, countReference);

        setupBlock(1, widthReference);
        setupBlock(2, countReference);
        setupBlock(3, entryList);
        setupBlock(4, entryList.getAlignment());
    }

    public Iterator<InsFillArrayData> getInsFillArrayData() {
        InsBlockList insBlockList = getInsBlockList();
        if (insBlockList == null) {
            return EmptyIterator.of();
        }
        insBlockList.link();
        return InstanceIterator.of(getReferencingLabels(), InsFillArrayData.class);
    }
    @Override
    public int size(){
        return entryList().size();
    }
    @Override
    public void setSize(int size) {
        if (getWidth() == 0) {
            size = 0;
        }
        Object lock = requestLock();
        entryList().setSize(size);
        releaseLock(lock);
    }
    public void ensureMinWidth(int width) {
        if (width > getWidth()) {
            setWidth(NumberX.toStandardWidth(width));
        }
    }
    public void ensureMinSize(int size) {
        if (size > size()) {
            setSize(size);
        }
    }
    public void clear() {
        Object lock = requestLock();
        entryList().clear();
        releaseLock(lock);
    }
    @Override
    public ArrayDataEntry get(int i) {
        return entryList().get(i);
    }
    @Override
    public Iterator<ArrayDataEntry> iterator() {
        return ObjectsUtil.cast(entryList().clonedIterator());
    }

    public int getWidth() {
        return entryList().getWidth();
    }
    public void setWidth(int width) {
        Object lock = requestLock();
        entryList().setWidth(width);
        releaseLock(lock);
    }
    public void put(int index, long value) {
        ensureMinSize(index + 1);
        get(index).set(value);
    }
    public void addValue(long value) {
        int index = size();
        setSize(index + 1);
        get(index).set(value);
    }
    public void addValues(long[] values) {
        int start = size();
        int length = values.length;
        setSize(start + length);
        for (int i = 0; i < length; i++) {
            get(start + i).set(values[i]);
        }
    }
    public void addValues(int[] values) {
        int start = size();
        int length = values.length;
        setSize(start + length);
        for (int i = 0; i < length; i++) {
            get(start + i).set(values[i]);
        }
    }
    public void addValues(short[] values) {
        int start = size();
        int length = values.length;
        setSize(start + length);
        for (int i = 0; i < length; i++) {
            get(start + i).set(values[i]);
        }
    }
    public void addValues(byte[] values) {
        int start = size();
        int length = values.length;
        setSize(start + length);
        for (int i = 0; i < length; i++) {
            get(start + i).set(values[i]);
        }
    }
    public void addValues(char[] values) {
        int start = size();
        int length = values.length;
        setSize(start + length);
        for (int i = 0; i < length; i++) {
            get(start + i).set(values[i]);
        }
    }
    public void addValues(float[] values) {
        int start = size();
        int length = values.length;
        if (length != 0) {
            ensureMinWidth(4);
        }
        setSize(start + length);
        for (int i = 0; i < length; i++) {
            get(start + i).set(Float.floatToIntBits(values[i]));
        }
    }
    public void addValues(double[] values) {
        int start = size();
        int length = values.length;
        if (length != 0) {
            ensureMinWidth(8);
        }
        setSize(start + length);
        for (int i = 0; i < length; i++) {
            get(start + i).set(Double.doubleToLongBits(values[i]));
        }
    }
    public long[] getValuesAsLong() {
        return entryList().getValues();
    }
    public int[] getValuesAsInt() {
        return entryList().getValuesAsInt();
    }
    public short[] getValuesAsShort() {
        return entryList().getValuesAsShort();
    }
    public byte[] getValuesAsByte() {
        return entryList().getValuesAsByte();
    }
    public char[] getValuesAsChar() {
        return entryList().getValuesAsChar();
    }
    public float[] getValuesAsFloat() {
        return entryList().getValuesAsFloat();
    }
    public double[] getValuesAsDouble() {
        return entryList().getValuesAsDouble();
    }

    @Override
    public Iterator<IntegerReference> getReferences() {
        return ObjectsUtil.cast(entryList().iterator());
    }
    InsArrayDataList entryList() {
        return block(3);
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
    public InstructionLabelType getLabelType() {
        return InstructionLabelType.ARRAY_DATA;
    }

    @Override
    public void appendCode(SmaliWriter writer) throws IOException {
        getSmaliDirective().append(writer);
        writer.appendInteger(getWidth());
        writer.indentPlus();
        entryList().append(findNewArrayType(), writer);
        writer.indentMinus();
        getSmaliDirective().appendEnd(writer);
    }

    @Override
    public void merge(Ins ins) {
        InsArrayData coming = (InsArrayData) ins;
        entryList().merge(coming.entryList());
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.ARRAY_DATA;
    }

    @Override
    public void fromSmali(SmaliInstruction smaliInstruction) {
        validateOpcode(smaliInstruction);
        entryList().fromSmali((SmaliPayloadArray) smaliInstruction);
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
        entryList().toSmali((SmaliPayloadArray) instruction);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        InsArrayData arrayData = (InsArrayData) obj;
        if (getIndex() != arrayData.getIndex()) {
            return false;
        }
        if (getWidth() != arrayData.getWidth()) {
            return false;
        }
        return entryList().equals(arrayData.entryList());
    }

    @Override
    public int hashCode() {
        int hash = 31 + getIndex() * 31 + getWidth() * 31;
        return hash + entryList().hashCode() * 31;
    }
}