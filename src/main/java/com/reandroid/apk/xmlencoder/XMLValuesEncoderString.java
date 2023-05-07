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

import com.reandroid.arsc.decoder.ValueDecoder;
import com.reandroid.arsc.value.Entry;
import com.reandroid.xml.XMLElement;
import com.reandroid.xml.XMLSpannable;

class XMLValuesEncoderString extends XMLValuesEncoder{
    XMLValuesEncoderString(EncodeMaterials materials) {
        super(materials);
    }

    @Override
    void encodeValue(Entry entry, XMLElement element){
        if(!element.hasChildElements()){
            super.encodeValue(entry, element);
            return;
        }
        encodeStyledString(entry, element);
    }
    @Override
    void encodeStringValue(Entry entry, String value){
        value = ValueDecoder.unQuoteWhitespace(value);
        value = ValueDecoder.unEscapeSpecialCharacter(value);
        entry.setValueAsString(value);
    }
    @Override
    void encodeNullValue(Entry entry){
        entry.setValueAsString("");
    }
    @Override
    void encodeBooleanValue(Entry entry, String value){
        entry.setValueAsString(value);
    }
    private void encodeStyledString(Entry entry, XMLElement element){
        XMLSpannable xmlSpannable = new XMLSpannable(element);
        entry.setValueAsString(xmlSpannable.getXml());
    }
}
