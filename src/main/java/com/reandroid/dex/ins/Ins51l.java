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

import com.reandroid.dex.index.IdSectionEntry;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class Ins51l extends Size10Ins implements RegistersSet {
    public Ins51l(Opcode<?> opcode) {
        super(opcode);
    }


    @Override
    public int getRegistersCount() {
        return 1;
    }
    @Override
    public void setRegistersCount(int count) {
    }
    @Override
    public int getRegister(int index) {
        return getByteUnsigned(1);
    }
    @Override
    public void setRegister(int index, int value) {
        setByte(1, value);
    }
    @Override
    public int getRegistersLimit(){
        return 0xff;
    }

    @Override
    public int getData() {
        return (int) getDataLong();
    }
    public long getDataLong() {
        return getLong();
    }

    @Override
    public void setData(int data) {
        setDataLong((long) data);
    }
    public void setDataLong(long data) {
        setLong(data);
    }


    void appendCodeData(SmaliWriter writer) throws IOException {
        writer.append(", ");
        long data = getDataLong();
        IdSectionEntry sectionItem = getSectionItem();
        if(sectionItem != null){
            sectionItem.append(writer);
        }else {
            writer.append(HexUtil.toHex(data, 1));
        }
    }
}