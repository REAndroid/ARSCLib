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
import com.reandroid.lib.arsc.value.attribute.AttributeValueType;
import com.reandroid.xml.XMLElement;


class XMLValuesEncoderStyle extends XMLValuesEncoderBag{
    XMLValuesEncoderStyle(EncodeMaterials materials) {
        super(materials);
    }
    @Override
    void encodeChildes(XMLElement parentElement, ResValueBag resValueBag){
        int count = parentElement.getChildesCount();
        ResValueBagItemArray itemArray = resValueBag.getResValueBagItemArray();
        for(int i=0;i<count;i++){
            XMLElement child=parentElement.getChildAt(i);
            EntryBlock attributeEntry=getMaterials()
                    .getAttributeBlock(child.getAttributeValue("name"));
            if(attributeEntry==null){
                throw new EncodeException("Unknown attribute name: '"+child.toText()
                        +"', for style: "+parentElement.getAttributeValue("name"));
            }
            encodeChild(parentElement.getChildAt(i), attributeEntry, itemArray.get(i));
        }
    }
    private void encodeChild(XMLElement child, EntryBlock attributeEntry, ResValueBagItem bagItem){

        bagItem.setId(attributeEntry.getResourceId());
        AttributeBag attributeBag=AttributeBag
                .create((ResValueBag) attributeEntry.getResValue());

        String valueText=child.getTextContent();
        ValueDecoder.EncodeResult encodeEnumFlag =
                attributeBag.encodeEnumOrFlagValue(valueText);
        if(encodeEnumFlag!=null){
            bagItem.setTypeAndData(encodeEnumFlag.valueType, encodeEnumFlag.value);
            return;
        }
        if(ValueDecoder.isReference(valueText)){
            if(valueText.startsWith("?")){
                bagItem.setType(ValueType.ATTRIBUTE);
            }else {
                bagItem.setType(ValueType.REFERENCE);
            }
            bagItem.setData(getMaterials().resolveReference(valueText));
        }else if(attributeBag.contains(AttributeValueType.STRING)) {
            bagItem.setValueAsString(valueText);
        }else if(EncodeUtil.isEmpty(valueText)) {
            bagItem.setTypeAndData(ValueType.NULL, 0);
        }else{
            ValueDecoder.EncodeResult encodeResult = ValueDecoder.encodeGuessAny(valueText);
            if(encodeResult!=null){
                bagItem.setTypeAndData(encodeResult.valueType,
                        encodeResult.value);
            }else {
                bagItem.setValueAsString(valueText);
            }
        }
    }
}
