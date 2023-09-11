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

import com.reandroid.dex.index.StringData;
import com.reandroid.dex.index.TypeId;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class DebugStartLocal extends DebugRegisterNumber {

    private final Base1Ule128Item<StringData> nameIndex;
    private final Base1Ule128Item<TypeId> typeIndex;

    DebugStartLocal(int childesCount, int flag) {
        super(childesCount + 2, flag);

        this.nameIndex = new Base1Ule128Item<>(SectionType.STRING_DATA);
        this.typeIndex = new Base1Ule128Item<>(SectionType.TYPE_ID);

        addChild(2, nameIndex);
        addChild(3, typeIndex);
    }
    DebugStartLocal(int childesCount, DebugElementType<?> elementType) {
        this(childesCount, elementType.getFlag());
    }
    public DebugStartLocal() {
        this(0, DebugElementType.START_LOCAL.getFlag());
    }

    public StringData getName(){
        return nameIndex.getItem();
    }
    public TypeId getTypeId(){
        return typeIndex.getItem();
    }

    public void appendExtra(SmaliWriter writer) throws IOException {
        writer.append(getElementType().getOpcode());
        writer.append(" v");
        writer.append(getRegisterNumber());
        writer.append(", ");
        getName().append(writer);
        writer.append(':');
        getTypeId().append(writer);
    }
    @Override
    public String toString() {
        StringData stringData = getName();
        TypeId typeId = getTypeId();
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append(", ");
        if(stringData == null){
            builder.append("name index = ");
            builder.append(nameIndex.get());
        }else {
            builder.append('"');
            builder.append(stringData.getString());
            builder.append('"');
        }
        builder.append(':');
        if(typeId == null){
            builder.append("type id = ");
            builder.append(typeIndex.get());
        }else {
            builder.append(typeId.getName());
        }
        return builder.toString();
    }
}
