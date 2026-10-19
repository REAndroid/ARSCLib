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
package com.reandroid.arsc.header;

import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.ShortItem;

public class SpecHeader extends SpecOrTypeHeader {

    private final ShortItem typesCount;
    private final IntegerItem count;

    public SpecHeader() {
        super(3, ChunkType.SPEC);

        ByteItem res0 = new ByteItem();
        this.typesCount = new ShortItem();
        this.count = new IntegerItem();

        addChild(4, res0);
        addChild(5, typesCount);
        addChild(6, count);
    }

    public IntegerReference typesCount() {
        return typesCount;
    }

    public IntegerReference count() {
        return count;
    }

    @Override
    public String toString() {
        return super.toString() + ", typesCount="
                + typesCount() + ", count=" + count();
    }
}
