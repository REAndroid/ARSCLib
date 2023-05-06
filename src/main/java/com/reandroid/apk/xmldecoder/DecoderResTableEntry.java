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
package com.reandroid.apk.xmldecoder;

import com.reandroid.apk.XmlHelper;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResTableEntry;
import com.reandroid.common.EntryStore;

import java.io.IOException;

public class DecoderResTableEntry<OUTPUT> extends DecoderTableEntry<ResTableEntry, OUTPUT> {
    public DecoderResTableEntry(EntryStore entryStore){
        super(entryStore);
    }
    @Override
    public OUTPUT decode(ResTableEntry tableEntry, EntryWriter<OUTPUT> writer) throws IOException{
        Entry entry = tableEntry.getParentEntry();
        String tag = XmlHelper.toXMLTagName(entry.getTypeName());
        writer.enableIndent(true);
        writer.startTag(tag);
        writer.attribute("name", entry.getName());
        if(!isId(tag)){
            writeText(writer, entry.getPackageBlock(), tableEntry.getValue());
        }
        return writer.endTag(tag);
    }

    private boolean isId(String tag){
        return "id".equals(tag);
    }
}
