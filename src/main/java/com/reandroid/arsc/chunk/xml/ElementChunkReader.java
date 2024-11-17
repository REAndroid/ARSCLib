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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.chunk.UnknownChunk;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.utils.collection.ArrayCollection;

import java.io.IOException;

class ElementChunkReader {

    private final ResXmlElementChunk elementChunk;

    // First in last out
    private ArrayCollection<ResXmlEndNamespace> endNamespaceStack;

    private boolean startElementRead = false;
    private boolean endElementRead = false;

    public ElementChunkReader(ResXmlElementChunk elementChunk) {
        this.elementChunk = elementChunk;
        this.endNamespaceStack = new ArrayCollection<>();
    }

    public void read(BlockReader reader) throws IOException {
        while (reader.isAvailable()) {
            Block block = getNext(reader.readHeaderBlock());
            if (block == null) {
                break;
            }
            block.readBytes(reader);
        }
    }
    private Block getNext(HeaderBlock headerBlock) throws IOException {
        if (!startElementRead) {
            return onElementNotStarted(headerBlock);
        }
        if (!endElementRead) {
            return onElementNotEnded(headerBlock);
        }
        return onElementEnded(headerBlock);
    }
    private Block onElementNotStarted(HeaderBlock headerBlock) throws IOException {
        ChunkType chunkType = headerBlock.getChunkType();
        if (chunkType == ChunkType.XML_START_ELEMENT) {
            startElementRead = true;
            return elementChunk.getStartElement();
        }
        if (chunkType == ChunkType.XML_END_ELEMENT) {
            // unlikely, lets throw for now
            throw new IOException("END element detected before START: " + headerBlock);
        }
        if (chunkType == ChunkType.XML_START_NAMESPACE) {
            return newNamespace();
        }
        if (chunkType == ChunkType.XML_END_NAMESPACE) {
            ResXmlEndNamespace endNamespace = pickEndNamespace();
            if (endNamespace == null) {
                // unlikely, unbalanced namespaces, instead of
                // throw lets discard/skip the bytes
                startElementRead = true;
                endElementRead = true;
                return newDiscardingChunk();
            }
            return endNamespace;
        }
        if (chunkType == ChunkType.XML_CDATA) {
            // TEXT detected before start, thus it belongs to parent node.
            return elementChunk.element().getParentNode().newText();
        }
        throw new IOException("Unexpected chunk: " + headerBlock);
    }
    private Block onElementNotEnded(HeaderBlock headerBlock) {
        ChunkType chunkType = headerBlock.getChunkType();
        if (chunkType == ChunkType.XML_END_ELEMENT) {
            endElementRead = true;
            return elementChunk.getEndElement();
        }
        if (chunkType == ChunkType.XML_END_NAMESPACE) {
            ResXmlEndNamespace endNamespace = pickEndNamespace();
            if (endNamespace == null) {
                // unlikely, now we are waiting for XML_END_ELEMENT,
                // lets skip
                return newDiscardingChunk();
            }
            return endNamespace;
        }
        if (chunkType == ChunkType.XML_START_ELEMENT
                || chunkType == ChunkType.XML_START_NAMESPACE) {
            return elementChunk.element().newElement();
        }
        if (chunkType == ChunkType.XML_CDATA) {
            return elementChunk.element().newText();
        }
        if (chunkType == ChunkType.XML) {
            // unlikely, document under element not allowed
            // by xml standard, but android permits it
            return elementChunk.element().newDocument();
        }
        return elementChunk.element().newUnknown();
    }
    private Block onElementEnded(HeaderBlock headerBlock) {
        if (!hasEndNamespaceStack()) {
            // properly completed
            return null;
        }
        ChunkType chunkType = headerBlock.getChunkType();
        if (chunkType == ChunkType.XML_END_NAMESPACE) {
            return pickEndNamespace();
        }
        return null;
    }

    private Block newDiscardingChunk() {
        return new UnknownChunk();
    }

    private ResXmlStartNamespace newNamespace() {
        ResXmlStartNamespace startNamespace = elementChunk
                .getStartNamespaceList().createNext();
        putEndNamespace(startNamespace.getEnd());
        return startNamespace;
    }

    private boolean hasEndNamespaceStack() {
        ArrayCollection<ResXmlEndNamespace> stack = this.endNamespaceStack;
        return stack != null && !stack.isEmpty();
    }
    private ResXmlEndNamespace pickEndNamespace() {
        ArrayCollection<ResXmlEndNamespace> stack = this.endNamespaceStack;
        if (stack != null && !stack.isEmpty()) {
            return stack.remove(stack.size() - 1);
        }
        return null;
    }

    private void putEndNamespace(ResXmlEndNamespace endNamespace) {
        ArrayCollection<ResXmlEndNamespace> stack = this.endNamespaceStack;
        if (stack == null) {
            stack = new ArrayCollection<>();
            this.endNamespaceStack = stack;
        }
        stack.add(endNamespace);
    }
}
