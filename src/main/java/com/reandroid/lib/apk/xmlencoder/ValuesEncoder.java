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

import com.reandroid.lib.apk.ApkUtil;
import com.reandroid.xml.XMLDocument;
import com.reandroid.xml.XMLElement;
import com.reandroid.xml.XMLException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

class ValuesEncoder {
    private final EncodeMaterials materials;
    private final Map<String, XMLValuesEncoder> xmlEncodersMap;
    private final Map<String, XMLValuesEncoderBag> xmlBagEncodersMap;
    private final XMLValuesEncoderCommon commonEncoder;
    private final XMLValuesEncoderBag bagCommonEncoder;
    public ValuesEncoder(EncodeMaterials materials){
        this.materials=materials;
        Map<String, XMLValuesEncoder> map = new HashMap<>();
        map.put("id", new XMLValuesEncoderId(materials));
        map.put("string", new XMLValuesEncoderString(materials));
        XMLValuesEncoderDimen encoderDimen=new XMLValuesEncoderDimen(materials);
        map.put("dimen", encoderDimen);
        map.put("fraction", encoderDimen);
        map.put("color", new XMLValuesEncoderColor(materials));
        map.put("integer", new XMLValuesEncoderInteger(materials));

        this.xmlEncodersMap=map;
        this.commonEncoder=new XMLValuesEncoderCommon(materials);

        Map<String, XMLValuesEncoderBag> mapBag=new HashMap<>();
        mapBag.put("attr", new XMLValuesEncoderAttr(materials));
        mapBag.put("plurals", new XMLValuesEncoderPlurals(materials));
        mapBag.put("array", new XMLValuesEncoderArray(materials));
        mapBag.put("style", new XMLValuesEncoderStyle(materials));
        this.xmlBagEncodersMap=mapBag;
        this.bagCommonEncoder=new XMLValuesEncoderBag(materials);

    }
    public void encodeValuesXml(File valuesXmlFile) throws XMLException {
        if(valuesXmlFile.getName().equals("public.xml")){
            return;
        }
        String simpleName = valuesXmlFile.getParentFile().getName()
                +File.separator+valuesXmlFile.getName();
        materials.logVerbose("Encoding: "+simpleName);

        String type = EncodeUtil.getTypeNameFromValuesXml(valuesXmlFile);
        String qualifiers = EncodeUtil.getQualifiersFromValuesXml(valuesXmlFile);
        XMLDocument xmlDocument = XMLDocument.load(valuesXmlFile);
        encodeValuesXml(type, qualifiers, xmlDocument);
    }
    private void encodeValuesXml(String type, String qualifiers, XMLDocument xmlDocument) {
        type=getType(xmlDocument, type);
        XMLValuesEncoder encoder;
        if(isBag(xmlDocument, type)){
            encoder=getBagEncoder(type);
        }else{
            encoder=getEncoder(type);
        }
        encoder.encode(type, qualifiers, xmlDocument);
    }
    private boolean isBag(XMLDocument xmlDocument, String type){
        if(type.startsWith("attr")){
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
        if(type.startsWith("string")){
            return false;
        }
        XMLElement documentElement=xmlDocument.getDocumentElement();
        int count=documentElement.getChildesCount();
        for(int i=0;i<count;i++){
            XMLElement element=documentElement.getChildAt(0);
            if(element.getChildesCount()>0){
                return true;
            }
        }
        return false;
    }
    private String getType(XMLDocument xmlDocument, String def){
        XMLElement documentElement=xmlDocument.getDocumentElement();
        if(documentElement.getChildesCount()==0){
            return def;
        }
        XMLElement first=documentElement.getChildAt(0);
        String type=first.getAttributeValue("type");
        if(type==null){
            type=first.getTagName();
        }
        if(type==null){
            return def;
        }
        if(type.endsWith("-array")){
            return "array";
        }
        if(type.equals("item")){
            return def;
        }
        return type;
    }
    private XMLValuesEncoder getEncoder(String type){
        type=EncodeUtil.sanitizeType(type);
        XMLValuesEncoder encoder=xmlEncodersMap.get(type);
        if(encoder!=null){
            return encoder;
        }
        return commonEncoder;
    }
    private XMLValuesEncoderBag getBagEncoder(String type){
        type=EncodeUtil.sanitizeType(type);
        XMLValuesEncoderBag encoder=xmlBagEncodersMap.get(type);
        if(encoder!=null){
            return encoder;
        }
        return bagCommonEncoder;
    }
}
