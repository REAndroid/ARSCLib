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

public class Ins35c extends Size6Ins implements RegisterNumber{
    public Ins35c(Opcode<?> opcode) {
        super(opcode);
    }
    @Override
    public int getData(){
        return getValueBytes().getShortUnsigned(2);
    }
    public void setData(int data){
        getValueBytes().putShort(2, data);
        cacheSectionItem();
    }

    @Override
    public int getRegistersCount() {
        return getNibble(3);
    }

    @Override
    public int getRegister(int index) {
        if(index < 4){
            return getNibble(8 + index);
        }
        return getNibble(2);
    }

    @Override
    public void setRegister(int index, int value) {
        if(index < 4){
            setNibble(8 + index, value);
        }
        setNibble(2, value);
    }
}