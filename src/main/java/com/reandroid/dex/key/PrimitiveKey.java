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
package com.reandroid.dex.key;

import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.NumberX;
import com.reandroid.utils.StringsUtil;

import java.io.IOException;

public abstract class PrimitiveKey implements Key {

    public PrimitiveKey() {
    }

    @Override
    public abstract Object asObject();
    public abstract TypeKey valueType();
    public abstract int width();
    public abstract long getValueAsLong();

    public boolean isNumber() { return false; }
    public boolean isBoolean() { return false; }
    public boolean isByte() { return false; }
    public boolean isChar() { return false; }
    public boolean isDouble() { return false; }
    public boolean isFloat() { return false; }
    public boolean isInteger() { return false; }
    public boolean isLong() { return false; }
    public boolean isShort() { return false; }
    public boolean isX() { return false; }

    @Override
    public int compareTo(Object obj) {
        if (obj == this) {
            return 0;
        }
        if (!(obj instanceof PrimitiveKey)) {
            return StringsUtil.compareToString(this, obj);
        }
        PrimitiveKey other = (PrimitiveKey) obj;
        return Long.compare(this.getValueAsLong(), other.getValueAsLong());
    }
    @Override
    public abstract void append(SmaliWriter writer) throws IOException;

    public static PrimitiveKey of(boolean value) {
        PrimitiveKey key;
        if (value) {
            key = TRUE_KEY;
            if (key == null) {
                key = new BooleanKey(true);
                TRUE_KEY = key;
            }
        } else {
            key = FALSE_KEY;
            if (key == null) {
                key = new BooleanKey(false);
                FALSE_KEY = key;
            }
        }
        return key;
    }
    public static PrimitiveKey of(byte value) { return new ByteKey(value); }
    public static PrimitiveKey of(char value) { return new CharKey(value); }
    public static PrimitiveKey of(double value) { return new DoubleKey(value); }
    public static PrimitiveKey of(float value) { return new FloatKey(value); }
    public static PrimitiveKey of(int value) { return new IntegerKey(value); }
    public static PrimitiveKey of(long value) { return new LongKey(value); }
    public static PrimitiveKey of(short value) { return new ShortKey(value); }
    public static PrimitiveKey of(int width, long value) { return new NumberXKey(width, value); }

    public static PrimitiveKey parse(String text) {
        return PrimitiveKeyHelper.parse(text);
    }
    public static PrimitiveKey readSafe(SmaliReader reader) {
        return PrimitiveKeyHelper.readSafe(reader);
    }
    public static abstract class NumberKey extends PrimitiveKey {

        public NumberKey() {
            super();
        }

        @Override
        public abstract Number asObject();

        @Override
        public boolean isNumber() {
            return true;
        }

        @Override
        public String toString() {
            return asObject().toString();
        }
    }

    public static class BooleanKey extends PrimitiveKey {

        private final boolean value;

        public BooleanKey(boolean value) {
            super();
            this.value = value;
        }

        public boolean value() {
            return value;
        }
        @Override
        public boolean isBoolean() {
            return true;
        }
        @Override
        public Boolean asObject() {
            return value();
        }

        @Override
        public int width() {
            return 0;
        }
        @Override
        public long getValueAsLong() {
            return value() ? 1 : 0;
        }

        @Override
        public TypeKey valueType() {
            return TypeKey.TYPE_Z;
        }

        @Override
        public int hashCode() {
            return value() ? 1 : 0;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            return value() == ((BooleanKey) obj).value();
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.append(value() ? "true" : "false");
        }

        @Override
        public String toString() {
            return value() ? "true" : "false";
        }
    }

    public static class ByteKey extends NumberKey {

        private final byte value;

        public ByteKey(byte value) {
            super();
            this.value = value;
        }

        public byte value() {
            return value;
        }
        @Override
        public Byte asObject() {
            return value;
        }

        @Override
        public int width() {
            return 1;
        }
        @Override
        public long getValueAsLong() {
            return value();
        }

        @Override
        public TypeKey valueType() {
            return TypeKey.TYPE_B;
        }
        @Override
        public boolean isByte() {
            return true;
        }

        @Override
        public int hashCode() {
            return value();
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            return value() == ((ByteKey) obj).value();
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.appendHex(value());
        }

        @Override
        public String toString() {
            return HexUtil.toSignedHex(value()) + "t";
        }
    }

    public static class CharKey extends PrimitiveKey {

        private final char value;

        public CharKey(char value) {
            super();
            this.value = value;
        }

        public char value() {
            return value;
        }
        @Override
        public Character asObject() {
            return value;
        }

        @Override
        public int width() {
            return 0;
        }
        @Override
        public long getValueAsLong() {
            return value();
        }

        @Override
        public TypeKey valueType() {
            return TypeKey.TYPE_C;
        }
        @Override
        public boolean isChar() {
            return true;
        }

        @Override
        public int hashCode() {
            return value();
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            return value() == ((CharKey) obj).value();
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.append(DexUtils.quoteChar(value()));
        }

        @Override
        public String toString() {
            return DexUtils.quoteChar(value());
        }
    }

    public static class DoubleKey extends NumberKey {

        private final double value;

        public DoubleKey(double value) {
            super();
            this.value = value;
        }

        public double value() {
            return value;
        }
        @Override
        public Double asObject() {
            return value;
        }

        @Override
        public int width() {
            return 8;
        }
        @Override
        public long getValueAsLong() {
            return Double.doubleToLongBits(value());
        }
        @Override
        public TypeKey valueType() {
            return TypeKey.TYPE_D;
        }
        @Override
        public boolean isDouble() {
            return true;
        }

        @Override
        public int compareTo(Object obj) {
            if (obj == this) {
                return 0;
            }
            if (!(obj instanceof DoubleKey)) {
                return StringsUtil.compareToString(this, obj);
            }
            return Double.compare(value(), ((DoubleKey) obj).value());
        }
        @Override
        public int hashCode() {
            return Double.hashCode(value());
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            return Double.doubleToLongBits(value()) == Double.doubleToLongBits(((DoubleKey) obj).value());
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.append(value());
        }

        @Override
        public String toString() {
            return Double.toString(value());
        }
    }

    public static class FloatKey extends NumberKey {

        private final float value;

        public FloatKey(float value) {
            super();
            this.value = value;
        }

        public float value() {
            return value;
        }
        @Override
        public Float asObject() {
            return value;
        }

        @Override
        public int width() {
            return 4;
        }
        @Override
        public long getValueAsLong() {
            return Float.floatToIntBits(value());
        }

        @Override
        public TypeKey valueType() {
            return TypeKey.TYPE_F;
        }
        @Override
        public boolean isFloat() {
            return true;
        }

        @Override
        public int compareTo(Object obj) {
            if (obj == this) {
                return 0;
            }
            if (!(obj instanceof FloatKey)) {
                return StringsUtil.compareToString(this, obj);
            }
            return Float.compare(value(), ((FloatKey) obj).value());
        }
        @Override
        public int hashCode() {
            return Float.hashCode(value());
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            return Float.floatToIntBits(this.value()) ==
                    Float.floatToIntBits(((FloatKey)obj).value());
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.append(value());
        }

        @Override
        public String toString() {
            return value() + "f";
        }
    }

    public static class IntegerKey extends NumberKey {

        private final int value;

        public IntegerKey(int value) {
            super();
            this.value = value;
        }

        public int value() {
            return value;
        }
        @Override
        public Integer asObject() {
            return value();
        }

        @Override
        public int width() {
            return 4;
        }
        @Override
        public long getValueAsLong() {
            return value();
        }

        @Override
        public TypeKey valueType() {
            return TypeKey.TYPE_I;
        }
        @Override
        public boolean isInteger() {
            return true;
        }

        @Override
        public int hashCode() {
            return value();
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            return value() == ((IntegerKey) obj).value();
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.appendHex(value());
        }

        @Override
        public String toString() {
            return HexUtil.toSignedHex(asObject());
        }
    }

    public static class LongKey extends NumberKey {

        private final long value;

        public LongKey(long value) {
            super();
            this.value = value;
        }

        public long value() {
            return value;
        }
        @Override
        public Long asObject() {
            return value;
        }

        @Override
        public int width() {
            return 8;
        }
        @Override
        public long getValueAsLong() {
            return value();
        }

        @Override
        public TypeKey valueType() {
            return TypeKey.TYPE_J;
        }
        @Override
        public boolean isLong() {
            return true;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(value());
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            return value() == ((LongKey) obj).value();
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.appendHex(value());
        }
        @Override
        public String toString() {
            return HexUtil.toSignedHex(asObject());
        }
    }

    public static class ShortKey extends NumberKey {

        private final short value;

        public ShortKey(short value) {
            super();
            this.value = value;
        }

        public short value() {
            return value;
        }
        @Override
        public Short asObject() {
            return value;
        }

        @Override
        public int width() {
            return 2;
        }
        @Override
        public long getValueAsLong() {
            return value();
        }

        @Override
        public TypeKey valueType() {
            return TypeKey.TYPE_S;
        }
        @Override
        public boolean isShort() {
            return true;
        }

        @Override
        public int hashCode() {
            return value();
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            return value() == ((ShortKey) obj).value();
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.appendHex(value());
        }
        @Override
        public String toString() {
            return HexUtil.toSignedHex(value()) + "S";
        }
    }

    public static class NumberXKey extends NumberKey {

        private final int width;
        private final long value;

        public NumberXKey(int width, long value) {
            super();
            this.width = width;
            this.value = value;
        }

        public long value() {
            return value;
        }
        @Override
        public Number asObject() {
            int width = width();
            long value = value();
            if (width == 1) {
                return (byte) value;
            }
            if (width == 2) {
                return (short) value;
            }
            if (width == 4) {
                return (int) value;
            }
            if (width == 8) {
                return value;
            }
            return NumberX.valueOf(width(), value());
        }
        @Override
        public int width() {
            return width;
        }
        @Override
        public long getValueAsLong() {
            return value();
        }
        @Override
        public boolean isX() {
            return true;
        }
        @Override
        public TypeKey valueType() {
            return null;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(value());
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            return value() == ((NumberXKey) obj).value();
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.appendHex(value());
        }
        @Override
        public String toString() {
            return NumberX.toHexString(width(), value());
        }
    }

    private static PrimitiveKey TRUE_KEY;
    private static PrimitiveKey FALSE_KEY;
}
