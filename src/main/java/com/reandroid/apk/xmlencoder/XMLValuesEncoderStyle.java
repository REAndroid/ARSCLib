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

import com.reandroid.arsc.array.ResValueMapArray;
import com.reandroid.arsc.decoder.ValueDecoder;
import com.reandroid.arsc.value.*;
import com.reandroid.arsc.value.attribute.AttributeBag;
import com.reandroid.xml.XMLElement;


class XMLValuesEncoderStyle extends XMLValuesEncoderBag{
    XMLValuesEncoderStyle(EncodeMaterials materials) {
        super(materials);
    }
    @Override
    void encodeChildes(XMLElement parentElement, ResTableMapEntry resValueBag){
        int count = parentElement.getChildesCount();
        ResValueMapArray itemArray = resValueBag.getValue();
        for(int i=0;i<count;i++){
            XMLElement child=parentElement.getChildAt(i);
            ResValueMap item = itemArray.get(i);
            String name=child.getAttributeValue("name");
            Integer id = decodeUnknownAttributeHex(name);
            if(id != null){
                item.setName(id);
                String value = child.getTextContent();
                ValueDecoder.EncodeResult encodeResult = ValueDecoder.encodeNullReference(value);
                if(encodeResult!=null){
                    item.setTypeAndData(encodeResult.valueType, encodeResult.value);
                }else if(ValueDecoder.isReference(value)){
                    ValueType valueType;
                    if(value.charAt(0) == '?'){
                        valueType = ValueType.ATTRIBUTE;
                    }else {
                        valueType = ValueType.REFERENCE;
                    }
                    item.setTypeAndData(valueType,
                            getMaterials().resolveReference(value));
                }else {
                    encodeResult = ValueDecoder.encodeGuessAny(value);
                    if(encodeResult!=null){
                        item.setTypeAndData(encodeResult.valueType, encodeResult.value);
                    }else {
                        item.setValueAsString(value);
                    }
                }
                continue;
            }

            Entry attributeEntry=getMaterials()
                    .getAttributeBlock(name);
            if(attributeEntry==null){
                throw new EncodeException("Unknown attribute name: '"+child.toText()
                        +"', for style: "+parentElement.getAttributeValue("name"));
            }
            encodeChild(child, attributeEntry, item);
        }
    }
    private void encodeChild(XMLElement child, Entry attributeEntry, ResValueMap bagItem){

        bagItem.setName(attributeEntry.getResourceId());
        ResTableMapEntry tableEntry = (ResTableMapEntry) attributeEntry.getTableEntry();
        AttributeBag attributeBag=AttributeBag
                .create(tableEntry.getValue());

        String valueText=child.getTextContent();
        ValueDecoder.EncodeResult encodeEnumFlag =
                attributeBag.encodeEnumOrFlagValue(valueText);
        if(encodeEnumFlag!=null){
            bagItem.setTypeAndData(encodeEnumFlag.valueType, encodeEnumFlag.value);
            return;
        }
        ValueDecoder.EncodeResult encodeResult = ValueDecoder.encodeNullReference(valueText);
        if(encodeResult!=null){
            bagItem.setTypeAndData(encodeResult.valueType, encodeResult.value);
            return;
        }
        if(ValueDecoder.isReference(valueText)){
            if(valueText.startsWith("?")){
                bagItem.setValueType(ValueType.ATTRIBUTE);
            }else {
                bagItem.setValueType(ValueType.REFERENCE);
            }
            bagItem.setData(getMaterials().resolveReference(valueText));
        }else if(attributeBag.isEqualType(AttributeDataFormat.STRING)) {
            bagItem.setValueAsString(ValueDecoder
                    .unEscapeSpecialCharacter(valueText));
        }else if(EncodeUtil.isEmpty(valueText)) {
            bagItem.setTypeAndData(ValueType.NULL, 0);
        }else{
            encodeResult = ValueDecoder.encodeGuessAny(valueText);
            if(encodeResult!=null){
                bagItem.setTypeAndData(encodeResult.valueType,
                        encodeResult.value);
            }else {
                bagItem.setValueAsString(ValueDecoder.unEscapeSpecialCharacter(valueText));
            }
        }
    }
}
