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
package com.reandroid.dex.model;

import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.value.DexValueType;

public class DexValue {
    private final DexValueBlock<?> dexValueBlock;

    public DexValue(DexValueBlock<?> dexValueBlock) {
        this.dexValueBlock = dexValueBlock;
    }

    public String getAsString() {
        return getDexValueBlock().getAsString();
    }
    public int getAsInteger() {
        return getAsInteger(0);
    }
    public int getAsInteger(int def) {
        return getDexValueBlock().getAsInteger(def);
    }
    public DexValueType<?> getValueType(){
        return getDexValueBlock().getValueType();
    }
    public DexValueBlock<?> getDexValueBlock() {
        return dexValueBlock;
    }
    @Override
    public String toString() {
        return getAsString();
    }
}
