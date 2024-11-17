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
package com.reandroid.arsc.chunk.xml;

import android.content.res.XmlResourceParser;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.utils.StringsUtil;
import org.xmlpull.v1.XmlPullParserException;

import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;

/**
 * See
 * https://android.googlesource.com/platform/frameworks/base/+/main/core/java/android/content/res/XmlBlock.java
 * */

public class ResXmlPullParser extends ResXmlEventParser implements XmlResourceParser {


    public ResXmlPullParser(Iterator<ResXmlEvent> eventIterator) {
        super(eventIterator);
    }
    public ResXmlPullParser(ResXmlNode xmlNode) {
        this(xmlNode.getParserEvents());
    }

    @Override
    public void close(){
    }
    @Override
    public int getAttributeNameResource(int index) {
        ResXmlAttribute attribute = getResXmlAttributeAt(index);
        if(attribute!=null){
            return attribute.getNameId();
        }
        return 0;
    }
    @Override
    public boolean getAttributeBooleanValue(String namespace, String attribute, boolean defaultValue) {
        ResXmlAttribute xmlAttribute = getAttribute(namespace, attribute);
        if(xmlAttribute != null) {
            int v = getAttributeIntValue(xmlAttribute, 0);
            return v != 0;
        }
        return defaultValue;
    }
    @Override
    public int getAttributeResourceValue(String namespace, String attribute, int defaultValue) {
        ResXmlAttribute xmlAttribute = getAttribute(namespace, attribute);
        if(xmlAttribute != null && xmlAttribute.getValueType() == ValueType.REFERENCE){
            return xmlAttribute.getData();
        }
        return defaultValue;
    }
    @Override
    public int getAttributeIntValue(String namespace, String attribute, int defaultValue) {
        return getAttributeIntValue(getAttribute(namespace, attribute), defaultValue);
    }
    @Override
    public int getAttributeUnsignedIntValue(String namespace, String attribute, int defaultValue) {
        return getAttributeIntValue(getAttribute(namespace, attribute), defaultValue);
    }
    @Override
    public float getAttributeFloatValue(String namespace, String attribute, float defaultValue) {
        ResXmlAttribute xmlAttribute = getAttribute(namespace, attribute);
        if(xmlAttribute != null){
            ValueType valueType = xmlAttribute.getValueType();
            if(valueType == ValueType.FLOAT) {
                return Float.intBitsToFloat(xmlAttribute.getData());
            }
        }
        return defaultValue;
    }

    @Override
    public int getAttributeListValue(int index, String[] options, int defaultValue) {
        ResXmlAttribute xmlAttribute = getResXmlAttributeAt(index);
        if(xmlAttribute == null ||
                xmlAttribute.getValueType() != ValueType.STRING
                || options == null || options.length == 0){
            return defaultValue;
        }
        return convertValueToList(xmlAttribute.getValueAsString(), options, defaultValue);
    }
    @Override
    public int getAttributeListValue(String namespace, String attribute, String[] options, int defaultValue) {
        ResXmlAttribute xmlAttribute = getAttribute(namespace, attribute);
        if(xmlAttribute == null ||
                xmlAttribute.getValueType() != ValueType.STRING
                || options == null || options.length == 0){
            return defaultValue;
        }
        return convertValueToList(xmlAttribute.getValueAsString(), options, defaultValue);
    }
    @Override
    public boolean getAttributeBooleanValue(int index, boolean defaultValue) {
        ResXmlAttribute xmlAttribute = getResXmlAttributeAt(index);
        if(xmlAttribute != null) {
            int v = getAttributeIntValue(xmlAttribute, 0);
            return v != 0;
        }
        return defaultValue;
    }
    @Override
    public int getAttributeResourceValue(int index, int defaultValue) {
        ResXmlAttribute xmlAttribute = getResXmlAttributeAt(index);
        if(xmlAttribute != null && xmlAttribute.getValueType() == ValueType.REFERENCE){
            return xmlAttribute.getData();
        }
        return defaultValue;
    }
    @Override
    public int getAttributeIntValue(int index, int defaultValue) {
        return getAttributeIntValue(getResXmlAttributeAt(index), defaultValue);
    }
    @Override
    public int getAttributeUnsignedIntValue(int index, int defaultValue) {
        return getAttributeIntValue(getResXmlAttributeAt(index), defaultValue);
    }
    @Override
    public float getAttributeFloatValue(int index, float defaultValue) {
        ResXmlAttribute xmlAttribute = getResXmlAttributeAt(index);
        if(xmlAttribute != null) {
            if(xmlAttribute.getValueType() == ValueType.FLOAT){
                return Float.intBitsToFloat(xmlAttribute.getData());
            }
        }
        return defaultValue;
    }

    @Override
    public String getIdAttribute() {
        ResXmlElement currentElement = getCurrentElement();
        if(currentElement != null){
            ResXmlAttribute attribute = currentElement.getIdAttribute();
            if(attribute != null){
                return attribute.getName();
            }
        }
        return null;
    }
    @Override
    public String getClassAttribute() {
        ResXmlElement currentElement = getCurrentElement();
        if(currentElement!=null){
            ResXmlAttribute attribute = currentElement.getClassAttribute();
            if(attribute != null){
                return attribute.getName();
            }
        }
        return null;
    }
    @Override
    public int getIdAttributeResourceValue(int defaultValue) {
        ResXmlElement currentElement = getCurrentElement();
        if(currentElement != null){
            ResXmlAttribute attribute = currentElement.getIdAttribute();
            if(attribute != null){
                return attribute.getNameId();
            }
        }
        return defaultValue;
    }
    @Override
    public int getStyleAttribute() {
        ResXmlElement currentElement = getCurrentElement();
        if(currentElement != null){
            ResXmlAttribute attribute = currentElement.getStyleAttribute();
            if(attribute != null){
                return attribute.getNameId();
            }
        }
        return 0;
    }

    @Override
    public void setInput(Reader in) throws XmlPullParserException {
        throw new XmlPullParserException("Unsupported operation");
    }
    @Override
    public void setInput(InputStream inputStream, String inputEncoding) throws XmlPullParserException {
        throw new XmlPullParserException("Unsupported operation");
    }
    private int convertValueToList(String value, String[] options, int defaultValue) {
        if (!StringsUtil.isEmpty(value)) {
            for (int i = 0; i < options.length; i++) {
                if (value.equals(options[i])) {
                    return i;
                }
            }
        }
        return defaultValue;
    }

    private int getAttributeIntValue(ResXmlAttribute xmlAttribute, int defaultValue) {
        if(xmlAttribute != null) {
            int type = xmlAttribute.getType() & 0xff;
            if(type > 0x10 && type <= 0x1f) {
                return xmlAttribute.getData();
            }
            // TODO: resolve if type is REFERENCE
        }
        return defaultValue;
    }
}
