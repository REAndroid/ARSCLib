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
package com.reandroid.arsc.value;

import com.reandroid.arsc.item.BlockItem;

public class SparseTypeEntry extends BlockItem {
    public SparseTypeEntry() {
        super(4);
    }
    public int getIdx(){
        return getShort(getBytesInternal(), 0) & 0xffff;
    }
    public void setIdx(int idx){
        putShort(getBytesInternal(), 0, (short) idx);
    }
    public int getOffset(){
        return getShort(getBytesInternal(), 2) & 0xffff;
    }
    public void setOffset(int offset){
        putShort(getBytesInternal(), 2, (short) offset);
    }
    @Override
    public String toString(){
        return "idx=" + getIdx() + ", offset=" + getOffset();
    }
}
