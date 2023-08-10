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
import com.reandroid.arsc.io.BlockLoad;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerArray;
import com.reandroid.arsc.item.IntegerReference;

import java.io.IOException;

public class AnnotationItemArray extends BlockArray<AnnotationItem> implements BlockLoad {
    private final IntegerReference itemCount;
    private final IntegerArray offsetsArray;
    public AnnotationItemArray(IntegerReference itemCount, IntegerArray offsetsArray) {
        super();
        this.itemCount = itemCount;
        this.offsetsArray = offsetsArray;
        if(itemCount instanceof Block){
            ((Block) itemCount).setBlockLoad(this);
        }
    }
    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender == itemCount){
            int count = itemCount.get();
            offsetsArray.setSize(count);
            setChildrenCount(count);
        }
    }
    @Override
    protected void onRefreshed() {
        itemCount.set(childrenCount());
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int[] offsets = offsetsArray.toArray();
        AnnotationItem[] children = getChildren();
        int count = offsets.length;
        for(int i = 0; i < count; i++){
            int offset = offsets[i];
            AnnotationItem item = children[i];
            if(offset == 0){
                item.setNull(true);
                continue;
            }
            reader.seek(offset);
            item.readBytes(reader);
        }
    }

    @Override
    public AnnotationItem[] newInstance(int length) {
        return new AnnotationItem[length];
    }
    @Override
    public AnnotationItem newInstance() {
        return new AnnotationItem();
    }
}
