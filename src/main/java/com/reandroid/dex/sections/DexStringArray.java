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

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.DexItemArray;
import com.reandroid.dex.base.DexOffsetArray;
import com.reandroid.dex.header.OffsetAndCount;
import com.reandroid.dex.index.StringIndex;

import java.io.IOException;

public class DexStringArray extends DexItemArray<StringIndex> {
    private final DexOffsetArray offsetArray;

    public DexStringArray(OffsetAndCount offsetAndCount, DexOffsetArray offsetArray){
        super(offsetAndCount, CREATOR);
        this.offsetArray = offsetArray;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        OffsetAndCount offsetAndCount = getOffsetAndCount();
        setChildesCount(offsetAndCount.getCount());
        StringIndex[] childes = getChildes();
        if(childes == null || childes.length == 0){
            return;
        }

        int[] offsets = offsetArray.getOffsets();
        int maximumPosition = getOffsetAndCount().getOffset();

        int length = childes.length;
        for(int i = 0; i < length; i++){
            StringIndex item = childes[i];
            int offset = offsets[i];
            if(offset == -1){
                item.setNull(true);
                continue;
            }
            reader.seek(offset);
            item.readBytes(reader);
            int position = reader.getPosition();
            if(position > maximumPosition){
                maximumPosition = position;
            }
        }
        reader.seek(maximumPosition);
    }

    private static final Creator<StringIndex> CREATOR = new Creator<StringIndex>() {
        @Override
        public StringIndex[] newInstance(int length) {
            return new StringIndex[length];
        }
        @Override
        public StringIndex newInstance() {
            return new StringIndex();
        }
    };
}
