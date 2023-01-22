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
import com.reandroid.arsc.value.ResValueBag;
import com.reandroid.arsc.value.ResValueBagItem;
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
    public void decode(ResValueBag resValueBag, XMLElement parentElement) {
        ResValueBagItem[] bagItems = resValueBag.getBagItems();
        EntryStore entryStore=getEntryStore();
        Set<ValueType> valueTypes = new HashSet<>();
        for(int i=0;i<bagItems.length;i++){
            ResValueBagItem bagItem = bagItems[i];
            ValueType valueType = bagItem.getValueType();
            XMLElement child = new XMLElement("item");
            if(valueType == ValueType.STRING){
                XmlHelper.setTextContent(child, bagItem.getValueAsPoolString());
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
    public boolean canDecode(ResValueBag resValueBag) {
        return isResBagArrayValue(resValueBag);
    }
    public static boolean isResBagArrayValue(ResValueBag resValueBag){
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
            if(item.getIdHigh()!=0x0100 && item.getIdHigh()!=0x0200){
                return false;
            }
            int id=item.getIdLow()-1;
            if(id!=i){
                return false;
            }
        }
        return true;
    }
}
