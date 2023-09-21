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

public class NullValue extends DexValueBlock<Block> {
    public NullValue() {
        super();
    }
    @Override
    public DexValueType<NullValue> getValueType() {
        return DexValueType.NULL;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append("null");
    }
    @Override
    public String getAsString() {
        return "null";
    }
    @Override
    public String toString() {
        return "NullValue";
    }
    public static NullValue getInstance(){
        synchronized (NullValue.class){
            if(sInstance == null){
                sInstance = new NullValue();
                sInstance.getValueTypeItem().set((byte) DexValueType.NULL.getFlag());
            }
            return sInstance;
        }
    }
    private static NullValue sInstance;
}
