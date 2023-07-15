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
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.coder.XmlSanitizer;
import com.reandroid.arsc.value.*;
import com.reandroid.arsc.value.attribute.AttributeBag;
import com.reandroid.xml.XMLElement;

import java.util.List;

public class XMLValuesEncoderStyle extends XMLValuesEncoderBag{
    public XMLValuesEncoderStyle(TableBlock tableBlock) {
        super(tableBlock);
    }
    @Override
    protected void encodeChildes(XMLElement parentElement, ResTableMapEntry resValueBag){
        List<XMLElement> childElementList = parentElement.getChildElementList();
        int count = childElementList.size();
        ResValueMapArray itemArray = resValueBag.getValue();
        for(int i=0;i<count;i++){
            XMLElement child = childElementList.get(i);
            ResValueMap item = itemArray.get(i);
            assert item != null;
            EncodeResult encodeResult = item.encodeStyle(child);
            if(encodeResult.isError()){
                throw new EncodeException(encodeResult.getError() + " '"+child.getDebugText()
                        +"', for style: "+parentElement.getAttributeValue("name"));
            }
        }
    }
    private void encodeChild(XMLElement child, Entry attributeEntry, ResValueMap bagItem){

        PackageBlock packageBlock = bagItem.getPackageBlock();
        bagItem.setName(attributeEntry.getResourceId());
        ResTableMapEntry tableEntry = (ResTableMapEntry) attributeEntry.getTableEntry();
        AttributeBag attributeBag = AttributeBag.create(tableEntry.getValue());

        String valueText = child.getTextContent();
        EncodeResult encodeEnumFlag =
                attributeBag.encodeEnumOrFlagValue(valueText);
        if(encodeEnumFlag != null){
            bagItem.setTypeAndData(encodeEnumFlag.valueType, encodeEnumFlag.value);
            return;
        }
        EncodeResult encodeResult = encodeReference(packageBlock, valueText);
        if(encodeResult != null){
            bagItem.setTypeAndData(encodeResult.valueType, encodeResult.value);
            return;
        }
        if(attributeBag.isEqualType(AttributeDataFormat.STRING)) {
            bagItem.setValueAsString(XmlSanitizer
                    .unEscapeUnQuote(valueText));
        }else{
            encodeResult = ValueCoder.encode(valueText);
            if(encodeResult!=null){
                bagItem.setTypeAndData(encodeResult.valueType,
                        encodeResult.value);
            }else {
                bagItem.setValueAsString(XmlSanitizer.unEscapeUnQuote(valueText));
            }
        }
    }
}
