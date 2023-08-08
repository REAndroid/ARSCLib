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

import com.reandroid.utils.HexUtil;

public class ShortItem extends BlockItem implements IntegerReference{
    private int mCache;

    public ShortItem(){
        super(2);
    }
    public ShortItem(short value){
        this();
        set(value);
    }
    @Override
    public void set(int value){
        if(value == mCache){
            return;
        }
        mCache = value;
        byte[] bytes = getBytesInternal();
        bytes[1]= (byte) (value >>> 8 & 0xff);
        bytes[0]= (byte) (value & 0xff);
    }
    @Override
    public int get(){
        return mCache;
    }
    public void set(short value){
        set(0xffff & value);
    }
    public int unsignedInt(){
        return get();
    }
    public short getShort(){
        return (short) mCache;
    }
    public String toHex(){
        return HexUtil.toHex4(getShort());
    }
    @Override
    protected void onBytesChanged() {
        // To save cpu usage, better to calculate once only when bytes changed
        mCache = readShortBytes();
    }
    private int readShortBytes(){
        byte[] bytes = getBytesInternal();
        return (bytes[0] & 0xff | (bytes[1] & 0xff) << 8);
    }
    @Override
    public String toString(){
        return String.valueOf(get());
    }
}
