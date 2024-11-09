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
package com.reandroid.arsc.item;

import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.chunk.xml.ResXmlIDMap;
import com.reandroid.utils.CompareUtil;

public class ResXmlString extends StringItem {

    private ResXmlID mResXmlID;
    private ResXmlString namespacePrefix;

    public ResXmlString(boolean utf8) {
        super(utf8);
    }

    public ResXmlID getOrCreateResXmlID() {
        ResXmlID resXmlID = getResXmlID();
        if(resXmlID == null) {
            ResXmlIDMap resXmlIDMap = getParentInstance(ResXmlDocument.class).getResXmlIDMap();
            linkResourceIdInternal(resXmlIDMap.getResXmlIDArray().createNext());
            resXmlID = getResXmlID();
        }
        return resXmlID;
    }
    public ResXmlID getResXmlID() {
        return mResXmlID;
    }
    public boolean hasResourceId() {
        return getResourceId() != 0;
    }
    public int getResourceId(){
        ResXmlID xmlId = getResXmlID();
        if(xmlId == null){
            return 0;
        }
        return xmlId.get();
    }
    public void setResourceId(int resourceId){
        if(resourceId == 0) {
            unLinkResourceIdInternal();
        }else {
            getOrCreateResXmlID().set(resourceId);
        }
    }
    @Override
    void ensureStringLinkUnlocked(){
    }
    public void unLinkResourceIdInternal() {
        ResXmlID resXmlID = this.mResXmlID;
        if(resXmlID != null) {
            this.mResXmlID = null;
            resXmlID.setResXmlStringInternal(null);
        }
    }
    public void linkResourceIdInternal(ResXmlID resXmlID) {
        if(resXmlID == null) {
            throw new NullPointerException("Can not link null id item");
        }
        if(this.mResXmlID == resXmlID) {
            return;
        }
        if(this.mResXmlID != null) {
            throw new IllegalStateException("Resource id string item is already linked");
        }
        this.mResXmlID = resXmlID;
        resXmlID.setResXmlStringInternal(this);
    }

    public ResXmlString getNamespacePrefix() {
        ResXmlString xmlString = this.namespacePrefix;
        if (xmlString != null && xmlString.getParent() == null) {
            xmlString = null;
            this.namespacePrefix = null;
        }
        return xmlString;
    }
    private String getNamespacePrefixString() {
        ResXmlString xmlString = getNamespacePrefix();
        if (xmlString != null) {
            return xmlString.getXml();
        }
        return null;
    }
    public boolean hasNamespacePrefix() {
        return getNamespacePrefix() != null;
    }
    public boolean equalsNamespace(String uri, String prefix) {
        if (uri == null || prefix == null) {
            return false;
        }
        if (!uri.equals(this.getXml())) {
            return false;
        }
        return prefix.equals(getNamespacePrefixString());
    }

    public void linkNamespacePrefixInternal(ResXmlString namespacePrefix) {
        if(namespacePrefix == this) {
            throw new IllegalArgumentException("Cyclic link of namespace prefix");
        }
        if(namespacePrefix == null) {
            throw new NullPointerException("Can not link null namespace prefix");
        }
        ResXmlString xmlString = getNamespacePrefix();
        if (xmlString == namespacePrefix) {
            return;
        }
        if (xmlString != null) {
            throw new IllegalStateException("Uri string item is already linked");
        }
        this.namespacePrefix = namespacePrefix;
    }

    @Override
    public boolean hasReference() {
        if(super.hasReference()){
            return true;
        }
        ResXmlID resXmlID = getResXmlID();
        if(resXmlID != null) {
            return resXmlID.hasReference();
        }
        return false;
    }
    @Override
    public int getReferencesSize() {
        int size = super.getReferencesSize();
        ResXmlID resXmlID = getResXmlID();
        if(resXmlID != null && resXmlID.hasReference()) {
            size ++;
        }
        return size;
    }

    @Override
    public boolean merge(StringItem other) {
        if(!super.merge(other)) {
            return false;
        }
        ResXmlString xmlString = (ResXmlString) other;
        int id = xmlString.getResourceId();
        if(id != 0) {
            ResXmlID xmlID = getOrCreateResXmlID();
            xmlID.set(id);
        }
        return true;
    }

    @Override
    public int compareTo(StringItem stringItem){
        if(!(stringItem instanceof ResXmlString)){
            return -1;
        }
        if(stringItem == this) {
            return 0;
        }
        ResXmlString xmlString = (ResXmlString) stringItem;
        int id1 = getResourceId();
        int id2 = xmlString.getResourceId();
        int i = CompareUtil.compare(id1 == 0, id2 == 0);
        if(i != 0) {
            return i;
        }
        i = CompareUtil.compareUnsigned(id1, id2);
        if(i != 0) {
            return i;
        }
        i = CompareUtil.compare(xmlString.hasStyle(), this.hasStyle());
        if(i != 0) {
            return i;
        }
        i = CompareUtil.compare(this.getXml(), stringItem.getXml());
        if( i != 0) {
            return i;
        }
        i = CompareUtil.compare(xmlString.hasNamespacePrefix(), this.hasNamespacePrefix());
        if(i != 0) {
            return i;
        }
        i = compareReferences(xmlString);
        if(i != 0) {
            return i;
        }
        i = CompareUtil.compare(this.getNamespacePrefixString(), xmlString.getNamespacePrefixString());
        if(i != 0) {
            return i;
        }
        return 0;
    }
}
