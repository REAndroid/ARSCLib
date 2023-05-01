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

import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.group.EntryGroup;
import com.reandroid.arsc.value.*;
import com.reandroid.common.EntryStore;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

public class XMLEntryDecoder<OUTPUT>{
    private final Object mLock = new Object();
    private final DecoderResTableEntry<OUTPUT> decoderEntry;
    private final DecoderResTableEntryMap<OUTPUT> decoderEntryMap;
    private Predicate<Entry> mDecodedEntries;

    public XMLEntryDecoder(EntryStore entryStore){
        this.decoderEntry = new DecoderResTableEntry<>(entryStore);
        this.decoderEntryMap = new DecoderResTableEntryMap<>(entryStore);
    }

    public void setDecodedEntries(Predicate<Entry> decodedEntries) {
        this.mDecodedEntries = decodedEntries;
    }

    private boolean shouldDecode(Entry entry){
        if(entry == null || entry.isNull()){
            return false;
        }
        if(this.mDecodedEntries != null){
            return mDecodedEntries.test(entry);
        }
        return true;
    }

    public OUTPUT decode(EntryWriter<OUTPUT> writer, Entry entry) throws IOException{
        if(!shouldDecode(entry)){
            return null;
        }
        synchronized (mLock){
            TableEntry<?, ?> tableEntry = entry.getTableEntry();
            if(tableEntry instanceof ResTableMapEntry){
                return decoderEntryMap.decode((ResTableMapEntry) tableEntry, writer);
            }
            return decoderEntry.decode((ResTableEntry) tableEntry, writer);
        }
    }
    public int decode(EntryWriter<OUTPUT> writer, Collection<Entry> entryList) throws IOException {
        int count = 0;
        for(Entry entry : entryList){
            OUTPUT output = decode(writer, entry);
            if(output != null){
                count ++;
            }
        }
        return count;
    }
    public int decode(EntryWriter<OUTPUT> writer, ResConfig resConfig, Collection<EntryGroup> entryGroupList) throws IOException {
        int count = 0;
        for(EntryGroup entryGroup : entryGroupList){
            OUTPUT output = decode(writer, entryGroup.getEntry(resConfig));
            if(output != null){
                count ++;
            }
        }
        return count;
    }
    public int decode(EntryWriter<OUTPUT> writer, TypeBlock typeBlock) throws IOException {
        Iterator<Entry> iterator = typeBlock.getEntryArray()
                .iterator(true);
        int count = 0;
        while (iterator.hasNext()){
            Entry entry = iterator.next();
            OUTPUT output = decode(writer, entry);
            if(output != null){
                count++;
            }
        }
        return count;
    }

    void deleteIfZero(int decodeCount, File file){
        if(decodeCount > 0){
            return;
        }
        file.delete();
        File dir = file.getParentFile();
        if(isEmptyDirectory(dir)){
            dir.delete();
        }
    }
    private boolean isEmptyDirectory(File dir){
        if(dir == null || !dir.isDirectory()){
            return false;
        }
        File[] files = dir.listFiles();
        return files == null || files.length == 0;
    }
    File toOutXmlFile(File resDirectory, TypeBlock typeBlock){
        String path = toValuesXml(typeBlock);
        return new File(resDirectory, path);
    }
    String toValuesXml(TypeBlock typeBlock){
        StringBuilder builder = new StringBuilder();
        char sepChar = File.separatorChar;
        builder.append("values");
        builder.append(typeBlock.getQualifiers());
        builder.append(sepChar);
        String type = typeBlock.getTypeName();
        builder.append(type);
        if(!type.endsWith("s")){
            builder.append('s');
        }
        builder.append(".xml");
        return builder.toString();
    }
}
