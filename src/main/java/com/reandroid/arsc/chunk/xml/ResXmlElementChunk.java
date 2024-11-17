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

    private final ResXmlStartNamespaceList mStartNamespaceList;
    private final ResXmlStartElement mStartElement;
    private final ResXmlNodeList mNodeList;
    private final ResXmlEndElement mEndElement;
    private final ResXmlChunkList<ResXmlEndNamespace> mEndNamespaceList;

    public ResXmlElementChunk() {
        super(5);

        ResXmlChunkList<ResXmlEndNamespace> endNamespaceList = new ResXmlChunkList<>();
        this.mStartNamespaceList = new ResXmlStartNamespaceList(endNamespaceList);

        ResXmlEndElement endElement = new ResXmlEndElement();
        this.mStartElement = new ResXmlStartElement(endElement);
        this.mNodeList = new ResXmlNodeList();
        this.mEndElement = endElement;
        this.mEndNamespaceList = endNamespaceList;

        addChild(0, mStartNamespaceList);
        addChild(1, mStartElement);
        addChild(2, mNodeList);
        addChild(3, mEndElement);
        addChild(4, mEndNamespaceList);
    }

    public ResXmlNodeList getNodeList() {
        return mNodeList;
    }

    ResXmlElement element() {
        return getParentInstance(ResXmlElement.class);
    }

    void onPreRemove(){
        mStartNamespaceList.clear();
        mEndNamespaceList.clearChildes();
        mStartElement.onPreRemove();
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

    ResXmlStartNamespaceList getStartNamespaceList(){
        return mStartNamespaceList;
    }

    ResXmlStartElement getStartElement(){
        return mStartElement;
    }

    ResXmlEndElement getEndElement(){
        return mEndElement;
    }

    void linkStringReferences() {
        mStartElement.linkStringReferences();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        new ElementChunkReader(this).read(reader);
    }
}
