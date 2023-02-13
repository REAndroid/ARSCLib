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

import com.reandroid.apk.XmlHelper;
import com.reandroid.arsc.decoder.ValueDecoder;
import com.reandroid.arsc.value.*;
import com.reandroid.arsc.value.plurals.PluralsQuantity;
import com.reandroid.common.EntryStore;
import com.reandroid.xml.XMLElement;

class XMLPluralsDecoder extends BagDecoder{
    public XMLPluralsDecoder(EntryStore entryStore) {
        super(entryStore);
    }
    @Override
    public void decode(ResTableMapEntry mapEntry, XMLElement parentElement) {
        ResValueMap[] bagItems = mapEntry.listResValueMap();
        int len=bagItems.length;
        EntryStore entryStore=getEntryStore();
        for(int i=0;i<len;i++){
            ResValueMap item = bagItems[i];
            int low = item.getName() & 0xffff;
            PluralsQuantity quantity = PluralsQuantity.valueOf((short) low);
            XMLElement child=new XMLElement("item");
            child.setAttribute("quantity", quantity.toString());

            if(item.getValueType() == ValueType.STRING){
                XmlHelper.setTextContent(child, item.getDataAsPoolString());
            }else {
                String value = ValueDecoder.decodeIntEntry(entryStore, item);
                child.setTextContent(value);
            }

            parentElement.addChild(child);
        }
    }

    @Override
    public boolean canDecode(ResTableMapEntry mapEntry) {
        return isResBagPluralsValue(mapEntry);
    }

    public static boolean isResBagPluralsValue(ResTableMapEntry valueItem){
        int parentId=valueItem.getParentId();
        if(parentId!=0){
            return false;
        }
        ResValueMap[] bagItems = valueItem.listResValueMap();
        if(bagItems==null||bagItems.length==0){
            return false;
        }
        int len=bagItems.length;
        for(int i=0;i<len;i++){
            ResValueMap item=bagItems[i];
            int name = item.getName();
            int high = (name >> 16) & 0xffff;
            if(high!=0x0100){
                return false;
            }
            int low = name & 0xffff;
            PluralsQuantity pq=PluralsQuantity.valueOf((short) low);
            if(pq==null){
                return false;
            }
        }
        return true;
    }
}
