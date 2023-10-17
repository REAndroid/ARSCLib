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

import com.reandroid.dex.id.StringId;
import com.reandroid.dex.id.TypeId;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.reference.Base1Ule128IdItemReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class DebugStartLocal extends DebugRegisterNumber {

    private final Base1Ule128IdItemReference<StringId> mName;
    private final Base1Ule128IdItemReference<TypeId> mType;

    DebugStartLocal(int childesCount, int flag) {
        super(childesCount + 2, flag);

        this.mName = new Base1Ule128IdItemReference<>(SectionType.STRING_ID);
        this.mType = new Base1Ule128IdItemReference<>(SectionType.TYPE_ID);

        addChild(2, mName);
        addChild(3, mType);
    }
    DebugStartLocal(int childesCount, DebugElementType<?> elementType) {
        this(childesCount, elementType.getFlag());
    }
    public DebugStartLocal() {
        this(0, DebugElementType.START_LOCAL.getFlag());
    }

    public String getName(){
        StringId stringId = mName.getItem();
        if(stringId != null){
            return stringId.getString();
        }
        return null;
    }
    public StringKey getNameKey(){
        return (StringKey) this.mName.getKey();
    }
    public void setName(String name){
        this.mName.setItem(StringKey.create(name));
    }
    public void setName(StringKey key){
        this.mName.setItem(key);
    }
    public String getType(){
        TypeId typeId = mType.getItem();
        if(typeId != null){
            return typeId.getName();
        }
        return null;
    }
    public TypeId getTypeId(){
        return mType.getItem();
    }
    public TypeKey getTypeKey(){
        return (TypeKey) mType.getKey();
    }
    public void setType(String type){
        this.mType.setItem(TypeKey.create(type));
    }
    public void setType(TypeKey typeKey){
        this.mType.setItem(typeKey);
    }

    @Override
    public void appendExtra(SmaliWriter writer) throws IOException {
        writer.append(getElementType().getOpcode());
        writer.append(" v");
        writer.append(getRegisterNumber());
        writer.append(", ");
        this.mName.append(writer);
        writer.append(':');
        this.mType.append(writer);
    }

    @Override
    public DebugElementType<? extends DebugStartLocal> getElementType() {
        return DebugElementType.START_LOCAL;
    }

    @Override
    public String toString() {
        return super.toString() + ", " + mName + ':' + mType;
    }
}
