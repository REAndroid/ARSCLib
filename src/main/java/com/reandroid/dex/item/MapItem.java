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
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class MapItem extends BaseItem {
    private final IntegerItem offset;
    public MapItem(IntegerItem offset) {
        super(SIZE);
        this.offset = offset;
    }
    public MapItemType getMapItemType(){
        return MapItemType.get(getType());
    }
    public int getType(){
        return getInteger(getBytesInternal(), OFFSET_TYPE);
    }
    public void setType(int type){
        putInteger(getBytesInternal(), OFFSET_TYPE, type);
    }
    public int getSize(){
        return getInteger(getBytesInternal(), OFFSET_SIZE);
    }
    public void setSize(int size){
        putInteger(getBytesInternal(), OFFSET_SIZE, size);
    }
    public int getOffset(){
        return getInteger(getBytesInternal(), OFFSET_OFFSET);
    }
    public void setOffset(int size){
        putInteger(getBytesInternal(), OFFSET_OFFSET, size);
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        IntegerItem offset = this.offset;
        reader.seek(offset.get());
        super.onReadBytes(reader);
    }
    @Override
    public String toString(){
        return "type=" + getMapItemType() + "(" + HexUtil.toHex8(getType()) + ")"
                + ", size="
                + getSize()
                + ", offset="
                + getOffset();
    }


    private static final int OFFSET_TYPE = 0;
    private static final int OFFSET_SIZE = 4;
    private static final int OFFSET_OFFSET = 8;

    private static final int SIZE = 12;
}
