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

import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.index.StringData;
import com.reandroid.dex.sections.SectionList;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class DebugParameter extends Ule128Item implements SmaliFormat {
    public DebugParameter(){
        super();
    }
    public StringData getParameterName(){
        SectionList sectionList = getParent(SectionList.class);
        if(sectionList != null){
            return sectionList.get(SectionType.STRING_DATA, get() - 1);
        }
        return null;
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        StringData stringData = getParameterName();
        if(stringData == null){
            return;
        }
        writer.newLine();
        writer.append(".param p");
        writer.append(getIndex());
        writer.append(", ");
        stringData.append(writer);
    }

    @Override
    public String toString() {
        return  ".param p" + getIndex() + ", \"" + getParameterName() + "\"";
    }
}
