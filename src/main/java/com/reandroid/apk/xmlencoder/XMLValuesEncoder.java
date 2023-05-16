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
import com.reandroid.arsc.decoder.ValueDecoder;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.xml.XMLDocument;
import com.reandroid.xml.XMLElement;

class XMLValuesEncoder {
    private final EncodeMaterials materials;
    XMLValuesEncoder(EncodeMaterials materials){
        this.materials=materials;
    }
    public void encode(String type, String qualifiers, XMLDocument xmlDocument){
        XMLElement documentElement = xmlDocument.getDocumentElement();
        TypeBlock typeBlock = getTypeBlock(type, qualifiers);

        int count = documentElement.getChildesCount();

        typeBlock.getEntryArray().ensureSize(count);

        for(int i=0;i<count;i++){
            XMLElement element = documentElement.getChildAt(i);
            encode(typeBlock, element);
        }
    }
    private void encode(TypeBlock typeBlock, XMLElement element){
        String name = element.getAttributeValue("name");
        int resourceId = getMaterials()
                .resolveLocalResourceId(typeBlock.getTypeName(), name);
        Entry entry = typeBlock
                .getOrCreateEntry((short) (0xffff & resourceId));

        encodeValue(entry, element);

        getMaterials().setEntryName(entry, name);
    }
    void encodeValue(Entry entry, XMLElement element){
        String value = getValue(element);
        encodeValue(entry, value);
    }
    void encodeValue(Entry entry, String value){
        if(EncodeUtil.isEmpty(value)){
            encodeNullValue(entry);
        }else if(isLiteralEmpty(value)){
            encodeLiteralEmptyValue(entry, value);
        }else if(isBoolean(value)){
            encodeBooleanValue(entry, value);
        }else if(ValueDecoder.isReference(value)){
            encodeReferenceValue(entry, value);
        }else {
            encodeStringValue(entry, value);
        }
    }
    void encodeNullValue(Entry entry){
        entry.setValueAsString("");
    }
    void encodeLiteralEmptyValue(Entry entry, String value){
        entry.setValueAsRaw(ValueType.NULL, 0);
    }
    void encodeBooleanValue(Entry entry, String value){
        entry.setValueAsBoolean("true".equals(value.toLowerCase()));
    }
    void encodeReferenceValue(Entry entry, String value){
        int resourceId = getMaterials().resolveReference(value);
        ValueType valueType;
        if(value.charAt(0) == '?'){
            valueType = ValueType.ATTRIBUTE;
        }else{
            valueType = ValueType.REFERENCE;
        }
        entry.setValueAsRaw(valueType, resourceId);
    }
    void encodeStringValue(Entry entry, String value){

    }
    private TypeBlock getTypeBlock(String type, String qualifiers){
        PackageBlock packageBlock = getMaterials().getCurrentPackage();
        return packageBlock.getOrCreateTypeBlock(qualifiers, type);
    }
    EncodeMaterials getMaterials() {
        return materials;
    }


    static String getValue(XMLElement element){
        String value=element.getTextContent();
        if(value!=null){
            return value;
        }
        return element.getAttributeValue("value");
    }
    static boolean isLiteralEmpty(String value){
        if(value==null){
            return false;
        }
        value=value.trim().toLowerCase();
        return value.equals("@empty");
    }
    static boolean isBoolean(String value){
        if(value==null){
            return false;
        }
        value=value.trim().toLowerCase();
        return value.equals("true")||value.equals("false");
    }
}
