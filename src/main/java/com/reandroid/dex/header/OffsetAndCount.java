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
package com.reandroid.dex.header;

import com.reandroid.arsc.item.ByteArray;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.IndirectInteger;

public class OffsetAndCount extends ByteArray {
    private final IntegerReference mCountReference;
    private final IntegerReference mOffsetReference;
    public OffsetAndCount(){
        super(8);
        this.mCountReference = new IndirectInteger(this, 0);
        this.mOffsetReference = new IndirectInteger(this, 4);
    }
    public int getCount(){
        return getInteger(0);
    }
    public void setCount(int count){
        putInteger(0, count);
    }
    public int getOffset(){
        return getInteger(4);
    }
    public void setOffset(int offset){
        putInteger(4, offset);
    }

    public IntegerReference getCountReference() {
        return mCountReference;
    }
    public IntegerReference getOffsetReference() {
        return mOffsetReference;
    }

    @Override
    public String toString(){
        return "(O " + getOffset() + ":" + getCount() + ")";
    }
}
