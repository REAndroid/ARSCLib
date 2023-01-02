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
package com.reandroid.lib.apk.xmlencoder;

import com.reandroid.lib.arsc.array.ResValueBagItemArray;
import com.reandroid.lib.arsc.decoder.ValueDecoder;
import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.arsc.value.ResValueBag;
import com.reandroid.lib.arsc.value.ResValueBagItem;
import com.reandroid.lib.arsc.value.ValueType;
import com.reandroid.lib.arsc.value.attribute.AttributeBag;
import com.reandroid.xml.XMLElement;


public class XMLValuesEncoderStyle extends XMLValuesEncoderBag{
    XMLValuesEncoderStyle(EncodeMaterials materials) {
        super(materials);
    }
    @Override
    void encodeChildes(XMLElement parentElement, ResValueBag resValueBag){
        int count = parentElement.getChildesCount();
        ResValueBagItemArray itemArray = resValueBag.getResValueBagItemArray();
        EncodeMaterials materials=getMaterials();
        for(int i=0;i<count;i++){
            XMLElement child=parentElement.getChildAt(i);
            ResValueBagItem bagItem = itemArray.get(i);
            EntryBlock entryBlock=materials
                    .getAttributeBlock(child.getAttributeValue("name"));
            if(entryBlock==null){
                throw new EncodeException("Unknown attribute name: '"+child.toText()+"'");
            }
            bagItem.setId(entryBlock.getResourceId());
            AttributeBag attributeBag=AttributeBag
                    .create((ResValueBag) entryBlock.getResValue());

            String valueText=child.getTextContent();
            ValueDecoder.EncodeResult encodeResult =
                    attributeBag.encodeName(valueText);
            if(encodeResult!=null){
                bagItem.setType(encodeResult.valueType);
                bagItem.setData(encodeResult.value);
                continue;
            }
            if(ValueDecoder.isReference(valueText)){
                bagItem.setType(ValueType.REFERENCE);
                bagItem.setData(getMaterials().resolveReference(valueText));
            }else if(EncodeUtil.isEmpty(valueText)) {
                bagItem.setType(ValueType.NULL);
                bagItem.setData(0);
            }else{
                encodeResult=ValueDecoder.encodeGuessAny(valueText);
                if(encodeResult!=null){
                    bagItem.setType(encodeResult.valueType);
                    bagItem.setData(encodeResult.value);
                }else {
                    bagItem.setValueAsString(valueText);
                }
            }
        }
    }
}
