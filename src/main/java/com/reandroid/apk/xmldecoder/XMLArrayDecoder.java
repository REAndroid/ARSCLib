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
package com.reandroid.apk.xmldecoder;

import com.reandroid.apk.ApkUtil;
import com.reandroid.apk.XmlHelper;
import com.reandroid.arsc.decoder.ValueDecoder;
import com.reandroid.arsc.value.ResTableMapEntry;
import com.reandroid.arsc.value.ResValueMap;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.common.EntryStore;
import com.reandroid.xml.XMLElement;

import java.util.HashSet;
import java.util.Set;

 class XMLArrayDecoder extends BagDecoder{
    public XMLArrayDecoder(EntryStore entryStore) {
        super(entryStore);
    }

    @Override
    public void decode(ResTableMapEntry mapEntry, XMLElement parentElement) {
        ResValueMap[] bagItems = mapEntry.listResValueMap();
        EntryStore entryStore=getEntryStore();
        Set<ValueType> valueTypes = new HashSet<>();
        for(int i=0;i<bagItems.length;i++){
            ResValueMap bagItem = bagItems[i];
            ValueType valueType = bagItem.getValueType();
            XMLElement child = new XMLElement("item");
            if(valueType == ValueType.STRING){
                XmlHelper.setTextContent(child, bagItem.getDataAsPoolString());
            }else {
                String value = ValueDecoder.decodeIntEntry(entryStore, bagItem);
                child.setTextContent(value);
            }
            parentElement.addChild(child);
            valueTypes.add(valueType);
        }
        if(valueTypes.contains(ValueType.STRING)){
            parentElement.setTagName(ApkUtil.TAG_STRING_ARRAY);
        }else if(valueTypes.size()==1 && valueTypes.contains(ValueType.INT_DEC)){
            parentElement.setTagName(ApkUtil.TAG_INTEGER_ARRAY);
        }
    }
    @Override
    public boolean canDecode(ResTableMapEntry mapEntry) {
        return isArrayValue(mapEntry);
    }
    public static boolean isArrayValue(ResTableMapEntry mapEntry){
        int parentId=mapEntry.getParentId();
        if(parentId!=0){
            return false;
        }
        ResValueMap[] bagItems = mapEntry.listResValueMap();
        if(bagItems==null||bagItems.length==0){
            return false;
        }
        int len=bagItems.length;
        for(int i=0;i<len;i++){
            ResValueMap item=bagItems[i];
            int name = item.getName();
            int high = (name >> 16) & 0xffff;
            if(high!=0x0100 && high!=0x0200){
                return false;
            }
            int low = name & 0xffff;
            int id = low - 1;
            if(id!=i){
                return false;
            }
        }
        return true;
    }
}
