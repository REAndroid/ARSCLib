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

import com.reandroid.dex.item.AnnotationSet;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FieldId extends ItemId implements SmaliFormat {
    private final List<AnnotationSet> annotations;
    public FieldId() {
        super(8);
        annotations = new ArrayList<>();
    }

    public String getKey(){
        StringBuilder builder = new StringBuilder();
        TypeId type = getClassType();
        if(type == null){
            return null;
        }
        StringData stringData = type.getStringData();
        if(stringData == null){
            return null;
        }
        builder.append(stringData.getString());
        builder.append("->");
        stringData = getNameString();
        if(stringData == null){
            return null;
        }
        builder.append(stringData.getString());
        builder.append(':');
        type = getFieldType();
        if(type == null){
            return null;
        }
        stringData = type.getStringData();
        if(stringData == null){
            return null;
        }
        builder.append(stringData.getString());
        return builder.toString();
    }
    public TypeId getClassType(){
        return getTypeId(getClassIndex());
    }
    public StringData getNameString(){
        return getStringData(getNameIndex());
    }
    public TypeId getFieldType(){
        return getTypeId(getTypeIndex());
    }

    public int getClassIndex(){
        return getShort(getBytesInternal(), 0) & 0xffff;
    }
    public void setClassIndex(int index){
        putShort(getBytesInternal(), 0, (short) index);
    }
    public int getTypeIndex(){
        return getShort(getBytesInternal(), 2) & 0xffff;
    }
    public void setTypeIndex(int index){
        putShort(getBytesInternal(), 2, (short) index);
    }
    public int getNameIndex(){
        return getInteger(getBytesInternal(), 4);
    }
    public void setNameIndex(int index){
        putInteger(getBytesInternal(), 4, index);
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getClassType().append(writer);
        writer.append("->");
        writer.append(getNameString().getString());
        writer.append(':');
        getFieldType().append(writer);
    }
    @Override
    public String toString(){
        String key = getKey();
        if(key != null){
            return key;
        }
        return getClassIndex() + "->" + getNameString() + ":" + getFieldType();
    }
}
