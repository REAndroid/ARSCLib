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
package com.reandroid.arsc.chunk;

import com.reandroid.arsc.array.SparseTypeEntryArray;
import com.reandroid.arsc.header.TypeHeader;
import com.reandroid.arsc.value.SparseTypeEntry;

import java.util.Collection;

public class SparseTypeBlock extends Chunk<TypeHeader>{
    private final SparseTypeEntryArray entryArray;
    public SparseTypeBlock() {
        super(new TypeHeader(), 1);
        entryArray = new SparseTypeEntryArray();
        addChild(entryArray);
        getHeaderBlock().setSparse(true);
    }
    public Collection<SparseTypeEntry> listEntries(){
        return getEntryArray().listItems();
    }
    public SparseTypeEntryArray getEntryArray() {
        return entryArray;
    }
    @Override
    protected void onChunkRefreshed() {
        getHeaderBlock().setSparse(true);
    }
}
