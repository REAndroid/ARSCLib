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

import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.value.AttributeDataFormat;
import com.reandroid.arsc.value.Entry;
import com.reandroid.xml.StyleDocument;
import com.reandroid.xml.XMLElement;

public class XMLValuesEncoderString extends XMLValuesEncoder{
    XMLValuesEncoderString(TableBlock tableBlock) {
        super(tableBlock);
    }

    @Override
    public void encodeValue(Entry entry, XMLElement element){
        if(!element.hasChildElements()){
            String text = getValue(element);
            AttributeDataFormat dataFormat = getType(element);
            if(dataFormat == null){
                dataFormat = AttributeDataFormat.STRING;
            }
            super.encodeValue(entry, dataFormat.valueTypes(), text);
            return;
        }
        encodeStyledString(entry, element);
    }
    private void encodeStyledString(Entry entry, XMLElement element){
        entry.setValueAsString(StyleDocument.copyInner(element));
    }
}
