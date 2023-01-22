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
import com.reandroid.arsc.io.BlockLoad;
import com.reandroid.arsc.io.BlockReader;

import java.io.IOException;

public class SpecFlagsArray extends IntegerArray implements BlockLoad {
    private final IntegerItem entryCount;
    public SpecFlagsArray(IntegerItem entryCount) {
        super();
        this.entryCount = entryCount;
        this.entryCount.setBlockLoad(this);
        setBlockLoad(this);
    }
    public void set(int entryId, int value){
        entryId = 0xffff & entryId;
        ensureArraySize(entryId);
        super.put(entryId, value);
        refresh();
    }
    @Override
    public Integer get(int entryId){
        entryId = 0xffff & entryId;
        return super.get(entryId);
    }
    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender==this.entryCount){
            super.setSize(entryCount.get());
        }
    }
    public void refresh(){
        entryCount.set(size());
    }
}
