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
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.coder.CommonType;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.value.*;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.xml.XMLAttribute;
import com.reandroid.xml.XMLElement;

import java.util.Iterator;
import java.util.List;

public class XMLValuesEncoderAttr extends XMLValuesEncoderBag{
    public XMLValuesEncoderAttr(TableBlock tableBlock) {
        super(tableBlock);
    }

    @Override
    int getChildesCount(XMLElement element){
        int count = element.getChildElementsCount() + element.getAttributeCount();
        if(element.getAttribute("formats")!=null){
            count = count-1;
        }
        return count;
    }
    @Override
    protected void encodeChildes(XMLElement parentElement, ResTableMapEntry mapEntry){
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
        formatItem.setValueType(ValueType.DEC);

        AttributeType typeFormats = AttributeType.FORMATS;
        formatItem.setAttributeType(typeFormats);

        formatItem.addAttributeTypeFormat(getFlagEnum(parentElement));

        AttributeDataFormat[] formats = AttributeDataFormat.parseValueTypes(
                parentElement.getAttributeValue(typeFormats.getName()));

        formatItem.addAttributeTypeFormats(formats);

        bagIndex++;

        Iterator<? extends XMLAttribute> iterator = parentElement.getAttributes();
        while (iterator.hasNext()){
            XMLAttribute attribute = iterator.next();
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
            EncodeResult encodeResult =
                    ValueCoder.encode(valueString, CommonType.INTEGER.valueTypes());
            if(encodeResult == null){
                throw new EncodeException("Expecting hex or integer value: '" + valueString
                        +"', on attribute: " + name + ", element: "
                        + parentElement.getAttributeValue("name"));
            }
            bagItem.setTypeAndData(encodeResult.valueType, encodeResult.value);
            bagIndex++;
        }
    }
    private void encodeEnumOrFlag(XMLElement element, ResTableMapEntry mapEntry){
        List<XMLElement> childElements = CollectionUtil.toList(element.getElements());
        int count = childElements.size();
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

        PackageBlock packageBlock = mapEntry.getParentEntry().getPackageBlock();
        for(int i = 0; i < count; i++){
            XMLElement child = childElements.get(i);
            AttributeDataFormat bagFormat = AttributeDataFormat.fromBagTypeName(child.getName());
            if(bagFormat != lastBagFormat){
                formatItem.addAttributeTypeFormat(bagFormat);
                lastBagFormat = bagFormat;
            }
            String name = child.getAttributeValue("name");
            int resourceId =  decodeNameResourceId(packageBlock, name);

            ResValueMap valueMap = mapArray.get(i + offset);
            valueMap.setName(resourceId);

            String valueString = child.getTextContent();
            EncodeResult encodeResult = ValueCoder.encode(valueString, bagFormat.valueTypes());
            if(encodeResult == null){
                throw new EncodeException("Expecting hex or integer value: '" + valueString
                        +"', on element: " + child.toText());
            }
            valueMap.setTypeAndData(encodeResult.valueType, encodeResult.value);
        }
    }
    private int decodeNameResourceId(PackageBlock packageBlock, String name){
        EncodeResult unknown = ValueCoder.encodeUnknownResourceId(name);
        int resourceId;
        if(unknown == null){
            int i = name.indexOf(':');
            if(i>0){
                name=name.substring(i+1);
            }
            //TODO: include package name
            resourceId = resolveLocalResourceId(packageBlock, "id", name);
        }else {
            resourceId = unknown.value;
        }
        return resourceId;
    }

    private AttributeDataFormat getFlagEnum(XMLElement parent){
        XMLElement first = CollectionUtil.getFirst(parent.getElements());
        if(first == null){
            return null;
        }
        return AttributeDataFormat.fromBagTypeName(first.getName());
    }
}
