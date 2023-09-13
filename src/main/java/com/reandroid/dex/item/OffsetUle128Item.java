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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.base.OffsetSupplier;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.sections.SectionType;

import java.io.IOException;

public class OffsetUle128Item<T extends Block> extends Ule128Item implements BlockRefresh {
    private final SectionType<T> sectionType;
    private T item;

    public OffsetUle128Item(SectionType<T> sectionType){
        super();
        this.sectionType = sectionType;
    }

    public T getItem(){
        return item;
    }
    public void setItem(T item) {
        int index = 0;
        if(item != null){
            index = item.getIndex();
        }
        this.item = item;
        set(index);
    }
    private void updateItem(){
        item = getAt(sectionType, get());
    }
    @Override
    public void refresh() {
        T item = getItem();
        if(item != null){
            OffsetSupplier supplier = (OffsetSupplier) item;
            set(supplier.getOffsetReference().get());
        }
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        updateItem();
    }
    @Override
    public String toString() {
        T item = this.item;
        if(item != null){
            return item.toString();
        }
        return sectionType.getName() + ": " + get();
    }
}
