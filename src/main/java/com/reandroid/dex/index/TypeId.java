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

import com.reandroid.dex.base.DexBlockItem;
import com.reandroid.dex.sections.SectionList;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class TypeId extends DexBlockItem implements SmaliFormat {
    public TypeId() {
        super(4);
    }

    public String getString(){
        StringData stringData = getStringData();
        if(stringData != null){
            return stringData.getString();
        }
        return null;
    }
    public StringData getStringData(){
        SectionList sectionList = getParentInstance(SectionList.class);
        if(sectionList != null){

            return sectionList.get(SectionType.STRING_DATA, getStringIndexValue());
        }
        return null;
    }
    public int getStringIndexValue(){
        return getInteger(getBytesInternal(), 0);
    }
    public void setStringIndex(int index){
        putInteger(getBytesInternal(), 0, index);
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getString());
    }
    @Override
    public String toString(){
        StringData stringData = getStringData();
        if(stringData != null){
            return stringData.getString();
        }
        return getIndex() + ":string-index=" + getStringIndexValue();
    }
}
