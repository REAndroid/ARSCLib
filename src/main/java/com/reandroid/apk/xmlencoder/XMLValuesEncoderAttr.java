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
    void encodeChildes(XMLElement parentElement, ResTableMapEntry mapEntry){
        encodeAttributes(parentElement, mapEntry);
        encodeEnumOrFlag(parentElement, mapEntry);
        EntryHeaderMap header = mapEntry.getHeader();
        boolean is_public = !mapEntry.getParentEntry()
                .getTypeName().contains("private");
        header.setPublic(is_public);
    }
    private void encodeAttributes(XMLElement parentElement, ResTableMapEntry mapEntry){
        ResValueMapArray mapArray = mapEntry.getValue();

        int bagIndex = 0;
        ResValueMap formatItem = mapArray.get(bagIndex);
        formatItem.setValueType(ValueType.INT_DEC);

        AttributeType typeFormats = AttributeType.FORMATS;
        formatItem.setAttributeType(typeFormats);

        formatItem.addAttributeTypeFormat(getFlagEnum(parentElement));

        AttributeDataFormat[] formats = AttributeDataFormat.parseValueTypes(
                parentElement.getAttributeValue(typeFormats.getName()));

        formatItem.addAttributeTypeFormats(formats);

        bagIndex++;

        for(XMLAttribute attribute : parentElement.listAttributes()){
            String name = attribute.getName();
            if("name".equals(name) || "formats".equals(name)){
                continue;
            }
            AttributeType attributeType = AttributeType.fromName(name);
            if(attributeType == null){
                throw new EncodeException("Unknown attribute: '"+name
                        +"', on attribute: " + attribute.toString() + ", element = "
                        + parentElement.getAttributeValue("name"));
            }
            ResValueMap bagItem = mapArray.get(bagIndex);
            bagItem.setAttributeType(attributeType);
            String valueString = attribute.getValue();
            if(!ValueDecoder.isHex(valueString) && !ValueDecoder.isInteger(valueString)){
                throw new EncodeException("Expecting hex or integer value: '" + valueString
                        +"', on attribute: " + name + ", element: "
                        + parentElement.getAttributeValue("name"));
            }
            ValueDecoder.EncodeResult encodeResult =
                    ValueDecoder.encodeHexOrInt(attribute.getValue());
            bagItem.setTypeAndData(encodeResult.valueType, encodeResult.value);
            bagIndex++;
        }
    }
    private void encodeEnumOrFlag(XMLElement element, ResTableMapEntry mapEntry){
        int count = element.getChildesCount();
        if(count == 0){
            return;
        }
        ResValueMapArray mapArray = mapEntry.getValue();

        int offset = element.getAttributeCount();
        if(element.getAttribute(AttributeType.FORMATS.getName()) != null){
            offset = offset - 1;
        }
        ResValueMap formatItem = mapArray.get(0);

        AttributeDataFormat lastBagFormat = AttributeDataFormat.typeOfBag(
                formatItem.getData());

        for(int i = 0; i < count; i++){
            XMLElement child = element.getChildAt(i);
            AttributeDataFormat bagFormat = AttributeDataFormat.fromBagTypeName(child.getTagName());
            if(bagFormat != lastBagFormat){
                formatItem.addAttributeTypeFormat(bagFormat);
                lastBagFormat = bagFormat;
            }
            String name = child.getAttributeValue("name");
            int resourceId =  decodeNameResourceId(name);

            ResValueMap valueMap = mapArray.get(i + offset);
            valueMap.setName(resourceId);

            String valueString = child.getTextContent();

            if(!ValueDecoder.isHex(valueString) && !ValueDecoder.isInteger(valueString)){
                throw new EncodeException("Expecting hex or integer value: '" + valueString
                        +"', on element: " + child.toText());
            }
            ValueDecoder.EncodeResult encodeResult =
                    ValueDecoder.encodeHexOrInt(child.getTextContent());

            valueMap.setTypeAndData(encodeResult.valueType, encodeResult.value);
        }
    }
    private int decodeNameResourceId(String name){
        Integer unknown = decodeUnknownAttributeHex(name);
        int resourceId;
        if(unknown == null){
            resourceId = getMaterials().resolveLocalResourceId("id", name);
        }else {
            resourceId = unknown;
        }
        return resourceId;
    }
    private void encodeEnumOrFlagOld(XMLElement element, ResTableMapEntry mapEntry){
        int count = element.getChildesCount();
        if(count == 0){
            return;
        }
        int offset = element.getAttributeCount();
        if(element.getAttribute("formats")!=null){
            offset = offset-1;
        }

        EncodeMaterials materials = getMaterials();
        ResValueMapArray mapArray = mapEntry.getValue();

        for(int i=0;i<count;i++){
            XMLElement child=element.getChildAt(i);

            String name = child.getAttributeValue("name");
            Integer unknown = decodeUnknownAttributeHex(name);
            int resourceId;
            if(unknown == null){
                resourceId = materials.resolveLocalResourceId("id",
                        name);
            }else {
                resourceId = unknown;
            }

            ValueDecoder.EncodeResult encodeResult =
                    ValueDecoder.encodeHexOrInt(child.getTextContent());

            if(encodeResult == null){
                throw new EncodeException("Unknown value for element '"+child.toText()+"'");
            }

            ResValueMap bagItem = mapArray.get(i+offset);
            bagItem.setName(resourceId);
            bagItem.setValueType(encodeResult.valueType);
            bagItem.setData(encodeResult.value);
        }
    }
    private AttributeDataFormat getFlagEnum(XMLElement parent){
        if(parent.getChildesCount() == 0){
            return null;
        }
        return AttributeDataFormat.fromBagTypeName(
                parent.getChildAt(0).getTagName());
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
    private boolean isFlag(XMLElement parent){
        if(parent.getChildesCount()==0){
            return false;
        }
        String tagName=parent.getChildAt(0).getTagName();
        return "flag".equals(tagName);
    }
    private boolean isEnum(XMLElement parent){
        if(parent.getChildesCount()==0){
            return false;
        }
        String tagName=parent.getChildAt(0).getTagName();
        return "enum".equals(tagName);
    }
}
