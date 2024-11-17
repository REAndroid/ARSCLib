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
import com.reandroid.arsc.item.ResXmlString;

abstract class ResXmlNamespaceChunk extends BaseXmlChunk implements ResXmlNamespace {

    ResXmlNamespaceChunk(ChunkType chunkType) {
        super(chunkType, 0);
    }

    @Override
    public String getUri() {
        return getString(getUriReference());
    }
    @Override
    public void setUri(String uri) {
        if (uri == null) {
            setUriReference(-1);
            return;
        }
        ResXmlString xmlString = getOrCreateString(uri);
        if(xmlString == null){
            throw new IllegalArgumentException("Null ResXmlString, add to parent element first");
        }
        setUriReference(xmlString.getIndex());
    }
    @Override
    public String getPrefix(){
        return getString(getPrefixReference());
    }
    @Override
    public void setPrefix(String prefix) {
        if(prefix == null) {
            setPrefixReference(-1);
            return;
        }
        ResXmlString xmlString = null;
        ResXmlString uriString = getResXmlString(getUriReference());
        if (uriString != null) {
            xmlString = uriString.getNamespacePrefix();
        }
        if (xmlString == null) {
            xmlString = getOrCreateString(prefix);
        }
        if(xmlString == null) {
            throw new IllegalArgumentException("Null ResXmlString, add to parent element first");
        }
        setPrefixReference(xmlString.getIndex());
    }
    @Override
    public int getUriReference(){
        return getStringReference();
    }
    void setUriReference(int ref) {
        int old = getUriReference();
        setStringReference(ref);
        if (old != ref) {
            onUriReferenceChanged(old, ref);
        }
    }
    void onUriReferenceChanged(int old, int uriReference){

    }

    public int getPrefixReference() {
        return getNamespaceReference();
    }
    public void setPrefixReference(int ref) {
        setNamespaceReference(ref);
    }
    @Override
    public void setLineNumber(int lineNumber) {
        super.setLineNumber(lineNumber);
    }
    public boolean isRemoved() {
        return getParent() == null;
    }
    @Override
    public boolean isUnused() {
        return isRemoved();
    }
    @Override
    public String toString() {
        return "xmlns:" + getPrefix() + "=\"" + getUri() + "\"";
    }
}
