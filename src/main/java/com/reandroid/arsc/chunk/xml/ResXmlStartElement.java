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
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.utils.StringsUtil;

import java.util.Iterator;

public class ResXmlStartElement extends BaseXmlChunk {

    private final ShortItem mAttributeStart;
    private final ShortItem mAttributeUnitSize;
    private final ShortItem mAttributeCount;

    private final ShortItem mIdAttributePosition;
    private final ShortItem mClassAttributePosition;
    private final ShortItem mStyleAttributePosition;

    private final ResXmlAttributeArray mAttributeArray;

    private final ResXmlEndElement mResXmlEndElement;

    public ResXmlStartElement(ResXmlEndElement endElement) {
        super(ChunkType.XML_START_ELEMENT, 7);

        this.mResXmlEndElement = endElement;

        mAttributeStart = new ShortItem(ATTRIBUTES_DEFAULT_START);
        mAttributeUnitSize = new ShortItem(ATTRIBUTES_UNIT_SIZE);
        mAttributeCount = new ShortItem();
        mIdAttributePosition = new ShortItem();
        mClassAttributePosition = new ShortItem();
        mStyleAttributePosition = new ShortItem();
        mAttributeArray = new ResXmlAttributeArray(mAttributeCount);

        addChild(mAttributeStart);
        addChild(mAttributeUnitSize);
        addChild(mAttributeCount);
        addChild(mIdAttributePosition);
        addChild(mClassAttributePosition);
        addChild(mStyleAttributePosition);
        addChild(mAttributeArray);
    }

    IntegerReference getAttributeStart() {
        return mAttributeStart;
    }
    IntegerReference getAttributeUnitSize() {
        return mAttributeUnitSize;
    }
    IntegerReference getIdAttributePosition() {
        return mIdAttributePosition;
    }
    IntegerReference getClassAttributePosition() {
        return mClassAttributePosition;
    }
    IntegerReference getStyleAttributePosition() {
        return mStyleAttributePosition;
    }

    public ResXmlAttribute getIdAttribute() {
        return getResXmlAttributeArray().get(mIdAttributePosition.get() - 1);
    }
    public ResXmlAttribute getClassAttribute() {
        return getResXmlAttributeArray().get(mClassAttributePosition.unsignedInt() - 1);
    }
    public ResXmlAttribute getStyleAttribute() {
        return getResXmlAttributeArray().get(mStyleAttributePosition.unsignedInt() - 1);
    }
    @Override
    void linkStringReferences(){
        super.linkStringReferences();
        getResXmlEndElement().linkStringReferences();
        linkNamespace();
    }
    @Override
    void onPreRemove(){
        super.onPreRemove();
        unlinkNamespace();
        getResXmlAttributeArray().clear();
        getResXmlEndElement().onPreRemove();
    }
    void unlinkNamespace(){
        ResXmlStartNamespace namespace = getResXmlStartNamespace();
        if(namespace != null){
            namespace.removeElementReference(this);
        }
    }
    void linkNamespace(){
        ResXmlStartNamespace namespace = getResXmlStartNamespace();
        if(namespace != null){
            namespace.addElementReference(this);
        }
    }
    public String getName(boolean includePrefix){
        String name = super.getName();
        if (includePrefix) {
            String prefix = getPrefix();
            if (prefix != null) {
                name = prefix + ":" + name;
            }
        }
        return name;
    }
    public void setName(String name){
        if (name == null) {
            setStringReference(-1);
        } else {
            setString(name);
        }
    }
    public Iterator<ResXmlAttribute> iterator(){
        return getResXmlAttributeArray().iterator();
    }
    public ResXmlAttributeArray getResXmlAttributeArray(){
        return mAttributeArray;
    }

    public String getUri(){
        ResXmlStartNamespace startNamespace = getResXmlStartNamespace();
        if(startNamespace != null){
            return startNamespace.getUri();
        }
        return null;
    }
    public String getPrefix(){
        ResXmlStartNamespace startNamespace = getResXmlStartNamespace();
        if(startNamespace != null){
            return startNamespace.getPrefix();
        }
        return null;
    }
    public void setTagNamespace(String uri, String prefix){
        unlinkNamespace();
        if(uri == null || prefix == null){
            setNamespaceReference(-1);
            return;
        }
        ResXmlElement parentElement = getNodeElement();
        if(parentElement == null){
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

    ResXmlStartNamespace getResXmlStartNamespace(){
        int uriRef = getNamespaceReference();
        if (uriRef != -1) {
            ResXmlElement parentElement = getNodeElement();
            if (parentElement != null) {
                return (ResXmlStartNamespace) parentElement
                        .getNamespaceForUriReference(uriRef);
            }
        }
        return null;
    }
    public ResXmlEndElement getResXmlEndElement(){
        return mResXmlEndElement;
    }

    @Override
    protected void onPreRefresh(){
        super.onPreRefresh();
        getResXmlAttributeArray().sort();
    }
    @Override
    protected void onChunkRefreshed() {
        super.onChunkRefreshed();
        refreshAttributeStart();
        refreshAttributeCount();
    }
    private void refreshAttributeStart(){
        int start = countUpTo(mAttributeArray);
        start = start - getHeaderBlock().getHeaderSize();
        mAttributeStart.set(start);
    }
    private void refreshAttributeCount(){
        int count = mAttributeArray.size();
        mAttributeCount.set(count);
    }
    @Override
    public void setLineNumber(int lineNumber){
        super.setLineNumber(lineNumber);
        getResXmlEndElement().setLineNumber(lineNumber);
    }

    @Override
    public String toString() {
        String name = getName(true);
        if (name == null) {
            return super.toString();
        }
        return name + " " + StringsUtil.join(iterator(), ' ');
    }

    private static final short ATTRIBUTES_UNIT_SIZE = 20;
    private static final short ATTRIBUTES_DEFAULT_START = 20;
}
