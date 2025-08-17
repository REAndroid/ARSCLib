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
package com.reandroid.dex.dexopt;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;

import java.io.IOException;

public class SizedBlockList<T extends Block> extends BlockList<T> {

    private final IntegerReference bytesSize;

    public SizedBlockList(IntegerReference bytesSize, Creator<? extends T> creator) {
        super(creator);
        this.bytesSize = bytesSize;
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        bytesSize.set(countBytes());
    }

    @Override
    protected void onReadBytes(BlockReader reader) throws IOException {
        int size = bytesSize.get();
        BlockReader itemsReader = reader.create(size);
        while (itemsReader.isAvailable()) {
            createNext().readBytes(itemsReader);
        }
        reader.offset(size);
    }
}
