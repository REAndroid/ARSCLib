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
import com.reandroid.arsc.container.SingleBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.BlockItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.utils.StringsUtil;

import java.io.IOException;

public class ResXmlStartElement extends BaseXmlChunk {

    private final ShortItem mAttributeStart;
    private final IntegerReference mAttributeUnitSize;
    private final ShortItem mAttributeCount;

    private final ResXmlAttributePosition mIdAttributePosition;
    private final ResXmlAttributePosition mClassAttributePosition;
    private final ResXmlAttributePosition mStyleAttributePosition;

    private final SingleBlockContainer<Block> firstPlaceHolder2;

    private final ResXmlAttributeArray mAttributeArray;

    private final ResXmlEndElement mResXmlEndElement;

    private final IntegerReference attributesOffset;

    private final UnknownBytes unknownBytes;

    public ResXmlStartElement(ResXmlEndElement endElement) {
        super(ChunkType.XML_START_ELEMENT, null, 9);

        this.mResXmlEndElement = endElement;

        mAttributeStart = new ShortItem(ATTRIBUTES_DEFAULT_START);
        ShortItem _unitSizeReferenceBlock = new ShortItem(ATTRIBUTES_UNIT_SIZE);
        ShortItem _attributeCount = new ShortItem();
        mAttributeCount = _attributeCount;

        mIdAttributePosition = new ResXmlAttributePosition(ResXmlAttributePosition.TYPE_ID);
        mClassAttributePosition = new ResXmlAttributePosition(ResXmlAttributePosition.TYPE_CLASS);
        mStyleAttributePosition = new ResXmlAttributePosition(ResXmlAttributePosition.TYPE_STYLE);

        firstPlaceHolder2 = new SingleBlockContainer<>();

        this.attributesOffset = new IntegerReference() {

            private final ResXmlStartElement _startElement = ResXmlStartElement.this;

            @Override
            public int get() {
                return _startElement.getHeaderBlock().getHeaderSize()
                        + _startElement.getAttributeStart().get();
            }
            @Override
            public void set(int value) {
                _startElement.getAttributeStart().set(value -
                        _startElement.getHeaderBlock().countBytes());
            }
            @Override
            public String toString() {
                return Integer.toString(get());
            }
        };

        this.unknownBytes = new UnknownBytes(this);


        IntegerReference unitSize = new IntegerReference() {
            private final ResXmlStartElement _startElement = ResXmlStartElement.this;
            private final IntegerReference _reference = _unitSizeReferenceBlock;
            @Override
            public int get() {
                int i = _reference.get();
                if (i < 20 && _startElement.getAttributeCount().get() != 0) {
                    i = 20;
                    _reference.set(i);
                }
                return i;
            }
            @Override
            public void set(int value) {
                _reference.set(value);
                ResXmlAttributeArray attributeArray = _startElement.getResXmlAttributeArray();
                int count = attributeArray.size();
                for (int i = 0; i < count; i++) {
                    attributeArray.get(i).setAttributesUnitSize(value);
                }
            }
            @Override
            public String toString() {
                return Integer.toString(_reference.get());
            }
        };
        this.mAttributeUnitSize = unitSize;

        this.mAttributeArray = new ResXmlAttributeArray(unitSize, _attributeCount);

        addChild(mAttributeStart);
        addChild(_unitSizeReferenceBlock);
        addChild(_attributeCount);
        addChild(mIdAttributePosition);
        addChild(mClassAttributePosition);
        addChild(mStyleAttributePosition);
        addChild(firstPlaceHolder2);
        addChild(unknownBytes);
        addChild(mAttributeArray);
    }

    IntegerReference getAttributeStart() {
        return mAttributeStart;
    }
    IntegerReference getAttributeCount() {
        return mAttributeCount;
    }
    IntegerReference getAttributeUnitSize() {
        return mAttributeUnitSize;
    }

    ResXmlAttributePosition getIdAttributePosition() {
        return mIdAttributePosition;
    }
    ResXmlAttributePosition getClassAttributePosition() {
        return mClassAttributePosition;
    }
    ResXmlAttributePosition getStyleAttributePosition() {
        return mStyleAttributePosition;
    }


    public IntegerReference getAttributesOffset() {
        return attributesOffset;
    }
    public UnknownBytes getUnknownBytes() {
        return unknownBytes;
    }

    @Override
    void linkStringReferences() {
        super.linkStringReferences();

        getResXmlEndElement().linkStringReferences();

        linkNamespace();

        getIdAttributePosition().linkAttribute();
        getClassAttributePosition().linkAttribute();
        getStyleAttributePosition().linkAttribute();
    }
    @Override
    void onPreRemove() {
        super.onPreRemove();
        unlinkNamespace();
        getResXmlAttributeArray().clear();
        getResXmlEndElement().onPreRemove();

        getIdAttributePosition().clear();
        getClassAttributePosition().clear();
        getStyleAttributePosition().clear();
    }
    void unlinkNamespace() {
        ResXmlStartNamespace namespace = getResXmlStartNamespace();
        if (namespace != null) {
            namespace.removeElementReference(this);
        }
    }
    void linkNamespace() {
        ResXmlStartNamespace namespace = getResXmlStartNamespace();
        if (namespace != null) {
            namespace.addElementReference(this);
        }
    }
    public String getName(boolean includePrefix) {
        String name = super.getName();
        if (includePrefix) {
            String prefix = getPrefix();
            if (prefix != null) {
                name = prefix + ":" + name;
            }
        }
        return name;
    }
    public void setName(String name) {
        if (name == null) {
            setStringReference(-1);
        } else {
            setString(name);
        }
    }
    public ResXmlAttributeArray getResXmlAttributeArray() {
        return mAttributeArray;
    }

    public String getUri() {
        ResXmlStartNamespace startNamespace = getResXmlStartNamespace();
        if (startNamespace != null) {
            return startNamespace.getUri();
        }
        return null;
    }
    public String getPrefix() {
        ResXmlStartNamespace startNamespace = getResXmlStartNamespace();
        if (startNamespace != null) {
            return startNamespace.getPrefix();
        }
        return null;
    }
    public void setTagNamespace(String uri, String prefix) {
        unlinkNamespace();
        if (uri == null || prefix == null) {
            setNamespaceReference(-1);
            return;
        }
        ResXmlElement parentElement = getNodeElement();
        if (parentElement == null) {
            return;
        }
        ResXmlNamespace ns = parentElement.getOrCreateNamespace(uri, prefix);
        setNamespaceReference(ns.getUriReference());
        linkNamespace();
    }

    @Override
    void setNamespaceReference(int value) {
        super.setNamespaceReference(value);
        getResXmlEndElement().setNamespaceReference(value);
    }

    @Override
    void setStringReference(int value) {
        super.setStringReference(value);
        getResXmlEndElement().setStringReference(value);
    }

    ResXmlStartNamespace getResXmlStartNamespace() {
        int uriRef = getNamespaceReference();
        if (uriRef != NULL_REFERENCE) {
            ResXmlElement parentElement = getNodeElement();
            if (parentElement != null) {
                return (ResXmlStartNamespace) parentElement
                        .getNamespaceForUriReference(uriRef);
            }
        }
        return null;
    }
    public ResXmlEndElement getResXmlEndElement() {
        return mResXmlEndElement;
    }

    @Override
    protected void onPreRefresh() {
        super.onPreRefresh();
        getResXmlAttributeArray().sort();
    }
    @Override
    public void setLineNumber(int lineNumber) {
        super.setLineNumber(lineNumber);
        getResXmlEndElement().setLineNumber(lineNumber);
    }

    @Override
    public SingleBlockContainer<Block> getFirstPlaceHolder() {
        return firstPlaceHolder2;
    }

    @Override
    protected void onChunkRefreshed() {
        super.onChunkRefreshed();
        updateAttributesOffset();
        refreshAttributePositions();
    }
    void updateAttributesOffset() {
        if (getAttributeCount().get() != 0) {
            getAttributesOffset().set(this.countUpTo(getResXmlAttributeArray()));
        }
    }
    void refreshAttributePositions() {
        getIdAttributePosition().refresh();
        getClassAttributePosition().refresh();
        getStyleAttributePosition().refresh();
    }
    void fixClassStyleAttributeNames() {
        getClassAttributePosition().fixName();
        getStyleAttributePosition().fixName();
    }

    @Override
    public String toString() {
        String name = getName(true);
        if (name == null) {
            return super.toString();
        }
        return name + " " + StringsUtil.join(getResXmlAttributeArray().iterator(), ' ');
    }

    public static class UnknownBytes extends BlockItem {

        private final ResXmlStartElement startElement;

        UnknownBytes(ResXmlStartElement startElement) {
            super(0);
            this.startElement = startElement;
        }

        public int size() {
            return countBytes();
        }
        public void setSize(int size) {
            setBytesLength(size, false);
            startElement.updateAttributesOffset();
        }
        @Override
        public byte[] getBytes() {
            return super.getBytesInternal();
        }
        public void setBytes(byte[] bytes) {
            int length = bytes.length;
            setSize(length);
            if (length != 0) {
                byte[] internal = getBytes();
                System.arraycopy(bytes, 0, internal, 0, length);
            }
        }
        public void clear() {
            setSize(0);
        }
        public boolean isEmpty() {
            return size() == 0;
        }

        @Override
        public void onReadBytes(BlockReader reader) throws IOException {
            ResXmlStartElement startElement = this.startElement;
            int size;
            if (startElement.getAttributeCount().get() == 0) {
                size = 0;
            } else {
                size = startElement.getAttributesOffset().get() - reader.getPosition();
            }
            setBytesLength(size, false);
            super.onReadBytes(reader);
        }

        @Override
        public String toString() {
            return "size = " + size();
        }
    }

    private static final short ATTRIBUTES_UNIT_SIZE = 20;
    private static final short ATTRIBUTES_DEFAULT_START = 20;
}
