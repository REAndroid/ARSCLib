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
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.IntegerList;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;
import java.util.Iterator;

public class AnnotationGroup extends BlockList<AnnotationItem> implements
        Iterable<AnnotationItem>, SmaliFormat {
    public AnnotationGroup(){
        super();
    }
    public void read(BlockReader reader) throws IOException {
        IntegerList integerList = new IntegerList();
        integerList.readBytes(reader);
        if(integerList.size() == 0){
            return;
        }
        int[] offsetsArray = integerList.toArray();
        for(int annotationOffset : offsetsArray){
            if(annotationOffset == 0){
                continue;
            }
            reader.seek(annotationOffset);
            AnnotationItem adi = new AnnotationItem();
            add(adi);
            adi.readBytes(reader);
        }
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        Iterator<AnnotationItem> iterator = iterator();
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
