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
package com.reandroid.utils;

public class NumberX extends Number implements Comparable<Number> {

    private final int width;
    private final long value;

    private NumberX(int width, long value) {
        if (width < 1 || width > 8) {
            throw new NumberFormatException("Width out of range [1 ... 8], " + width);
        }
        this.width = width;
        this.value = value;
    }

    public long unsigned() {
        long l = longValue();
        int width = width();
        if (width == 8) {
            return l;
        }
        long mask = 0xffffffffffffffffL >>> ((8 - width) * 8);
        return l & mask;
    }

    public int width() {
        return width;
    }

    @Override
    public int intValue() {
        return (int) longValue();
    }
    @Override
    public long longValue() {
        return value;
    }
    @Override
    public float floatValue() {
        return Float.intBitsToFloat(intValue());
    }
    @Override
    public double doubleValue() {
        return Double.longBitsToDouble(longValue());
    }


    public String toHex() {
        return toHexString(width(), longValue());
    }
    @Override
    public int compareTo(Number number) {
        return Long.compare(longValue(), number.longValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Number)) {
            return false;
        }
        Number number = (Number) obj;
        return this.longValue() == number.longValue();
    }
    @Override
    public int hashCode() {
        return Long.hashCode(longValue());
    }
    @Override
    public String toString() {
        return Long.toString(longValue());
    }

    public static NumberX valueOf(long value) {
        return new NumberX(widthOfSigned(value), value);
    }

    public static NumberX valueOfUnsigned(int width, long value) {
        value = encodeUnSigned(width, value);
        return new NumberX(width, value);
    }

    public static NumberX valueOf(int width, long value) {
        return new NumberX(width, value);
    }

    public static int toStandardWidth(int width) {
        if (width == 3) {
            width = 4;
        } else if (width == 5 || width == 6 || width == 7) {
            width = 8;
        }
        return width;
    }
    public static int widthOfSigned(long signedValue) {
        int width = 1;
        if (signedValue < 0) {
            while (minValueForWidth(width) > signedValue) {
                width ++;
            }
        } else if (signedValue > 0) {
            while (maxValueForWidth(width) < signedValue) {
                width ++;
            }
        }
        return width;
    }

    public static long encodeUnSigned(int width, long unsignedValue) {
        int shift = (width - 1) * 8;
        long high = (unsignedValue >>> shift) & 0xff;
        if (high < 0x80) {
            return unsignedValue;
        }
        shift = (16 - width) * 8;
        long mask = ~(0xffffffffffffffffL >>> shift);
        unsignedValue = unsignedValue | mask;
        return unsignedValue;
    }

    public static long maxValueForWidth(int width) {
        return ~minValueForWidth(width);
    }
    public static long minValueForWidth(int width) {
        int shift = (width - 1) * 8;
        long min = 0x80L << shift;
        return -min;
    }

    private static int hexWidth(long value) {
        if (value == 0) {
            return 1;
        }
        int i = 0;
        while (value != 0) {
            value = value >>> 4;
            i++;
        }
        return i;
    }
    public static String toHexString(int width, long value) {
        if (width == 0) {
            width = 1;
        }
        boolean neg = false;
        if (value < 0) {
            value = -value;
            neg = true;
        }
        int length = NumbersUtil.min(width * 2, hexWidth(value));
        int w = length + 2;
        if (neg) {
            w ++;
        }
        byte[] results = new byte[w];
        int j = 0;
        if (neg) {
            results[j] = '-';
            j ++;
        }
        results[j] = '0';
        results[j + 1] = 'x';
        j = w - 1;
        for (int i = 0; i < length; i ++) {
            results[j] = toHexChar((int) (value & 0xf));
            value = value >>> 4;
            j --;
        }
        return new String(results);
    }
    private static byte toHexChar(int i){
        if (i >= 0) {
            if (i < 10) {
                i = i + '0';
                return (byte) i;
            }
            if (i <= 16) {
                i = i - 10;
                i = i + 'a';
                return (byte) i;
            }
        }
        return 0;
    }
}
