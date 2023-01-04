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

import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.TypeBlock;
import com.reandroid.lib.arsc.decoder.ValueDecoder;
import com.reandroid.lib.arsc.item.SpecString;
import com.reandroid.lib.arsc.pool.TypeStringPool;
import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.arsc.value.ValueType;
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

        typeBlock.getEntryBlockArray().ensureSize(count);

        for(int i=0;i<count;i++){
            XMLElement element = documentElement.getChildAt(i);
            encode(typeBlock, element);
        }
    }
    private void encode(TypeBlock typeBlock, XMLElement element){
        String name = element.getAttributeValue("name");
        int resourceId = getMaterials()
                .resolveLocalResourceId(typeBlock.getTypeName(), name);
        EntryBlock entryBlock = typeBlock
                .getOrCreateEntry((short) (0xffff & resourceId));

        encodeValue(entryBlock, element);

        SpecString specString = getMaterials().getSpecString(name);
        entryBlock.setSpecReference(specString);
    }
    void encodeValue(EntryBlock entryBlock, XMLElement element){
        String value = getValue(element);
        encodeValue(entryBlock, value);
    }
    void encodeValue(EntryBlock entryBlock, String value){
        if(EncodeUtil.isEmpty(value)){
            encodeNullValue(entryBlock);
        }else if(isLiteralEmpty(value)){
            encodeLiteralEmptyValue(entryBlock, value);
        }else if(isBoolean(value)){
            encodeBooleanValue(entryBlock, value);
        }else if(ValueDecoder.isReference(value)){
            encodeReferenceValue(entryBlock, value);
        }else {
            encodeStringValue(entryBlock, value);
        }
    }
    void encodeNullValue(EntryBlock entryBlock){
        // Nothing to do
    }
    void encodeLiteralEmptyValue(EntryBlock entryBlock, String value){
        entryBlock.setValueAsRaw(ValueType.NULL, 0);
    }
    void encodeBooleanValue(EntryBlock entryBlock, String value){
        entryBlock.setValueAsBoolean("true".equals(value.toLowerCase()));
    }
    void encodeReferenceValue(EntryBlock entryBlock, String value){
        int resourceId = getMaterials().resolveReference(value);
        entryBlock.setValueAsReference(resourceId);
    }
    void encodeStringValue(EntryBlock entryBlock, String value){

    }
    private TypeBlock getTypeBlock(String type, String qualifiers){
        PackageBlock packageBlock = getMaterials().getCurrentPackage();
        TypeStringPool typeStringPool = packageBlock.getTypeStringPool();
        byte typeId = typeStringPool.idOf(type);
        return packageBlock.getSpecTypePairArray()
                .getOrCreateTypeBlock(typeId, qualifiers);
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
