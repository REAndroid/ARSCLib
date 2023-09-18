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
package com.reandroid.dex.refactor;

import com.reandroid.arsc.group.ItemGroup;
import com.reandroid.dex.item.StringData;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.EmptyList;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

public class RenameInfoString extends RenameInfo<StringData> {

    public RenameInfoString(String search, String replace) {
        super(search, replace);
    }

    @Override
    public String getKey() {
        return getSearch();
    }
    @Override
    SectionType<StringData> getSectionType() {
        return SectionType.STRING_DATA;
    }

    @Override
    void apply(ItemGroup<StringData> group){
        String replace = getReplace();
        for(StringData stringData : group){
            stringData.setString(replace);
        }
    }
    @Override
    List<RenameInfo<?>> createChildRenames() {
        return EmptyList.of();
    }

    @Override
    void append(Writer writer, boolean appendCount) throws IOException {
        int count = getRenameCount();
        if(appendCount && count == 0){
            return;
        }
        appendIndent(writer);
        writer.append('"');
        writer.write(getKey());
        writer.append('"');
        writer.write("=");
        writer.append('"');
        writer.write(getReplace());
        writer.append('"');
        if(appendCount){
            writer.append("  // count=");
            writer.write(Integer.toString(getRenameCount()));
        }
        writer.write("\n");
    }
    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        try {
            write(writer, false);
            writer.close();
        } catch (IOException exception) {
            return exception.toString();
        }
        return writer.toString();
    }
}
