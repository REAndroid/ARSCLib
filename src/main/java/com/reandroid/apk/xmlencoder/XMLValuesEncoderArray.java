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

import com.reandroid.apk.ApkUtil;
import com.reandroid.arsc.array.ResValueMapArray;
import com.reandroid.arsc.decoder.ValueDecoder;
import com.reandroid.arsc.value.ResTableMapEntry;
import com.reandroid.arsc.value.ResValueMap;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.xml.XMLElement;

class XMLValuesEncoderArray extends XMLValuesEncoderBag{
    XMLValuesEncoderArray(EncodeMaterials materials) {
        super(materials);
    }
    @Override
    void encodeChildes(XMLElement parentElement, ResTableMapEntry mapEntry){
        int count = parentElement.getChildesCount();
        String tagName = parentElement.getTagName();
        boolean force_string = false;
        boolean force_integer = false;
        if(ApkUtil.TAG_STRING_ARRAY.equals(tagName)){
            force_string = true;
        }else if(ApkUtil.TAG_INTEGER_ARRAY.equals(tagName)){
            force_integer = true;
        }
        EncodeMaterials materials = getMaterials();
        ResValueMapArray itemArray = mapEntry.getValue();
        for(int i=0;i<count;i++){
            XMLElement child=parentElement.getChildAt(i);

            ResValueMap bagItem = itemArray.get(i);
            String name = child.getAttributeValue("name");
            if(name == null){
                bagItem.setName(0x01000001 + i);
            }else {
                Integer unknown = decodeUnknownAttributeHex(name);
                int resourceId;
                if(unknown == null){
                    resourceId = materials.resolveLocalResourceId("id", name);
                }else {
                    resourceId = unknown;
                }
                bagItem.setName(resourceId);
            }


            String valueText=child.getTextContent();
            if(ValueDecoder.isReference(valueText)){
                ValueType valueType;
                if(valueText.charAt(0) == '?'){
                    valueType = ValueType.ATTRIBUTE;
                }else {
                    valueType = ValueType.REFERENCE;
                }
                bagItem.setTypeAndData(valueType,
                        getMaterials().resolveReference(valueText));
            }else if(force_string){
                bagItem.setValueAsString(ValueDecoder
                        .unEscapeSpecialCharacter(valueText));
            }else if(force_integer){
                valueText=trimText(valueText);
                if(!ValueDecoder.isInteger(valueText)){
                    throw new EncodeException("Invalid integer value for array name="
                            +parentElement.getAttributeValue("name")
                            +", entry no"+(i+1)+", near line: " + child.getLineNumber());
                }
                bagItem.setTypeAndData(ValueType.INT_DEC,
                        ValueDecoder.parseInteger(valueText));
            }else if(EncodeUtil.isEmpty(valueText)) {
                bagItem.setTypeAndData(ValueType.NULL, 0);
            }else {
                ValueDecoder.EncodeResult encodeResult =
                        ValueDecoder.encodeGuessAny(valueText);
                if(encodeResult!=null){
                    bagItem.setTypeAndData(encodeResult.valueType,
                            encodeResult.value);
                }else {
                    bagItem.setValueAsString(ValueDecoder
                            .unEscapeSpecialCharacter(valueText));
                }
            }
        }
    }
    private static String trimText(String text){
        if(text==null){
            return null;
        }
        return text.trim();
    }
}
