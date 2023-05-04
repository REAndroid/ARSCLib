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

import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.pool.ResXmlStringPool;
import com.reandroid.arsc.util.HexUtil;

import java.util.ArrayList;
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
    public List<ReferenceItem> getReferencedList(){
        return mReferencedList;
    }
    public void addReference(ReferenceItem ref){
        if(ref!=null){
            mReferencedList.add(ref);
        }
    }
    public boolean hasReference(){
        return mReferencedList.size()>0;
    }
    public int getReferenceCount(){
        return mReferencedList.size();
    }
    private void reUpdateReferences(int newIndex){
        for(ReferenceItem ref:mReferencedList){
            ref.set(newIndex);
        }
    }
    @Override
    public void onIndexChanged(int oldIndex, int newIndex){
        //TODO: We have to ignore this to avoid conflict with ResXmlIDMap.removeSafely
    }
    public String getName(){
        ResXmlString xmlString = getResXmlString();
        if(xmlString==null){
            return null;
        }
        return xmlString.getHtml();
    }
    public ResXmlString getResXmlString(){
        ResXmlStringPool stringPool=getXmlStringPool();
        if(stringPool==null){
            return null;
        }
        return stringPool.get(getIndex());
    }
    private ResXmlStringPool getXmlStringPool(){
        ResXmlDocument resXmlDocument = getParentInstance(ResXmlDocument.class);
        if(resXmlDocument!=null){
            return resXmlDocument.getStringPool();
        }
        return null;
    }
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("USED-BY=");
        builder.append(getReferenceCount());
        builder.append('{');
        String name = getName();
        if(name!=null){
            builder.append(name);
        }else {
            builder.append(getIndex());
        }
        builder.append(':');
        builder.append(HexUtil.toHex8(get()));
        builder.append('}');
        return builder.toString();
    }
}
