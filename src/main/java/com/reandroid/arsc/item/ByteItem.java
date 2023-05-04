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

 public class ByteItem extends BlockItem {
    public ByteItem() {
        super(1);
    }
    public boolean getBit(int index){
        return ((get()>>index) & 0x1) == 1;
    }
    public void putBit(int index, boolean bit){
        int val=get();
        int left=val>>index;
        if(bit){
            left=left|0x1;
        }else {
            left=left & 0xFE;
        }
        left=left<<index;
        index=8-index;
        int right=(0xFF>>index) & val;
        val=left|right;
        set((byte) val);
    }
    public void set(byte b){
        getBytesInternal()[0]=b;
    }
    public byte get(){
        return getBytesInternal()[0];
    }
    public int unsignedInt(){
        return 0xff & get();
    }
    public String toHex(){
        return HexUtil.toHex2(get());
    }
    @Override
    public String toString(){
        return String.valueOf(get());
    }
}
