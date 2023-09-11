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
package com.reandroid.dex.model;

import com.reandroid.archive.PathTree;
import com.reandroid.arsc.container.ExpandableBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.header.DexHeader;
import com.reandroid.dex.index.ClassId;
import com.reandroid.dex.index.StringData;
import com.reandroid.dex.index.TypeId;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionList;
import com.reandroid.dex.sections.SectionType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DexFile extends ExpandableBlockContainer {

    private final SectionList sectionList;
    private Map<String, DexClass> dexClasses = new HashMap<>();
    private PathTree<StringData> pathTree;

    public DexFile() {
        super(1);
        this.sectionList = new SectionList();
        addChild(sectionList);
    }
    public void decode(File outDir) throws IOException {
        int size = dexClasses.size();
        System.out.println("Total: " + size);
        int i = 0;
        for(DexClass dexClass : dexClasses.values()){
            i++;
            System.out.println(i + "/" + size + ": " + dexClass);
            dexClass.decode(outDir);
        }
        System.out.println("Done: " + outDir);
    }

    public SectionList getSectionList(){
        return sectionList;
    }
    public DexHeader getHeader() {
        return getSectionList().getHeader();
    }
    private void mapClasses(){
        Section<ClassId> sectionClass = sectionList.get(SectionType.CLASS_ID);
        int count = sectionClass.getCount();
        Map<String, DexClass> dexClasses = new HashMap<>(count);
        this.dexClasses = dexClasses;
        for(int i = 0; i < count; i++){
            DexClass dexClass = DexClass.create(sectionClass.get(i));
            dexClasses.put(dexClass.getName(), dexClass);
        }
    }
    private void buildPathTree(){
        PathTree<StringData> pathTree = PathTree.newRoot();
        Section<TypeId> typeSection = getSectionList().get(SectionType.TYPE_ID);
        Iterator<TypeId> iterator = typeSection.iterator();
        while (iterator.hasNext()){
            TypeId typeId = iterator.next();
            StringData stringData = typeId.getNameData();
            String name = stringData.getString();
            pathTree.add(name, stringData);
        }
        this.pathTree = pathTree;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        super.onReadBytes(reader);
        //buildPathTree();
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
