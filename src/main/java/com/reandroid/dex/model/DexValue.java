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

import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.value.DexValueType;
import com.reandroid.dex.value.PrimitiveValue;

import java.io.IOException;

public class DexValue extends Dex {

    private final DexDeclaration dexDeclaration;
    private final DexValueBlock<?> dexValueBlock;

    public DexValue(DexDeclaration dexDeclaration, DexValueBlock<?> dexValueBlock) {
        this.dexDeclaration = dexDeclaration;
        this.dexValueBlock = dexValueBlock;
    }

    public String getAsString() {
        return getDexValueBlock().getAsString();
    }
    public int getAsInteger() {
        return getAsInteger(0);
    }
    public int getAsInteger(int def) {
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof PrimitiveValue){
            return (int) ((PrimitiveValue)value).getNumberValue();
        }
        return def;
    }
    public DexValueType<?> getValueType(){
        return getDexValueBlock().getValueType();
    }
    public DexValueBlock<?> getDexValueBlock() {
        return dexValueBlock;
    }

    public DexDeclaration getDexDef() {
        return dexDeclaration;
    }
    @Override
    public DexClassRepository getClassRepository() {
        return getDexDef().getClassRepository();
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        getDexValueBlock().append(writer);
    }
    @Override
    public String toString() {
        return getAsString();
    }

    public static DexValue create(DexDeclaration dexDeclaration, DexValueBlock<?> valueBlock){
        if(valueBlock != null){
            return new DexValue(dexDeclaration, valueBlock);
        }
        return null;
    }
}
