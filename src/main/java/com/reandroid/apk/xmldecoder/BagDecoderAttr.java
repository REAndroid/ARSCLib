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
package com.reandroid.apk.xmldecoder;

import com.reandroid.apk.XmlHelper;
import com.reandroid.arsc.array.CompoundItemArray;
import com.reandroid.arsc.util.HexUtil;
import com.reandroid.arsc.value.*;
import com.reandroid.arsc.value.attribute.AttributeBag;
import com.reandroid.arsc.value.attribute.AttributeBagItem;
import com.reandroid.common.EntryStore;

import java.io.IOException;

class BagDecoderAttr<OUTPUT> extends BagDecoder<OUTPUT>{
    public BagDecoderAttr(EntryStore entryStore){
        super(entryStore);
    }

    @Override
    public OUTPUT decode(ResTableMapEntry mapEntry, EntryWriter<OUTPUT> writer) throws IOException {
        Entry entry = mapEntry.getParentEntry();
        String tag = XmlHelper.toXMLTagName(entry.getTypeName());
        writer.enableIndent(true);
        writer.startTag(tag);
        writer.attribute("name", entry.getName());
        writeParentAttributes(writer, mapEntry.getValue());
        ResValueMap formatsMap = mapEntry.getByType(AttributeType.FORMATS);

        AttributeDataFormat bagType = AttributeDataFormat.typeOfBag(formatsMap.getData());

        ResValueMap[] bagItems = mapEntry.listResValueMap();


        for(int i = 0; i < bagItems.length; i++){
            ResValueMap item = bagItems[i];
            AttributeType attributeType = item.getAttributeType();
            if(attributeType != null){
                continue;
            }
            writer.enableIndent(true);
            writer.startTag(bagType.getName());

            String name = item.decodeName();
            writer.attribute("name", name);
            int rawVal = item.getData();
            String value;
            if(item.getValueType() == ValueType.INT_HEX){
                value = HexUtil.toHex8(rawVal);
            }else {
                value = Integer.toString(rawVal);
            }
            writer.text(value);

            writer.endTag(bagType.getName());
        }
        return writer.endTag(tag);
    }

    private void writeParentAttributes(EntryWriter<OUTPUT> writer, CompoundItemArray<? extends ResValueMap> itemArray) throws IOException {
        for(ResValueMap valueMap : itemArray.getChildes()){
            AttributeType type = valueMap.getAttributeType();
            if(type == null){
                continue;
            }
            String value;
            if(type == AttributeType.FORMATS){
                value = AttributeDataFormat.toString(
                        AttributeDataFormat.decodeValueTypes(valueMap.getData()));
            }else {
                value = Integer.toString(valueMap.getData());
            }
            if(value == null){
                continue;
            }
            writer.attribute(type.getName(), value);
        }
    }
    @Override
    public boolean canDecode(ResTableMapEntry mapEntry) {
        return AttributeBag.isAttribute(mapEntry);
    }
}
