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
package com.reandroid.arsc.item;

import com.reandroid.utils.NumberX;

public class NumberBlock extends BlockItem implements LongReference {

    public NumberBlock(int width) {
        super(width);
    }

    @Override
    public long getLong() {
        int width = width();
        if (width > 8) {
            width = 8;
        }
        return NumberX.valueOfUnsigned(width, unsigned()).longValue();
    }
    @Override
    public void set(long value) {
        byte[] bytes = getBytesInternal();
        int length = bytes.length;
        validate(length, value);
        putNumber(bytes, 0, length, value);
    }
    @Override
    public int get() {
        return (int) getLong();
    }
    @Override
    public void set(int value) {
        set((long) value);
    }

    public int width() {
        return countBytes();
    }
    public void width(int width) {
        if (width == width()) {
            return;
        }
        long value = getLong();
        setBytesLength(width, false);
        set(value);
    }
    private long unsigned() {
        byte[] bytes = getBytesInternal();
        return getUnsignedNumber(bytes, 0, bytes.length);
    }
    private void validate(int width, long value) {
        if (value != 0) {
            if (width == 0) {
                throw new NumberFormatException("Width == 0 for value = " + value);
            }
            if ((value < 0 && value < NumberX.minValueForWidth(width)) ||
                    (value > 0 && value > NumberX.maxValueForWidth(width))) {
                throw new NumberFormatException("Out of range for width = " +
                        width + ", value = " + value);
            }
        }
    }

    public String toHexString() {
        return NumberX.toHexString(width(), getLong());
    }

    @Override
    public String toString() {
        return toHexString();
    }
}
