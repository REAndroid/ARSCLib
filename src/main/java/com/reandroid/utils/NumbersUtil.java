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

public class NumbersUtil {

    public static int getUInt(int value, int start, int bitCount) {
        return (value >> (start - bitCount + 1)) & ((1 << bitCount) - 1);
    }
    public static int setUInt(int out, int value, int start, int bitCount) {
        int mask = (1 << bitCount) - 1;
        value = value & mask;
        int shift = start - bitCount + 1;
        return (out & ~(mask << shift)) | (value << shift);
    }
    public static int toSignedInt(int bitsCount, int unsigned) {
        if (unsigned == 0 || bitsCount <= 0) {
            return 0;
        }
        if (bitsCount > 31) {
            return unsigned;
        }
        int mask = 1 << bitsCount;
        unsigned &= mask - 1;
        int max = 1 << (bitsCount - 1);
        if (unsigned < max) {
            return unsigned;
        }
        return unsigned - mask;
    }
    public static long toSigned(int bitsCount, long unsigned) {
        if (unsigned == 0 || bitsCount <= 0) {
            return 0;
        }
        if (bitsCount > 63) {
            return unsigned;
        }
        long max = 1L << (bitsCount - 1);
        long mask = max << 1;
        unsigned &= mask - 1;
        if (unsigned < max) {
            return unsigned;
        }
        return unsigned - mask;
    }
    public static int minimumBitsForSigned(long num) {
        int bits = 0;
        if (num < 0) {
            num = -num;
        } else {
            bits = 1;
        }
        bits = bits + countBits(num);
        long sign = 1L << (bits - 1);
        if (num > sign && bits < 64) {
            bits = bits + 1;
        }
        return bits;
    }
    public static int minimumBitsForUnSigned(long unsigned) {
        if (unsigned == 0) {
            return 1;
        }
        return countBits(unsigned);
    }
    public static int countBits(long num) {
        int bits = 0;
        while (num != 0) {
            num = num >>> 1;
            bits ++;
        }
        return bits;
    }
    public static int minimumBytesForSigned(long num) {
        int bits = minimumBitsForSigned(num);
        int i = bits / 8;
        if ((bits & 0x7) != 0) {
            i ++;
        }
        return i;
    }
    public static long maxValue(int bitsCount) {
        if (bitsCount <= 0) {
            return 0;
        }
        return (1L << (bitsCount - 1)) - 1;
    }
    public static long minValue(int bitsCount) {
        if (bitsCount <= 0) {
            return 0;
        }
        long min = 1L << (bitsCount - 1);
        return -min;
    }
    public static int abs(int i){
        if(i < 0){
            i = -i;
        }
        return i;
    }
    public static int max(int i1, int i2){
        if(i1 > i2){
            return i1;
        }
        return i2;
    }
    public static int min(int i1, int i2){
        if(i1 < i2){
            return i1;
        }
        return i2;
    }
    public static String toBinaryString(int i) {
        StringBuilder builder = new StringBuilder(34);
        builder.append("0b");
        String b = Integer.toBinaryString(i);
        int rem = 32 - b.length();
        while (rem > 0) {
            builder.append('0');
            rem --;
        }
        builder.append(b);
        return builder.toString();
    }
}
