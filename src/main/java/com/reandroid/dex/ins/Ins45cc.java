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

import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.id.ProtoId;
import com.reandroid.dex.key.DualKeyReference;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.reference.IdSectionReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliInstruction;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;

public class Ins45cc extends Size8Ins implements RegistersSet, DualKeyReference {

    private final IdSectionReference<ProtoId> reference2;

    public Ins45cc(Opcode<?> opcode) {
        super(opcode);
        final Ins45cc ins45cc = this;
        this.reference2 = new IdSectionReference<ProtoId>(ins45cc, UsageMarker.USAGE_INSTRUCTION) {
            @Override
            public int get() {
                return ins45cc.getData2();
            }
            @Override
            public void set(int value) {
                ins45cc.setData2(value);
            }
            @Override
            public SectionType<ProtoId> getSectionType() {
                return ins45cc.getSectionType2();
            }
            @Override
            protected String buildTrace(ProtoId currentItem) {
                return SizeXIns.buildTrace(ins45cc, currentItem, get());
            }
        };
    }

    @Override
    public Key getKey2() {
        return reference2.getKey();
    }
    @Override
    public void setKey2(Key key) {
        reference2.setItem(key);
    }
    public ProtoId getSectionId2() {
        return reference2.getItem();
    }
    public void setSectionId2(IdItem idItem) {
        reference2.setItem((ProtoId) idItem);
    }

    @Override
    public int getRegistersCount() {
        return getNibble(3);
    }
    @Override
    public void setRegistersCount(int count) {
        setNibble(3, count);
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
        }else {
            setNibble(2, value);
        }
    }
    @Override
    public int getRegisterLimit(int index){
        return 0x0f;
    }

    @Override
    public int getData(){
        return getShortUnsigned(2);
    }
    @Override
    public void setData(int data){
        setShort(2, data);
    }

    public int getData2(){
        return getShortUnsigned(6);
    }
    public void setData2(int data){
        setShort(6, data);
    }
    @Override
    public SectionType<ProtoId> getSectionType2() {
        return ObjectsUtil.cast(super.getSectionType2());
    }

    @Override
    void pullSectionItem() {
        super.pullSectionItem();
        reference2.pullItem();
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        reference2.refresh();
    }

    @Override
    public void replaceKeys(Key search, Key replace) {
        super.replaceKeys(search, replace);
        Key key = getKey2();
        if (key == null) {
            return;
        }
        Key update = key.replaceKey(search, replace);
        if(key != update){
            setKey2(update);
        }
    }
    @Override
    public Iterator<IdItem> usedIds() {
        return CombiningIterator.two(super.usedIds(), SingleIterator.of(getSectionId2()));
    }

    @Override
    void appendOperand(SmaliWriter writer) throws IOException {
        super.appendOperand(writer);
        writer.append(", ");
        reference2.append(writer);
    }

    @Override
    public void fromSmali(SmaliInstruction smaliInstruction) throws IOException {
        super.fromSmali(smaliInstruction);
        setKey2(smaliInstruction.getKey2());
    }
}