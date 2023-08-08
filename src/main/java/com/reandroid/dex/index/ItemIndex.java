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
package com.reandroid.dex.index;

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.DexFile;
import com.reandroid.dex.base.DexItem;
import com.reandroid.dex.item.AnnotationGroup;
import com.reandroid.dex.sections.DexSection;
import com.reandroid.dex.sections.DexStringPool;
import com.reandroid.dex.sections.IndexSections;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.collection.EmptyList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class ItemIndex extends DexItem implements SmaliFormat {
    private List<AnnotationGroup> annotations;
    ItemIndex(int bytesLength) {
        super(bytesLength);
        this.annotations = EmptyList.of();
    }
    public void addAnnotations(AnnotationGroup annotationGroup){
        List<AnnotationGroup> annotations = this.annotations;
        if(annotations.isEmpty()){
            annotations = new ArrayList<>();
            this.annotations = annotations;
        }
        annotations.add(annotationGroup);
    }

    public List<AnnotationGroup> getAnnotations() {
        return annotations;
    }

    TypeIndex getTypeIndex(IntegerReference reference){
        return getTypeIndex(reference.get());
    }
    TypeIndex getTypeIndex(int index){
        if(index < 0){
            return null;
        }
        IndexSections indexSections = getIndexSections();
        if(indexSections != null){
            return indexSections.getTypeIndex(index);
        }
        return null;
    }
    StringIndex getStringIndex(int index){
        if(index < 0){
            return null;
        }
        DexStringPool stringPool = getStringPool();
        if(stringPool != null){
            return stringPool.get(index);
        }
        return null;
    }
    DexStringPool getStringPool(){
        DexFile dexFile = getDexFile();
        if(dexFile != null){
            return dexFile.getStringPool();
        }
        return null;
    }
    DexSection<TypeIndex> getTypeSection(){
        IndexSections indexSections = getIndexSections();
        if(indexSections != null){
            return indexSections.getTypeSection();
        }
        return null;
    }
    IndexSections getIndexSections(){
        return getParentInstance(IndexSections.class);
    }
    DexFile getDexFile(){
        return getParentInstance(DexFile.class);
    }

    void appendAnnotations(SmaliWriter writer) throws IOException {
        List<AnnotationGroup> annotations = getAnnotations();
        if(annotations.size() == 0){
            return;
        }
        Iterator<AnnotationGroup> iterator = annotations.iterator();
        writer.newLine();
        while (iterator.hasNext()){
            iterator.next().append(writer);
        }
    }
}
