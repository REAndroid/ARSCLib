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

import com.reandroid.arsc.chunk.SpecBlock;
import com.reandroid.utils.HexUtil;

public class SpecFlag implements IntegerReference {

    private final SpecFlagsArray array;
    private final int entryId;

    public SpecFlag(SpecFlagsArray array, int entryId) {
        this.array = array;
        this.entryId = entryId;
    }

    public void add(SpecBlock.Flag flag) {
        set(get() | flag.getFlag());
    }
    public void remove(SpecBlock.Flag flag) {
        set(get() & ~flag.getFlag());
    }

    public boolean hasFlags() {
        return get() != 0;
    }

    public int getEntryId() {
        return entryId;
    }
    @Override
    public int get() {
        if (isRemoved()) {
            return 0;
        }
        return array.get(entryId);
    }
    @Override
    public void set(int value) {
        if (!isRemoved()) {
            array.set(entryId, value);
        }
    }
    public boolean isRemoved() {
        return entryId >= array.size();
    }

    @Override
    public int hashCode() {
        return entryId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SpecFlag)) {
            return false;
        }
        SpecFlag specFlag = (SpecFlag) obj;
        return entryId == specFlag.entryId
                && array == specFlag.array;
    }

    public String toHexString() {
        return HexUtil.toHex8(get());
    }
    @Override
    public String toString() {
        if (isRemoved()) {
            return HexUtil.toHex("REMOVED-SPEC ", getEntryId(), 4);
        }
        int value = get();
        if (value == 0) {
            return "";
        }
        int flag = SpecBlock.Flag.SPEC_PUBLIC.getFlag()
                | SpecBlock.Flag.SPEC_STAGED_API.getFlag();
        if ((flag & value) == value) {
            return SpecBlock.Flag.toString(value);
        }
        return HexUtil.toHex8(value);
    }
}
