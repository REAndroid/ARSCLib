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

import com.reandroid.arsc.util.HexUtil;

public class LongItem extends BlockItem{
    private long mCache;
    public LongItem() {
        super(8);
    }
    public void set(long value){
        if(value == mCache){
            return;
        }
        mCache = value;
        putLong(getBytesInternal(), 0, value);
    }
    public long get(){
        return mCache;
    }
    public String toHex(){
        return HexUtil.toHex(get(), 16);
    }

    @Override
    protected void onBytesChanged() {
        mCache = getLong(getBytesInternal(), 0);
    }
    @Override
    public String toString(){
        return String.valueOf(get());
    }
}
