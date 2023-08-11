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

import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.item.EncodedArray;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class ArrayValue extends DexValue<EncodedArray>{
    public ArrayValue() {
        super(new EncodedArray());
    }
    public int getCountValue() {
        return getValue().getCountValue();
    }
    public void addValue(DexValue<?> value){
        getElements().add(value);
    }
    public BlockList<DexValue<?>> getElements() {
        return getValue().getElements();
    }

    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append('{');
        writer.indentPlus();
        BlockList<DexValue<?>> elements = getElements();
        int count = elements.size();
        for(int i = 0; i < count; i++){
            if(i != 0){
                writer.append(',');
            }
            writer.newLine();
            elements.get(i).append(writer);
        }
        writer.indentMinus();
        if(count > 0){
            writer.newLine();
        }
        writer.append('}');
    }
    @Override
    public String toString(){
        return getValue().toString();
    }
}
