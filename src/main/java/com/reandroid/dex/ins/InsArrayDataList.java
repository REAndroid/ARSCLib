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
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.DexBlockAlign;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliPayloadArray;
import com.reandroid.dex.smali.model.SmaliSet;
import com.reandroid.dex.smali.model.SmaliValueX;
import com.reandroid.utils.StringsUtil;

import java.io.IOException;
import java.util.Iterator;

class InsArrayDataList extends CountedBlockList<ArrayDataEntry> {

    private final IntegerReference widthReference;
    private final DexBlockAlign alignment;

    public InsArrayDataList(IntegerReference widthReference, IntegerReference countReference) {
        super(ArrayDataEntry.newCreator(widthReference),
                widthCheckingReference(widthReference, countReference));
        this.widthReference = widthReference;
        this.alignment = new DexBlockAlign(this);
        this.alignment.setAlignment(2);
    }
    public DexBlockAlign getAlignment() {
        return alignment;
    }


    @Override
    public void setSize(int size) {
        super.setSize(size);
        IntegerReference countReference = getCountReference();
        if (size != countReference.get()) {
            countReference.set(size);
        }
        getAlignment().align();
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
            getAlignment().align();
        }
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
        setSize(0);
    }

    public void fromSmali(SmaliPayloadArray smaliPayloadArray) {
        setWidth(smaliPayloadArray.getWidth());
        SmaliSet<SmaliValueX> entries = smaliPayloadArray.getEntries();
        int size = entries.size();
        setSize(size);
        for (int i = 0; i < size; i++) {
            this.get(i).fromSmali(entries.get(i));
        }
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
    }

    @Override
    public String toString() {
        return "width = " + getWidth() + ", size = " + size()
                + "\n  " + StringsUtil.join(iterator(), "\n  ");
    }

    private static IntegerReference widthCheckingReference(IntegerReference width, IntegerReference reference) {
        return new IntegerReference() {
            @Override
            public int get() {
                if (width.get() == 0) {
                    return 0;
                }
                return reference.get();
            }
            @Override
            public void set(int value) {
                reference.set(value);
            }
            @Override
            public String toString() {
                return Integer.toString(get());
            }
        };
    }
}
