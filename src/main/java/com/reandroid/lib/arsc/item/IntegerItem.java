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
package com.reandroid.lib.arsc.item;


public class IntegerItem extends BlockItem implements ReferenceItem{
    private int mCache;
    public IntegerItem(){
        super(4);
    }
    public IntegerItem(int val){
        this();
        set(val);
    }
    @Override
    public void set(int val){
        if(val==mCache){
            return;
        }
        mCache=val;
        byte[] bts = getBytesInternal();
        bts[3]= (byte) (val >>> 24 & 0xff);
        bts[2]= (byte) (val >>> 16 & 0xff);
        bts[1]= (byte) (val >>> 8 & 0xff);
        bts[0]= (byte) (val & 0xff);
    }
    @Override
    public int get(){
        return mCache;
    }



    @Override
    public void onBytesChanged() {
        mCache=readIntBytes();
    }
    private int readIntBytes(){
        byte[] bts = getBytesInternal();
        return bts[0] & 0xff |
                (bts[1] & 0xff) << 8 |
                (bts[2] & 0xff) << 16 |
                (bts[3] & 0xff) << 24;
    }
    @Override
    public String toString(){
        return String.valueOf(get());
    }

}
