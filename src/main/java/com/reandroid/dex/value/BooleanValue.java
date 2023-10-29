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
package com.reandroid.dex.value;

import com.reandroid.arsc.base.Block;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class BooleanValue extends DexValueBlock<Block> {
    public BooleanValue(){
        super(DexValueType.BOOLEAN);
    }
    public boolean get(){
        return getValueSize() == 1;
    }
    public void set(boolean value){
        setValueSize(value ? 1 : 0);
    }
    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.BOOLEAN;
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(Boolean.toString(get()));
    }
    @Override
    public String getAsString() {
        return Boolean.toString(get());
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + getValueType().getType();
        hash = hash * 31 + getValueSize();
        return hash;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BooleanValue value = (BooleanValue) obj;
        return get() == value.get();
    }
    @Override
    public String toString(){
        return getAsString();
    }
}
