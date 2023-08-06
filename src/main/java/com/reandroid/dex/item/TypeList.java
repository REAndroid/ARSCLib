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

import com.reandroid.arsc.io.BlockReader;

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.ShortList;
import com.reandroid.dex.index.TypeIndex;
import com.reandroid.dex.sections.DexSection;

import java.io.IOException;

public class TypeList extends ShortList {
    private final IntegerReference offsetReference;
    public TypeList(IntegerReference offsetReference) {
        super();
        this.offsetReference = offsetReference;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        int offset = offsetReference.get();
        if(offset == 0){
            return;
        }
        int position = reader.getPosition();
        reader.seek(offset);
        super.onReadBytes(reader);
        reader.seek(position);
    }
    public TypeIndex[] toTypes(DexSection<TypeIndex> typeSection){
        return typeSection.toArray(toArray());
    }
}
