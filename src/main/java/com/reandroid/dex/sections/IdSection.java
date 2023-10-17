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

import com.reandroid.dex.base.DexItemArray;
import com.reandroid.dex.base.IntegerPair;
import com.reandroid.dex.id.IdItem;

public class IdSection<T extends IdItem> extends Section<T> {

    public IdSection(IntegerPair countAndOffset, SectionType<T> sectionType) {
        super(sectionType, new IdSectionArray<>(countAndOffset, sectionType.getCreator()));
    }
    IdSection(SectionType<T> sectionType, IdSectionArray<T> itemArray){
        super(sectionType, itemArray);
    }

    @Override
    public T get(int i){
        return getItemArray().get(i);
    }
    @Override
    public T[] get(int[] indexes){
        if(indexes == null || indexes.length == 0){
            return null;
        }
        DexItemArray<T> itemArray = getItemArray();
        int length = indexes.length;
        T[] results = itemArray.newInstance(indexes.length);
        for(int i = 0; i < length; i++){
            results[i] = itemArray.get(indexes[i]);
        }
        return results;
    }
    public T createItem() {
        return getItemArray().createNext();
    }
    @Override
    void onRefreshed(int position){
        position += getItemArray().countBytes();
        updateNextSection(position);
    }

}
