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
package com.reandroid.dex;

import com.reandroid.arsc.container.ExpandableBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.index.ClassId;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.header.DexHeader;
import com.reandroid.dex.sections.*;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.*;

public class DexFile extends ExpandableBlockContainer {


    private final SectionList sectionList;

    public DexFile() {
        super(1);
        this.sectionList = new SectionList();
        addChild(sectionList);
    }

    public SectionList getSectionList(){
        return sectionList;
    }
    public DexHeader getHeader() {
        return getSectionList().getHeader();
    }


    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        super.onReadBytes(reader);
        //TEST
        Section<ClassId> sectionClass = sectionList.get(SectionType.CLASS_ID);
        for(ClassId classId:sectionClass){
            StringWriter writer = new StringWriter();
            SmaliWriter smaliWriter=new SmaliWriter(writer);
            classId.append(smaliWriter);
            smaliWriter.close();
            System.err.println(writer.toString());
        }

    }

    public void read(byte[] dexBytes) throws IOException {
        BlockReader reader = new BlockReader(dexBytes);
        readBytes(reader);
        reader.close();
    }
    public void read(InputStream inputStream) throws IOException {
        BlockReader reader = new BlockReader(inputStream);
        readBytes(reader);
        reader.close();
    }
    public void read(File file) throws IOException {
        BlockReader reader = new BlockReader(file);
        readBytes(reader);
        reader.close();
    }
    public static boolean isDexFile(File file){
        if(file == null || !file.isFile()){
            return false;
        }
        DexHeader dexHeader = null;
        try {
            InputStream inputStream = new FileInputStream(file);
            dexHeader = DexHeader.readHeader(inputStream);
            inputStream.close();
        } catch (IOException ignored) {
        }
        return isDexFile(dexHeader);
    }
    public static boolean isDexFile(InputStream inputStream){
        DexHeader dexHeader = null;
        try {
            dexHeader = DexHeader.readHeader(inputStream);
            inputStream.close();
        } catch (IOException ignored) {
        }
        return isDexFile(dexHeader);
    }
    private static boolean isDexFile(DexHeader dexHeader){
        if(dexHeader == null){
            return false;
        }
        if(dexHeader.magic.isDefault()){
            return false;
        }
        int version = dexHeader.version.getVersionAsInteger();
        return version > 0 && version < 1000;
    }
}
