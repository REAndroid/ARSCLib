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
package com.reandroid.arsc.array;

import com.reandroid.arsc.item.ShortArrayBlock;

public class ShortOffsetArray extends ShortArrayBlock implements OffsetArray {
    public ShortOffsetArray(){
        super();
    }
    @Override
    public int getOffset(int i){
        int offset = super.get(i);
        if(offset == 0xffff){
            offset = NO_ENTRY;
        }else {
            offset = offset * 4;
        }
        return offset;
    }

    @Override
    public void setOffset(int index, int value){
        if(value != NO_ENTRY){
            value = value / 4;
        }
        super.put(index, value);
    }
    @Override
    public int[] getOffsets(){
        int length = size();
        int[] result = new int[length];
        for(int i = 0; i < length; i++){
            result[i] = getOffset(i);
        }
        return result;
    }
    @Override
    public void clear(){
        super.setSize(0);
    }
}
