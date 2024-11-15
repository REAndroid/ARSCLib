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
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.utils.collection.*;

import java.io.IOException;
import java.util.Iterator;

public class ResXmlElementChunk extends FixedBlockContainer {

    private final BlockList<ResXmlStartNamespace> mStartNamespaceList;
    private final ResXmlStartElement mStartElement;
    private final ResXmlNodeList mNodeList;
    private final ResXmlEndElement mEndElement;
    private final BlockList<ResXmlEndNamespace> mEndNamespaceList;

    public ResXmlElementChunk() {
        super(5);

        this.mStartNamespaceList = new ResXmlChunkList<>();
        ResXmlEndElement endElement = new ResXmlEndElement();
        this.mStartElement = new ResXmlStartElement(endElement);
        this.mNodeList = new ResXmlNodeList();
        this.mEndElement = endElement;
        this.mEndNamespaceList = new ResXmlChunkList<>();

        addChild(0, mStartNamespaceList);
        addChild(1, mStartElement);
        addChild(2, mNodeList);
        addChild(3, mEndElement);
        addChild(4, mEndNamespaceList);
    }

    public ResXmlNodeList getNodeList() {
        return mNodeList;
    }

    private ResXmlElement element(){
        return getParentInstance(ResXmlElement.class);
    }

    public ResXmlNamespace newNamespace(String uri, String prefix){
        return createXmlStartNamespace(uri, prefix);
    }
    void onPreRemove(){
        mStartNamespaceList.clearChildes();
        mEndNamespaceList.clearChildes();
        mStartElement.onPreRemove();
    }
    public void setName(String name) {
        ResXmlStartElement startElement = getStartElement();
        if(name == null){
            startElement.setName(null);
            return;
        }
        String prefix = null;
        int i = name.lastIndexOf(':');
        if(i >= 0){
            prefix = name.substring(0, i);
            i++;
            name = name.substring(i);
        }
        startElement.setName(name);
        if(prefix == null){
            return;
        }
        ResXmlNamespace namespace = element()
                .getOrCreateNamespaceByPrefix(prefix);
        if (namespace != null) {
            startElement.setNamespaceReference(namespace.getUriReference());
        }
    }

    private ResXmlStartNamespace createXmlStartNamespace(){
        return createXmlStartNamespace(null, null);
    }
    private ResXmlStartNamespace createXmlStartNamespace(String uri, String prefix) {
        if (uri != null) {
            updateStartNamespaces();
        }

        ResXmlStartNamespace startNamespace = new ResXmlStartNamespace();
        ResXmlEndNamespace endNamespace = new ResXmlEndNamespace();
        startNamespace.setEnd(endNamespace);

        mStartNamespaceList.add(startNamespace);
        mEndNamespaceList.add(0, endNamespace);

        if (uri != null) {
            startNamespace.setNamespace(uri, prefix);
        }
        return startNamespace;
    }
    private void updateStartNamespaces() {
        Iterator<ResXmlNamespace> iterator = element().getAllNamespaces();
        while (iterator.hasNext()) {
            ResXmlStartNamespace namespace = (ResXmlStartNamespace) iterator.next();
            namespace.ensureUniqueUri();
        }
    }
    BlockList<ResXmlStartNamespace> getStartNamespaceList(){
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
        boolean startElementRead = false;
        boolean endElementRead = false;
        ArrayCollection<ResXmlEndNamespace> openedNamespaces = new ArrayCollection<>();
        while (true) {
            HeaderBlock headerBlock = reader.readHeaderBlock();
            ChunkType chunkType = headerBlock.getChunkType();
            if (chunkType == ChunkType.XML_START_ELEMENT) {
                if (!startElementRead) {
                    getStartElement().readBytes(reader);
                    startElementRead = true;
                } else if (!endElementRead) {
                    element().newElement().readBytes(reader);
                }
            } else if (chunkType == ChunkType.XML_END_ELEMENT) {
                if (!endElementRead) {
                    getEndElement().readBytes(reader);
                    endElementRead = true;
                } else {
                    unBalancedFinish();
                }
            } else if (chunkType == ChunkType.XML_START_NAMESPACE) {
                ResXmlStartNamespace startNamespace = createXmlStartNamespace();
                startNamespace.readBytes(reader);
                openedNamespaces.add(startNamespace.getEnd());
            } else if (chunkType == ChunkType.XML_END_NAMESPACE) {
                ResXmlEndNamespace endNamespace = openedNamespaces
                        .remove(openedNamespaces.size() - 1);
                endNamespace.readBytes(reader);
            } else if (!endElementRead && chunkType == ChunkType.XML_CDATA) {
                element().newText().readBytes(reader);
            } else if (!endElementRead && chunkType == ChunkType.XML) {
                element().newDocument().readBytes(reader);
            }
            if (startElementRead && endElementRead && openedNamespaces.isEmpty()) {
                return;
            }
        }
    }

    private void unBalancedFinish() throws IOException{
        throw new IOException("Unbalanced end element");
    }
}
