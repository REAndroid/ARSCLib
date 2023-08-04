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
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.value.DexValue;

import java.io.IOException;
import java.util.Iterator;

public class AnnotationItem extends FixedBlockContainer {
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
    public int getElementsCount(){
        return elementsCount.getValue();
    }
    public TypeIndex getTypeIndex(){
        DexFile dexFile = getParentInstance(DexFile.class);
        if(dexFile != null){
            return dexFile.getTypeSection().get(typeIndex.getValue());
        }
        return null;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        int count = getElementsCount();
        BlockList<AnnotationElement> elements = this.annotationElements;
        for(int i = 0; i < count; i++){
            AnnotationElement element = new AnnotationElement();
            elements.add(element);
            element.readBytes(reader);
        }
        elements.size();
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(".annotation system ");
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
