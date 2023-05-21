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

import com.reandroid.arsc.value.ValueHeader;
import com.reandroid.arsc.value.Entry;
import com.reandroid.xml.XMLElement;

public class XMLValuesEncoderId extends XMLValuesEncoder{
    public XMLValuesEncoderId(EncodeMaterials materials) {
        super(materials);
    }

    @Override
    public void encodeValue(Entry entry, XMLElement element){
        if(element.hasChildElements()){
            throw new IllegalArgumentException("Can not encode as ID : " + element);
        }
        entry.setValueAsBoolean(false);
        setVisibility(entry);
    }
    private void setVisibility(Entry entry){
        ValueHeader valueHeader = entry.getHeader();
        valueHeader.setWeak(true);
        valueHeader.setPublic(true);
    }
}
