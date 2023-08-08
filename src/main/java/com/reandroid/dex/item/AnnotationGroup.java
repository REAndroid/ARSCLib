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

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.CountedArray;
import com.reandroid.dex.base.DexOffsetArray;
import com.reandroid.dex.base.IntegerList;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;
import java.util.Iterator;

public class AnnotationGroup extends FixedBlockContainer implements
        Iterable<AnnotationItem>, SmaliFormat {
    private final IntegerReference offsetReference;
    private final IntegerItem itemCount;
    private final DexOffsetArray offsetArray;
    private final AnnotationItemArray itemsArray;
    public AnnotationGroup(IntegerReference offsetReference){
        super(3);
        this.offsetReference = offsetReference;
        this.itemCount = new IntegerItem();
        this.offsetArray = new DexOffsetArray(itemCount);
        this.itemsArray = new AnnotationItemArray(itemCount, offsetArray);
        addChild(0, itemCount);
        addChild(1, offsetArray);
        addChild(2, itemsArray);
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        int groupOffset = offsetReference.get();
        if(groupOffset == 0){
            return;
        }
        int position = reader.getPosition();
        reader.seek(groupOffset);
        super.onReadBytes(reader);
        reader.seek(position);
    }
    @Override
    public Iterator<AnnotationItem> iterator() {
        return itemsArray.iterator();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        Iterator<AnnotationItem> iterator = itemsArray.iterator();
        boolean appendOnce = false;
        while (iterator.hasNext()){
            if(appendOnce){
                writer.newLine();
                writer.newLine();
            }
            iterator.next().append(writer);
            appendOnce = true;
        }
    }
}
