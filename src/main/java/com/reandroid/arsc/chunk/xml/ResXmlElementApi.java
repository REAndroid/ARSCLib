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

import com.reandroid.arsc.item.IntegerReference;

/**
 * A wrapper api class to expose all methods of class {@link ResXmlElement}
 * */
@SuppressWarnings("unused")
public class ResXmlElementApi {

    private final ResXmlElement element;

    public ResXmlElementApi(ResXmlElement element) {
        this.element = element;
    }

    public ResXmlAttributeArray getAttributeArray() {
        return getStartElement().getResXmlAttributeArray();
    }
    public ResXmlStartElement getStartElement() {
        return element.getChunk().getStartElement();
    }
    public ResXmlEndElement getEndElement() {
        return element.getChunk().getEndElement();
    }
    public ResXmlStartNamespace getResXmlStartNamespace() {
        return getStartElement().getResXmlStartNamespace();
    }
    public ResXmlEndNamespace getResXmlEndNamespace() {
        ResXmlStartNamespace startNamespace = getResXmlStartNamespace();
        if (startNamespace != null) {
            return startNamespace.getEnd();
        }
        return null;
    }
    public ResXmlStartNamespaceList getStartNamespaceList() {
        return element.getChunk().getStartNamespaceList();
    }

    public IntegerReference getAttributeStart() {
        return getStartElement().getAttributeStart();
    }
    public IntegerReference getAttributeCount() {
        return getStartElement().getAttributeCount();
    }
    public IntegerReference getAttributeUnitSize() {
        return getStartElement().getAttributeUnitSize();
    }

    public ResXmlAttributePosition getIdAttributePosition() {
        return getStartElement().getIdAttributePosition();
    }
    public ResXmlAttributePosition getClassAttributePosition() {
        return getStartElement().getClassAttributePosition();
    }
    public ResXmlAttributePosition getStyleAttributePosition() {
        return getStartElement().getStyleAttributePosition();
    }

    public ResXmlElement getElement() {
        return element;
    }
    public ResXmlStartElement.UnknownBytes getUnknownBytes() {
        return getStartElement().getUnknownBytes();
    }
    @Override
    public String toString() {
        return element.toString();
    }
}
