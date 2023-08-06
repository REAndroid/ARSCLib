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

import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.item.AnnotationElement;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DexAnnotationPool extends FixedBlockContainer {
    private final BlockList<AnnotationElement> directoryList;
    public final Map<Integer, AnnotationElement> offsetMap;
    public int requestCount;
    public int min = Integer.MAX_VALUE;
    public int max;
    public DexAnnotationPool() {
        super(1);
        this.directoryList = new BlockList<>();
        offsetMap = new HashMap<>();
        addChild(0, directoryList);
    }
    public AnnotationElement getOrRead(BlockReader reader) throws IOException {
        requestCount ++;
        int offset = reader.getPosition();
        if(offset < min){
            min = offset;
        }
        if(offset > max){
            max=offset;
        }
        AnnotationElement element = offsetMap.get(offset);
        if(element != null){
            new AnnotationElement().readBytes(reader);
            return element;
        }
        element = new AnnotationElement();
        directoryList.add(element);
        offsetMap.put(offset, element);
        element.readBytes(reader);
        return element;
    }
}
