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

import com.reandroid.arsc.array.OffsetArray;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.header.OffsetAndCount;

import java.io.IOException;

public class DexOffsetArray extends OffsetArray {
    private final OffsetAndCount offsetAndCount;
    public DexOffsetArray(OffsetAndCount offsetAndCount){
        super();
        this.offsetAndCount = offsetAndCount;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        setSize(offsetAndCount.getCount());
        super.onReadBytes(reader);
    }
}
