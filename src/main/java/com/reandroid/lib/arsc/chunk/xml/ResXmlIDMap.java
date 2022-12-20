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
package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.array.ResXmlIDArray;
import com.reandroid.lib.arsc.chunk.BaseChunk;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.ResXmlID;
import com.reandroid.lib.arsc.pool.ResXmlStringPool;

import java.io.IOException;
import java.util.Collection;

public class ResXmlIDMap extends BaseChunk {
    private final ResXmlIDArray mResXmlIDArray;
    public ResXmlIDMap() {
        super(ChunkType.XML_RESOURCE_MAP, 1);
        this.mResXmlIDArray=new ResXmlIDArray(getHeaderBlock());
        addChild(mResXmlIDArray);
    }
    public ResXmlIDArray getResXmlIDArray(){
        return mResXmlIDArray;
    }

    public Collection<ResXmlID> listResXmlID(){
        return getResXmlIDArray().listItems();
    }
    public void addResourceId(int index, int resId){
        getResXmlIDArray().addResourceId(index, resId);
    }
    public ResXmlID getResXmlID(int ref){
        return getResXmlIDArray().get(ref);
    }
    public ResXmlID getOrCreate(int resId){
        return getResXmlIDArray().getOrCreate(resId);
    }
    public ResXmlID getByResId(int resId){
        return getResXmlIDArray().getByResId(resId);
    }
    @Override
    protected void onChunkRefreshed() {

    }
    ResXmlStringPool getXmlStringPool(){
        Block parent=this;
        while (parent!=null){
            if(parent instanceof ResXmlBlock){
                return ((ResXmlBlock)parent).getStringPool();
            }
            parent=parent.getParent();
        }
        return null;
    }
}
