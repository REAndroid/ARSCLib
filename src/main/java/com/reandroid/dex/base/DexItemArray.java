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
import com.reandroid.dex.header.OffsetAndCount;

import java.io.IOException;

public class DexItemArray<T extends Block> extends BlockArray<T>{
    private final OffsetAndCount offsetAndCount;
    private final Creator<T> creator;
    public DexItemArray(OffsetAndCount offsetAndCount,
                        Creator<T> creator) {
        super(creator.newInstance(0));
        this.creator = creator;
        this.offsetAndCount = offsetAndCount;
    }

    protected OffsetAndCount getOffsetAndCount() {
        return offsetAndCount;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        OffsetAndCount offsetAndCount = getOffsetAndCount();
        setChildesCount(offsetAndCount.getCount());
        reader.seek(offsetAndCount.getOffset());
        super.onReadBytes(reader);
    }

    @Override
    protected void onRefreshed() {

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
