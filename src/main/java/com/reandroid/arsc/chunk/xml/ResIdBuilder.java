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
package com.reandroid.arsc.chunk.xml;

import com.reandroid.arsc.array.ResXmlIDArray;
import com.reandroid.arsc.array.StringArray;
import com.reandroid.arsc.item.ResXmlID;
import com.reandroid.arsc.item.ResXmlString;
import com.reandroid.arsc.pool.ResXmlStringPool;

import java.util.*;

public class ResIdBuilder implements Comparator<Integer> {
    private final Map<Integer, String> mIdNameMap;
    public ResIdBuilder(){
        this.mIdNameMap=new HashMap<>();
    }
    public void buildTo(ResXmlIDMap resXmlIDMap){
        ResXmlStringPool stringPool = resXmlIDMap.getXmlStringPool();
        StringArray<ResXmlString> xmlStringsArray = stringPool.getStringsArray();
        ResXmlIDArray xmlIDArray = resXmlIDMap.getResXmlIDArray();
        List<Integer> idList=getSortedIds();
        int size = idList.size();
        xmlStringsArray.ensureSize(size);
        xmlIDArray.ensureSize(size);
        for(int i=0;i<size;i++){
            ResXmlString xmlString = xmlStringsArray.get(i);
            if(xmlString.hasReference()){
                ResXmlString replaceXmlString=new ResXmlString(xmlString.isUtf8(), xmlString.get());
                xmlStringsArray.setItem(i, replaceXmlString);
                xmlStringsArray.add(xmlString);
                xmlString=replaceXmlString;
            }
            ResXmlID xmlID = xmlIDArray.get(i);
            if(xmlID.hasReference()){
                ResXmlID replaceXmlId = new ResXmlID(xmlID.get());
                xmlIDArray.setItem(i, replaceXmlId);
                xmlIDArray.add(xmlID);
                xmlID=replaceXmlId;
            }
            int resourceId = idList.get(i);
            String name = mIdNameMap.get(resourceId);
            xmlID.set(resourceId);
            xmlString.set(name);
        }
    }
    public void add(int id, String name){
        if(id==0){
            return;
        }
        if(name==null){
            name="";
        }
        mIdNameMap.put(id, name);
    }
    private List<Integer> getSortedIds(){
        List<Integer> results=new ArrayList<>(mIdNameMap.keySet());
        results.sort(this);
        return results;
    }
    @Override
    public int compare(Integer i1, Integer i2) {
        return i1.compareTo(i2);
    }
}
