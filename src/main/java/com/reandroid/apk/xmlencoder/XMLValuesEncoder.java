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

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.coder.*;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.value.AttributeDataFormat;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ValueItem;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.xml.XMLDocument;
import com.reandroid.xml.XMLElement;

import java.util.Iterator;

public class XMLValuesEncoder {
    private final EncodeMaterials materials;
    XMLValuesEncoder(EncodeMaterials materials){
        this.materials=materials;
    }
    public void encode(String type, String qualifiers, XMLDocument xmlDocument){
        XMLElement documentElement = xmlDocument.getDocumentElement();
        TypeBlock typeBlock = getTypeBlock(type, qualifiers);

        int count = documentElement.getChildElementsCount();

        typeBlock.getEntryArray().ensureSize(count);

        Iterator<? extends XMLElement> iterator = documentElement.getElements();
        while (iterator.hasNext()){
            encode(typeBlock, iterator.next());
        }
    }
    public Entry encode(TypeBlock typeBlock, XMLElement element){
        String name = element.getAttributeValue("name");
        Entry entry = typeBlock.getOrCreateDefinedEntry(name);
        if(entry == null){
            throw new EncodeException("Undefined entry name: '"
                    + name + "', element = " + element);
        }
        encodeValue(entry, element);
        return entry;
    }
    public void encodeValue(Entry entry, XMLElement element){
        String value = getValue(element);
        EncodeResult encodeResult = getMaterials()
                .encodeReference(value);
        if(encodeResult != null){
            entry.setValueAsRaw(encodeResult.valueType, encodeResult.value);
            return;
        }
        ValueType[] expectedTypes = AttributeDataFormat
                .getExpectedValueTypes(element.getAttributeValue("type"));
        if(expectedTypes == null){
            expectedTypes = CommonType.getExpectedTypes(entry.getTypeName());
        }
        encodeValue(entry, expectedTypes, value);
    }
    public void encodeValue(Entry entry, String text){
        ValueType[] expectedTypes = CommonType.getExpectedTypes(entry.getTypeName());
        encodeValue(entry, expectedTypes, text);
    }
    public void encodeValue(Entry entry, ValueType[] expectedTypes, String text){
        EncodeResult encodeResult = getMaterials()
                .encodeReference(text);
        if(encodeResult == null){
            encodeResult = ValueCoder.encode(text, expectedTypes);
        }
        if(encodeResult != null){
            entry.setValueAsRaw(encodeResult.valueType, encodeResult.value);
        }else {
            // TODO: should check expectedTypes contains ValueType.STRING ?
            text = XmlSanitizer.unEscapeUnQuote(text);
            entry.setValueAsString(text);
        }
    }
    private TypeBlock getTypeBlock(String type, String qualifiers){
        PackageBlock packageBlock = getMaterials().getCurrentPackage();
        return packageBlock.getOrCreateTypeBlock(qualifiers, type);
    }
    void encodeAny(ValueItem value, String text){
        EncodeResult encodeResult = getMaterials().encodeReference(text);
        if(encodeResult == null){
            encodeResult = ValueCoder.encode(text);
        }
        if(encodeResult != null){
            value.setTypeAndData(encodeResult.valueType, encodeResult.value);
            return;
        }
        value.setValueAsString(text);
    }
    EncodeMaterials getMaterials() {
        return materials;
    }
    static String getValue(XMLElement element){
        String value = element.getTextContent();
        if(value != null){
            return value;
        }
        return element.getAttributeValue("value");
    }
    static AttributeDataFormat getType(XMLElement element){
        return AttributeDataFormat.fromValueTypeName(element.getAttributeValue("type"));
    }
}
