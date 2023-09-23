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
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.MergingIterator;
import com.reandroid.utils.io.IOUtil;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DexDirectory implements Iterable<DexFile> {
    private final List<DexFile> dexFileList;

    public DexDirectory() {
        this.dexFileList = new ArrayList<>();
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
