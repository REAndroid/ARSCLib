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

import com.reandroid.dex.id.ProtoId;
import com.reandroid.dex.key.ProtoKey;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class Ins4rcc extends Size8Ins implements RegistersSet {

    private ProtoId mProtoId;

    public Ins4rcc(Opcode<?> opcode) {
        super(opcode);
    }

    public ProtoId getProtoId(){
        return mProtoId;
    }
    public void setProtoId(ProtoKey protoKey){
        Section<ProtoId> section = getSection(SectionType.PROTO_ID);
        setProtoId(section.getOrCreate(protoKey));
    }
    public void setProtoId(ProtoId protoId){
        this.mProtoId = protoId;
        setShort(6, protoId.getIndex());
    }

    @Override
    public int getData(){
        return getShortUnsigned(2);
    }
    @Override
    public void setData(int data){
        setShort(2, data);
    }

    @Override
    public int getRegistersCount() {
        return getByteUnsigned(1);
    }
    @Override
    public void setRegistersCount(int count) {
        setByte(1, count);
    }

    @Override
    public int getRegister(int index) {
        return getShortUnsigned(4) + index;
    }

    @Override
    public void setRegister(int index, int value) {
        if(index != 0) {
            setShort(1, value + 1);
        }else {
            setShort(4, value);
        }
    }
    @Override
    public boolean isRegistersRange(){
        return true;
    }
    @Override
    public int getRegistersLimit(){
        return 0xffff;
    }


    public int getProtoIndex(){
        return getShortUnsigned(6);
    }
    public void setProtoIndex(int data){
        setShort(6, data);
        cacheProto();
    }

    @Override
    void cacheSectionItem() {
        super.cacheSectionItem();
        cacheProto();
    }

    @Override
    void updateSectionItem() {
        super.updateSectionItem();
        updateProtoId();
    }
    private void updateProtoId() {
        ProtoId protoId = this.mProtoId;
        if(protoId != null){
            setProtoIndex(protoId.getIndex());
        }
    }
    private void cacheProto() {
        mProtoId = get(SectionType.PROTO_ID, getProtoIndex());
    }
    @Override
    void appendCode(SmaliWriter writer) throws IOException {
        super.appendCode(writer);
        writer.append(", ");
        getProtoId().append(writer);
    }
}