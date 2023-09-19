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

import com.reandroid.arsc.item.ShortArrayBlock;
import com.reandroid.dex.base.DexPositionAlign;
import com.reandroid.dex.base.PositionAlignedItem;

public class ShortList extends IntegerList implements PositionAlignedItem {
    private final DexPositionAlign positionAlign;
    public ShortList(){
        super(1, new ShortArrayBlock());
        this.positionAlign = new DexPositionAlign();
        addChild(2, positionAlign);
    }

    @Override
    public DexPositionAlign getPositionAlign(){
        return positionAlign;
    }

    @Override
    public String toString() {
        DexPositionAlign dexPositionAlign = getPositionAlign();
        if(dexPositionAlign.size() > 0){
            return super.toString() + dexPositionAlign;
        }
        return super.toString();
    }
}
