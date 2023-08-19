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
package com.reandroid.dex.index;

import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class MethodId extends ItemId {
    public MethodId() {
        super(SIZE);
    }

    public TypeId getClassType(){
        return getTypeId(getClassIndex());
    }
    public StringData getNameString(){
        return getStringData(getNameIndex());
    }
    public ProtoId getProto(){
        return getSectionList().get(SectionType.PROTO_ID, getProtoIndex());
    }

    public int getClassIndex(){
        return getShort(getBytesInternal(), OFFSET_CLASS) & 0xffff;
    }
    public void setClassIndex(int index){
        putShort(getBytesInternal(), OFFSET_CLASS, (short) index);;
    }
    public int getProtoIndex(){
        return getShort(getBytesInternal(), OFFSET_PROTO) & 0xffff;
    }
    public void setProtoIndex(int index){
        putShort(getBytesInternal(), OFFSET_PROTO, (short) index);;
    }
    public int getNameIndex(){
        return getInteger(getBytesInternal(), OFFSET_NAME);
    }
    public void setNameIndex(int index){
        putInteger(getBytesInternal(), OFFSET_NAME, index);
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getClassType().append(writer);
        writer.append("->");
        writer.append(getNameString().getString());
        writer.append('(');
        getProto().append(writer);
        writer.append(')');
        getProto().getReturnTypeId().append(writer);
    }
    @Override
    public String toString() {
        return getClassType() + "->" + getNameString() + getProto();
    }

    private static final int OFFSET_CLASS = 0;
    private static final int OFFSET_PROTO = 2;
    private static final int OFFSET_NAME = 4;
    private static final int SIZE = 8;

}
