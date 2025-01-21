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
        value = value >> (start + 1 - bitCount);
        int mask = ~(0xffffffff << bitCount);
        return value & mask;
    }
    public static int setUInt(int out, int value, int start, int bitCount) {
        int shift = (start - bitCount) + 1;
        int mask = 0xffffffff >>> (32 - bitCount);
        value = value & mask;
        mask = ~(mask << shift);
        out = out & mask;
        value = value << shift;
        return out | value;
    }
    public static int toSignedInt(int bitsCount, int unsigned) {
        if (unsigned <= 0) {
            return unsigned;
        }
        int mask = 0xffffffff >>> (32 - bitsCount);
        unsigned = unsigned & mask;
        int maxValue = 1 << (bitsCount - 1);
        if (unsigned < maxValue) {
            return unsigned;
        }
        return ~mask | unsigned;
    }
    public static long toSigned(int bitsCount, long unsigned) {
        if (unsigned <= 0) {
            return unsigned;
        }
        long mask = 0xffffffffffffffffL >>> (64 - bitsCount);
        unsigned = unsigned & mask;
        long maxValue = 1L << (bitsCount - 1);
        if (unsigned < maxValue) {
            return unsigned;
        }
        return ~mask | unsigned;
    }
    public static long maxValue(int bitsCount) {
        if (bitsCount <= 0) {
            return 0;
        }
        return 0xffffffffffffffffL >>> (65 - bitsCount);
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
}
