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
import com.reandroid.arsc.chunk.xml.ResXmlIDMap;

public class ResXmlString extends StringItem {
    public ResXmlString(boolean utf8) {
        super(utf8);
    }
    public ResXmlString(boolean utf8, String value) {
        this(utf8);
        set(value);
    }
    public int getResourceId(){
        ResXmlIDMap idMap = getResXmlIDMap();
        if(idMap == null){
            return 0;
        }
        ResXmlID xmlId = idMap.getResXmlID(getIndex());
        if(xmlId == null){
            return 0;
        }
        return xmlId.get();
    }
    private ResXmlIDMap getResXmlIDMap(){
        ResXmlDocument resXmlDocument = getParentInstance(ResXmlDocument.class);
        if(resXmlDocument!=null){
            return resXmlDocument.getResXmlIDMap();
        }
        return null;
    }
    @Override
    void ensureStringLinkUnlocked(){
    }
    @Override
    public int compareTo(StringItem stringItem){
        if(!(stringItem instanceof ResXmlString)){
            return -1;
        }
        ResXmlString xmlString = (ResXmlString) stringItem;
        int id1 = getResourceId();
        int id2 = xmlString.getResourceId();
        if(id1 != 0 && id2 !=0){
            return Long.compare(0xffffffffL & id1, 0xffffffffL & id2);
        }
        if(id1 != 0){
            return -1;
        }
        if(id2 != 0){
            return 1;
        }
        return super.compareTo(stringItem);
    }
}
