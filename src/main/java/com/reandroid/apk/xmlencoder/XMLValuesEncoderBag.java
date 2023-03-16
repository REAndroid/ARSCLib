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
package com.reandroid.apk.xmlencoder;

import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResTableMapEntry;
import com.reandroid.xml.XMLElement;

class XMLValuesEncoderBag extends XMLValuesEncoder{
    XMLValuesEncoderBag(EncodeMaterials materials) {
        super(materials);
    }
    @Override
    void encodeValue(Entry entry, XMLElement element){
        ResTableMapEntry tableMapEntry = new ResTableMapEntry();
        entry.setTableEntry(tableMapEntry);
        String parent=element.getAttributeValue("parent");
        if(!EncodeUtil.isEmpty(parent)){
            int parentId=getMaterials().resolveReference(parent);
            tableMapEntry.getHeader().setParentId(parentId);
        }
        tableMapEntry.getValue().setChildesCount(getChildesCount(element));
        encodeChildes(element, tableMapEntry);
        tableMapEntry.refresh();
    }
    void encodeChildes(XMLElement element, ResTableMapEntry mapEntry){
        throw new EncodeException("Unimplemented bag type encoder: "
                +element.getTagName());

    }
    int getChildesCount(XMLElement element){
        return element.getChildesCount();
    }

    @Override
    void encodeNullValue(Entry entry){
        // Nothing to do
    }
}
