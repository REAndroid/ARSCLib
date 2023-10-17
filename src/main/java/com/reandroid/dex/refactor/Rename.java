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

import com.reandroid.dex.id.StringId;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.model.DexDirectory;
import com.reandroid.dex.model.DexFile;
import com.reandroid.dex.sections.DexFileBlock;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionList;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.FilterIterator;
import com.reandroid.utils.collection.MergingIterator;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Rename implements Iterable<RenameInfo<?>>{
    private final List<RenameInfo<?>> renameInfoList;

    public Rename(){
        this.renameInfoList = new ArrayList<>();
    }

    public void apply(DexDirectory directory){
        for(RenameInfo<?> info : this){
            info.apply(directory);
        }
    }
    public void apply(DexFile dexFile){
        apply(dexFile.getDexFileBlock());
        dexFile.clearPools();
    }
    public void apply(DexFileBlock dexFileBlock){
        dexFileBlock.linkTypeSignature();
        SectionList sectionList = dexFileBlock.getSectionList();
        sectionList.get(SectionType.METHOD_ID).getPool();
        for(RenameInfo<?> renameInfo : this){
            renameInfo.apply(sectionList);
        }
        lookStrings(sectionList.get(SectionType.STRING_ID));
    }
    private void lookStrings(Section<StringId> sectionString){
        lookStrings(sectionString.iterator());
    }
    private void lookStrings(Iterator<StringId> iterator){
        List<RenameInfo<?>> renameInfoList = CollectionUtil.toList(FilterIterator.of(
                getAll(), RenameInfo::looksStrings));
        if(renameInfoList.isEmpty()){
            return;
        }
        while (iterator.hasNext()){
            lookString(renameInfoList, iterator.next());
        }
    }
    private void lookString(List<RenameInfo<?>> renameInfoList, StringId stringData){
        for(RenameInfo<?> renameInfo : renameInfoList){
            if(renameInfo.lookString(stringData)){
                return;
            }
        }
    }
    public Iterator<RenameInfo<?>> getAll(){
        return new MergingIterator<>(ComputeIterator.of(iterator(),
                RenameInfo::iterator));
    }

    public void addClass(String search, String replace){
        add(new RenameInfoClass(search, replace));
    }
    public void addMethod(String typeName, String[] parameters, String search, String replace){
        add(new RenameInfoMethodName(typeName, parameters, search, replace));
    }
    public void addMethod(MethodKey methodKey, String replace){
        add(new RenameInfoMethodName(methodKey, replace));
    }
    public void addAnnotation(String typeName, String search, String replace){
        add(new RenameInfoAnnotationName(typeName, search, replace));
    }
    public void addField(String typeName, String search, String replace){
        add(new RenameInfoFieldName(typeName, search, replace));
    }
    public void addPackage(String search, String replace){
        add(new RenameInfoPackage(search, replace));
    }
    public void addString(String search, String replace){
        add(new RenameInfoString(search, replace));
    }

    public void add(RenameInfo<?> renameInfo){
        if(renameInfo == null || contains(renameInfo)){
            return;
        }
        this.renameInfoList.add(renameInfo);
    }
    public boolean contains(RenameInfo<?> renameInfo){
        if(renameInfo == null){
            return false;
        }
        if(this.renameInfoList.contains(renameInfo)){
            return true;
        }
        for(RenameInfo<?> info : this){
            if(info.contains(renameInfo)){
                return true;
            }
        }
        return false;
    }
    @Override
    public Iterator<RenameInfo<?>> iterator(){
        return renameInfoList.iterator();
    }
    public void write(Writer writer) throws IOException {
        for(RenameInfo<?> info : this){
            info.write(writer, true);
        }
    }
    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        try {
            write(writer);
            writer.close();
        } catch (IOException exception) {
            return exception.toString();
        }
        return writer.toString();
    }
}
