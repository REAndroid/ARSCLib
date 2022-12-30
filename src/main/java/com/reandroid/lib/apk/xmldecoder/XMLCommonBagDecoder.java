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

import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.decoder.ValueDecoder;
import com.reandroid.lib.arsc.value.ResValueBag;
import com.reandroid.lib.arsc.value.ResValueBagItem;
import com.reandroid.lib.arsc.value.ValueType;
import com.reandroid.lib.common.EntryStore;
import com.reandroid.xml.XMLElement;

class XMLCommonBagDecoder extends BagDecoder{
    public XMLCommonBagDecoder(EntryStore entryStore) {
        super(entryStore);
    }

    @Override
    public void decode(ResValueBag resValueBag, XMLElement parentElement) {

        PackageBlock currentPackage=resValueBag
                .getEntryBlock().getPackageBlock();

        int parentId = resValueBag.getParentId();
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
        ResValueBagItem[] bagItems = resValueBag.getBagItems();
        EntryStore entryStore = getEntryStore();
        for(int i=0;i< bagItems.length;i++){
            ResValueBagItem item=bagItems[i];
            int resourceId=item.getId();
            XMLElement child=new XMLElement("item");
            String name = ValueDecoder.decodeAttributeName(
                    entryStore, currentPackage, item.getId());

            child.setAttribute("name", name);

            String value = ValueDecoder.decode(entryStore, currentPackageId,
                    resourceId, item.getValueType(), item.getData());

            child.setTextContent(value);

            parentElement.addChild(child);
        }
    }
    @Override
    public boolean canDecode(ResValueBag resValueBag) {
        return resValueBag!=null;
    }
}
