package com.reandroid.arsc.chunk.xml;

import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.model.ResourceLibrary;
import com.reandroid.arsc.pool.ResXmlStringPool;
import com.reandroid.arsc.refactor.ResourceMergeOption;
import com.reandroid.common.Namespace;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONException;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.IterableIterator;
import com.reandroid.utils.collection.SingleIterator;
import com.reandroid.xml.XMLElement;
import com.reandroid.xml.XMLUtil;
import com.reandroid.xml.base.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Predicate;

public class ResXmlElement extends ResXmlDocumentOrElement implements Element<ResXmlNode> {

    public ResXmlElement() {
        super(new ResXmlElementChunk());
    }

    public String getName() {
        return getStartElement().getName();
    }
    @Override
    public String getName(boolean includePrefix) {
        return getStartElement().getName(includePrefix);
    }
    @Override
    public void setName(String name) {
        getChunk().setName(name);
    }
    public boolean equalsName(String name){
        if(name == null){
            return getName() == null;
        }
        name = XMLUtil.splitName(name);
        return name.equals(getName(false));
    }

    @Override
    public ResXmlNamespace getNamespace() {
        return getChunk().getStartElement()
                .getResXmlStartNamespace();
    }

    @Override
    public void setNamespace(Namespace namespace){
        if (namespace != null) {
            setNamespace(namespace.getUri(), namespace.getPrefix());
        }else {
            setNamespace(null, null);
        }
    }
    public void setNamespace(String uri, String prefix) {
        getStartElement().setTagNamespace(uri, prefix);
    }
    @Override
    public String getPrefix() {
        return getStartElement().getPrefix();
    }
    @Override
    public String getUri() {
        return getStartElement().getUri();
    }

    @Override
    ResXmlElementChunk getChunk() {
        return (ResXmlElementChunk) super.getChunk();
    }

    @Override
    ResXmlNodeList getNodeList() {
        return getChunk().getNodeList();
    }

    @Override
    public int getAttributeCount() {
        return getAttributeArray().size();
    }

    @Override
    public ResXmlAttribute getAttributeAt(int i) {
        return getAttributeArray().get(i);
    }
    @Override
    public Iterator<ResXmlAttribute> getAttributes() {
        return getAttributeArray().clonedIterator();
    }
    public Iterator<ResXmlAttribute> getAttributes(Predicate<? super ResXmlAttribute> predicate) {
        return getAttributeArray().iterator(predicate);
    }
    public boolean removeAttribute(ResXmlAttribute attribute) {
        return getAttributeArray().remove(attribute);
    }
    public boolean removeAttribute(int index) {
        return getAttributeArray().remove(index) != null;
    }
    public boolean removeAttributeIf(Predicate<? super ResXmlAttribute> predicate) {
        return getAttributeArray().removeIf(predicate);
    }
    public boolean removeAttributesWithId(int resourceId) {
        return removeAttributeIf(attribute -> attribute.equalsNameId(resourceId));
    }
    public boolean removeAttributesWithName(String name) {
        return removeAttributeIf(attribute -> attribute.equalsNameWithNoId(name));
    }
    @Override
    public int getNamespaceCount() {
        return getChunk().getStartNamespaceList().size();
    }
    @Override
    public ResXmlNamespace getNamespaceAt(int i) {
        return getChunk().getStartNamespaceList().get(i);
    }
    @Override
    public Iterator<ResXmlNamespace> getNamespaces() {
        return ObjectsUtil.cast(getChunk().getStartNamespaceList().iterator());
    }
    public boolean removeNamespace(ResXmlNamespace namespace) {
        return getChunk().getStartNamespaceList()
                .remove((ResXmlStartNamespace) namespace);
    }
    public boolean removeNamespaceIf(Predicate<? super ResXmlNamespace> predicate) {
        return getChunk().getStartNamespaceList()
                .removeIf(predicate);
    }
    public Iterator<ResXmlNamespace> getAllNamespaces() {
        return new IterableIterator<ResXmlElement, ResXmlNamespace> (
                CollectionUtil.reversedOf(getParentElementsWithSelf())) {
            @Override
            public Iterator<ResXmlNamespace> iterator(ResXmlElement element) {
                return element.getNamespaces();
            }
        };
    }
    ResXmlNamespace getNamespaceForUriReference(int reference) {
        if (reference != -1) {
            Iterator<ResXmlNamespace> iterator = getAllNamespaces();
            while (iterator.hasNext()) {
                ResXmlNamespace namespace = iterator.next();
                if (reference == namespace.getUriReference()) {
                    return namespace;
                }
            }
        }
        return null;
    }
    public ResXmlNamespace getNamespace(String uri, String prefix) {
        if (uri != null && prefix != null) {
            Iterator<ResXmlNamespace> iterator = getAllNamespaces();
            while (iterator.hasNext()) {
                ResXmlNamespace namespace = iterator.next();
                if (uri.equals(namespace.getUri()) &&
                        prefix.equals(namespace.getPrefix())) {
                    return namespace;
                }
            }
        }
        return null;
    }
    public ResXmlNamespace getOrCreateNamespace(String uri, String prefix) {
        ResXmlNamespace namespace = getNamespace(uri, prefix);
        if (namespace == null) {
            namespace = getRootElement().newNamespace(uri, prefix);
        }
        return namespace;
    }
    private void ensureNamespace(String uri, String prefix, int lineNumber) {
        ResXmlNamespace namespace = getNamespace(uri, prefix);
        if (namespace == null) {
            namespace = newNamespace(uri, prefix);
            namespace.setLineNumber(lineNumber);
        }
    }
    public ResXmlNamespace getNamespaceByUri(String uri) {
        ResXmlNamespace result = null;
        Iterator<ResXmlNamespace> iterator = getAllNamespaces();
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
    public ResXmlNamespace getNamespaceByPrefix(String prefix) {
        ResXmlNamespace result = null;
        Iterator<ResXmlNamespace> iterator = getAllNamespaces();
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
    public ResXmlNamespace getOrCreateNamespaceByPrefix(String prefix){
        if (StringsUtil.isBlank(prefix)) {
            return null;
        }
        ResXmlNamespace namespace = getNamespaceByPrefix(prefix);
        if (namespace != null) {
            return namespace;
        }
        String uri;
        if (Namespace.PREFIX_ANDROID.equals(prefix)) {
            uri = Namespace.URI_ANDROID;
        } else {
            uri = Namespace.URI_RES_AUTO;
        }
        return getOrCreateNamespace(uri, prefix);
    }
    public String getComment() {
        String comment = getStartComment();
        if (StringsUtil.isEmpty(comment)) {
            String end = getEndComment();
            if (end != null) {
                comment = end;
            }
        }
        return comment;
    }
    public void setComment(String comment) {
        setStartComment(comment);
    }
    public String getStartComment() {
        return getStartElement().getComment();
    }
    public void setStartComment(String comment) {
        getStartElement().setComment(comment);
    }
    public String getEndComment() {
        return getEndElement().getComment();
    }
    public void setEndComment(String comment) {
        getEndElement().setComment(comment);
    }

    @Override
    public void setAttributesUnitSize(int size, boolean recursive) {
        getAttributeArray().setUnitSize(size);
        if (recursive) {
            super.setAttributesUnitSize(size, true);
        }
    }

    @Override
    public ResXmlNamespace newNamespace(String uri, String prefix) {
        return getChunk().newNamespace(uri, prefix);
    }
    @Override
    public ResXmlAttribute newAttribute() {
        return getAttributeArray().createNext();
    }
    public ResXmlAttribute newAttribute(String name, int resourceId) {
        ResXmlAttribute attribute = newAttribute();
        attribute.setName(name, resourceId);
        return attribute;
    }

    @Override
    void linkStringReferences() {
        getChunk().linkStringReferences();
        super.linkStringReferences();
    }

    @Override
    Iterator<ParserEvent> getParserEvents() {
        return CombiningIterator.singleThree(

                ParserEvent.startTag(this),

                SingleIterator.of(ParserEvent.startComment(this)),

                new IterableIterator<ResXmlNode, ParserEvent>(iterator()) {
                    @Override
                    public Iterator<ParserEvent> iterator(ResXmlNode node) {
                        return node.getParserEvents();
                    }
                },

                SingleIterator.of(ParserEvent.endTag(this))
        );
    }

    @Override
    int autoSetLineNumber(int start) {
        start ++;
        Iterator<ResXmlNamespace> namespaces = getNamespaces();
        while (namespaces.hasNext()) {
            namespaces.next().setLineNumber(start);
        }
        getStartElement().setLineNumber(start);
        Iterator<ResXmlNode> iterator = iterator();
        int i = start;
        while (iterator.hasNext()) {
            ResXmlNode node = iterator.next();
            start = node.autoSetLineNumber(start);
        }
        if (i != start) {
            start ++;
        }
        getEndElement().setLineNumber(start);
        return start;
    }
    @Override
    public XMLElement toXml(boolean decode) {
        XMLElement element = new XMLElement();
        element.setName(getName(false));
        Iterator<ResXmlNamespace> namespaces = getNamespaces();
        while (namespaces.hasNext()) {
            ResXmlNamespace namespace = namespaces.next();
            element.addNamespace(namespace.getUri(), namespace.getPrefix());
        }
        element.setNamespace(getNamespace());
        getAttributeArray().toXml(element, decode);
        return element;
    }

    @Override
    public boolean isElement() {
        return true;
    }

    @Override
    public int getLineNumber() {
        return getStartLineNumber();
    }
    @Override
    public void setLineNumber(int lineNumber) {
        setStartLineNumber(lineNumber);
    }
    public int getStartLineNumber() {
        return getStartElement().getLineNumber();
    }
    public void setStartLineNumber(int lineNumber) {
        getStartElement().setLineNumber(lineNumber);
    }
    public int getEndLineNumber() {
        return getEndElement().getLineNumber();
    }
    public void setEndLineNumber(int lineNumber) {
        getEndElement().setLineNumber(lineNumber);
    }

    public ResXmlElement getParentElement() {
        ResXmlNode parent = getParentNode();
        if (parent instanceof ResXmlElement) {
            return (ResXmlElement) parent;
        }
        return null;
    }
    public ResXmlElement getRootElement() {
        return CollectionUtil.getLast(getParentElementsWithSelf());
    }
    public Iterator<ResXmlElement> getParentElementsWithSelf() {
        return visitParentNodes(ResXmlElement.class, ResXmlDocument.class);
    }

    public ResXmlDocument getParentDocument() {
        return getParentInstance(ResXmlDocument.class);
    }
    public ResXmlStringPool getStringPool() {
        ResXmlDocument document = getParentDocument();
        if (document != null) {
            return document.getStringPool();
        }
        return null;
    }

    public boolean isUndefined() {
        return size() == 0 &&
                this.getAttributeCount() == 0 &&
                this.getNamespaceCount() == 0 &&
                StringsUtil.isEmpty(getName());
    }

    public ResXmlAttribute getOrCreateAndroidAttribute(String name, int resourceId){
        return getOrCreateAttribute(
                ResourceLibrary.URI_ANDROID,
                ResourceLibrary.PREFIX_ANDROID,
                name,
                resourceId);
    }
    public ResXmlAttribute getOrCreateAttribute(String uri, String prefix, String name, int resourceId) {
        return getAttributeArray().getOrCreateAttribute(uri, prefix, name, resourceId);
    }
    public ResXmlAttribute getOrCreateAttribute(String name, int resourceId) {
        return getAttributeArray().getOrCreateAttribute(name, resourceId);
    }
    public ResXmlAttribute createAndroidAttribute(String name, int resourceId) {
        return getAttributeArray().getOrCreateAndroidAttribute(name, resourceId);
    }
    public ResXmlAttribute createAttribute(String name, int resourceId) {
        return newAttribute(name, resourceId);
    }
    public ResXmlAttribute searchAttributeByName(String name) {
        return getAttributeArray().searchAttributeByName(name);
    }
    public ResXmlAttribute searchAttributeByResourceId(int resourceId) {
        return getAttributeArray().searchAttributeByResourceId(resourceId);
    }

    public ResXmlAttribute getIdAttribute() {
        return getStartElement().getIdAttribute();
    }
    public ResXmlAttribute getClassAttribute() {
        return getStartElement().getClassAttribute();
    }
    public ResXmlAttribute getStyleAttribute() {
        return getStartElement().getStyleAttribute();
    }

    private ResXmlAttributeArray getAttributeArray() {
        return getStartElement().getResXmlAttributeArray();
    }
    private ResXmlStartElement getStartElement() {
        return getChunk().getStartElement();
    }
    private ResXmlEndElement getEndElement() {
        return getChunk().getEndElement();
    }

    @Override
    public void merge(ResXmlNode xmlNode) {
        if (xmlNode == this) {
            return;
        }

        ResXmlElement coming = (ResXmlElement) xmlNode;

        setName(coming.getName(false));

        Iterator<ResXmlNamespace> namespaces = coming.getNamespaces();
        while (namespaces.hasNext()) {
            ResXmlNamespace ns = namespaces.next();
            ensureNamespace(ns.getUri(), ns.getPrefix(), ns.getLineNumber());
        }

        setNamespace(coming.getNamespace());

        getAttributeArray().merge(coming.getAttributeArray());

        setStartComment(coming.getStartComment());
        setEndComment(coming.getEndComment());

        setStartLineNumber(coming.getStartLineNumber());
        setEndLineNumber(coming.getEndLineNumber());

        super.merge(xmlNode);
    }
    @Override
    public void mergeWithName(ResourceMergeOption mergeOption, ResXmlNode xmlNode) {
        if (xmlNode == this) {
            return;
        }

        ResXmlElement coming = (ResXmlElement) xmlNode;

        setName(coming.getName(false));

        Iterator<ResXmlNamespace> namespaces = coming.getNamespaces();
        while (namespaces.hasNext()) {
            ResXmlNamespace ns = namespaces.next();
            ensureNamespace(ns.getUri(), ns.getPrefix(), ns.getLineNumber());
        }

        setNamespace(coming.getNamespace());

        getAttributeArray().mergeWithName(mergeOption, coming.getAttributeArray());

        setStartComment(coming.getStartComment());
        setEndComment(coming.getEndComment());

        setStartLineNumber(coming.getStartLineNumber());
        setEndLineNumber(coming.getEndLineNumber());

        super.mergeWithName(mergeOption, xmlNode);
    }


    private void addNamespaceFromJson(JSONObject jsonObject) {
        ResXmlStartNamespace startNamespace = (ResXmlStartNamespace)
                getChunk().newNamespace(null, null);
        startNamespace.fromJson(jsonObject);
    }
    private void setNamespaceFromJson(JSONObject jsonObject) {
        String uri = jsonObject.optString(JSON_uri, null);
        String prefix = jsonObject.optString(JSON_prefix, null);
        if (uri == null && prefix != null) {
            throw new JSONException("Provided " + JSON_prefix + "="
                    + prefix + ", but missing: " + JSON_uri);
        }
        if (prefix == null && uri != null) {
            throw new JSONException("Provided " + JSON_uri + "="
                    + uri + ", but missing: " + JSON_prefix);
        }
        setNamespace(uri, prefix);
    }
    @Override
    String nodeTypeName() {
        return JSON_node_type_element;
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(JSON_node_type, nodeTypeName());

        jsonObject.put(JSON_namespaces,
                BlockList.toJsonArray(getChunk().getStartNamespaceList()));

        jsonObject.put(JSON_name, getName());

        jsonObject.put(JSON_uri, getUri());
        jsonObject.put(JSON_prefix, getPrefix());

        jsonObject.put(JSON_line, getStartLineNumber());
        jsonObject.put(JSON_line_end, getEndLineNumber());

        jsonObject.put(JSON_comment, getComment());

        jsonObject.put(JSON_attributes, getAttributeArray().toJson());

        jsonObject.put(JSON_nodes, nodesToJson());

        return jsonObject;
    }

    @Override
    public void fromJson(JSONObject json) {
        setName(json.getString(JSON_name));
        setStartLineNumber(json.optInt(JSON_line));
        setEndLineNumber(json.optInt(JSON_line));
        JSONArray namespacesArray = json.optJSONArray(JSON_namespaces);
        if (namespacesArray != null) {
            int length = namespacesArray.length();
            for (int i = 0; i < length; i++) {
                addNamespaceFromJson(namespacesArray.getJSONObject(i));
            }
        }

        setNamespaceFromJson(json);

        setComment(json.optString(JSON_comment, null));

        getAttributeArray().fromJson(json.optJSONArray(JSON_attributes));

        nodesFromJson(json);
    }

    @Override
    public void serialize(XmlSerializer serializer, boolean decode) throws IOException {
        setIndent(serializer, true);
        Iterator<ResXmlNamespace> namespaces = getNamespaces();
        while (namespaces.hasNext()) {
            ResXmlNamespace namespace = namespaces.next();
            serializer.setPrefix(namespace.getPrefix(), namespace.getUri());
        }
        serializeComment(serializer, getStartComment());

        serializer.startTag(getUri(), getName(false));

        getAttributeArray().serialize(serializer, decode);

        serializeNodes(serializer, decode);

        serializer.endTag(getUri(), getName(false));

        serializeComment(serializer, getEndComment());
    }
    @Override
    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("Not START_TAG event", parser, null);
        }
        setStartLineNumber(parser.getLineNumber());
        parseNamespaces(parser);
        setName(parser.getName());
        setNamespace(parser.getNamespace(), parser.getPrefix());
        parseAttributes(parser);
        parser.next();

        parseInnerNodes(parser);
        parser.next();
    }

    private void parseNamespaces(XmlPullParser parser) throws XmlPullParserException {
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
            name = XMLUtil.splitName(name);
            String value = parser.getAttributeValue(i);
            if(Namespace.isValidNamespace(value, prefix)){
                getOrCreateNamespace(value, name);
            }
        }
    }
    private void parseAttributes(XmlPullParser parser) throws IOException {
        int count = parser.getAttributeCount();
        for (int i = 0; i < count; i++) {
            String name = parser.getAttributeName(i);
            String prefix = XMLUtil.splitPrefix(name);
            name = XMLUtil.splitName(name);
            String value = parser.getAttributeValue(i);
            if(Namespace.isValidNamespace(value, prefix)){
                continue;
            }
            if (prefix == null) {
                prefix = StringsUtil.emptyToNull(parser.getAttributePrefix(i));
            }
            String uri;
            if (prefix != null) {
                uri = parser.getAttributeNamespace(i);
                if (StringsUtil.isEmpty(uri)) {
                    ResXmlNamespace ns = getNamespaceByPrefix(prefix);
                    if(ns != null){
                        uri = ns.getUri();
                    }
                }
            } else {
                uri = null;
            }
            ResXmlAttribute attribute = newAttribute();
            attribute.encode(false, uri, prefix, name, value);
        }
        if (count != 0) {
            getAttributeArray().computePositionsAndSort();
        }
    }

    @Override
    public void removeUnusedNamespaces() {
        removeNamespaceIf(ResXmlNamespace::isUnused);
        super.removeUnusedNamespaces();
    }
    @Override
    public void removeUndefinedAttributes() {
        removeAttributeIf(ResXmlAttribute::isUndefined);
        super.removeUndefinedAttributes();
    }
    @Override
    public void fixNamespaces() {
        Iterator<ResXmlNamespace> namespaces = getNamespaces();
        while (namespaces.hasNext()) {
            ResXmlStartNamespace namespace = (ResXmlStartNamespace) namespaces.next();
            namespace.fixEmpty();
        }
        Iterator<ResXmlAttribute> iterator = getAttributes();
        while (iterator.hasNext()) {
            iterator.next().autoSetNamespace();
        }
        super.fixNamespaces();
    }

    @Override
    void onPreRemove() {
        super.onPreRemove();
        getChunk().onPreRemove();
    }

    @Override
    public String toString() {
        touchChildNodesForDebug();
        StringBuilder builder = new StringBuilder();
        builder.append('<');
        builder.append(getName(true));
        Iterator<ResXmlNamespace> namespaces = getNamespaces();
        if (namespaces.hasNext()) {
            builder.append(' ');
            builder.append(StringsUtil.join(namespaces, ' '));
        }
        Iterator<ResXmlAttribute> attributes = getAttributes();
        if (attributes.hasNext()) {
            builder.append(' ');
            builder.append(StringsUtil.join(attributes, ' '));
        }
        builder.append('>');
        return builder.toString();
    }
}
