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
import com.reandroid.arsc.chunk.xml.*;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.coder.ValueDecoder;
import com.reandroid.arsc.value.AttributeDataFormat;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.arsc.value.attribute.AttributeBag;
import com.reandroid.xml.*;

import java.io.File;
import java.io.InputStream;

public class XMLFileEncoder {
    private final EncodeMaterials materials;
    private ResXmlDocument resXmlDocument;
    private String mCurrentPath;
    public XMLFileEncoder(EncodeMaterials materials){
        this.materials=materials;
    }

    // Just for logging purpose
    public void setCurrentPath(String path) {
        this.mCurrentPath = path;
    }
    public ResXmlDocument encode(String xmlString){
        try {
            return encode(XMLDocument.load(xmlString));
        } catch (XMLException ex) {
            materials.logMessage(ex.getMessage());
        }
        return null;
    }
    public ResXmlDocument encode(InputStream inputStream){
        try {
            return encode(XMLDocument.load(inputStream));
        } catch (XMLException ex) {
            materials.logMessage(ex.getMessage());
        }
        return null;
    }
    public ResXmlDocument encode(File xmlFile){
        setCurrentPath(xmlFile.getAbsolutePath());
        try {
            return encode(XMLDocument.load(xmlFile));
        } catch (XMLException ex) {
            materials.logMessage(ex.getMessage());
        }
        return null;
    }
    public ResXmlDocument encode(XMLDocument xmlDocument){
        resXmlDocument = new ResXmlDocument();
        resXmlDocument.setPackageBlock(materials.getCurrentPackage());
        buildIdMap(xmlDocument);
        buildElement(xmlDocument);
        resXmlDocument.refresh();
        return resXmlDocument;
    }
    public ResXmlDocument getResXmlBlock(){
        return resXmlDocument;
    }
    private void buildElement(XMLDocument xmlDocument){
        XMLElement element = xmlDocument.getDocumentElement();
        ResXmlElement resXmlElement = resXmlDocument.createRootElement(element.getTagName());
        buildElement(element, resXmlElement);
    }
    private void buildElement(XMLElement element, ResXmlElement resXmlElement){
        ensureNamespaces(element, resXmlElement);
        resXmlElement.setTag(element.getTagName());
        buildAttributes(element, resXmlElement);
        for(XMLNode node:element.getChildNodes()){
            if(node instanceof XMLText){
                resXmlElement.addResXmlText(((XMLText)node).getText(true));
            }else if(node instanceof XMLComment){
                resXmlElement.setComment(((XMLComment)node).getCommentText());
            }else if(node instanceof XMLElement){
                XMLElement child=(XMLElement) node;
                ResXmlElement childXml=resXmlElement.createChildElement();
                buildElement(child, childXml);
            }
        }
    }
    private void buildAttributes(XMLElement element, ResXmlElement resXmlElement){
        for(XMLAttribute attribute:element.listAttributes()){
            if(attribute instanceof SchemaAttr){
                continue;
            }
            if(SchemaAttr.looksSchema(attribute.getName(), attribute.getValue())){
                continue;
            }
            String name = attribute.getNameWoPrefix();
            String prefix = attribute.getNamePrefix();
            EncodeResult unknownId = ValueCoder.encodeUnknownResourceId(name);
            int resourceId;
            Entry entry = null;
            if(unknownId == null && prefix != null){
                entry = getAttributeBlock(attribute);
                if(entry == null){
                    throw new EncodeException("No resource found for attribute: "
                            + attribute.getName() + ", at file "+mCurrentPath);
                }
                resourceId = entry.getResourceId();
            }else if(unknownId != null){
                resourceId = unknownId.value;
            }else {
                resourceId = 0;
            }
            ResXmlAttribute xmlAttribute = resXmlElement.createAttribute(name, resourceId);
            if(prefix != null){
                ResXmlStartNamespace ns = resXmlElement.getStartNamespaceByPrefix(prefix);
                if(ns == null){
                    ns = forceCreateNamespace(resXmlElement, resourceId, prefix);
                }
                if(ns == null){
                    throw new EncodeException("Namespace not found: "
                            + attribute.toString()
                            + ", path=" + mCurrentPath);
                }
                xmlAttribute.setNamespaceReference(ns.getUriReference());
            }
            String valueText = attribute.getValue();
            EncodeResult encodeResult = materials.encodeReference(valueText);
            if(encodeResult != null){
                xmlAttribute.setTypeAndData(encodeResult.valueType, encodeResult.value);
                continue;
            }
            if(entry != null){
                AttributeBag attributeBag = AttributeBag
                        .create((ResValueMapArray) entry.getTableEntry().getValue());

                encodeResult =
                        attributeBag.encodeEnumOrFlagValue(valueText);
                if(encodeResult == null){
                    AttributeDataFormat[] formats = attributeBag.getFormats();
                    encodeResult = ValueCoder.encode(valueText, formats);
                }
                if(encodeResult != null){
                    xmlAttribute.setTypeAndData(encodeResult.valueType, encodeResult.value);
                    continue;
                }
                if(attributeBag.isEqualType(AttributeDataFormat.STRING)) {
                    xmlAttribute.setValueAsString(ValueDecoder
                            .unEscapeUnQuote(valueText));
                    continue;
                }
                // TODO: should throw here ?
            }
            encodeResult = ValueCoder.encode(valueText);
            if(encodeResult != null){
                xmlAttribute.setTypeAndData(encodeResult.valueType, encodeResult.value);
            }else {
                xmlAttribute.setValueAsString(ValueDecoder
                        .unEscapeUnQuote(valueText));
            }
        }
        resXmlElement.calculatePositions();
    }
    private void ensureNamespaces(XMLElement element, ResXmlElement resXmlElement){
        for(XMLAttribute attribute:element.listAttributes()){
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
        idBuilder.buildTo(resXmlDocument.getResXmlIDMap());
    }
    private void searchResIds(ResIdBuilder idBuilder, XMLElement element){
        for(XMLAttribute attribute : element.listAttributes()){
            addResourceId(idBuilder, attribute);
        }
        int count=element.getChildesCount();
        for(int i=0;i<count;i++){
            searchResIds(idBuilder, element.getChildAt(i));
        }
    }
    private void addResourceId(ResIdBuilder idBuilder, XMLAttribute attribute){
        String name = attribute.getNameWoPrefix();
        EncodeResult encodeResult = ValueCoder.encodeUnknownResourceId(name);
        if(encodeResult != null){
            idBuilder.add(encodeResult.value, name);
            return;
        }
        Entry entry = getAttributeBlock(attribute);
        if(entry !=null){
            idBuilder.add(entry.getResourceId(), entry.getName());
        }
    }
    private Entry getAttributeBlock(XMLAttribute attribute){
        if(attribute instanceof SchemaAttr){
            return null;
        }
        String name=attribute.getName();
        if(name.indexOf(':')<0){
            return null;
        }
        return materials.getAttributeBlock(name);
    }
    private ResXmlStartNamespace forceCreateNamespace(ResXmlElement resXmlElement,
                                                      int resourceId, String prefix){
        if(!materials.isForceCreateNamespaces()){
            return null;
        }
        int pkgId = (resourceId>>24) & 0xff;
        String uri;
        if(pkgId == 1){
            uri = EncodeUtil.URI_ANDROID;
        }else {
            uri=EncodeUtil.URI_APP;
        }
        ResXmlElement root = resXmlElement.getRootResXmlElement();
        ResXmlStartNamespace ns = root.getOrCreateNamespace(uri, prefix);
        materials.logVerbose("Force created ns: "+prefix+":"+uri);
        return ns;
    }
}
