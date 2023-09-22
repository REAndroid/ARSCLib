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

import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class Reg implements SmaliFormat {

    private final RegisterFactory factory;
    private final RegisterNumber registerNumber;
    private final int index;

    public Reg(RegisterFactory factory, RegisterNumber registerNumber, int index){
        this.factory = factory;
        this.registerNumber = registerNumber;
        this.index = index;
    }

    public int getValue() {
        int val = registerNumber.getRegister(getIndex());
        return getFactory().getValue(val);
    }
    public int getIndex() {
        return index;
    }
    public boolean isParameter() {
        return getFactory().isParameter(registerNumber.getRegister(getIndex()));
    }

    public RegisterFactory getFactory() {
        return factory;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        if(isParameter()){
            writer.append('p');
        }else {
            writer.append('v');
        }
        writer.append(getValue());
    }

    @Override
    public String toString() {
        if(isParameter()){
            return "p" + getValue();
        }
        return "v" + getValue();
    }
}
