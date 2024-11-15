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
import java.util.Iterator;
import java.util.Set;

public class ResXmlStartNamespace extends ResXmlNamespaceChunk
        implements JSONConvert<JSONObject> {

    private final Set<ResXmlAttribute> mReferencedAttributes;
    private final Set<ResXmlStartElement> mReferencedElements;

    public ResXmlStartNamespace() {
        super(ChunkType.XML_START_NAMESPACE);
        this.mReferencedAttributes = new HashSet<>();
        this.mReferencedElements = new HashSet<>();
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
    ResXmlEndNamespace getEnd(){
        return (ResXmlEndNamespace) getPair();
    }
    void setEnd(ResXmlEndNamespace namespace){
        setPair(namespace);
    }
    @Override
    void linkStringReferences(){
        super.linkStringReferences();
        ResXmlEndNamespace end = getEnd();
        if (end != null) {
            end.linkStringReferences();
        }
    }

    @Override
    public void onChunkLoaded() {
        super.onChunkLoaded();
        linkStringReferences();
    }

    @Override
    void onPreRemove(){
        ResXmlEndNamespace end = getEnd();
        if (end != null) {
            end.removeSelf();
        }
        mReferencedAttributes.clear();
        mReferencedElements.clear();
        super.onPreRemove();
    }

    @Override
    public boolean isUnused() {
        if (isRemoved()) {
            return true;
        }
        return mReferencedAttributes.size() == 0
                && mReferencedElements.size() == 0;
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
}
