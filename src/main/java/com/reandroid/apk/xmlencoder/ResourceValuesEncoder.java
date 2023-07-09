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
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.io.IOUtil;
import com.reandroid.arsc.value.Entry;
import com.reandroid.xml.XMLDocument;
import com.reandroid.xml.XMLElement;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ResourceValuesEncoder {
    private final EncodeMaterials materials;
    private final Map<String, XMLValuesEncoder> xmlEncodersMap;
    private final Map<String, XMLValuesEncoderBag> xmlBagEncodersMap;
    private final XMLValuesEncoder commonEncoder;
    private final XMLValuesEncoderBag bagCommonEncoder;

    public ResourceValuesEncoder(EncodeMaterials materials){
        this.materials = materials;
        this.commonEncoder = new XMLValuesEncoder(materials);
        Map<String, XMLValuesEncoder> map = new HashMap<>();
        map.put("id", new XMLValuesEncoderId(materials));
        map.put("string", new XMLValuesEncoderString(materials));

        this.xmlEncodersMap = map;

        Map<String, XMLValuesEncoderBag> mapBag = new HashMap<>();
        XMLValuesEncoderAttr encoderAttr = new XMLValuesEncoderAttr(materials);
        mapBag.put("attr", encoderAttr);
        mapBag.put("^attr-private", encoderAttr);
        mapBag.put("plurals", new XMLValuesEncoderPlurals(materials));
        mapBag.put("array", new XMLValuesEncoderArray(materials));
        mapBag.put("style", new XMLValuesEncoderStyle(materials));
        this.xmlBagEncodersMap = mapBag;
        this.bagCommonEncoder = new XMLValuesEncoderStyle(materials);

    }
    public void encodeValuesXml(File valuesXmlFile) throws IOException, XmlPullParserException {
        if(valuesXmlFile.getName().equals("public.xml")){
            return;
        }
        materials.logVerbose("Encoding: " + IOUtil.shortPath(valuesXmlFile, 4));

        String type = EncodeUtil.getTypeNameFromValuesXml(valuesXmlFile);
        String qualifiers = EncodeUtil.getQualifiersFromValuesXml(valuesXmlFile);
        XMLDocument xmlDocument = XMLDocument.load(valuesXmlFile);
        encodeValuesXml(type, qualifiers, xmlDocument);
    }
    public void encodeValue(String qualifiers, XMLElement element){
        String type = getType(element, null);
        if(type == null){
            throw new EncodeException("Can not determine type: " + element);
        }
        encodeValue(type, qualifiers, element);
    }
    public void encodeValue(String type, String qualifiers, XMLElement element){
        boolean is_bag = isBag(element);
        encodeValue(is_bag, type, qualifiers, element);
    }
    public void encodeValue(boolean is_bag, String type, String qualifiers, XMLElement element){
        PackageBlock packageBlock = getEncodeMaterials()
                .getCurrentPackage().getTableBlock().getCurrentPackage();
        Entry entry = packageBlock
                .getOrCreate(qualifiers, type, element.getAttributeValue("name"));
        encodeValue(is_bag, entry, element);
    }
    public void encodeValue(Entry entry, XMLElement element){
        boolean is_bag = isBag(element);
        encodeValue(is_bag, entry, element);
    }
    public void encodeValue(boolean is_bag, Entry entry, XMLElement element){
        XMLValuesEncoder encoder;
        String type = entry.getTypeName();
        if(is_bag){
            encoder = getBagEncoder(type);
        }else{
            encoder = getEncoder(type);
        }
        encoder.encodeValue(entry, element);
    }
    public void encodeValues(String type, String qualifiers, XMLDocument xmlDocument){
        type = getType(xmlDocument, type);
        boolean is_bag = isBag(xmlDocument, type);
        encodeValues(is_bag, type, qualifiers, xmlDocument);
    }
    public void encodeValues(boolean is_bag, String type, String qualifiers, XMLDocument xmlDocument){
        XMLValuesEncoder encoder;
        if(is_bag){
            encoder = getBagEncoder(type);
        }else{
            encoder = getEncoder(type);
        }
        encoder.encode(type, qualifiers, xmlDocument);
    }
    public EncodeMaterials getEncodeMaterials(){
        return materials;
    }
    private void encodeValuesXml(String type, String qualifiers, XMLDocument xmlDocument) {
        type=getType(xmlDocument, type);
        XMLValuesEncoder encoder;
        if(isBag(xmlDocument, type)){
            encoder = getBagEncoder(type);
        }else{
            encoder=getEncoder(type);
        }
        encoder.encode(type, qualifiers, xmlDocument);
    }
    private boolean isBag(XMLElement element){
        if(element.hasChildElements()){
            return true;
        }
        String type = getType(element, element.getName());
        return isBagTypeName(type);
    }
    private boolean isBag(XMLDocument xmlDocument, String type){
        if(isBagTypeName(type)){
            return true;
        }
        if(type.startsWith("string")){
            return false;
        }
        XMLElement documentElement = xmlDocument.getDocumentElement();
        Iterator<? extends XMLElement> childElements = documentElement.getElements();
        while (childElements.hasNext()){
            XMLElement element = childElements.next();
            if(element.getChildElementsCount()>0){
                return true;
            }
        }
        return false;
    }
    private boolean isBagTypeName(String type){
        if(type.startsWith("attr")){
            return true;
        }
        if(type.startsWith("^attr")){
            return true;
        }
        if(type.startsWith("style")){
            return true;
        }
        if(type.startsWith("plurals")){
            return true;
        }
        if(type.startsWith("array")){
            return true;
        }
        return false;
    }
    private String getType(XMLDocument xmlDocument, String def){
        XMLElement documentElement = xmlDocument.getDocumentElement();
        XMLElement first = CollectionUtil.getFirst(documentElement.getElements());
        if(first == null){
            return def;
        }
        String type = first.getName();
        if(type==null){
            return def;
        }
        if(type.endsWith("-array")){
            return "array";
        }
        if(type.startsWith("attr-private")){
            return "^attr-private";
        }
        if(type.equals("item")){
            return def;
        }
        return type;
    }
    private String getType(XMLElement element, String def){
        String type = element.getName();
        if(type == null){
            return def;
        }
        if(type.endsWith("-array")){
            return "array";
        }
        if(type.startsWith("attr-private")){
            return "^attr-private";
        }
        if(type.equals("item")){
            return def;
        }
        return type;
    }
    public XMLValuesEncoder getEncoder(String type){
        type=EncodeUtil.sanitizeType(type);
        XMLValuesEncoder encoder = xmlEncodersMap.get(type);
        if(encoder != null){
            return encoder;
        }
        return commonEncoder;
    }
    public XMLValuesEncoderBag getBagEncoder(String type){
        type = EncodeUtil.sanitizeType(type);
        XMLValuesEncoderBag encoder = xmlBagEncodersMap.get(type);
        if(encoder!=null){
            return encoder;
        }
        return bagCommonEncoder;
    }
}
