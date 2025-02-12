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
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.chunk.Chunk;
import com.reandroid.arsc.container.SingleBlockContainer;
import com.reandroid.arsc.header.XmlNodeHeader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.ResXmlString;
import com.reandroid.arsc.pool.ResXmlStringPool;
import com.reandroid.utils.ObjectsUtil;

class BaseXmlChunk extends Chunk<XmlNodeHeader> {

    private final IntegerItem mNamespaceReference;
    private final IntegerItem mStringReference;

    BaseXmlChunk(ChunkType chunkType, SingleBlockContainer<Block> firstPlaceHolder, int initialChildesCount) {
        super(new XmlNodeHeader(chunkType), firstPlaceHolder, initialChildesCount + 2);

        this.mNamespaceReference = new IntegerItem(NULL_REFERENCE);
        this.mStringReference = new IntegerItem(NULL_REFERENCE);

        addChild(mNamespaceReference);
        addChild(mStringReference);
    }
    BaseXmlChunk(ChunkType chunkType, int initialChildesCount) {
        this(chunkType, new SingleBlockContainer<>(), initialChildesCount);
    }

    void onPreRemove() {
        ResXmlStringPool stringPool = getStringPool();
        if (stringPool == null) {
            return;
        }
        stringPool.removeReference(getHeaderBlock().getCommentReference());
        stringPool.removeReference(mNamespaceReference);
        stringPool.removeReference(mStringReference);
    }
    void linkStringReferences() {
        linkStringReference(getHeaderBlock().getCommentReference());
        linkStringReference(mNamespaceReference);
        linkStringReference(mStringReference);
    }
    private void linkStringReference(IntegerItem item) {
        ResXmlString xmlString = getResXmlString(item.get());
        if (xmlString != null) {
            xmlString.addReferenceIfAbsent(item);
        }
    }
    void unLinkStringReference(IntegerItem item) {
        ResXmlString xmlString = getResXmlString(item.get());
        if (xmlString!=null) {
            xmlString.removeReference(item);
        }
    }
    public void setLineNumber(int val) {
        getHeaderBlock().getLineNumber().set(val);
    }
    public int getLineNumber() {
        return getHeaderBlock().getLineNumber().get();
    }
    public void setCommentReference(int reference) {
        if (reference == getCommentReference()) {
            return;
        }
        IntegerItem comment = getHeaderBlock().getCommentReference();
        unLinkStringReference(comment);
        getHeaderBlock().getCommentReference().set(reference);
        linkStringReference(comment);
    }
    public int getCommentReference() {
        return getHeaderBlock().getCommentReference().get();
    }
    void setNamespaceReference(int value) {
        if (value == getNamespaceReference()) {
            return;
        }
        unLinkStringReference(mNamespaceReference);
        mNamespaceReference.set(value);
        linkStringReference(mNamespaceReference);
    }
    int getNamespaceReference() {
        return mNamespaceReference.get();
    }
    void setStringReference(int reference) {
        if (reference == getStringReference()) {
            return;
        }
        unLinkStringReference(mStringReference);
        mStringReference.set(reference);
        linkStringReference(mStringReference);
    }
    int getStringReference() {
        return mStringReference.get();
    }
    void setString(String str) {
        ResXmlStringPool stringPool = getStringPool();
        if (stringPool != null) {
            ResXmlString xmlString = stringPool.getOrCreate(str);
            setStringReference(xmlString.getIndex());
        }
    }
    ResXmlStringPool getStringPool() {
        Block parent = getParent();
        while (parent != null) {
            if (parent instanceof ResXmlDocument) {
                return ((ResXmlDocument)parent).getStringPool();
            }
            if (parent instanceof ResXmlElement) {
                return ((ResXmlElement)parent).getStringPool();
            }
            parent = parent.getParent();
        }
        return null;
    }
    ResXmlString getResXmlString(int reference) {
        if (reference == NULL_REFERENCE) {
            return null;
        }
        ResXmlStringPool stringPool = getStringPool();
        if (stringPool != null) {
            return stringPool.get(reference);
        }
        return null;
    }
    String getString(int reference) {
        ResXmlString xmlString = getResXmlString(reference);
        if (xmlString != null) {
            return xmlString.get();
        }
        return null;
    }
    ResXmlString getOrCreateString(String str) {
        ResXmlStringPool stringPool = getStringPool();
        if (stringPool == null) {
            return null;
        }
        return stringPool.getOrCreate(str);
    }
    int getOrCreateStringReference(String str) {
        ResXmlString xmlString = getOrCreateString(str);
        if (xmlString != null) {
            return xmlString.getIndex();
        }
        return NULL_REFERENCE;
    }

    public String getName() {
        return getString(getStringReference());
    }
    public String getUri() {
        return getString(getNamespaceReference());
    }
    public String getComment() {
        return getString(getCommentReference());
    }
    public void setComment(String comment) {
        if (comment == null || comment.length() == 0) {
            setCommentReference(NULL_REFERENCE);
        } else {
            String old = getComment();
            if (comment.equals(old)) {
                return;
            }
            ResXmlString xmlString = getOrCreateString(comment);
            setCommentReference(xmlString.getIndex());
        }
    }
    public ResXmlElement getNodeElement() {
        return getParentInstance(ResXmlElement.class);
    }
    @Override
    protected void onChunkRefreshed() {
    }

    @Override
    public String toString() {
        ChunkType chunkType = getHeaderBlock().getChunkType();
        if (chunkType == null) {
            return super.toString();
        }
        return chunkType.toString() + ": line=" + getLineNumber() +
                " {" + getName() + "}";
    }

    public static final int NULL_REFERENCE = ObjectsUtil.of(-1);
}
