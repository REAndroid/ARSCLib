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
import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.chunk.Chunk;
import com.reandroid.arsc.header.XmlNodeHeader;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;
import java.io.OutputStream;


/**
 * A wrapper api class to expose all methods of hidden class {@link BaseXmlChunk}
 * */
@SuppressWarnings("unused")
public class ResXmlChunkApi implements BlockRefresh {

    private final BaseXmlChunk chunk;

    public ResXmlChunkApi(Chunk<XmlNodeHeader> chunk) {
        this.chunk = (BaseXmlChunk) chunk;
    }

    public int getLineNumber() {
        return chunk.getLineNumber();
    }
    public void setLineNumber(int val) {
        chunk.setLineNumber(val);
    }

    public String getComment() {
        return chunk.getComment();
    }
    public void setComment(String comment) {
        chunk.setComment(comment);
    }

    public int getCommentReference() {
        return chunk.getCommentReference();
    }
    public void setCommentReference(int reference) {
        chunk.setCommentReference(reference);
    }


    public String getNamespace() {
        return chunk.getString(getNamespaceReference());
    }
    public void setNamespace(String namespace) {
        setNamespaceReference(chunk.getOrCreateStringReference(namespace));
    }
    public int getNamespaceReference() {
        return chunk.getNamespaceReference();
    }
    public void setNamespaceReference(int reference) {
        chunk.setNamespaceReference(reference);
    }

    public String getName() {
        return chunk.getName();
    }
    public void setName(String name) {
        chunk.setString(name);
    }

    public String getString() {
        return chunk.getString(getStringReference());
    }
    public void setString(String str) {
        chunk.setString(str);
    }

    public int getStringReference() {
        return chunk.getStringReference();
    }
    public void setStringReference(int value) {
        chunk.setStringReference(value);
    }

    public XmlNodeHeader getHeader() {
        return chunk.getHeaderBlock();
    }
    public int getIndex() {
        return chunk.getIndex();
    }
    public void setIndex(int index) {
        chunk.setIndex(index);
    }
    public Block getParent() {
        return chunk.getParent();
    }
    public void setParent(Block parent) {
        chunk.setParent(parent);
    }
    public <T> T getParent(Class<T> parentClass) {
        return chunk.getParent(parentClass);
    }
    public <T> T getParentInstance(Class<T> parentClass) {
        return chunk.getParentInstance(parentClass);
    }
    @Override
    public void refresh() {
        chunk.refresh();
    }
    public byte[] getBytes() {
        return chunk.getBytes();
    }
    public int countBytes() {
        return chunk.countBytes();
    }
    public void readBytes(BlockReader reader) throws IOException {
        chunk.readBytes(reader);
    }
    public int writeBytes(OutputStream stream) throws IOException {
        return chunk.writeBytes(stream);
    }
    public Chunk<XmlNodeHeader> getChunk() {
        return chunk;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ResXmlChunkApi resXmlChunkApi = (ResXmlChunkApi) obj;
        return ObjectsUtil.equals(chunk, resXmlChunkApi.chunk);
    }

    @Override
    public int hashCode() {
        return ObjectsUtil.hash(chunk);
    }

    @Override
    public String toString() {
        return chunk.toString();
    }
}
