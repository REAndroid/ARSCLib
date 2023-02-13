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
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.decoder.ValueDecoder;
import com.reandroid.arsc.value.ResTableMapEntry;
import com.reandroid.arsc.value.ResValueMap;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.common.EntryStore;
import com.reandroid.xml.XMLElement;

class XMLCommonBagDecoder extends BagDecoder{
    public XMLCommonBagDecoder(EntryStore entryStore) {
        super(entryStore);
    }

    @Override
    public void decode(ResTableMapEntry mapEntry, XMLElement parentElement) {

        PackageBlock currentPackage= mapEntry
                .getParentEntry().getPackageBlock();

        int parentId = mapEntry.getParentId();
        String parent;
        if(parentId!=0){
            parent = ValueDecoder.decodeEntryValue(getEntryStore(),
                    currentPackage, ValueType.REFERENCE, parentId);
        }else {
            parent=null;
        }
        if(parent!=null){
            parentElement.setAttribute("parent", parent);
        }
        int currentPackageId=currentPackage.getId();
        ResValueMap[] bagItems = mapEntry.listResValueMap();
        EntryStore entryStore = getEntryStore();
        for(int i=0;i< bagItems.length;i++){
            ResValueMap item=bagItems[i];
            int resourceId=item.getName();
            XMLElement child=new XMLElement("item");
            String name = ValueDecoder.decodeAttributeName(
                    entryStore, currentPackage, item.getName());

            child.setAttribute("name", name);

            ValueType valueType = item.getValueType();
            if(valueType == ValueType.STRING){
                XmlHelper.setTextContent(child, item.getDataAsPoolString());
            }else {
                String value = ValueDecoder.decode(entryStore, currentPackageId,
                        resourceId, item.getValueType(), item.getData());
                child.setTextContent(value);
            }
            parentElement.addChild(child);
        }
    }
    @Override
    public boolean canDecode(ResTableMapEntry mapEntry) {
        return mapEntry !=null;
    }
}
