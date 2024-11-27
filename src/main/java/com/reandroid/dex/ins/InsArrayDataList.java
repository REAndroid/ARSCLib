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
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.CountedBlockList;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.NumberBlock;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliPayloadArray;
import com.reandroid.dex.smali.model.SmaliSet;
import com.reandroid.dex.smali.model.SmaliValueX;
import com.reandroid.utils.NumberX;
import com.reandroid.utils.StringsUtil;

import java.io.IOException;
import java.util.Iterator;

public class InsArrayDataList extends CountedBlockList<InsArrayDataList.ArrayDataEntry> {

    private final IntegerReference widthReference;

    public InsArrayDataList(IntegerReference widthReference, IntegerReference countReference) {
        super(new EntryCreator(widthReference), countReference);
        this.widthReference = widthReference;
    }

    @Override
    public void setSize(int size) {
        super.setSize(size);
        IntegerReference countReference = getCountReference();
        if (size != countReference.get()) {
            countReference.set(size);
        }
    }

    public int getWidth() {
        return widthReference.get();
    }
    public void setWidth(int width) {
        this.widthReference.set(width);
        if (width == 0) {
            clear();
        } else {
            int size = size();
            for (int i = 0; i < size; i++) {
                get(i).width(width);
            }
        }
    }
    public void ensureWidth(int width) {
        if (width > getWidth()) {
            setWidth(NumberX.toStandardWidth(width));
        }
    }
    public void put(int index, long value) {
        ensureSize(index + 1);
        get(index).set(value);
    }
    public void addValues(long[] values) {
        int length = values.length;
        int index = size();
        setSize(index + length);
        for (int i = 0; i < length; i++) {
            long value = values[i];
            ensureWidth(NumberX.widthOfSigned(value));
            get(index + i).set(value);
        }
    }
    public void addValues(int[] values) {
        int length = values.length;
        int index = size();
        setSize(index + length);
        for (int i = 0; i < length; i++) {
            long value = values[i];
            ensureWidth(NumberX.widthOfSigned(value));
            get(index + i).set(value);
        }
    }
    public void addValues(short[] values) {
        int length = values.length;
        int index = size();
        ensureWidth(2);
        setSize(index + length);
        for (int i = 0; i < length; i++) {
            get(index + i).set((long) values[i]);
        }
    }
    public void addValues(byte[] values) {
        int length = values.length;
        int index = size();
        ensureWidth(1);
        setSize(index + length);
        for (int i = 0; i < length; i++) {
            get(index + i).set((long) values[i]);
        }
    }
    public void addValues(char[] values) {
        int length = values.length;
        int index = size();
        setSize(index + length);
        for (int i = 0; i < length; i++) {
            long value = values[i];
            ensureWidth(NumberX.widthOfSigned(value));
            get(index + i).set(value);
        }
    }
    public void addValues(float[] values) {
        int length = values.length;
        int index = size();
        ensureWidth(4);
        setSize(index + length);
        for (int i = 0; i < length; i++) {
            long value = Float.floatToIntBits(values[i]);
            get(index + i).set(value);
        }
    }
    public void addValues(double[] values) {
        int length = values.length;
        int index = size();
        ensureWidth(8);
        setSize(index + length);
        for (int i = 0; i < length; i++) {
            long value = Double.doubleToLongBits(values[i]);
            get(index + i).set(value);
        }
    }
    public void addValue(long value) {
        ensureWidth(NumberX.widthOfSigned(value));
        createNext().set(value);
    }
    public long[] getValues() {
        int size = size();
        long[] results = new long[size];
        for (int i = 0; i < size; i++) {
            results[i] = get(i).getLong();
        }
        return results;
    }
    public int[] getValuesAsInt() {
        int size = size();
        int[] results = new int[size];
        for (int i = 0; i < size; i++) {
            results[i] = (int) get(i).getLong();
        }
        return results;
    }
    public short[] getValuesAsShort() {
        int size = size();
        short[] results = new short[size];
        for (int i = 0; i < size; i++) {
            results[i] = (short) get(i).getLong();
        }
        return results;
    }
    public byte[] getValuesAsByte() {
        int size = size();
        byte[] results = new byte[size];
        for (int i = 0; i < size; i++) {
            results[i] = (byte) get(i).getLong();
        }
        return results;
    }
    public char[] getValuesAsChar() {
        int size = size();
        char[] results = new char[size];
        for (int i = 0; i < size; i++) {
            results[i] = (char) get(i).getLong();
        }
        return results;
    }
    public float[] getValuesAsFloat() {
        int size = size();
        float[] results = new float[size];
        for (int i = 0; i < size; i++) {
            results[i] = Float.intBitsToFloat(get(i).get());
        }
        return results;
    }
    public double[] getValuesAsDouble() {
        int size = size();
        double[] results = new double[size];
        for (int i = 0; i < size; i++) {
            results[i] = Double.longBitsToDouble(get(i).getLong());
        }
        return results;
    }

    public boolean isEmpty() {
        return size() == 0;
    }
    public void clear() {
        clearChildes();
        updateCountReference();
    }

    public void fromSmali(SmaliPayloadArray smaliPayloadArray) {
        setWidth(smaliPayloadArray.getWidth());
        SmaliSet<SmaliValueX> entries = smaliPayloadArray.getEntries();
        int size = entries.size();
        setSize(size);
        for (int i = 0; i < size; i++) {
            this.get(i).fromSmali(entries.get(i));
        }
        updateCountReference();
    }

    public void toSmali(SmaliPayloadArray smaliPayloadArray) {
        int size = this.size();
        smaliPayloadArray.setWidth(getWidth());
        for (int i = 0; i < size; i++) {
            smaliPayloadArray.addEntry(get(i).toSmali());
        }
    }

    public void append(TypeKey arrayType, SmaliWriter writer) throws IOException {
        Iterator<ArrayDataEntry> iterator = iterator();
        while (iterator.hasNext()) {
            ArrayDataEntry entry = iterator.next();
            writer.newLine();
            entry.append(arrayType, writer);
        }
    }
    public void merge(InsArrayDataList dataList) {
        setWidth(dataList.getWidth());
        int size = dataList.size();
        setSize(size);
        for (int i = 0; i < size; i++) {
            this.get(i).merge(dataList.get(i));
        }
        updateCountReference();
    }

    @Override
    public String toString() {
        return "width = " + getWidth() + ", size = " + size()
                + "\n  " + StringsUtil.join(iterator(), "\n  ");
    }

    public static class ArrayDataEntry extends NumberBlock implements SmaliFormat {

        public ArrayDataEntry(int width) {
            super(width);
        }

        public void fromSmali(SmaliValueX smaliValueX) {
            set(smaliValueX.asLongValue());
        }
        public SmaliValueX toSmali() {
            return new SmaliValueX(width(), getLong());
        }
        @Override
        public void append(SmaliWriter writer) throws IOException {
            append(null, writer);
        }
        public void append(TypeKey arrayType, SmaliWriter writer) throws IOException {
            writer.appendHex(width(), getLong());
            appendFloatOrDoubleComment(arrayType, writer);
        }
        private void appendFloatOrDoubleComment(TypeKey arrayType, SmaliWriter writer) {
            if (arrayType == null) {
                return;
            }
            long data = getLong();
            if (data == 0) {
                return;
            }
            TypeKey typeKey = arrayType.setArrayDimension(0);
            if (TypeKey.TYPE_F.equals(typeKey)) {
                float f = Float.intBitsToFloat((int) data);
                writer.appendComment(f + "f");
            } else if (TypeKey.TYPE_D.equals(typeKey)) {
                double d = Double.longBitsToDouble(data);
                writer.appendComment(Double.toString(d));
            }
        }

        public void merge(ArrayDataEntry entry) {
            set(entry.getLong());
        }

        @Override
        public int hashCode() {
            return Block.hashCodeOf(getBytesInternal());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            ArrayDataEntry other = (ArrayDataEntry) obj;
            return Block.areEqual(this.getBytesInternal(), other.getBytesInternal());
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            String hex = toHexString();
            builder.append(hex);
            int width = width();
            if (width == 1) {
                builder.append('t');
            } else if (width == 2) {
                builder.append('S');
            } else if (width == 8 && hex.length() > 10) {
                builder.append('L');
            }
            return builder.toString();
        }
    }

    static class EntryCreator implements Creator<ArrayDataEntry> {

        private final IntegerReference width;

        public EntryCreator(IntegerReference width) {
            this.width = width;
        }

        @Override
        public ArrayDataEntry[] newArrayInstance(int length) {
            return new ArrayDataEntry[length];
        }
        @Override
        public ArrayDataEntry newInstance() {
            return new ArrayDataEntry(this.width.get());
        }
    }
}
