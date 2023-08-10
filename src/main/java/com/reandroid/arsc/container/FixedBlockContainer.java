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
package com.reandroid.arsc.container;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockContainer;

public class FixedBlockContainer extends BlockContainer<Block> {
    private final Block[] mChildren;
    public FixedBlockContainer(int childrenCount){
        super();
        mChildren=new Block[childrenCount];
    }
    public void addChild(int index, Block block){
        mChildren[index]=block;
        block.setIndex(index);
        block.setParent(this);
    }
    @Override
    protected void onRefreshed(){
    }
    @Override
    public int getChildrenCount() {
        return mChildren.length;
    }
    @Override
    public Block[] getChildren() {
        return mChildren;
    }
}
