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

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.IntegerVisitor;
import com.reandroid.arsc.item.VisitableInteger;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.MergingIterator;
import com.reandroid.utils.io.IOUtil;
import com.reandroid.xml.XMLFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DexDirectory implements Iterable<DexFile>, VisitableInteger {
    private final List<DexFile> dexFileList;
    private Object mTag;
    private DexFile mCurrent;

    public DexDirectory() {
        this.dexFileList = new ArrayList<>();
    }

    public Object getTag() {
        return mTag;
    }
    public void setTag(Object tag) {
        this.mTag = tag;
    }
    public void replaceRFields(){
        Map<Integer, RField> map = RField.mapRFields(listRFields().iterator());
        IntegerVisitor visitor = new IntegerVisitor() {
            @Override
            public void visit(Object sender, IntegerReference reference) {
                DexFile.replaceRFields(mCurrent, map, reference);
            }
        };
        this.visitIntegers(visitor);
        File dir = (File) getTag();
        File pubXml = new File(dir, "public.xml");
        try {
            XmlSerializer serializer = XMLFactory.newSerializer(pubXml);
            RClass.serializePublicXml(map.values(), serializer);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
    @Override
    public void visitIntegers(IntegerVisitor visitor) {
        for(DexFile dexFile : this){
            mCurrent = dexFile;
            dexFile.visitIntegers(visitor);
            save(dexFile);
        }
    }
    private void save(DexFile dexFile){
        Object tag = dexFile.getTag();
        if(!(tag instanceof File)){
            return;
        }
        File file = (File) tag;
        String name = file.getName();
        name = name.substring(0, name.length()-4);
        name = name + "_mod.dex";
        File modFile = new File(file.getParentFile(), name);
        try {
            dexFile.refresh();
            dexFile.sortStrings();
            dexFile.refresh();
            dexFile.write(modFile);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
    public List<RField> listRFields() {
        List<RField> fieldList = CollectionUtil.toUniqueList(getRFields());
        fieldList.sort(CompareUtil.getComparableComparator());
        return fieldList;
    }
    public Iterator<RField> getRFields() {
        return new MergingIterator<>(ComputeIterator.of(getRClasses(),
                RClass::getStaticFields));
    }
    public Iterator<RClass> getRClasses() {
        return new MergingIterator<>(ComputeIterator.of(iterator(),
                DexFile::getRClasses));
    }
    public DexClass get(String name) {
        for(DexFile dexFile : this) {
            DexClass dexClass = dexFile.get(name);
            if(dexClass != null){
                return dexClass;
            }
        }
        return null;
    }
    public Iterator<DexClass> getDexClasses() {
        return new MergingIterator<>(ComputeIterator.of(iterator(),
                DexFile::getDexClasses));
    }
    @Override
    public Iterator<DexFile> iterator() {
        return getDexFileList().iterator();
    }

    public void refresh(){
        for(DexFile dexFile : this){
            dexFile.refresh();
        }
    }
    public void addDirectory(File dir) throws IOException {
        File[] files = dir.listFiles();
        if(files == null){
            return;
        }
        for(File file : files){
            String name = file.getName();
            if(file.isFile() && name.endsWith(".dex") && !name.contains("_mod")){
                add(file);
            }
        }
    }
    public void add(File file) throws IOException {
        DexFile dexFile = DexFile.read(file);
        dexFile.setTag(file);
        add(dexFile);
    }
    public void add(InputStream inputStream) throws IOException {
        DexFile dexFile = DexFile.read(inputStream);
        add(dexFile);
    }
    public void add(DexFile dexFile){
        if(dexFile == null){
            return;
        }
        if(!dexFileList.contains(dexFile)){
            dexFileList.add(dexFile);
        }
    }
    public DexFile get(int i){
        return dexFileList.get(i);
    }
    public int size() {
        return dexFileList.size();
    }
    public void clear() {
        dexFileList.clear();
    }
    public List<DexFile> getDexFileList() {
        return dexFileList;
    }
    public void serializePublicXml(XmlSerializer serializer) throws IOException {
        serializer.startDocument("utf-8", null);
        serializer.text("\n");
        serializer.startTag(null, PackageBlock.TAG_resources);

        List<RField> fieldList = listRFields();
        for(RField rField : fieldList) {
            rField.serializePublicXml(serializer);
        }

        serializer.text("\n");
        serializer.endTag(null, PackageBlock.TAG_resources);
        serializer.endDocument();
        serializer.flush();
        IOUtil.close(serializer);
    }
}
