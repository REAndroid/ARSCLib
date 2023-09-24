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

public class HandlerOffset {

    private final HandlerOffsetArray offsetArray;
    private final int byteIndex;

    HandlerOffset(HandlerOffsetArray offsetArray, int index) {
        this.offsetArray = offsetArray;
        this.byteIndex = index * 8;
    }

    public int getStartAddress() {
        return Block.getInteger(offsetArray.getBytesInternal(), this.byteIndex);
    }

    public void setStartAddress(int value) {
        Block.putInteger(offsetArray.getBytesInternal(), this.byteIndex, value);
    }

    public int getCatchCodeUnit() {
        return Block.getShortUnsigned(offsetArray.getBytesInternal(), byteIndex + 4);
    }

    public void setCatchCodeUnit(int value) {
        Block.putShort(offsetArray.getBytesInternal(), byteIndex + 4, value);
    }

    public int getOffset() {
        return Block.getShortUnsigned(offsetArray.getBytesInternal(), byteIndex + 6);
    }

    public void setOffset(int value) {
        Block.putShort(offsetArray.getBytesInternal(), byteIndex + 6, value);
    }

    @Override
    public String toString() {
        int i = byteIndex / 8;
        return "(" + i + ":start=" + getStartAddress()
                + ", catch=" + getCatchCodeUnit()
                + ", offset=" + getOffset() + ")";
    }
}
