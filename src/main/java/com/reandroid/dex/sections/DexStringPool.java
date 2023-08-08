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

import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.dex.base.DexOffsetArray;
import com.reandroid.dex.base.IntegerPair;
import com.reandroid.dex.header.DexHeader;
import com.reandroid.dex.index.StringIndex;

import java.util.Iterator;

public class DexStringPool extends FixedBlockContainer implements Iterable<StringIndex> {

    private final DexStringArray dexStringArray;

    public DexStringPool(IntegerPair countAndOffset) {
        super(2);
        DexOffsetArray offsetArray = new DexOffsetArray(countAndOffset.getFirst());
        DexStringArray dexStringArray = new DexStringArray(countAndOffset, offsetArray);
        this.dexStringArray = dexStringArray;

        addChild(0, offsetArray);
        addChild(1, dexStringArray);
    }
    public DexStringPool(DexHeader dexHeader) {
        this(dexHeader.strings);
    }

    public StringIndex get(int index){
        return dexStringArray.get(index);
    }
    public int size(){
        return dexStringArray.childesCount();
    }
    @Override
    public Iterator<StringIndex> iterator() {
        return dexStringArray.iterator();
    }
}
