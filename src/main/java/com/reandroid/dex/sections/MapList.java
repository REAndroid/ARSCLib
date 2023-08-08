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

import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.header.DexHeader;
import com.reandroid.dex.item.MapItem;
import com.reandroid.dex.item.MapItemArray;

import java.io.IOException;
import java.util.Iterator;

public class MapList extends FixedBlockContainer implements Iterable<MapItem> {
    private final IntegerReference offsetReference;

    private final IntegerItem itemCount;
    private final MapItemArray mapItemArray;

    public MapList(IntegerReference offsetReference) {
        super(2);
        this.offsetReference = offsetReference;
        this.itemCount = new IntegerItem();
        this.mapItemArray = new MapItemArray(itemCount);
        addChild(0, itemCount);
        addChild(1, mapItemArray);
    }
    public MapList(DexHeader header){
        this(header.map);
    }

    @Override
    public Iterator<MapItem> iterator() {
        return mapItemArray.iterator();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int offset = this.offsetReference.get();
        if(offset == 0){
            return;
        }
        reader.seek(offset);
        super.onReadBytes(reader);
        mapItemArray.childesCount();
    }

}
