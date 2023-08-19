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
package com.reandroid.dex.debug;

import com.reandroid.arsc.item.ByteItem;
import com.reandroid.dex.item.DexContainerItem;

public class DebugElement extends DexContainerItem {
    private final ByteItem elementType;

    DebugElement(int childesCount, int flag) {
        super(childesCount + 1);

        this.elementType = new ByteItem();
        this.elementType.set((byte) flag);

        addChild(0, elementType);
    }
    DebugElement(int childesCount, DebugElementType<?> elementType) {
        this(childesCount, elementType.getFlag());
    }

    public int getElementTypeFlag(){
        return elementType.unsignedInt();
    }
    public DebugElementType<?> getElementType() {
        return DebugElementType.fromFlag(getElementTypeFlag());
    }
    @Override
    public String toString() {
        return "Type = " + getElementType();
    }
}
