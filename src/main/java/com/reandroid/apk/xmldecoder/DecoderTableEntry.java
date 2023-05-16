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

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.decoder.ValueDecoder;
import com.reandroid.arsc.value.*;
import com.reandroid.common.EntryStore;

import java.io.IOException;

abstract class DecoderTableEntry<INPUT extends TableEntry<?, ?>, OUTPUT> {
    private final EntryStore entryStore;
    public DecoderTableEntry(EntryStore entryStore){
        this.entryStore = entryStore;
    }
    public EntryStore getEntryStore() {
        return entryStore;
    }
    public abstract OUTPUT decode(INPUT tableEntry, EntryWriter<OUTPUT> writer) throws IOException;

    void writeText(EntryWriter<?> writer, PackageBlock packageBlock, ValueItem valueItem)
            throws IOException {

        if(valueItem.getValueType() == ValueType.STRING){
            XMLDecodeHelper.writeTextContent(writer, valueItem.getDataAsPoolString());
        }else {
            String value = ValueDecoder.decodeEntryValue(
                    getEntryStore(),
                    packageBlock,
                    valueItem.getValueType(),
                    valueItem.getData());
            writer.text(value);
        }
    }
    void writeText(EntryWriter<?> writer, ResValueMap attributeValue)
            throws IOException {
        if(attributeValue.getValueType() == ValueType.STRING){
            XMLDecodeHelper.writeTextContent(writer, attributeValue.getDataAsPoolString());
        }else {
            String value = ValueDecoder.decode(getEntryStore(),
                    attributeValue.getPackageBlock().getId(),
                    attributeValue);
            writer.text(value);
        }
    }
}
