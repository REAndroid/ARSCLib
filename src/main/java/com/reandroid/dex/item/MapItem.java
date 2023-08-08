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

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.DexItem;
import com.reandroid.dex.base.IndirectInteger;
import com.reandroid.dex.common.MapItemType;

public class MapItem extends DexItem {
    private final IndirectInteger type;
    private final IndirectInteger count;
    private final IndirectInteger offset;
    public MapItem() {
        super(SIZE);
        int offset = -4;
        this.type = new IndirectInteger(this, offset += 4);
        this.count = new IndirectInteger(this, offset += 4);
        this.offset = new IndirectInteger(this, offset += 4);
    }
    public MapItemType getMapItemType(){
        return MapItemType.get(getType().get());
    }
    public IntegerReference getType(){
        return type;
    }
    public IntegerReference getCount(){
        return count;
    }
    public IntegerReference getOffset(){
        return offset;
    }
    @Override
    public String toString() {
        return  "type=" + getMapItemType() +
                ", count=" + count +
                ", offset=" + offset;
    }

    private static final int SIZE = 12;
}
