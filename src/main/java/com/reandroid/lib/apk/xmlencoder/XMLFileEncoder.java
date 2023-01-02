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

import com.reandroid.lib.arsc.chunk.xml.ResIdBuilder;
import com.reandroid.lib.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.lib.arsc.chunk.xml.ResXmlBlock;
import com.reandroid.lib.arsc.chunk.xml.ResXmlElement;
import com.reandroid.lib.arsc.decoder.ValueDecoder;
import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.arsc.value.ResValueBag;
import com.reandroid.lib.arsc.value.ValueType;
import com.reandroid.lib.arsc.value.attribute.AttributeBag;
import com.reandroid.xml.*;

import java.io.File;
import java.io.InputStream;

public class XMLFileEncoder {
    private final EncodeMaterials materials;
    private ResXmlBlock resXmlBlock;
    public XMLFileEncoder(EncodeMaterials materials){
        this.materials=materials;
    }
    public void encode(String xmlString){
        try {
            encode(XMLDocument.load(xmlString));
        } catch (XMLException ex) {
            materials.logMessage(ex.getMessage());
        }
    }
    public void encode(InputStream inputStream){
        try {
            encode(XMLDocument.load(inputStream));
        } catch (XMLException ex) {
            materials.logMessage(ex.getMessage());
        }
    }
    public void encode(File xmlFile){
        try {
            encode(XMLDocument.load(xmlFile));
        } catch (XMLException ex) {
            materials.logMessage(ex.getMessage());
        }
    }
    public void encode(XMLDocument xmlDocument){
        resXmlBlock=new ResXmlBlock();
        buildIdMap(xmlDocument);
        buildElement(xmlDocument);
        resXmlBlock.refresh();
    }
    public ResXmlBlock getResXmlBlock(){
        return resXmlBlock;
    }
    private void buildElement(XMLDocument xmlDocument){
        XMLElement element = xmlDocument.getDocumentElement();
        ResXmlElement resXmlElement = resXmlBlock.createRootElement(element.getTagName());
        buildElement(element, resXmlElement);
    }
    private void buildElement(XMLElement element, ResXmlElement resXmlElement){
        ensureNamespaces(element, resXmlElement);
        resXmlElement.setTag(element.getTagName());
        buildAttributes(element, resXmlElement);
        int count=element.getChildesCount();
        for(int i=0;i<count;i++){
            XMLElement child=element.getChildAt(i);
            ResXmlElement childXml=resXmlElement.createChildElement();
            buildElement(child, childXml);
        }
    }
    private void buildAttributes(XMLElement element, ResXmlElement resXmlElement){
        int count=element.getAttributeCount();
        for(int i=0;i<count;i++){
            XMLAttribute attribute=element.getAttributeAt(i);
            if(SchemaAttr.looksSchema(attribute.getName(), attribute.getValue())){
                continue;
            }
            EntryBlock entryBlock=materials.getAttributeBlock(attribute.getName());
            int resourceId=0;
            if(entryBlock!=null){
                resourceId=entryBlock.getResourceId();
            }
            String valueText=attribute.getValue();

            ResXmlAttribute xmlAttribute =
                    resXmlElement.createAttribute(attribute.getName(), resourceId);
            if(entryBlock!=null){
                AttributeBag attributeBag=AttributeBag
                        .create((ResValueBag) entryBlock.getResValue());

                ValueDecoder.EncodeResult encodeResult =
                        attributeBag.encodeName(valueText);
                if(encodeResult!=null){
                    xmlAttribute.setValueType(encodeResult.valueType);
                    xmlAttribute.setRawValue(encodeResult.value);
                    continue;
                }
            }

            if(ValueDecoder.isReference(valueText)){
                xmlAttribute.setValueType(ValueType.REFERENCE);
                xmlAttribute.setRawValue(materials.resolveReference(valueText));
            }else if(EncodeUtil.isEmpty(valueText)) {
                xmlAttribute.setValueType(ValueType.NULL);
                xmlAttribute.setRawValue(0);
            }else{
                ValueDecoder.EncodeResult encodeResult =
                        ValueDecoder.encodeGuessAny(valueText);
                if(encodeResult!=null){
                    xmlAttribute.setValueType(encodeResult.valueType);
                    xmlAttribute.setRawValue(encodeResult.value);
                }else {
                    xmlAttribute.setValueAsString(valueText);
                }
            }
        }
    }
    private void ensureNamespaces(XMLElement element, ResXmlElement resXmlElement){
        int count=element.getAttributeCount();
        for(int i=0;i<count;i++){
            XMLAttribute attribute = element.getAttributeAt(i);
            String prefix = SchemaAttr.getPrefix(attribute.getName());
            if(prefix==null){
                continue;
            }
            String uri=attribute.getValue();
            resXmlElement.getOrCreateNamespace(uri, prefix);
        }
    }
    private void buildIdMap(XMLDocument xmlDocument){
        ResIdBuilder idBuilder=new ResIdBuilder();
        XMLElement element= xmlDocument.getDocumentElement();
        searchResIds(idBuilder, element);
        idBuilder.buildTo(resXmlBlock.getResXmlIDMap());
    }
    private void searchResIds(ResIdBuilder idBuilder, XMLElement element){
        int count=element.getAttributeCount();
        for(int i=0;i<count;i++){
            XMLAttribute attribute = element.getAttributeAt(i);
            addResourceId(idBuilder, attribute);
        }
        count=element.getChildesCount();
        for(int i=0;i<count;i++){
            searchResIds(idBuilder, element.getChildAt(i));
        }
    }
    private void addResourceId(ResIdBuilder idBuilder, XMLAttribute attribute){
        EntryBlock entryBlock = materials.getAttributeBlock(attribute.getName());
        if(entryBlock!=null){
            idBuilder.add(entryBlock.getResourceId(), entryBlock.getName());
        }
    }
}
