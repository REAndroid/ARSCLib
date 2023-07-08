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
package com.reandroid.xml;

import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.IndexIterator;
import com.reandroid.utils.collection.SizedSupplier;
import com.reandroid.xml.parser.XMLSpanParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.function.Predicate;

public class XMLElement extends XMLNodeTree{
    private final ArrayList<XMLAttribute> mAttributes;
    private String mName;
    private XMLNamespace mNamespace;
    private final ArrayList<XMLNamespace> mNamespaceList;
    public XMLElement(){
        super();
        mAttributes = new ArrayList<>();
        mNamespaceList = new ArrayList<>();
    }
    public XMLElement(String tagName){
        this();
        setName(tagName);
    }
    @Override
    XMLElement clone(XMLNode parent){
        XMLElement element = newElement();
        if(parent instanceof XMLNodeTree){
            ((XMLNodeTree)parent).add(element);
        }
        int count = getNamespaceCount();
        for(int i = 0; i < count; i++){
            getNamespaceAt(i).clone(element);
        }
        setName(getUri(), getPrefix(), getName(false));
        count = getAttributeCount();
        for(int i = 0; i < count; i++){
            element.addAttribute(getAttributeAt(i).clone(element));
        }
        Iterator<XMLNode> iterator = iterator();
        while(iterator.hasNext()){
            iterator.next().clone(element);
        }
        return element;
    }
    public void addText(String text){
        XMLText xmlText = newText();
        xmlText.setText(text);
        add(xmlText);
    }
    public XMLAttribute getAttributeAt(int index){
        return mAttributes.get(index);
    }
    public Iterator<? extends XMLAttribute> getAttributes() {
        return new IndexIterator<>(new SizedSupplier<XMLAttribute>() {
            @Override
            public int size() {
                return getAttributeCount();
            }
            @Override
            public XMLAttribute get(int index) {
                return getAttributeAt(index);
            }
        });
    }
    public String getUri(){
        XMLNamespace namespace = getNamespace();
        if(namespace != null){
            return namespace.getUri();
        }
        return null;
    }
    public String getPrefix(){
        XMLNamespace namespace = getNamespace();
        if(namespace != null){
            return namespace.getUri();
        }
        return null;
    }
    public XMLNamespace getNamespace(){
        return mNamespace;
    }
    public void setNamespace(XMLNamespace namespace) {
        this.mNamespace = namespace;
    }
    public void setNamespace(String uri, String prefix) {
        setNamespace(getOrCreateXMLNamespace(uri, prefix));
    }

    public int getNamespaceCount(){
        return mNamespaceList.size();
    }
    public XMLNamespace getNamespaceAt(int index){
        return mNamespaceList.get(index);
    }
    public void addNamespace(String uri, String prefix){
        if(uri == null || prefix == null){
            return;
        }
        addNamespace(new XMLNamespace(uri, prefix));
    }
    public void addNamespace(XMLNamespace namespace){
        if(namespace != null && !mNamespaceList.contains(namespace)){
            mNamespaceList.add(namespace);
        }
    }
    public XMLNamespace getOrCreateXMLNamespace(String uri, String prefix){
        if(uri == null || prefix == null){
            return null;
        }
        XMLNamespace namespace = getXMLNamespace(uri, prefix);
        if(namespace != null){
            return namespace;
        }
        namespace = new XMLNamespace(uri, prefix);
        getRootElement().addNamespace(namespace);
        return namespace;
    }
    public XMLNamespace getXMLNamespace(String uri, String prefix){
        if(uri == null || prefix == null){
            return null;
        }
        int count = getNamespaceCount();
        for(int i = 0; i < count; i++){
            XMLNamespace namespace = getNamespaceAt(i);
            if(namespace.isEqual(uri, prefix)){
                return namespace;
            }
        }
        XMLElement parent = getParentElement();
        if(parent != null){
            return parent.getXMLNamespace(uri, prefix);
        }
        return null;
    }
    public XMLNamespace getXMLNamespaceByUri(String uri){
        if(uri == null){
            return null;
        }
        int count = getNamespaceCount();
        for(int i = 0; i < count; i++){
            XMLNamespace namespace = getNamespaceAt(i);
            if(uri.equals(namespace.getUri())){
                return namespace;
            }
        }
        XMLElement parent = getParentElement();
        if(parent != null){
            return parent.getXMLNamespaceByUri(uri);
        }
        return null;
    }
    public XMLNamespace getXMLNamespaceByPrefix(String prefix){
        if(prefix == null){
            return null;
        }
        int count = getNamespaceCount();
        for(int i = 0; i < count; i++){
            XMLNamespace namespace = getNamespaceAt(i);
            if(prefix.equals(namespace.getPrefix())){
                return namespace;
            }
        }
        XMLElement parent = getParentElement();
        if(parent != null){
            return parent.getXMLNamespaceByPrefix(prefix);
        }
        return null;
    }
    public Collection<XMLAttribute> listAttributes(){
        return mAttributes;
    }
    public int getChildElementsCount(){
        return super.size(XMLElement.class);
    }
    public List<XMLElement> getChildElementList(){
        return CollectionUtil.toList(iterator(XMLElement.class));
    }
    public Iterator<? extends XMLElement> getElements(){
        return iterator(XMLElement.class);
    }
    public Iterator<XMLElement> getElements(Predicate<XMLElement> filter){
        return iterator(XMLElement.class, filter);
    }
    public Iterator<XMLElement> getElements(String name){
        return getElements(element -> element.equalsName(name));
    }
    public int getAttributeCount(){
        return mAttributes.size();
    }
    public String getAttributeValue(String name){
        XMLAttribute attribute = getAttribute(name);
        if(attribute != null){
            return attribute.getValue();
        }
        return null;
    }
    public XMLAttribute getAttribute(String name){
        if(name == null){
            return null;
        }
        int count = getAttributeCount();
        for(int i = 0; i < count; i++){
            XMLAttribute attribute = getAttributeAt(i);
            if (attribute.equalsName(name)){
                return attribute;
            }
        }
        return null;
    }
    public void clearAttributes(){
        if(mAttributes.size() == 0){
            return;
        }
        for(int i = 0; i < mAttributes.size(); i++){
            XMLAttribute attribute = mAttributes.get(i);
            attribute.setParent(null);
        }
        mAttributes.clear();
        mAttributes.trimToSize();
    }
    public XMLAttribute removeAttribute(String name){
        XMLAttribute attribute = getAttribute(name);
        if(attribute != null){
            attribute.setParent(null);
        }
        return attribute;
    }
    public XMLAttribute removeAttribute(XMLAttribute attribute){
        if( mAttributes.remove(attribute) && attribute != null){
            attribute.setParent(null);
        }
        return attribute;
    }
    public XMLAttribute removeAttributeAt(int index){
        XMLAttribute attribute = mAttributes.remove(index);
        if(attribute != null){
            attribute.setParent(null);
        }
        return attribute;
    }
    public XMLAttribute setAttribute(String name, String value){
        if(XMLUtil.isEmpty(name)){
            return null;
        }
        XMLAttribute xmlAttribute = getAttribute(name);
        if(xmlAttribute == null){
            if(XMLNamespace.looksNamespace(name, value)){
                XMLNamespace namespace = new XMLNamespace(value, XMLUtil.splitName(name));
                addNamespace(namespace);
            }else{
                addAttribute(newAttribute().set(name,value));
            }
        }else {
            xmlAttribute.setValue(value);
        }
        return xmlAttribute;
    }
    public void addAttribute(String name, String value){
        if(XMLUtil.isEmpty(name)){
            return;
        }
        if(XMLNamespace.looksNamespace(name, value)){
            XMLNamespace namespace = new XMLNamespace(value, XMLUtil.splitName(name));
            addNamespace(namespace);
        }else{
            addAttribute(newAttribute().set(name,value));
        }
    }
    public void addAttribute(String uri, String name, String value){
        if(XMLUtil.isEmpty(name)){
            return;
        }
        if(XMLNamespace.looksNamespace(name, value)){
            XMLNamespace namespace = new XMLNamespace(value, XMLUtil.splitName(name));
            addNamespace(namespace);
        }else{
            XMLAttribute attribute = new XMLAttribute();
            addAttribute(attribute);
            attribute.setName(uri, name);
            attribute.setValue(value);
        }
    }
    public void addAttribute(XMLAttribute xmlAttribute){
        if(xmlAttribute == null){
            return;
        }
        mAttributes.add(xmlAttribute);
        xmlAttribute.setParent(this);
    }
    public XMLElement getParentElement(){
        XMLNode parent = getParent();
        if(parent instanceof XMLElement){
            return (XMLElement) parent;
        }
        return null;
    }
    public XMLElement getRootElement(){
        XMLElement parent = getParentElement();
        if(parent != null){
            return parent.getRootElement();
        }
        return this;
    }
    public XMLDocument getParentDocument(){
        XMLElement root = getRootElement();
        XMLNode parent = root.getParent();
        if(parent instanceof XMLDocument){
            return (XMLDocument) parent;
        }
        return null;
    }
    public int getDepth(){
        int result = 0;
        XMLElement parent = getParentElement();
        while (parent != null){
            result ++;
            parent = parent.getParentElement();
        }
        return result;
    }
    public boolean equalsName(String name){
        if(name == null){
            return getName() == null;
        }
        String prefix = XMLUtil.splitPrefix(name);
        if(prefix != null && !prefix.equals(getPrefix())){
            return false;
        }
        return name.equals(getName());
    }
    public String getName(){
        return mName;
    }
    public String getName(boolean includePrefix){
        String name = mName;
        if(!includePrefix){
            return name;
        }
        String prefix = getPrefix();
        if(prefix != null){
            name = prefix + ":" + name;
        }
        return name;
    }
    public void setName(String name){
        setName(null, null, name);
    }
    public void setName(String uri, String name){
        setName(uri, null, name);
    }
    public void setName(String uri, String prefix, String name){
        mName = XMLUtil.splitName(name);
        if(prefix == null){
            prefix = XMLUtil.splitPrefix(name);
        }
        if(XMLUtil.isEmpty(uri)){
            uri = null;
        }
        if(uri == null && prefix == null){
            return;
        }
        XMLNamespace namespace;
        if(uri == null){
            namespace = getXMLNamespaceByPrefix(prefix);
            if(namespace == null){
                throw new IllegalArgumentException("Namespace not found for prefix: " + prefix);
            }
        }else {
            namespace = getXMLNamespaceByUri(uri);
            if(namespace == null){
                throw new IllegalArgumentException("Namespace not found for uri: " + uri);
            }
        }
        setNamespace(namespace);
    }
    public String getTextContent(){
        boolean hasText = false;
        StringWriter writer = new StringWriter();
        try {
            Iterator<XMLNode> iterator = iterator();
            while (iterator.hasNext()){
                XMLNode xmlNode = iterator.next();
                xmlNode.write(writer, true);
                hasText = hasText | xmlNode instanceof XMLText;
            }
            writer.flush();
            writer.close();
            if(hasText){
                return writer.toString();
            }
        } catch (IOException ignored) {
        }
        return null;
    }
    public boolean hasChildElements(){
        return !CollectionUtil.isEmpty(getElements());
    }
    public boolean hasTextContent() {
        return !CollectionUtil.isEmpty(iterator(XMLText.class));
    }
    public void setTextContent(String text, boolean escape){
        super.clear();
        if(escape){
            text = XMLUtil.escapeXmlChars(text);
        }
        addText(text);
    }
    @Override
    void startSerialize(XmlSerializer serializer) throws IOException {
        int count = getNamespaceCount();
        for(int i = 0; i < count; i++){
            XMLNamespace namespace = getNamespaceAt(i);
            serializer.setPrefix(namespace.getPrefix(),
                    namespace.getUri());
        }
        serializer.startTag(getUri(), getName(false));
        count = getAttributeCount();
        for(int i = 0; i < count; i++){
            getAttributeAt(i).serialize(serializer);
        }
    }
    @Override
    void endSerialize(XmlSerializer serializer) throws IOException {
        serializer.endTag(getUri(), getName(false));
    }

    @Override
    public void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        int event = parser.getEventType();
        if(event != XmlPullParser.START_TAG){
            throw new XmlPullParserException("Not START_TAG event");
        }
        parseNamespaces(parser);
        setName(parser.getNamespace(), parser.getPrefix(), parser.getName());
        parseAttributes(parser);
        event = parser.next();
        XMLText xmlText = null;
        while (event != XmlPullParser.END_TAG && event != XmlPullParser.END_DOCUMENT){
            if(event == XmlPullParser.START_TAG){
                XMLElement element = newElement();
                add(element);
                element.parse(parser);
                xmlText = null;
            }else if(XMLText.isTextEvent(event)){
                if(xmlText == null){
                    xmlText = newText();
                }
                xmlText.parse(parser);
                if(!xmlText.isIndent()){
                    add(xmlText);
                }
            }else if(event == XmlPullParser.COMMENT){
                XMLComment comment = newComment();
                if(comment != null){
                    add(comment);
                    comment.parse(parser);
                }else {
                    parser.next();
                }
                xmlText = null;
            }else {
                xmlText = null;
                parser.next();
            }
            event = parser.getEventType();
        }
        if(parser.getEventType() == XmlPullParser.END_TAG){
            parser.next();
        }
    }
    private void parseNamespaces(XmlPullParser parser) throws XmlPullParserException {
        int count = parser.getNamespaceCount(getDepth());
        for(int i = 0; i < count; i++){
            addNamespace(parser.getNamespaceUri(i),
                    parser.getNamespacePrefix(i));
        }
        count = parser.getAttributeCount();
        for(int i = 0; i < count; i++){
            String name = parser.getAttributeName(i);
            String value = parser.getAttributeValue(i);
            if(XMLNamespace.looksNamespace(name, value)){
                addNamespace(value, XMLUtil.splitName(name));
            }
        }
    }
    private void parseAttributes(XmlPullParser parser){
        int count = parser.getAttributeCount();
        for(int i = 0; i < count; i++){
            String name = parser.getAttributeName(i);
            String value = parser.getAttributeValue(i);
            if(!XMLNamespace.looksNamespace(name, value)){
                String uri = parser.getAttributeNamespace(i);
                addAttribute(uri, name, value);
            }
        }
    }
    @Override
    void write(Appendable appendable) throws IOException {
        appendable.append('<');
        appendable.append(getName());
        appendAttributes(appendable);
        boolean haveChildes = false;
        Iterator<XMLNode> itr = iterator();
        while (itr.hasNext()){
            if(!haveChildes){
                appendable.append(">");
            }
            XMLNode child = itr.next();
            child.write(appendable);
            haveChildes = true;
        }
        if(haveChildes){
            appendable.append("</");
            appendable.append(getName());
            appendable.append('>');
        }else {
            appendable.append("/>");
        }
    }
    private void appendAttributes(Appendable appendable) throws IOException {
        Iterator<? extends XMLAttribute> itr = getAttributes();
        while (itr.hasNext()){
            itr.next().write(appendable);
        }
    }
    protected List<XMLNode> listSpannable(){
        List<XMLNode> results = new ArrayList<>();
        for(XMLNode child:getChildNodes()){
            if((child instanceof XMLElement) || (child instanceof XMLText)){
                results.add(child);
            }
        }
        return results;
    }
    protected String getSpannableText() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        for(XMLAttribute attribute:listAttributes()){
            builder.append(' ');
            builder.append(attribute.toText(0, false));
        }
        return builder.toString();
    }

    private static XMLElement parseSpanSafe(String spanText){
        if(spanText==null){
            return null;
        }
        try {
            XMLSpanParser spanParser = new XMLSpanParser();
            return spanParser.parse(spanText);
        } catch (IOException ignored) {
            return null;
        }
    }
}
