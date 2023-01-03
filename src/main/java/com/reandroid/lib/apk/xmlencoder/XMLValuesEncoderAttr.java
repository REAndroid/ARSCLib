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
import com.reandroid.lib.arsc.value.ResValueBag;
import com.reandroid.lib.arsc.value.ResValueBagItem;
import com.reandroid.lib.arsc.value.ValueType;
import com.reandroid.lib.arsc.value.attribute.AttributeBag;
import com.reandroid.lib.arsc.value.attribute.AttributeItemType;
import com.reandroid.lib.arsc.value.attribute.AttributeValueType;
import com.reandroid.xml.XMLAttribute;
import com.reandroid.xml.XMLElement;

class XMLValuesEncoderAttr extends XMLValuesEncoderBag{
    XMLValuesEncoderAttr(EncodeMaterials materials) {
        super(materials);
    }
    @Override
    int getChildesCount(XMLElement element){
        int count = element.getChildesCount() + element.getAttributeCount();
        if(element.getAttribute("formats")!=null){
            count = count-1;
        }
        return count;
    }
    @Override
    void encodeChildes(XMLElement parentElement, ResValueBag resValueBag){
        encodeAttributes(parentElement, resValueBag);
        encodeEnumOrFlag(parentElement, resValueBag);
        // TODO: re-check if this is necessary
        resValueBag.getEntryBlock().setEntryTypeShared(true);
    }
    private void encodeAttributes(XMLElement parentElement, ResValueBag resValueBag){
        int count=parentElement.getAttributeCount();
        ResValueBagItemArray bagItemArray = resValueBag.getResValueBagItemArray();

        int bagIndex=0;

        ResValueBagItem formatItem = bagItemArray.get(bagIndex);

        formatItem.setIdHigh((short) 0x0100);
        formatItem.setIdLow(AttributeItemType.FORMAT.getValue());
        formatItem.setType(ValueType.INT_DEC);
        formatItem.setDataHigh(getChildTypes(parentElement));

        AttributeValueType[] valueTypes = AttributeValueType
                .valuesOf(parentElement.getAttributeValue("formats"));

        formatItem.setDataLow((short) (0xffff &
                AttributeValueType.getByte(valueTypes)));

        bagIndex++;

        for(int i=0;i<count;i++){
            XMLAttribute attribute = parentElement.getAttributeAt(i);
            String name = attribute.getName();
            if("name".equals(name) || "formats".equals(name)){
                continue;
            }
            AttributeItemType itemType = AttributeItemType.fromName(name);
            if(itemType==null){
                throw new EncodeException("Unknown attribute: '"+name
                        +"', on attribute: "+attribute.toString());
            }
            ResValueBagItem bagItem = bagItemArray.get(bagIndex);
            bagItem.setIdHigh((short) 0x0100);
            bagItem.setIdLow(itemType.getValue());
            bagItem.setType(ValueType.INT_DEC);
            bagItem.setData(ValueDecoder.parseInteger(attribute.getValue()));
            bagIndex++;
        }
    }
    private void encodeEnumOrFlag(XMLElement element, ResValueBag resValueBag){
        int count=element.getChildesCount();
        if(count==0){
            return;
        }
        int offset = element.getAttributeCount();
        if(element.getAttribute("formats")!=null){
            offset = offset-1;
        }

        EncodeMaterials materials = getMaterials();
        ResValueBagItemArray bagItemArray = resValueBag.getResValueBagItemArray();

        for(int i=0;i<count;i++){
            XMLElement child=element.getChildAt(i);

            int resourceId=materials.resolveLocalResourceId("id",
                    child.getAttributeValue("name"));

            ValueDecoder.EncodeResult encodeResult =
                    ValueDecoder.encodeHexOrInt(child.getTextContent());

            if(encodeResult == null){
                throw new EncodeException("Unknown value for element '"+child.toText()+"'");
            }

            ResValueBagItem bagItem = bagItemArray.get(i+offset);
            bagItem.setId(resourceId);
            bagItem.setType(encodeResult.valueType);
            bagItem.setData(encodeResult.value);
        }
    }
    private short getChildTypes(XMLElement parent){
        if(parent.getChildesCount()==0){
            return 0;
        }
        String tagName=parent.getChildAt(0).getTagName();
        if("enum".equals(tagName)){
            return AttributeBag.TYPE_ENUM;
        }
        if("flag".equals(tagName)){
            return AttributeBag.TYPE_FLAG;
        }
        return 0;
    }
}
