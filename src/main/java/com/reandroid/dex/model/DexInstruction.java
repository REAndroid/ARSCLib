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

import com.reandroid.dex.ins.Ins;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class DexInstruction extends DexModel{
    private final DexMethod dexMethod;
    private final Ins ins;

    public DexInstruction(DexMethod dexMethod, Ins ins) {
        this.dexMethod = dexMethod;
        this.ins = ins;
    }

    public Ins getIns() {
        return ins;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getIns().append(writer);
    }
}
