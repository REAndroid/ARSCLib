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
import com.reandroid.arsc.coder.CoderInteger;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.coder.XmlSanitizer;
import com.reandroid.arsc.value.ResTableMapEntry;
import com.reandroid.arsc.value.ResValueMap;
import com.reandroid.xml.XMLElement;

import java.util.Iterator;

public class XMLValuesEncoderArray extends XMLValuesEncoderBag{
    public XMLValuesEncoderArray(EncodeMaterials materials) {
        super(materials);
    }
    @Override
    protected void encodeChildes(XMLElement parentElement, ResTableMapEntry mapEntry){
        int count = parentElement.getChildElementsCount();
        String tagName = parentElement.getName();
        boolean force_string = false;
        boolean force_integer = false;
        if(ApkUtil.TAG_STRING_ARRAY.equals(tagName)){
            force_string = true;
        }else if(ApkUtil.TAG_INTEGER_ARRAY.equals(tagName)){
            force_integer = true;
        }
        EncodeMaterials materials = getMaterials();
        ResValueMapArray itemArray = mapEntry.getValue();
        Iterator<? extends XMLElement> iterator = parentElement.getElements();
        int i = -1;
        //TODO: -1 ?
        while (iterator.hasNext()){
            i++;
            XMLElement child = iterator.next();
            ResValueMap bagItem = itemArray.get(i);
            String name = child.getAttributeValue("name");
            if(name == null){
                bagItem.setName(0x01000001 + i);
            }else {
                EncodeResult unknown = ValueCoder.encodeUnknownResourceId(name);
                int resourceId;
                if(unknown == null){
                    resourceId = materials.resolveLocalResourceId("id", name);
                }else {
                    resourceId = unknown.value;
                }
                bagItem.setName(resourceId);
            }

            String valueText = child.getTextContent();
            EncodeResult encodeResult = getMaterials().encodeReference(valueText);
            if(encodeResult != null){
                bagItem.setTypeAndData(encodeResult.valueType, encodeResult.value);
                continue;
            }
            if(force_string){
                bagItem.setValueAsString(XmlSanitizer
                        .unEscapeUnQuote(valueText));
            }else if(force_integer){
                valueText = trimText(valueText);
                encodeResult = CoderInteger.INS.encode(valueText);
                if(encodeResult == null){
                    throw new EncodeException("Invalid integer value for array name="
                            +parentElement.getAttributeValue("name")
                            +", entry no"+(i+1)+", near line: " + child.getLineNumber());
                }
                bagItem.setTypeAndData(encodeResult.valueType, encodeResult.value);
            }else if(EncodeUtil.isEmpty(valueText)) {
                bagItem.setValueAsString("");
            }else {
                encodeResult = ValueCoder.encode(valueText);
                if(encodeResult!=null){
                    bagItem.setTypeAndData(encodeResult.valueType,
                            encodeResult.value);
                }else {
                    bagItem.setValueAsString(XmlSanitizer
                            .unEscapeUnQuote(valueText));
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
