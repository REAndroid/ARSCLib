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

import com.reandroid.arsc.chunk.ChunkType;

public class ResXmlEndElement extends BaseXmlChunk  {

    public ResXmlEndElement() {
        super(ChunkType.XML_END_ELEMENT, 0);
    }

    @Override
    protected void onPreRefresh() {
        super.onPreRefresh();
        syncWithStartElement();
    }

    private void syncWithStartElement() {
        ResXmlStartElement startElement = getStartElement();
        if (startElement == null) {
            return;
        }
        setStringReference(startElement.getStringReference());
        setNamespaceReference(startElement.getNamespaceReference());
    }

    private ResXmlStartElement getStartElement() {
        ResXmlElementChunk elementChunk = getParentInstance(ResXmlElementChunk.class);
        if (elementChunk != null) {
            return elementChunk.getStartElement();
        }
        return null;
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public String toString() {
        return "</" + getName() + ">";
    }
}
