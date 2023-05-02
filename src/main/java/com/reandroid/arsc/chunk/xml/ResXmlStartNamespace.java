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
import com.reandroid.xml.SchemaAttr;
import com.reandroid.xml.XMLAttribute;

import java.util.HashSet;
import java.util.Set;

public class ResXmlStartNamespace extends ResXmlNamespace {
    private final Set<ResXmlAttribute> mReferencedAttributes;

    public ResXmlStartNamespace() {
        super(ChunkType.XML_START_NAMESPACE);
        this.mReferencedAttributes = new HashSet<>();
    }
    public ResXmlEndNamespace getEnd(){
        return (ResXmlEndNamespace) getPair();
    }
    public void setEnd(ResXmlEndNamespace namespace){
        setPair(namespace);
    }
    @Override
    void linkStringReferences(){
        super.linkStringReferences();
        ResXmlEndNamespace end = getEnd();
        if(end!=null){
            end.linkStringReferences();
        }
    }
    @Override
    void onRemoved(){
        ResXmlEndNamespace end = getEnd();
        if(end!=null){
            end.onRemoved();
        }
        mReferencedAttributes.clear();
    }
    public boolean hasReferencedAttributes(){
        return mReferencedAttributes.size()>0;
    }
    public void clearReferencedAttributes(){
        mReferencedAttributes.clear();
    }
    public Set<ResXmlAttribute> getReferencedAttributes(){
        return mReferencedAttributes;
    }
    void addAttributeReference(ResXmlAttribute attribute){
        if(attribute!=null){
            mReferencedAttributes.add(attribute);
        }
    }
    void removeAttributeReference(ResXmlAttribute attribute){
        if(attribute!=null){
            mReferencedAttributes.remove(attribute);
        }
    }
    public XMLAttribute decodeToXml(){
        String uri=getUri();
        String prefix=getPrefix();
        if(isEmpty(uri) || isEmpty(prefix)){
            return null;
        }
        SchemaAttr schemaAttr=new SchemaAttr(prefix, uri);
        schemaAttr.setLineNumber(getLineNumber());
        return schemaAttr;
    }
    private boolean isEmpty(String txt){
        if(txt==null){
            return true;
        }
        txt=txt.trim();
        return txt.length()==0;
    }
}
