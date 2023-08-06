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
package com.reandroid.dex.item;

import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.dex.DexFile;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.common.AnnotationVisibility;
import com.reandroid.dex.index.TypeIndex;
import com.reandroid.dex.sections.DexAnnotationPool;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;
import java.util.Iterator;

public class AnnotationItem extends FixedBlockContainer implements SmaliFormat {
    private final ByteItem visibility;
    private final Ule128Item typeIndex;
    private final Ule128Item elementsCount;
    private final BlockList<AnnotationElement> annotationElements;
    public AnnotationItem() {
        super(4);
        this.visibility = new ByteItem();
        this.typeIndex = new Ule128Item();
        this.elementsCount = new Ule128Item();
        this.annotationElements = new BlockList<>();
        addChild(0, visibility);
        addChild(1, typeIndex);
        addChild(2, elementsCount);
        addChild(3, annotationElements);
    }
    public AnnotationVisibility getVisibility(){
        return AnnotationVisibility.valueOf(visibility.unsignedInt());
    }
    public int getElementsCount(){
        return elementsCount.get();
    }
    public TypeIndex getTypeIndex(){
        DexFile dexFile = getParentInstance(DexFile.class);
        if(dexFile != null){
            return dexFile.getTypeSection().get(typeIndex.get());
        }
        return null;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        int count = getElementsCount();
        DexAnnotationPool pool = getParentInstance(DexFile.class).getAnnotationPool();
        BlockList<AnnotationElement> elements = this.annotationElements;
        for(int i = 0; i < count; i++){
            AnnotationElement element = pool.getOrRead(reader);
            elements.add(element);
        }
        elements.size();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(".annotation ");
        writer.append(getVisibility().getName());
        writer.append(' ');
        getTypeIndex().append(writer);
        Iterator<AnnotationElement> iterator = annotationElements.iterator();
        writer.indentPlus();
        while (iterator.hasNext()){
            writer.newLine();
            iterator.next().append(writer);
        }
        writer.indentMinus();
        writer.newLine();
        writer.append(".end annotation");
    }
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(".annotation ");
        builder.append(getVisibility());
        builder.append(' ');
        builder.append(getTypeIndex());
        Iterator<AnnotationElement> iterator = annotationElements.iterator();
        while (iterator.hasNext()){
            builder.append('\n');
            builder.append(iterator.next());
        }
        builder.append('\n');
        builder.append(".end annotation");
        return builder.toString();
    }
}
