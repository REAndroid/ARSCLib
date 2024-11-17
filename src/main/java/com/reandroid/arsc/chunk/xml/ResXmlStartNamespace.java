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
import com.reandroid.arsc.model.ResourceLibrary;
import com.reandroid.arsc.pool.ResXmlStringPool;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.StringsUtil;

import java.util.HashSet;
import java.util.Set;

public class ResXmlStartNamespace extends ResXmlNamespaceChunk
        implements JSONConvert<JSONObject> {

    private final Set<ResXmlAttribute> mReferencedAttributes;
    private final Set<ResXmlStartElement> mReferencedElements;
    private final ResXmlEndNamespace mEndNamespace;

    public ResXmlStartNamespace(ResXmlEndNamespace endNamespace) {
        super(ChunkType.XML_START_NAMESPACE);
        this.mEndNamespace = endNamespace;
        this.mReferencedAttributes = new HashSet<>();
        this.mReferencedElements = new HashSet<>();
    }

    public ResXmlElement element() {
        return getParentInstance(ResXmlElement.class);
    }

    void ensureUniqueUri() {
        ResXmlString xmlString = getResXmlString(getUriReference());
        if (xmlString != null) {
            ResXmlString prefixXmlString = xmlString.getNamespacePrefix();
            if (prefixXmlString != null && prefixXmlString.getIndex() == getPrefixReference()) {
                return;
            }
        }
        setNamespace(getUri(), getPrefix());
    }
    void setNamespace(String uri, String prefix) {
        ResXmlStringPool stringPool = getStringPool();
        if (stringPool == null) {
            return;
        }
        ResXmlString resXmlString = stringPool.getOrCreateNamespaceString(uri, prefix);
        if (resXmlString == null) {
            return;
        }
        setUriReference(resXmlString.getIndex());
        setPrefixReference(resXmlString.getNamespacePrefix().getIndex());
    }

    @Override
    void onUriReferenceChanged(int old, int uriReference){
        for(ResXmlAttribute attribute : mReferencedAttributes){
            attribute.setUriReference(uriReference);
        }
        for(ResXmlStartElement element : mReferencedElements){
            element.setNamespaceReference(uriReference);
        }
    }
    @Override
    public void onChunkLoaded() {
        super.onChunkLoaded();
        linkStringReferences();
    }
    ResXmlEndNamespace getEnd() {
        return mEndNamespace;
    }

    @Override
    void setStringReference(int value) {
        super.setStringReference(value);
        getEnd().setStringReference(value);
    }
    @Override
    void setNamespaceReference(int value) {
        super.setNamespaceReference(value);
        getEnd().setNamespaceReference(value);
    }

    @Override
    public void setLineNumber(int lineNumber) {
        if (lineNumber != getLineNumber()) {
            super.setLineNumber(lineNumber);
            getEnd().setLineNumber(lineNumber);
        }
    }

    @Override
    void onPreRemove() {
        mReferencedAttributes.clear();
        mReferencedElements.clear();
        super.onPreRemove();
    }

    @Override
    protected void onPreRefresh() {
        super.onPreRefresh();
        ResXmlEndNamespace end = getEnd();
        end.setNamespaceReference(getNamespaceReference());
        end.setStringReference(getStringReference());
    }

    @Override
    public boolean isUnused() {
        return getReferencedCount() == 0;
    }
    public boolean isUndefined() {
        if (isRemoved()) {
            return true;
        }
        return getUriReference() == -1 &&
                getPrefixReference() == -1;
    }
    public int getReferencedCount() {
        if (isRemoved()) {
            return 0;
        }
        return mReferencedAttributes.size() + mReferencedElements.size();
    }
    public boolean isBetterThan(ResXmlStartNamespace namespace) {
        if (namespace == null) {
            return true;
        }
        return getReferencedCount() > namespace.getReferencedCount();
    }

    void addAttributeReference(ResXmlAttribute attribute){
        if(attribute != null){
            mReferencedAttributes.add(attribute);
        }
    }
    void removeAttributeReference(ResXmlAttribute attribute){
        if(attribute != null){
            mReferencedAttributes.remove(attribute);
        }
    }
    void addElementReference(ResXmlStartElement element){
        if(element != null){
            mReferencedElements.add(element);
        }
    }
    void removeElementReference(ResXmlStartElement element){
        if(element != null){
            mReferencedElements.remove(element);
        }
    }
    boolean fixEmpty() {
        boolean changed = fixEmptyPrefix();
        if(fixEmptyUri()){
            changed = true;
        }
        return changed;
    }
    private boolean fixEmptyPrefix(){
        if(!StringsUtil.isBlank(getPrefix())){
            return false;
        }
        setPrefix("ns" + getIndex());
        return true;
    }
    private boolean fixEmptyUri(){
        if(!StringsUtil.isBlank(getUri())){
            return false;
        }
        setUri(ResourceLibrary.URI_RES_AUTO);
        return true;
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ResXmlNode.JSON_uri, getUri());
        jsonObject.put(ResXmlNode.JSON_prefix, getPrefix());
        jsonObject.put(ResXmlNode.JSON_line, getLineNumber());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setNamespace(json.getString(ResXmlElement.JSON_uri),
                json.getString(ResXmlElement.JSON_prefix));
        setLineNumber(json.optInt(ResXmlElement.JSON_line));
    }

    public void merge(ResXmlStartNamespace namespace) {
        if (namespace == this) {
            return;
        }
        this.setNamespace(namespace.getUri(), namespace.getPrefix());
        this.setLineNumber(namespace.getLineNumber());
        this.setComment(namespace.getComment());
        ResXmlEndNamespace end = this.getEnd();
        ResXmlEndNamespace coming = namespace.getEnd();
        end.setLineNumber(coming.getLineNumber());
        end.setComment(coming.getComment());
    }
}
