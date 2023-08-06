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

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.DexFile;
import com.reandroid.dex.index.StringIndex;
import com.reandroid.dex.index.TypeIndex;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class PrimitiveValue extends DexValue<NumberValue>{
    public PrimitiveValue() {
        super(new NumberValue());
    }
    public long getNumberValue(){
        return getValue().getNumberValue();
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        NumberValue numberValue = getValue();
        numberValue.setSize(0);
        super.onReadBytes(reader);
        numberValue.setSize(getValueSize() + 1);
        numberValue.readBytes(reader);

        numberValue.getSize();
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        DexValueType valueType = getValueType();
        if(valueType == DexValueType.TYPE){
            DexFile dexFile = getParentInstance(DexFile.class);
            TypeIndex type = dexFile.getTypeSection().get((int) getNumberValue());
            type.append(writer);
        }else if(valueType == DexValueType.STRING){
            DexFile dexFile = getParentInstance(DexFile.class);
            StringIndex stringIndex = dexFile.getStringPool().get((int) getNumberValue());
            stringIndex.append(writer);
        }else {
            writer.append(HexUtil.toHex(getNumberValue(), getValueSize()));
        }
    }
    @Override
    public String toString() {
        DexValueType valueType = getValueType();
        if(valueType == DexValueType.TYPE){
            DexFile dexFile = getParentInstance(DexFile.class);
            TypeIndex type = dexFile.getTypeSection().get((int) getNumberValue());
            return type.toString();
        }
        if(valueType == DexValueType.STRING){
            DexFile dexFile = getParentInstance(DexFile.class);
            StringIndex type = dexFile.getStringPool().get((int) getNumberValue());
            return "\"" + type.getString() + "\"";
        }
        if(valueType == DexValueType.INT){
            return HexUtil.toHex(getNumberValue(), getValueSize());
        }
        return getValueType() + ":" + getNumberValue();
    }
}
