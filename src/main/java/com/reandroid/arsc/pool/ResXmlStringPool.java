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
package com.reandroid.arsc.pool;

import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.chunk.xml.ResXmlIDMap;
import com.reandroid.arsc.item.ResXmlID;
import com.reandroid.arsc.item.ResXmlString;
import com.reandroid.arsc.list.ResXmlIDList;
import com.reandroid.arsc.list.StringItemList;
import com.reandroid.utils.NumbersUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.xml.StyleDocument;


public class ResXmlStringPool extends StringPool<ResXmlString> {

    public ResXmlStringPool(boolean is_utf8) {
        super(is_utf8, false, ResXmlString::new);
    }

    @Override
    public ResXmlString getOrCreate(String str) {
        return getOrCreate(0, str);
    }
    @Override
    public ResXmlString getOrCreate(StyleDocument styleDocument) {
        String xml = styleDocument.getXml();
        if (!styleDocument.hasElements()) {
            return getOrCreate(0, xml);
        }
        ResXmlString xmlString = get(xml, resXmlString -> {
            if (!xml.equals(resXmlString.getXml()) ||
                    resXmlString.hasResourceId()) {
                return false;
            }
            return resXmlString.hasStyle();
        });
        if (xmlString == null) {
            xmlString = createNewString();
            xmlString.set(styleDocument);
        }
        return xmlString;
    }

    public ResXmlString getOrCreate(int resourceId, String str) {
        ResXmlString xmlString = get(str, resXmlString ->
                resXmlString.equalsValue(resourceId, str));
        if (xmlString == null) {
            xmlString = createNewString(str);
            xmlString.setResourceId(resourceId);
        }
        return xmlString;
    }
    public ResXmlString getNamespaceString(String uri, String prefix) {
        return get(uri, resXmlString -> resXmlString.equalsNamespace(uri, prefix));
    }
    public ResXmlString getOrCreateNamespaceString(String uri, String prefix) {
        if (uri == null || prefix == null || uri.equals(prefix)) {
            return null;
        }
        ResXmlString xmlString = getNamespaceString(uri, prefix);
        if (xmlString == null) {
            xmlString = get(uri, resXmlString -> !resXmlString.hasNamespacePrefix()
                    && !resXmlString.hasResourceId() && !resXmlString.hasStyle());
            if (xmlString != null) {
                ResXmlString prefixXmlString = getOrCreate(prefix);
                xmlString.linkNamespacePrefixInternal(prefixXmlString);
            }
        }
        if (xmlString == null) {
            xmlString = createNewString(uri);
            ResXmlString prefixXmlString = getOrCreate(prefix);
            xmlString.linkNamespacePrefixInternal(prefixXmlString);
        }
        return xmlString;
    }

    @Override
    public void onPreAddInternal(int index, ResXmlString item) {
        ResXmlIDList xmlIDMap = getResXmlIDMap().getResXmlIDArray();
        if (index < xmlIDMap.size() - 1) {
            xmlIDMap.createAt(index);
        }
        super.onPreAddInternal(index, item);
    }

    @Override
    public void onSortedInternal() {
        super.onSortedInternal();
        getResXmlIDMap().getResXmlIDArray().sort();
    }

    private ResXmlIDMap getResXmlIDMap() {
        ResXmlDocument resXmlDocument = getParentInstance(ResXmlDocument.class);
        if (resXmlDocument != null) {
            return resXmlDocument.getResXmlIDMap();
        }
        return ObjectsUtil.getNull();
    }

    public void linkResXmlIDMapInternal() {
        ResXmlIDMap resXmlIDMap = getResXmlIDMap();
        if (resXmlIDMap == null) {
            return;
        }
        StringItemList<ResXmlString> stringsArray = getStringsArray();
        int size = NumbersUtil.min(resXmlIDMap.size(), stringsArray.size());
        for (int i = 0; i < size; i++) {
            ResXmlString resXmlString = stringsArray.get(i);
            ResXmlID xmlID = resXmlIDMap.get(i);
            resXmlString.linkResourceIdInternal(xmlID);
        }
    }

    @Override
    public void onChunkLoaded() {
        super.onChunkLoaded();
        linkResXmlIDMapInternal();
    }

    @Override
    protected void onPreRefresh() {
        super.onPreRefresh();
        sort();
    }
}
