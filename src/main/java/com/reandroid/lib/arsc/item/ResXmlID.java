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

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.chunk.xml.ResXmlBlock;
import com.reandroid.lib.arsc.pool.ResXmlStringPool;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ResXmlID extends IntegerItem {
    private final List<ReferenceItem> mReferencedList;
    public ResXmlID(int resId){
        super(resId);
        this.mReferencedList=new ArrayList<>();
    }
    public ResXmlID(){
        this(0);
    }
    public boolean removeReference(ReferenceItem ref){
        return mReferencedList.remove(ref);
    }
    public boolean removeAllReference(Collection<ReferenceItem> referenceItems){
        return mReferencedList.removeAll(referenceItems);
    }
    public void removeAllReference(){
        mReferencedList.clear();
    }
    public List<ReferenceItem> getReferencedList(){
        return mReferencedList;
    }
    public void addReference(ReferenceItem ref){
        if(ref!=null){
            mReferencedList.add(ref);
        }
    }
    public void addReference(Collection<ReferenceItem> refList){
        if(refList==null){
            return;
        }
        for(ReferenceItem ref:refList){
            addReference(ref);
        }
    }
    private void reUpdateReferences(int newIndex){
        for(ReferenceItem ref:mReferencedList){
            ref.set(newIndex);
        }
    }
    @Override
    public void onIndexChanged(int oldIndex, int newIndex){
        reUpdateReferences(newIndex);
    }
    public String getName(){
        ResXmlStringPool stringPool=getXmlStringPool();
        if(stringPool==null){
            return null;
        }
        ResXmlString xmlString = stringPool.get(getIndex());
        if(xmlString==null){
            return null;
        }
        return xmlString.getHtml();
    }
    private ResXmlStringPool getXmlStringPool(){
        Block parent=this;
        while (parent!=null){
            if(parent instanceof ResXmlBlock){
                return ((ResXmlBlock)parent).getStringPool();
            }
            parent=parent.getParent();
        }
        return null;
    }
    @Override
    public String toString(){
        return getIndex()+": "+String.format("0x%08x", get());
    }
}
