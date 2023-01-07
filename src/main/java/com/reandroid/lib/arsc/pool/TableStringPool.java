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
package com.reandroid.lib.arsc.pool;

import com.reandroid.lib.arsc.array.StringArray;
import com.reandroid.lib.arsc.array.TableStringArray;
import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.header.HeaderBlock;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.TableString;

import java.io.IOException;
import java.io.InputStream;

 public class TableStringPool extends BaseStringPool<TableString> {
    public TableStringPool(boolean is_utf8) {
        super(is_utf8);
    }

    @Override
    StringArray<TableString> newInstance(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        return new TableStringArray(offsets, itemCount, itemStart, is_utf8);
    }
    public void merge(TableStringPool stringPool){
        if(stringPool==null||stringPool==this){
            return;
        }
        StringArray<TableString> existArray = getStringsArray();
        if(existArray.childesCount()!=0){
            return;
        }
        StringArray<TableString> comingArray = stringPool.getStringsArray();
        int count=comingArray.childesCount();
        existArray.ensureSize(count);
        for(int i=0;i<count;i++){
            TableString exist = existArray.get(i);
            TableString coming = comingArray.get(i);
            exist.set(coming.get());
        }
        getStyleArray().merge(stringPool.getStyleArray());
        refreshUniqueIdMap();
    }

     /**
      * Loads string pool from table block (resources.arsc)
      */
    public static TableStringPool readFromTable(InputStream inputStream) throws IOException {
        //TODO: for better result, make blockReader to
        // load buffer only the size of string pool
        BlockReader blockReader = new BlockReader(inputStream);
        HeaderBlock tableHeader = blockReader.readHeaderBlock();
        if(tableHeader.getChunkType()!=ChunkType.TABLE){
            throw new IOException("Not TableBlock: "+tableHeader);
        }
        blockReader.seek(tableHeader.getHeaderSize());
        TableStringPool stringPool = new TableStringPool(true);
        stringPool.readBytes(blockReader);
        blockReader.close();
        return stringPool;
    }
}
