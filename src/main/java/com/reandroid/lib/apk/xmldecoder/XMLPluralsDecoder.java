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
package com.reandroid.lib.apk.xmldecoder;

import com.reandroid.lib.arsc.decoder.ValueDecoder;
import com.reandroid.lib.arsc.value.BaseResValue;
import com.reandroid.lib.arsc.value.ResValueBag;
import com.reandroid.lib.arsc.value.ResValueBagItem;
import com.reandroid.lib.arsc.value.plurals.PluralsQuantity;
import com.reandroid.lib.common.EntryStore;
import com.reandroid.xml.XMLElement;

class XMLPluralsDecoder extends BagDecoder{
    public XMLPluralsDecoder(EntryStore entryStore) {
        super(entryStore);
    }
    @Override
    public void decode(ResValueBag resValueBag, XMLElement parentElement) {
        ResValueBagItem[] bagItems = resValueBag.getBagItems();
        int len=bagItems.length;
        EntryStore entryStore=getEntryStore();
        for(int i=0;i<len;i++){
            ResValueBagItem item = bagItems[i];

            PluralsQuantity quantity = PluralsQuantity.valueOf(item.getIdLow());

            String value = ValueDecoder.decodeIntEntry(entryStore, item);
            XMLElement child=new XMLElement("item");
            child.setAttribute("quantity", quantity.toString());
            child.setTextContent(value);

            parentElement.addChild(child);
        }
    }

    @Override
    public boolean canDecode(ResValueBag resValueBag) {
        return isResBagPluralsValue(resValueBag);
    }

    public static boolean isResBagPluralsValue(BaseResValue baseResValue){
        ResValueBag resValueBag=(ResValueBag)baseResValue;
        int parentId=resValueBag.getParentId();
        if(parentId!=0){
            return false;
        }
        ResValueBagItem[] bagItems = resValueBag.getBagItems();
        if(bagItems==null||bagItems.length==0){
            return false;
        }
        int len=bagItems.length;
        for(int i=0;i<len;i++){
            ResValueBagItem item=bagItems[i];
            if(item.getIdHigh()!=0x0100){
                return false;
            }
            PluralsQuantity pq=PluralsQuantity.valueOf(item.getIdLow());
            if(pq==null){
                return false;
            }
        }
        return true;
    }
}
