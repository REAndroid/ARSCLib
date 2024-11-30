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

class ArrayKeyHelper {

    public static boolean isAllTypeOf(Class<? extends Key> keyClass, ArrayKey arrayKey) {
        int size = arrayKey.size();
        if (size == 0) { return false; }
        for (int i = 0; i < size; i++) {
            if (!keyClass.isInstance(arrayKey.get(i))) {
                return false;
            }
        }
        return true;
    }
    public static Key[] toStringKeys(String[] values) {
        if (values == null) {
            return null;
        }
        int length = values.length;
        if (length == 0) {
            return null;
        }
        Key[] results = new Key[length];
        for (int i = 0; i < length; i++) {
            results[i] = StringKey.create(values[i]);
        }
        return results;
    }

    public static Key[] toPrimitiveKeys(byte[] values) {
        if (values == null) { return null; }
        int length = values.length;
        if (length == 0) { return null; }
        Key[] results = new Key[length];
        for (int i = 0; i < length; i++) {
            results[i] = PrimitiveKey.of(values[i]);
        }
        return results;
    }

    public static Key[] toPrimitiveKeys(short[] values) {
        if (values == null) { return null; }
        int length = values.length;
        if (length == 0) { return null; }
        Key[] results = new Key[length];
        for (int i = 0; i < length; i++) {
            results[i] = PrimitiveKey.of(values[i]);
        }
        return results;
    }

    public static Key[] toPrimitiveKeys(int[] values) {
        if (values == null) { return null; }
        int length = values.length;
        if (length == 0) { return null; }
        Key[] results = new Key[length];
        for (int i = 0; i < length; i++) {
            results[i] = PrimitiveKey.of(values[i]);
        }
        return results;
    }

    public static Key[] toPrimitiveKeys(long[] values) {
        if (values == null) { return null; }
        int length = values.length;
        if (length == 0) { return null; }
        Key[] results = new Key[length];
        for (int i = 0; i < length; i++) {
            results[i] = PrimitiveKey.of(values[i]);
        }
        return results;
    }

    public static Key[] toPrimitiveKeys(float[] values) {
        if (values == null) { return null; }
        int length = values.length;
        if (length == 0) { return null; }
        Key[] results = new Key[length];
        for (int i = 0; i < length; i++) {
            results[i] = PrimitiveKey.of(values[i]);
        }
        return results;
    }

    public static Key[] toPrimitiveKeys(double[] values) {
        if (values == null) { return null; }
        int length = values.length;
        if (length == 0) { return null; }
        Key[] results = new Key[length];
        for (int i = 0; i < length; i++) {
            results[i] = PrimitiveKey.of(values[i]);
        }
        return results;
    }

    public static Key[] toPrimitiveKeys(char[] values) {
        if (values == null) { return null; }
        int length = values.length;
        if (length == 0) { return null; }
        Key[] results = new Key[length];
        for (int i = 0; i < length; i++) {
            results[i] = PrimitiveKey.of(values[i]);
        }
        return results;
    }

    public static Key[] toPrimitiveKeys(boolean[] values) {
        if (values == null) { return null; }
        int length = values.length;
        if (length == 0) { return null; }
        Key[] results = new Key[length];
        for (int i = 0; i < length; i++) {
            results[i] = PrimitiveKey.of(values[i]);
        }
        return results;
    }

    public static String[] toStringValues(ArrayKey arrayKey) {
        if (!isAllTypeOf(StringKey.class, arrayKey)) { return null; }
        int size = arrayKey.size();
        String[] results = new String[size];
        for (int i = 0; i < size; i++) {
            results[i] = ((StringKey) arrayKey.get(i)).getString();
        }
        return results;
    }

    public static byte[] toByteValues(ArrayKey arrayKey) {
        if (!isAllTypeOf(PrimitiveKey.ByteKey.class, arrayKey)) { return null; }
        int size = arrayKey.size();
        byte[] results = new byte[size];
        for (int i = 0; i < size; i++) {
            results[i] = ((PrimitiveKey.ByteKey) arrayKey.get(i)).value();
        }
        return results;
    }

    public static short[] toShortValues(ArrayKey arrayKey) {
        if (!isAllTypeOf(PrimitiveKey.ShortKey.class, arrayKey)) { return null; }
        int size = arrayKey.size();
        short[] results = new short[size];
        for (int i = 0; i < size; i++) {
            results[i] = ((PrimitiveKey.ShortKey) arrayKey.get(i)).value();
        }
        return results;
    }

    public static int[] toIntValues(ArrayKey arrayKey) {
        if (!isAllTypeOf(PrimitiveKey.IntegerKey.class, arrayKey)) { return null; }
        int size = arrayKey.size();
        int[] results = new int[size];
        for (int i = 0; i < size; i++) {
            results[i] = ((PrimitiveKey.IntegerKey) arrayKey.get(i)).value();
        }
        return results;
    }

    public static long[] toLongValues(ArrayKey arrayKey) {
        if (!isAllTypeOf(PrimitiveKey.LongKey.class, arrayKey)) { return null; }
        int size = arrayKey.size();
        long[] results = new long[size];
        for (int i = 0; i < size; i++) {
            results[i] = ((PrimitiveKey.LongKey) arrayKey.get(i)).value();
        }
        return results;
    }

    public static float[] toFloatValues(ArrayKey arrayKey) {
        if (!isAllTypeOf(PrimitiveKey.FloatKey.class, arrayKey)) { return null; }
        int size = arrayKey.size();
        float[] results = new float[size];
        for (int i = 0; i < size; i++) {
            results[i] = ((PrimitiveKey.FloatKey) arrayKey.get(i)).value();
        }
        return results;
    }

    public static double[] toDoubleValues(ArrayKey arrayKey) {
        if (!isAllTypeOf(PrimitiveKey.DoubleKey.class, arrayKey)) { return null; }
        int size = arrayKey.size();
        double[] results = new double[size];
        for (int i = 0; i < size; i++) {
            results[i] = ((PrimitiveKey.DoubleKey) arrayKey.get(i)).value();
        }
        return results;
    }

    public static char[] toCharValues(ArrayKey arrayKey) {
        if (!isAllTypeOf(PrimitiveKey.CharKey.class, arrayKey)) { return null; }
        int size = arrayKey.size();
        char[] results = new char[size];
        for (int i = 0; i < size; i++) {
            results[i] = ((PrimitiveKey.CharKey) arrayKey.get(i)).value();
        }
        return results;
    }

    public static boolean[] toBooleanValues(ArrayKey arrayKey) {
        if (!isAllTypeOf(PrimitiveKey.BooleanKey.class, arrayKey)) { return null; }
        int size = arrayKey.size();
        boolean[] results = new boolean[size];
        for (int i = 0; i < size; i++) {
            results[i] = ((PrimitiveKey.BooleanKey) arrayKey.get(i)).value();
        }
        return results;
    }

    public static long[] toNumberValues(ArrayKey arrayKey) {
        if (!isAllTypeOf(PrimitiveKey.NumberKey.class, arrayKey)) { return null; }
        int size = arrayKey.size();
        long[] results = new long[size];
        for (int i = 0; i < size; i++) {
            results[i] = ((PrimitiveKey.NumberKey) arrayKey.get(i)).getValueAsLong();
        }
        return results;
    }
}
