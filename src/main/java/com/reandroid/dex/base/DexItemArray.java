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
package com.reandroid.dex.base;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockArray;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;

import java.io.IOException;

public class DexItemArray<T extends Block> extends BlockArray<T>{

    private final IntegerPair countAndOffset;
    private final Creator<T> creator;

    public DexItemArray(IntegerPair countAndOffset,
                        Creator<T> creator) {
        super(creator.newInstance(0));
        this.creator = creator;
        this.countAndOffset = countAndOffset;
    }

    protected IntegerPair getCountAndOffset() {
        return countAndOffset;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        IntegerPair countAndOffset = getCountAndOffset();
        setChildesCount(countAndOffset.getFirst().get());
        reader.seek(countAndOffset.getSecond().get());
        super.onReadBytes(reader);
    }

    @Override
    protected void onRefreshed() {
        IntegerReference count = getCountAndOffset().getFirst();
        count.set(childesCount());
        calculateOffset();
    }
    protected void calculateOffset() {

    }
    @Override
    public T[] newInstance(int length) {
        return creator.newInstance(length);
    }
    @Override
    public T newInstance() {
        return creator.newInstance();
    }
}
