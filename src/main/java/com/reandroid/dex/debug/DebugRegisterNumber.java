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
package com.reandroid.dex.debug;

import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

class DebugRegisterNumber extends DebugElement {

    private final Ule128Item registerNumber;

    DebugRegisterNumber(int childesCount, int flag) {
        super(childesCount + 1, flag);
        this.registerNumber = new Ule128Item();
        addChild(1, registerNumber);
    }
    DebugRegisterNumber(int childesCount, DebugElementType<?> elementType) {
        this(childesCount, elementType.getFlag());
    }

    public int getRegisterNumber() {
        return registerNumber.get();
    }
    public void setRegisterNumber(int registerNumber){
        this.registerNumber.set(registerNumber);
    }

    public void appendExtra(SmaliWriter writer) throws IOException {
        writer.append(getElementType().getOpcode());
        writer.append(" v");
        writer.append(getRegisterNumber());
    }
    @Override
    public String toString() {
        return getElementType() + " v" + getRegisterNumber();
    }
}
