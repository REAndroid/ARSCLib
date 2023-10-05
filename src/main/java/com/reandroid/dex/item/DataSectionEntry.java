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
import com.reandroid.arsc.base.BlockArray;
import com.reandroid.arsc.base.OffsetSupplier;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.*;
import com.reandroid.dex.index.ClassId;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyItem;

import java.io.IOException;

public class DataSectionEntry extends DexContainerItem
        implements PositionedItem, OffsetSupplier, OffsetReceiver, KeyItem, UsageMarker{

    private IntegerReference mReference;

    private ClassId mUserClassId;
    private boolean mShared;
    private int mUsageType;

    public DataSectionEntry(int childesCount) {
        super(childesCount);
    }


    public void copyFrom(DataSectionEntry item){
        if(item == null){
            return;
        }
        BlockReader reader = new BlockReader(item.getBytes());
        try {
            this.readBytes(reader);
        } catch (IOException ignored) {
        }
    }
    public boolean isSharedUsage() {
        return mShared;
    }
    public void addUsage(ClassId classId){
        if(classId == null || mShared || classId == mUserClassId){
            return;
        }
        if(mUserClassId != null){
            mShared = true;
        }else {
            mUserClassId = classId;
        }
    }
    public void removeSelf() {
        Block parent = getParent();
        if(!(parent instanceof DexItemArray<?>)){
            return;
        }
        BlockArray<DataSectionEntry> itemArray = (BlockArray<DataSectionEntry>) parent;
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
    public int getOffset(){
        IntegerReference reference = getOffsetReference();
        if(reference != null){
            return reference.get();
        }
        return 0;
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
    public int getUsageType() {
        return mUsageType;
    }
    @Override
    public void addUsageType(int usage){
        this.mUsageType |= usage;
    }
    @Override
    public boolean containsUsage(int usage){
        if(usage == 0){
            return this.mUsageType == 0;
        }
        return (this.mUsageType & usage) == usage;
    }
    @Override
    public void clearUsageType(){
        this.mUsageType = USAGE_NONE;
    }
    public void removeLastAlign(){

    }
}
