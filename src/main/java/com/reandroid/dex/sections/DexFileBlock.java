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
package com.reandroid.dex.sections;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.group.ItemGroup;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.common.BytesOutputStream;
import com.reandroid.dex.header.DexHeader;
import com.reandroid.dex.item.AnnotationElement;
import com.reandroid.dex.item.AnnotationItem;
import com.reandroid.dex.item.StringData;
import com.reandroid.dex.value.ArrayValue;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.value.StringValue;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.FilterIterator;

import java.io.*;
import java.util.Iterator;

public class DexFileBlock extends FixedBlockContainer {

    private final SectionList sectionList;

    public DexFileBlock() {
        super(1);
        this.sectionList = new SectionList();
        addChild(0, sectionList);
    }

    public Iterator<StringData> unusedStrings(){
        return getStringsWithUsage(StringData.USAGE_NONE);
    }
    public Iterator<StringData> getStringsContainsUsage(int usage){
        return FilterIterator.of(getStrings(),
                stringData -> stringData.containsUsage(usage));
    }
    public Iterator<StringData> getStringsWithUsage(int usage){
        return FilterIterator.of(getStrings(),
                stringData -> stringData.getStringUsage() == usage);
    }
    public Iterator<StringData> getStrings(){
        Section<StringData> stringSection = get(SectionType.STRING_DATA);
        if(stringSection == null){
            return EmptyIterator.of();
        }
        return stringSection.iterator();
    }
    public void sortSection(SectionType<?>[] order){
        refresh();
        getSectionList().sortSection(order);
        refresh();
    }
    public void sortStrings(){
        getSectionList().sortStrings();
    }
    public void linkTypeSignature(){
        Section<AnnotationItem> annotationSection = getSectionList().get(SectionType.ANNOTATION);
        if(annotationSection == null){
            return;
        }
        ItemGroup<AnnotationItem> group = annotationSection.getPool().getGroup(ANNOTATION_SIG_KEY);
        if(group == null){
            return;
        }
        for(AnnotationItem item : group){
            AnnotationElement element = item.getElement(0);
            if(element == null){
                continue;
            }
            DexValueBlock<?> dexValue = element.getValue();
            if(!(dexValue instanceof ArrayValue)){
                continue;
            }
            ArrayValue arrayValue = (ArrayValue) dexValue;
            linkTypeSignature(arrayValue);
        }
    }
    private void linkTypeSignature(ArrayValue arrayValue){
        for(DexValueBlock<?> value : arrayValue){
            if(!(value instanceof StringValue)){
                continue;
            }
            StringData stringData = ((StringValue) value).getStringData();
            if(stringData != null){
                stringData.addStringUsage(StringData.USAGE_TYPE);
            }
        }
    }

    public<T1 extends Block> Section<T1> get(SectionType<T1> sectionType){
        return getSectionList().get(sectionType);
    }
    public DexHeader getHeader() {
        return getSectionList().getHeader();
    }
    public SectionList getSectionList(){
        return sectionList;
    }
    public MapList getMapList(){
        return getSectionList().getMapList();
    }

    @Override
    protected void onPreRefresh() {
        sectionList.refresh();
    }
    @Override
    protected void onRefreshed() {
        sectionList.updateHeader();
    }
    @Override
    public byte[] getBytes(){
        BytesOutputStream outputStream = new BytesOutputStream(
                getHeader().fileSize.get());
        try {
            writeBytes(outputStream);
            outputStream.close();
        } catch (IOException ignored) {
        }
        return outputStream.toByteArray();
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
    public void write(File file) throws IOException {
        File dir = file.getParentFile();
        if(dir != null && !dir.exists()){
            dir.mkdirs();
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        writeBytes(outputStream);
        outputStream.close();
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

    public static final String ANNOTATION_SIG_KEY = "Ldalvik/annotation/Signature;->value()";
}
