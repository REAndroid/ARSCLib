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

import com.reandroid.dex.base.ShortList;
import com.reandroid.dex.sections.DexSection;

import java.io.IOException;

public class TypeList extends ShortList {
    public TypeList() {
        super();
    }
    public TypeIndex[] toTypes(DexSection<TypeIndex> typeSection){
        return typeSection.toArray(toArray());
    }

    @Override
    public String toString() {
        return "size=" + size();
    }
    public static TypeList read(BlockReader reader, int offset) throws IOException {
        if(offset <= 0){
            return null;
        }
        int position = reader.getPosition();
        reader.seek(offset);
        TypeList typeList = new TypeList();
        typeList.readBytes(reader);
        reader.seek(position);
        if(typeList.size() > 0){
            return typeList;
        }
        return null;
    }
}
