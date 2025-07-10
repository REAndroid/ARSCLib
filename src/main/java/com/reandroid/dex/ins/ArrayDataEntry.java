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
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.NumberBlock;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliValueX;

import java.io.IOException;

public class ArrayDataEntry extends NumberBlock implements PayloadEntry {

    public ArrayDataEntry(int width) {
        super(width);
    }

    public void fromSmali(SmaliValueX smaliValueX) {
        set(smaliValueX.getValueAsLong());
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
        if (arrayType == null || !writer.isEnableComments()) {
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
        } else if (TypeKey.TYPE_C.equals(typeKey)) {
            char c = (char) data;
            writer.appendComment("'" + c + "'");
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
    public InsArrayData getPayload() {
        return getParentInstance(InsArrayData.class);
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
        } else if (width == 8 && (getLong() & 0xffffffff80000000L) != 0) {
            builder.append('L');
        }
        return builder.toString();
    }

    public static Creator<ArrayDataEntry> newCreator(IntegerReference width) {
        return () -> new ArrayDataEntry(width.get());
    }
}
