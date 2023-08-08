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
import com.reandroid.dex.index.*;
import com.reandroid.dex.header.DexHeader;
import com.reandroid.dex.reader.DexReader;
import com.reandroid.dex.sections.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DexFile extends ExpandableBlockContainer {

    private final DexHeader dexHeader;
    private final DexStringPool stringPool;
    private final IndexSections sections;

    private final MapList mapList;


    public DexFile() {
        super(4);

        DexHeader header = new DexHeader();
        this.dexHeader =  header;
        this.stringPool = new DexStringPool(header);
        this.sections = new IndexSections(header);

        this.mapList = new MapList(header);

        addChild(dexHeader);
        addChild(stringPool);
        addChild(sections);
        addChild(mapList);
    }

    public DexHeader getHeader() {
        return dexHeader;
    }
    public DexStringPool getStringPool(){
        return stringPool;
    }

    public IndexSections getSections() {
        return sections;
    }

    public DexSection<TypeIndex> getTypeSection() {
        return getSections().getTypeSection();
    }
    public DexSection<ProtoIndex> getProtoSection() {
        return getSections().getProtoSection();
    }
    public DexSection<FieldIndex> getFieldSection() {
        return getSections().getFieldSection();
    }
    public DexSection<ClassIndex> getClassSection() {
        return getSections().getClassSection();
    }
    public DexSection<MethodIndex> getMethodSection(){
        return getSections().getMethodSection();
    }

    public MapList getMapList(){
        return mapList;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        DexReader dexReader = DexReader.create(this, reader);
        super.onReadBytes(dexReader);
        System.err.println("annotation: " + dexReader.getAnnotationPool());
        System.err.println("code: " + dexReader.getCodePool());
    }

    public void read(byte[] dexBytes) throws IOException {
        DexReader reader = new DexReader(this, dexBytes);
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
