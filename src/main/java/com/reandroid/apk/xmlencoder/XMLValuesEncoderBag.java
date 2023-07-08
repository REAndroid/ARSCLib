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

import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResTableMapEntry;
import com.reandroid.xml.XMLElement;

public abstract class XMLValuesEncoderBag extends XMLValuesEncoder{
    public XMLValuesEncoderBag(EncodeMaterials materials) {
        super(materials);
    }
    @Override
    public final void encodeValue(Entry entry, XMLElement element){
        if(encodeIfReference(entry, element)){
            return;
        }
        entry.ensureComplex(true);
        ResTableMapEntry tableMapEntry = (ResTableMapEntry) entry.getTableEntry();
        String parent = element.getAttributeValue("parent");
        if(!EncodeUtil.isEmpty(parent)){
            int parentId = getMaterials().resolveReference(parent);
            tableMapEntry.setParentId(parentId);
        }
        tableMapEntry.setValuesCount(getChildesCount(element));
        encodeChildes(element, tableMapEntry);
    }
    private boolean encodeIfReference(Entry entry, XMLElement element){
        if(element.hasChildElements()
                || !element.hasTextContent()
                || element.getAttributeCount() > 1){
            return false;
        }
        String text = element.getTextContent();
        EncodeResult encodeResult = getMaterials().encodeReference(text);
        if(encodeResult != null){
            entry.setValueAsRaw(encodeResult.valueType, encodeResult.value);
            return true;
        }
        return false;
    }
    protected abstract void encodeChildes(XMLElement element, ResTableMapEntry mapEntry);

    int getChildesCount(XMLElement element){
        return element.getChildElementsCount();
    }

}
