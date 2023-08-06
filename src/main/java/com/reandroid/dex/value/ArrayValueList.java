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
package com.reandroid.dex.value;

import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.dex.base.Ule128Item;

import java.util.Iterator;

public class ArrayValueList extends FixedBlockContainer {
    private final Ule128Item elementCount;
    private final BlockList<DexValue<?>> elements;
    public ArrayValueList() {
        super(2);
        this.elementCount = new Ule128Item();
        this.elements = new BlockList<>();
        addChild(0, elementCount);
        addChild(1, elements);
    }
    public int getCount() {
        return elementCount.get();
    }
    public BlockList<DexValue<?>> getElements() {
        return elements;
    }
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        Iterator<DexValue<?>> iterator = getElements().iterator();
        boolean appendOnce = false;
        while (iterator.hasNext()){
            if(appendOnce){
                builder.append(',');
            }
            builder.append('\n');
            builder.append("    ");
            builder.append(iterator.next());
            appendOnce = true;
        }
        if(appendOnce){
            builder.append('\n');
        }
        builder.append('}');
        return builder.toString();
    }
}
