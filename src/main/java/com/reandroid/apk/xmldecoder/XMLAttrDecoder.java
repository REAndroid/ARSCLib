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

import com.reandroid.arsc.value.ResTableMapEntry;
import com.reandroid.arsc.value.attribute.AttributeBag;
import com.reandroid.arsc.value.attribute.AttributeBagItem;
import com.reandroid.common.EntryStore;
import com.reandroid.xml.XMLElement;

class XMLAttrDecoder extends BagDecoder{
    public XMLAttrDecoder(EntryStore entryStore){
        super(entryStore);
    }
    @Override
    public void decode(ResTableMapEntry mapEntry, XMLElement parentElement){
        AttributeBag attributeBag=AttributeBag.create(mapEntry.getValue());
        decodeParentAttributes(parentElement, attributeBag);

        boolean is_flag=attributeBag.isFlag();
        String tagName=is_flag?"flag":"enum";

        AttributeBagItem[] bagItems = attributeBag.getBagItems();
        EntryStore entryStore=getEntryStore();
        for(int i=0;i< bagItems.length;i++){
            AttributeBagItem item=bagItems[i];
            if(item.isType()){
                continue;
            }
            XMLElement child=new XMLElement(tagName);
            String name = item.getNameOrHex(entryStore);
            child.setAttribute("name", name);
            int rawVal=item.getData();
            String value;
            if(is_flag){
                value=String.format("0x%08x", rawVal);
            }else {
                value=String.valueOf(rawVal);
            }
            child.setTextContent(value);
            parentElement.addChild(child);
        }
    }
    @Override
    public boolean canDecode(ResTableMapEntry mapEntry) {
        return AttributeBag.isAttribute(mapEntry);
    }

    private void decodeParentAttributes(XMLElement element, AttributeBag attributeBag){
        String formats= attributeBag.decodeValueType();
        if(formats!=null){
            element.setAttribute("formats", formats);
        }
        AttributeBagItem boundItem=attributeBag.getMin();
        if(boundItem!=null){
            element.setAttribute("min", boundItem.getBound().toString());
        }
        boundItem=attributeBag.getMax();
        if(boundItem!=null){
            element.setAttribute("max", boundItem.getBound().toString());
        }
        boundItem=attributeBag.getL10N();
        if(boundItem!=null){
            element.setAttribute("l10n", boundItem.getBound().toString());
        }
    }
}
