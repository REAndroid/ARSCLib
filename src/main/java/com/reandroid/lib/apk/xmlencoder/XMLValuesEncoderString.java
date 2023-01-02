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

import com.reandroid.lib.arsc.decoder.ValueDecoder;
import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.xml.XMLDocument;
import com.reandroid.xml.XMLElement;

import java.util.ArrayList;
import java.util.List;

public class XMLValuesEncoderString extends XMLValuesEncoder{
    XMLValuesEncoderString(EncodeMaterials materials) {
        super(materials);
    }
    @Override
    public void encode(String type, String qualifiers, XMLDocument xmlDocument){
        preloadStringPool(xmlDocument);
        super.encode(type, qualifiers, xmlDocument);
    }
    @Override
    void encodeStringValue(EntryBlock entryBlock, String value){
        entryBlock.setValueAsString(value);
    }
    @Override
    void encodeNullValue(EntryBlock entryBlock){
        entryBlock.setValueAsString("");
    }
    @Override
    void encodeBooleanValue(EntryBlock entryBlock, String value){
        entryBlock.setValueAsString(value);
    }
    private void preloadStringPool(XMLDocument xmlDocument){
        XMLElement documentElement = xmlDocument.getDocumentElement();
        List<String> stringList = new ArrayList<>();
        int count = documentElement.getChildesCount();
        for(int i=0;i<count;i++){
            String value=getValue(documentElement.getChildAt(i));
            if(value==null || ValueDecoder.isReference(value)){
                continue;
            }
            stringList.add(value);
        }
        getMaterials().addTableStringPool(stringList);
    }
}
