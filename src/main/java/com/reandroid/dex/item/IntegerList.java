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
import com.reandroid.arsc.io.BlockLoad;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerArrayBlock;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.common.IntegerArray;
import com.reandroid.dex.base.FixedDexContainer;
import com.reandroid.dex.base.NumberIntegerReference;
import com.reandroid.dex.base.OffsetReceiver;
import com.reandroid.dex.base.PositionedItem;

import java.io.IOException;

public class IntegerList extends BaseItem implements
        IntegerArray, BlockLoad, PositionedItem, OffsetSupplier, OffsetReceiver {

    private IntegerReference mReference;

    private final IntegerItem itemCount;
    private final IntegerArray arrayBlock;

    public IntegerList(int childesCount, IntegerArray arrayBlock){
        super(childesCount + 2);
        this.itemCount = new IntegerItem();
        this.arrayBlock = arrayBlock;
        addChild(0, itemCount);
        addChild(1, (Block) arrayBlock);

        itemCount.setBlockLoad(this);
    }
    public IntegerList(){
        this(0, new IntegerArrayBlock());
    }
    public int[] toArray(){
        return IntegerArray.toArray(arrayBlock);
    }
    @Override
    public int get(int i) {
        return arrayBlock.get(i);
    }
    @Override
    public int size() {
        return arrayBlock.size();
    }
    @Override
    public void setSize(int size){
        arrayBlock.setSize(size);
        itemCount.set(size);
    }

    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender == this.itemCount){
            arrayBlock.setSize(itemCount.get());
        }
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
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(": ");
        if(mReference != null){
            builder.append("offset=");
            builder.append(mReference.get());
            builder.append(", ");
        }
        if(arrayBlock.size() != itemCount.get()) {
            builder.append("count=");
            builder.append(itemCount);
            builder.append(", ");
        }
        if(getChildesCount() > 2){
            builder.append(getChildes()[2]);
            builder.append(", ");
        }
        builder.append(IntegerArray.toString(this));
        return builder.toString();
    }
}
