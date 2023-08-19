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
package com.reandroid.dex.instruction;

import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.dex.base.FixedDexContainer;
import com.reandroid.dex.item.IntegerList;

public class InsPacked extends PayloadIns {
    private final ShortItem elementCount;
    private final IntegerItem firstKey;
    private final IntegerList elements;

    public InsPacked() {
        super(3, Opcode.PACKED_SWITCH_PAYLOAD);
        this.elementCount = new ShortItem();
        this.firstKey = new IntegerItem();
        this.elements = new IntegerList(elementCount);

        addChild(1, elementCount);
        addChild(2, firstKey);
        addChild(3, elements);
    }

    @Override
    public String toString() {
        return "InsPacked{" +
                "elementCount=" + elementCount +
                ", firstKey=" + firstKey +
                ", elements=" + elements +
                '}';
    }
}