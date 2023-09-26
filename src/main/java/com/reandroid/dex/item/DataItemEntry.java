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
import com.reandroid.arsc.base.OffsetSupplier;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.*;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyItem;

public class DataItemEntry extends DexContainerItem
        implements PositionedItem, OffsetSupplier, OffsetReceiver, KeyItem {

    private IntegerReference mReference;

    public DataItemEntry(int childesCount) {
        super(childesCount);
    }

    public void removeSelf() {
        Block parent = getParent();
        if(!(parent instanceof DexItemArray<?>)){
            return;
        }
        DexItemArray<DataItemEntry> itemArray = (DexItemArray<DataItemEntry>) parent;
        setParent(null);
        itemArray.remove(this);
        setPosition(0);
    }
    @Override
    public Key getKey() {
        return null;
    }
    @Override
    public void setPosition(int position) {
        IntegerReference reference = getOffsetReference();
        if(reference == null){
            reference = new NumberIntegerReference(position);
            setOffsetReference(reference);
        }else {
            reference.set(position);
        }
    }
    @Override
    public IntegerReference getOffsetReference() {
        return mReference;
    }
    @Override
    public void setOffsetReference(IntegerReference reference) {
        this.mReference = reference;
    }
}
