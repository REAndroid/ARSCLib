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

import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.xml.XMLUtil;

import java.io.IOException;

public class ResXmlElementChunk extends FixedBlockContainer {

    public ResXmlElementChunk() {
        super(5);

        ResXmlChunkList<ResXmlEndNamespace> endNamespaceList = new ResXmlChunkList<>();
        ResXmlStartNamespaceList startNamespaceList = new ResXmlStartNamespaceList(endNamespaceList);

        ResXmlEndElement endElement = new ResXmlEndElement();
        ResXmlStartElement startElement = new ResXmlStartElement(endElement);

        ResXmlNodeList nodeList = new ResXmlNodeList();

        setupBlock(0, startNamespaceList);
        setupBlock(1, startElement);
        setupBlock(2, nodeList);
        setupBlock(3, endElement);
        setupBlock(4, endNamespaceList);
    }

    ResXmlStartNamespaceList getStartNamespaceList() {
        return block(0);
    }
    ResXmlStartElement getStartElement() {
        return block(1);
    }
    public ResXmlNodeList getNodeList() {
        return block(2);
    }
    ResXmlEndElement getEndElement(){
        return block(3);
    }
    ResXmlChunkList<ResXmlEndNamespace> getEndNamespaceList() {
        return block(4);
    }

    ResXmlElement element() {
        return getParentInstance(ResXmlElement.class);
    }

    void onPreRemove(){
        getStartNamespaceList().clear();
        getEndNamespaceList().clearBlocks();
        getStartElement().onPreRemove();
    }
    public void setName(String name) {
        ResXmlStartElement startElement = getStartElement();
        if(name == null){
            startElement.setName(null);
            return;
        }
        String prefix = XMLUtil.splitPrefix(name);
        name = XMLUtil.splitName(name);
        startElement.setName(name);
        if(prefix == null){
            return;
        }
        ResXmlNamespace namespace = element()
                .getOrCreateNamespaceForPrefix(prefix);
        if (namespace != null) {
            startElement.setNamespaceReference(namespace.getUriReference());
        }
    }

    void linkStringReferences() {
        getStartElement().linkStringReferences();
    }

    @Override
    public void readBytes(BlockReader reader) throws IOException {
        new ElementChunkReader(this).read(reader);
    }
}
