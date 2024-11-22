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

import com.reandroid.arsc.container.BlockList;
import com.reandroid.common.Namespace;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONException;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.IterableIterator;
import com.reandroid.utils.collection.Swappable;
import com.reandroid.xml.XMLUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.util.Comparator;
import java.util.Iterator;

public class ResXmlStartNamespaceList extends ResXmlChunkList<ResXmlStartNamespace>
        implements JSONConvert<JSONArray> {

    private final BlockList<ResXmlEndNamespace> endNamespaceList;

    public ResXmlStartNamespaceList(BlockList<ResXmlEndNamespace> endNamespaceList) {
        super();
        this.endNamespaceList = endNamespaceList;
    }


    public ResXmlNamespace getForUri(String uri) {
        ResXmlNamespace result = null;
        Iterator<ResXmlStartNamespace> iterator = getVisibleNamespaces();
        while (iterator.hasNext()) {
            ResXmlNamespace namespace = iterator.next();
            if (ObjectsUtil.equals(uri, namespace.getUri())) {
                if (result == null) {
                    result = namespace;
                } else if (Namespace.isValidPrefix(namespace.getPrefix())) {
                    result = namespace;
                }
            }
        }
        return result;
    }

    public ResXmlNamespace getOrCreateForPrefix(String prefix){
        if (StringsUtil.isBlank(prefix)) {
            return null;
        }
        ResXmlNamespace namespace = getForPrefix(prefix);
        if (namespace != null) {
            return namespace;
        }
        String uri;
        if (Namespace.PREFIX_ANDROID.equals(prefix)) {
            uri = Namespace.URI_ANDROID;
        } else {
            uri = Namespace.URI_RES_AUTO;
        }
        return getOrCreate(uri, prefix);
    }
    public ResXmlNamespace getForPrefix(String prefix) {
        ResXmlNamespace result = null;
        Iterator<ResXmlStartNamespace> iterator = getVisibleNamespaces();
        while (iterator.hasNext()) {
            ResXmlNamespace namespace = iterator.next();
            if (ObjectsUtil.equals(prefix, namespace.getPrefix())) {
                if (result == null) {
                    result = namespace;
                } else if (Namespace.isValidUri(namespace.getUri())) {
                    result = namespace;
                }
            }
        }
        return result;
    }
    public ResXmlStartNamespace getForUriReference(int reference) {
        if (reference != -1) {
            Iterator<ResXmlElement> iterator = element()
                    .getParentElementsWithSelf();
            while (iterator.hasNext()) {
                ResXmlStartNamespaceList list = iterator.next()
                        .getNamespaceList();
                ResXmlStartNamespace namespace = list.getLocalForUriReference(reference);
                if (namespace != null) {
                    return namespace;
                }
            }
        }
        return null;
    }
    private ResXmlStartNamespace getLocalForUriReference(int reference) {
        if (reference == -1) {
            return null;
        }
        int size = size();
        for (int i = 0; i < size; i++) {
            ResXmlStartNamespace namespace = get(i);
            if (reference == namespace.getUriReference()) {
                return namespace;
            }
        }
        return null;
    }
    public ResXmlStartNamespace getOrCreate(String uri, String prefix) {
        ResXmlStartNamespace namespace = get(uri, prefix);
        if (namespace == null) {
            namespace = element().getRootElement()
                    .getNamespaceList().createLocal(uri, prefix);
        }
        return namespace;
    }
    public ResXmlStartNamespace get(String uri, String prefix) {
        ResXmlStartNamespace result = null;
        Iterator<ResXmlElement> iterator = element()
                .getDescendingParentsWithSelf();
        while (iterator.hasNext()) {
            ResXmlStartNamespaceList list = iterator.next()
                    .getNamespaceList();
            ResXmlStartNamespace namespace = list.getLocal(uri, prefix);
            if (namespace != null && namespace.isBetterThan(result)) {
                result = namespace;
            }
        }
        return result;
    }
    public Iterator<ResXmlStartNamespace> getVisibleNamespaces() {
        return new IterableIterator<ResXmlElement, ResXmlStartNamespace>(
                element().getDescendingParentsWithSelf()) {
            @Override
            public Iterator<ResXmlStartNamespace> iterator(ResXmlElement element) {
                return element.getNamespaceList().iterator();
            }
        };
    }
    private ResXmlStartNamespace createLocal(String uri, String prefix) {
        if (uri == null) {
            return null;
        }
        if (prefix == null) {
            prefix = generateUniquePrefix();
        }
        return createNext(uri, prefix);
    }
    private ResXmlStartNamespace getLocal(String uri, String prefix) {
        if (uri == null) {
            return null;
        }
        ResXmlStartNamespace result = null;
        int size = size();
        for (int i = 0; i < size; i++) {
            ResXmlStartNamespace namespace = get(i);
            if (uri.equals(namespace.getUri())
                    && ObjectsUtil.equals(prefix, namespace.getPrefix())) {
                if (result == null ||
                        namespace.getReferencedCount() > result.getReferencedCount()) {
                    result = namespace;
                }
            }
        }
        return result;
    }
    private String generateUniquePrefix() {
        String name = "ns";
        int i = 1;
        int maxTrials = 10000;
        while (i < maxTrials) {
            String prefix = name + i;
            if (!containsPrefixEveryWhere(prefix)) {
                return prefix;
            }
            i ++;
        }
        throw new IllegalArgumentException(
                "Failed to generate unique prefix, trials = " + i);
    }
    private boolean containsPrefixEveryWhere(String prefix) {
        Iterator<ResXmlElement> iterator = document().recursiveElements();
        while (iterator.hasNext()) {
            ResXmlStartNamespaceList list = iterator.next()
                    .getNamespaceList();
            if (list.containsPrefix(prefix)) {
                return true;
            }
        }
        return false;
    }
    private boolean containsPrefix(String prefix) {
        int size = size();
        for (int i = 0; i < size; i++) {
            ResXmlStartNamespace namespace = get(i);
            if (prefix.equals(namespace.getPrefix())) {
                return true;
            }
        }
        return false;
    }
    public Iterator<ResXmlNamespace> getNamespaces() {
        return ObjectsUtil.cast(iterator());
    }
    public boolean isEmpty() {
        return size() == 0;
    }
    private void ensureUniqueUri() {
        ResXmlDocument document = document();
        if (document != null) {
            Iterator<ResXmlElement> iterator = document.recursiveElements();
            while (iterator.hasNext()) {
                iterator.next().getNamespaceList()
                        .ensureUniqueUriLocal();
            }
        }
    }
    private void ensureUniqueUriLocal() {
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).ensureUniqueUri();
        }
    }
    public ResXmlStartNamespace createNext(String uri, String prefix) {
        if (uri != null) {
            ensureUniqueUri();
        }
        ResXmlStartNamespace startNamespace = createNext();
        if (uri != null || prefix != null) {
            startNamespace.setNamespace(uri, prefix);
        }
        return startNamespace;
    }
    @Override
    public ResXmlStartNamespace createNext() {
        ResXmlEndNamespace endNamespace = new ResXmlEndNamespace();
        ResXmlStartNamespace startNamespace = new ResXmlStartNamespace(endNamespace);
        endNamespace.setStart(startNamespace);
        add(startNamespace);
        endNamespaceList.add(endNamespace);
        return startNamespace;
    }

    @Override
    public boolean sort(Comparator<? super ResXmlStartNamespace> comparator) {
        if (super.sort(comparator)) {
            endNamespaceList.sort(CompareUtil.getComparableComparator());
            return true;
        }
        return false;
    }

    @Override
    public boolean sort(Comparator<? super ResXmlStartNamespace> comparator, Swappable swappable) {
        if (super.sort(comparator, swappable)) {
            endNamespaceList.sort(CompareUtil.getComparableComparator());
            return true;
        }
        return false;
    }


    @Override
    public void onPreRemove(ResXmlStartNamespace startNamespace) {
        super.onPreRemove(startNamespace);
        endNamespaceList.remove(startNamespace.getEnd());
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        clearUndefined();
    }

    public void clearUndefined() {
        removeIf(ResXmlStartNamespace::isUndefined);
    }
    public void clearUnused() {
        removeIf(ResXmlStartNamespace::isUnused);
    }

    @Override
    public JSONArray toJson() {
        return toJsonArray(this);
    }
    @Override
    public void fromJson(JSONArray jsonArray) {
        if (jsonArray != null) {
            int size = jsonArray.length();
            for (int i = 0; i < size; i++) {
                Object obj = jsonArray.get(i);
                if (obj != null) {
                    if (!(obj instanceof JSONObject)) {
                        throw new JSONException("Expecting JSONObject but found: "
                                + obj.getClass());
                    }
                    createNext().fromJson((JSONObject) obj);
                }
            }
        }
    }
    private ResXmlDocument document() {
        return element().getParentDocument();
    }
    private ResXmlElement element() {
        return getParentInstance(ResXmlElement.class);
    }

    private void ensureNamespace(String uri, String prefix, int lineNumber) {
        ResXmlNamespace namespace = get(uri, prefix);
        if (namespace == null) {
            namespace = createNext(uri, prefix);
            namespace.setLineNumber(lineNumber);
        }
    }

    public void merge(ResXmlStartNamespaceList namespaceList) {
        if (namespaceList != this) {
            int size = namespaceList.size();
            for (int i = 0; i < size; i++) {
                createNext().merge(namespaceList.get(i));
            }
        }
    }
    public void parse(XmlPullParser parser) throws XmlPullParserException {
        int count = parser.getNamespaceCount(parser.getDepth());
        for(int i = 0; i < count; i++) {
            ensureNamespace(
                    parser.getNamespaceUri(i),
                    parser.getNamespacePrefix(i),
                    parser.getLineNumber());
        }
        count = parser.getAttributeCount();
        for(int i = 0; i < count; i++) {
            String name = parser.getAttributeName(i);
            String prefix = XMLUtil.splitPrefix(name);
            if (prefix == null) {
                continue;
            }
            name = XMLUtil.splitName(name);
            String value = parser.getAttributeValue(i);
            if(Namespace.isValidNamespace(value, prefix)){
                ensureNamespace(value, name, 0);
            }
        }
    }
}
