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
package com.reandroid.arsc.item;

import com.reandroid.arsc.base.Block;

public class IndirectLong extends IndirectItem<BlockItem> implements LongReference {

    public IndirectLong(BlockItem blockItem, int offset){
        super(blockItem, offset);
    }

    @Override
    public long getLong() {
        return Block.getLong(getBytesInternal(), getOffset());
    }

    @Override
    public void set(long value) {
        Block.putLong(getBytesInternal(), getOffset(), value);
    }
    @Override
    public int get(){
        return (int) getLong();
    }
    @Override
    public void set(int value){
        set((long) value);
    }
    public boolean isNull(){
        return (getBytesInternal().length - getOffset()) < 8;
    }
    @Override
    public String toString(){
        if (isNull()) {
            return "NULL";
        }
        return Long.toString(getLong());
    }
}
