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

import com.reandroid.arsc.array.ResXmlAttributeArray;
import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.model.ResourceLibrary;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.SingleBlockContainer;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ResXmlString;
import com.reandroid.arsc.pool.ResXmlStringPool;
import com.reandroid.common.Namespace;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.collection.SingleIterator;
import com.reandroid.utils.collection.*;
import com.reandroid.xml.*;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class ResXmlElement extends ResXmlNode implements JSONConvert<JSONObject>,
        Comparator<ResXmlNode> {
    private final BlockList<ResXmlStartNamespace> mStartNamespaceList;
    private final SingleBlockContainer<ResXmlStartElement> mStartElementContainer;
    private final BlockList<ResXmlNode> mBody;
    private final SingleBlockContainer<ResXmlEndElement> mEndElementContainer;
    private final BlockList<ResXmlEndNamespace> mEndNamespaceList;

    public ResXmlElement() {
        super(5);
        this.mStartNamespaceList = new BlockList<>();
        this.mStartElementContainer= new SingleBlockContainer<>();
        this.mBody = new BlockList<>();
        this.mEndElementContainer = new SingleBlockContainer<>();
        this.mEndNamespaceList = new BlockList<>();
        addChild(0, mStartNamespaceList);
        addChild(1, mStartElementContainer);
        addChild(2, mBody);
        addChild(3, mEndElementContainer);
        addChild(4, mEndNamespaceList);
    }

    public void addIndent(int scale){
        if(hasText()){
            return;
        }
        int depth = getDepth();
        if(depth > MAX_INDENT_DEPTH){
            depth = MAX_INDENT_DEPTH;
        }
        int indent = (depth + 1) * scale;
        ResXmlTextNode textNode = null;
        for (ResXmlElement element : listElements()){
            textNode = createResXmlTextNode(element.getIndex());
            textNode.makeIndent(indent);
            element.addIndent(scale);
        }
        if(textNode != null){
            indent = depth * scale;
            textNode = new ResXmlTextNode();
            addNode(textNode);
            textNode.makeIndent(indent);
        }
    }
    public int clearIndents(){
        return removeNodes(resXmlNode -> {
            if(resXmlNode instanceof ResXmlTextNode){
                return  ((ResXmlTextNode) resXmlNode).isIndent();
            }
            return false;
        });
    }
    /**
     * Iterates every xml-nodes (ResXmlElement & ResXmlTextNode) and child nodes recursively
     *
     * */
    public Iterator<ResXmlNode> recursiveXmlNodes() throws ConcurrentModificationException{
        return CombiningIterator.of(SingleIterator.of(this),
                ComputeIterator.of(getResXmlNodes(), xmlNode -> {
                    if(xmlNode instanceof ResXmlElement){
                        return ((ResXmlElement) xmlNode).recursiveXmlNodes();
                    }
                    return SingleIterator.of(xmlNode);
                }));
    }
    /**
     * Iterates every attribute on this element and on child elements recursively
     * */
    public Iterator<ResXmlAttribute> recursiveAttributes() throws ConcurrentModificationException{
        return new MergingIterator<>(new ComputeIterator<>(recursiveElements(), RECURSIVE_ATTRIBUTES));
    }
    /**
     * Iterates every element and child elements recursively
     * */
    public Iterator<ResXmlElement> recursiveElements(){
        return CombiningIterator.of(SingleIterator.of(this),
                ComputeIterator.of(getElements(), RECURSIVE_ELEMENTS));
    }
    public ResXmlAttribute getIdAttribute(){
        ResXmlStartElement startElement = getStartElement();
        if(startElement != null){
            return startElement.getIdAttribute();
        }
        return null;
    }
    public ResXmlAttribute getClassAttribute(){
        ResXmlStartElement startElement = getStartElement();
        if(startElement != null){
            return startElement.getClassAttribute();
        }
        return null;
    }
    public ResXmlAttribute getStyleAttribute(){
        ResXmlStartElement startElement = getStartElement();
        if(startElement != null){
            return startElement.getStyleAttribute();
        }
        return null;
    }
    public ResXmlNamespace getNamespaceAt(int i){
        return mStartNamespaceList.get(i);
    }
    public int getNamespaceCount(){
        return mStartNamespaceList.size();
    }
    public ResXmlNamespace getNamespace(String uri, String prefix){
        return getXmlStartNamespace(uri, prefix);
    }
    public ResXmlNamespace getOrCreateNamespace(String uri, String prefix){
        return getOrCreateXmlStartNamespace(uri, prefix);
    }
    public ResXmlNamespace newNamespace(String uri, String prefix){
        return createXmlStartNamespace(uri, prefix);
    }
    public ResXmlNamespace getNamespaceByUri(String uri){
        return getStartNamespaceByUri(uri);
    }
    public ResXmlNamespace getNamespaceByPrefix(String prefix){
        return getStartNamespaceByPrefix(prefix, null);
    }
    public ResXmlNamespace getOrCreateNamespaceByPrefix(String prefix){
        if(prefix == null || prefix.trim().length() == 0){
            return null;
        }
        ResXmlNamespace namespace = getNamespaceByPrefix(prefix);
        if(namespace != null){
            return namespace;
        }
        String uri;
        if(ResourceLibrary.PREFIX_ANDROID.equals(prefix)){
            uri = ResourceLibrary.URI_ANDROID;
        }else {
            uri = ResourceLibrary.URI_RES_AUTO;
        }
        return getOrCreateNamespace(uri, prefix);
    }
    public int autoSetAttributeNamespaces(){
        int changedCount = 0;
        for(ResXmlAttribute attribute : listAttributes()){
            boolean changed = attribute.autoSetNamespace();
            if(changed){
                changedCount ++;
            }
        }
        for(ResXmlNode child : getXmlNodeList()){
            if(child instanceof ResXmlElement){
                changedCount += ((ResXmlElement)child).autoSetAttributeNamespaces();
            }
        }
        if(fixEmptyNamespaces()){
            changedCount ++;
        }
        return changedCount;
    }
    public boolean fixEmptyNamespaces(){
        boolean changed = false;
        for(ResXmlStartNamespace ns : getStartNamespaceList()){
            if(ns.fixEmpty()){
                changed = true;
            }
        }
        return changed;
    }
    public int autoSetAttributeNames(){
        int changedCount = 0;
        for(ResXmlAttribute attribute : listAttributes()){
            boolean changed = attribute.autoSetName();
            if(changed){
                changedCount ++;
            }
        }
        for(ResXmlNode child : getXmlNodeList()){
            if(child instanceof ResXmlElement){
                changedCount += ((ResXmlElement)child).autoSetAttributeNames();
            }
        }
        return changedCount;
    }
    @Override
    int autoSetLineNumber(int start){
        start ++;
        setLineNumber(start);
        int attrCount = getAttributeCount();
        if(attrCount != 0){
            start +=(attrCount - 1);
        }
        boolean haveElement = false;
        for(ResXmlNode xmlNode : getXmlNodeList()){
            start = xmlNode.autoSetLineNumber(start);
            if(!haveElement && xmlNode instanceof ResXmlElement){
                haveElement = true;
            }
        }
        if(haveElement){
            start ++;
        }
        return start;
    }
    public void clearNullNodes(){
        clearNullNodes(true);
    }
    private void clearNullNodes(boolean recursive){
        for(ResXmlNode node:listXmlNodes()){
            if(node.isNull()){
                removeNode(node);
            }
            if(!recursive || !(node instanceof ResXmlElement)){
                continue;
            }
            ((ResXmlElement)node).clearNullNodes(true);
        }
    }
    int removeUnusedNamespaces(){
        int count = 0;
        List<ResXmlStartNamespace> nsList = new ArrayList<>(getStartNamespaceList());
        for(ResXmlStartNamespace ns : nsList){
            boolean removed = ns.removeIfNoReference();
            if(removed){
                count ++;
            }
        }
        for(ResXmlNode node : getXmlNodeList()){
            if(node instanceof ResXmlElement){
                ResXmlElement child = (ResXmlElement) node;
                count += child.removeUnusedNamespaces();
            }
        }
        return count;
    }
    public int removeUndefinedAttributes(){
        int count = 0;
        ResXmlStartElement start = getStartElement();
        if(start != null){
            count += start.removeUndefinedAttributes();
        }
        for(ResXmlNode xmlNode : getXmlNodeList()){
            if(xmlNode instanceof ResXmlElement){
                count += ((ResXmlElement)xmlNode).removeUndefinedAttributes();
            }
        }
        return count;
    }
    public void changeIndex(ResXmlElement element, int index){
        int i = 0;
        for(ResXmlNode xmlNode:mBody.getChildes()){
            if(i == index){
                element.setIndex(i);
                i++;
            }
            if(xmlNode==element){
                continue;
            }
            xmlNode.setIndex(i);
            i++;
        }
        mBody.sort(this);
    }
    public int indexOf(String tagName){
        ResXmlElement element = getElementByTagName(tagName);
        if(element != null){
            return element.getIndex();
        }
        return -1;
    }
    public int lastIndexOf(String tagName){
        return lastIndexOf(tagName, -1);
    }
    public int lastIndexOf(String tagName, int def){
        ResXmlElement last = CollectionUtil.getLast(getElements(tagName));
        if(last != null){
            def = indexOf(last, def);
        }
        return def;
    }
    public int indexOf(ResXmlElement element){
        return indexOf(element, -1);
    }
    public int indexOf(ResXmlNode resXmlNode, int def){
        int index = 0;
        for(ResXmlNode xmlNode : mBody.getChildes()){
            if(xmlNode == resXmlNode){
                return index;
            }
            index++;
        }
        return def;
    }
    public void setAttributesUnitSize(int size, boolean setToAll){
        ResXmlStartElement startElement = getStartElement();
        startElement.setAttributesUnitSize(size);
        if(setToAll){
            for(ResXmlElement child:listElements()){
                child.setAttributesUnitSize(size, setToAll);
            }
        }
    }
    public String getStartComment(){
        ResXmlStartElement start = getStartElement();
        if(start!=null){
            return start.getComment();
        }
        return null;
    }
    String getEndComment(){
        ResXmlEndElement end = getEndElement();
        if(end!=null){
            return end.getComment();
        }
        return null;
    }
    public int getLineNumber(){
        ResXmlStartElement start = getStartElement();
        if(start != null){
            return start.getLineNumber();
        }
        return 0;
    }
    public void setLineNumber(int lineNumber){
        ResXmlStartElement start = getStartElement();
        if(start != null){
            start.setLineNumber(lineNumber);
            start.getResXmlEndElement().setLineNumber(lineNumber);
        }
        int count = getNamespaceCount();
        for(int i = 0; i < count; i++){
           getNamespaceAt(i).setLineNumber(lineNumber);
        }
    }
    public int getStartLineNumber(){
        ResXmlStartElement start = getStartElement();
        if(start!=null){
            return start.getLineNumber();
        }
        return 0;
    }
    public int getEndLineNumber(){
        ResXmlEndElement end = getEndElement();
        if(end!=null){
            return end.getLineNumber();
        }
        return 0;
    }
    public void setStartLineNumber(int lineNumber){
        ResXmlStartElement start = getStartElement();
        if(start!=null){
            start.setLineNumber(lineNumber);
        }
    }
    public void setEndLineNumber(int lineNumber){
        ResXmlEndElement end = getEndElement();
        if(end != null){
            end.setLineNumber(lineNumber);
        }
    }
    public void setComment(String comment){
        getStartElement().setComment(comment);
    }
    public void calculateAttributesOrder(){
        ResXmlStartElement start = getStartElement();
        if(start != null){
            start.calculatePositions();
        }
    }
    public ResXmlAttribute newAttribute(){
        return getStartElement().newAttribute();
    }
    @Override
    void onRemoved(){
        for(ResXmlStartNamespace startNamespace:getStartNamespaceList()){
            startNamespace.onRemoved();
        }
        ResXmlStartElement start = getStartElement();
        if(start != null){
            start.onRemoved();
        }
        for(ResXmlNode xmlNode : listXmlNodes()){
            xmlNode.onRemoved();
        }
    }
    @Override
    void linkStringReferences(){
        for(ResXmlStartNamespace startNamespace:getStartNamespaceList()){
            startNamespace.linkStringReferences();
        }
        ResXmlStartElement start = getStartElement();
        if(start != null){
            start.linkStringReferences();
        }
        for(ResXmlNode xmlNode : getXmlNodeList()){
            xmlNode.linkStringReferences();
        }
    }
    public ResXmlElement createChildElement(){
        return createChildElement(-1, null);
    }
    public ResXmlElement createChildElement(int position){
        return createChildElement(position, null);
    }
    public ResXmlElement createChildElement(String name){
        return createChildElement(-1, name);
    }
    public ResXmlElement createChildElement(int position, String name){
        int lineNo = getStartElement().getLineNumber() + 1;
        ResXmlElement resXmlElement = new ResXmlElement();
        resXmlElement.newStartElement(lineNo);
        if(position >= 0){
            addNode(position, resXmlElement);
        }else {
            addNode(resXmlElement);
        }
        if(name != null){
            resXmlElement.setName(name);
        }
        return resXmlElement;
    }
    public ResXmlTextNode createResXmlTextNode(){
        return createResXmlTextNode(-1, null);
    }
    public ResXmlTextNode createResXmlTextNode(int position){
        return createResXmlTextNode(position, null);
    }
    public ResXmlTextNode createResXmlTextNode(String text){
        return createResXmlTextNode(-1, text);
    }
    public ResXmlTextNode createResXmlTextNode(int position, String text){
        ResXmlTextNode xmlTextNode = new ResXmlTextNode();
        if(position >= 0){
            addNode(position, xmlTextNode);
        }else {
            addNode(xmlTextNode);
        }
        if(text != null){
            xmlTextNode.setText(text);
        }
        return xmlTextNode;
    }
    public void addResXmlText(String text){
        if(text == null){
            return;
        }
        createResXmlTextNode(text);
    }
    public ResXmlAttribute getOrCreateAndroidAttribute(String name, int resourceId){
        return getOrCreateAttribute(
                ResourceLibrary.URI_ANDROID,
                ResourceLibrary.PREFIX_ANDROID,
                name,
                resourceId);
    }
    public ResXmlAttribute getOrCreateAttribute(String uri, String prefix, String name, int resourceId){
        ResXmlAttribute attribute = searchAttribute(name, resourceId);
        if(attribute == null){
            attribute = createAttribute(name, resourceId);
            if(uri != null && resourceId != 0){
                attribute.setNamespace(uri, prefix);
            }
        }
        return attribute;
    }
    public ResXmlAttribute getOrCreateAttribute(String name, int resourceId){
        ResXmlAttribute attribute=searchAttribute(name, resourceId);
        if(attribute==null){
            attribute=createAttribute(name, resourceId);
        }
        return attribute;
    }
    public ResXmlAttribute createAndroidAttribute(String name, int resourceId){
        ResXmlAttribute attribute=createAttribute(name, resourceId);
        ResXmlStartNamespace ns = getOrCreateXmlStartNamespace(ResourceLibrary.URI_ANDROID,
                ResourceLibrary.PREFIX_ANDROID);
        attribute.setNamespaceReference(ns.getUriReference());
        return attribute;
    }
    public ResXmlAttribute createAttribute(String name, int resourceId){
        ResXmlAttribute attribute=new ResXmlAttribute();
        addAttribute(attribute);
        attribute.setName(name, resourceId);
        return attribute;
    }
    public void addAttribute(ResXmlAttribute attribute){
        getStartElement().getResXmlAttributeArray().add(attribute);
    }
    public ResXmlElement getElementByTagName(String name){
        if(name == null){
            return null;
        }
        return CollectionUtil.getFirst(getElements(name));
    }
    private ResXmlAttribute searchAttribute(String name, int resourceId){
        if(resourceId==0){
            return searchAttributeByName(name);
        }
        return searchAttributeByResourceId(resourceId);
    }
    // Searches attribute with resource id = 0
    public ResXmlAttribute searchAttributeByName(String name){
        ResXmlStartElement startElement=getStartElement();
        if(startElement!=null){
            return startElement.searchAttributeByName(name);
        }
        return null;
    }
    public ResXmlAttribute searchAttributeByResourceId(int resourceId){
        ResXmlStartElement startElement=getStartElement();
        if(startElement!=null){
            return startElement.searchAttributeByResourceId(resourceId);
        }
        return null;
    }
    public String getName(){
        return getName(false);
    }
    public String getName(boolean includePrefix){
        ResXmlStartElement startElement = getStartElement();
        if(startElement != null){
            return startElement.getTagName(includePrefix);
        }
        return null;
    }
    public void setName(String uri, String prefix, String name){
        setName(name);
        setTagNamespace(uri, prefix);
    }
    public void setName(String name){
        ResXmlStringPool stringPool = getStringPool();
        if(stringPool == null){
            return;
        }
        ensureStartEndElement();
        ResXmlStartElement startElement = getStartElement();
        if(name == null){
            startElement.setName(null);
            return;
        }
        String prefix = null;
        int i = name.lastIndexOf(':');
        if(i >= 0){
            prefix = name.substring(0, i);
            i++;
            name = name.substring(i);
        }
        startElement.setName(name);
        if(prefix == null){
            return;
        }
        ResXmlNamespace namespace = getOrCreateNamespaceByPrefix(prefix);
        if(namespace != null){
            startElement.setNamespaceReference(namespace.getUriReference());
        }
    }
    public boolean equalsName(String name){
        if(name == null){
            return getName() == null;
        }
        name = XMLUtil.splitName(name);
        return name.equals(getName(false));
    }
    public String getUri(){
        ResXmlStartElement startElement = getStartElement();
        if(startElement != null){
            return startElement.getUri();
        }
        return null;
    }
    public String getPrefix(){
        ResXmlStartElement startElement = getStartElement();
        if(startElement != null){
            return startElement.getPrefix();
        }
        return null;
    }
    /**
     * Use setName(String)
     * */
    @Deprecated
    public void setTag(String tag){
        setName(tag);
    }
    /**
     * Use getName(true)
     * */
    @Deprecated
    public String getTagName(){
        return getName(true);
    }
    /**
     * Use getName()
     * */
    @Deprecated
    public String getTag(){
        return getName();
    }
    /**
     * Use getUri()
     * */
    @Deprecated
    public String getTagUri(){
        return getUri();
    }
    /**
     * Use getPrefix()
     * */
    @Deprecated
    public String getTagPrefix(){
        return getPrefix();
    }
    public ResXmlNamespace getTagNamespace(){
        ResXmlStartElement startElement = getStartElement();
        if(startElement != null){
            return startElement.getResXmlStartNamespace();
        }
        return null;
    }
    public void setTagNamespace(Namespace namespace){
        if(namespace != null){
            setTagNamespace(namespace.getUri(), namespace.getPrefix());
        }else {
            setTagNamespace(null, null);
        }
    }
    public void setTagNamespace(String uri, String prefix){
        ResXmlStartElement startElement = getStartElement();
        if(startElement != null){
            startElement.setTagNamespace(uri, prefix);
        }
    }
    public int removeAttributesWithId(int resourceId){
        return removeAttributes(getAttributesWithId(resourceId));
    }
    public int removeAttributesWithName(String name){
        return removeAttributes(getAttributesWithName(name));
    }
    public int removeAttributes(Predicate<? super ResXmlAttribute> predicate){
        return removeAttributes(getAttributes(predicate));
    }
    public int removeAttributes(Iterator<? extends ResXmlAttribute> attributes){
        Iterator<ResXmlAttribute> iterator = CollectionUtil.copyOf(attributes);
        int count = 0;
        while (iterator.hasNext()){
            boolean removed = removeAttribute(iterator.next());
            if(removed){
                count ++;
            }
        }
        return count;
    }
    public Iterator<ResXmlAttribute> getAttributes(){
        ResXmlAttributeArray attributeArray = getAttributeArray();
        if(attributeArray != null){
            if(attributeArray.getChildesCount() == 0){
                return EmptyIterator.of();
            }
            return attributeArray.iterator();
        }
        return EmptyIterator.of();
    }
    public Iterator<ResXmlAttribute> getAttributes(Predicate<? super ResXmlAttribute> filter){
        ResXmlAttributeArray attributeArray = getAttributeArray();
        if(attributeArray != null){
            if(attributeArray.getChildesCount() == 0){
                return EmptyIterator.of();
            }
            return attributeArray.iterator(filter);
        }
        return EmptyIterator.of();
    }
    public Iterator<ResXmlAttribute> getAttributesWithId(int resourceId){
        return getAttributes(attribute -> attribute.getNameResourceID() == resourceId);
    }
    public Iterator<ResXmlAttribute> getAttributesWithName(String name){
        return getAttributes(attribute ->
                attribute.getNameResourceID() == 0 && attribute.equalsName(name));
    }
    public int getAttributeCount() {
        ResXmlStartElement startElement=getStartElement();
        if(startElement!=null){
            return startElement.getResXmlAttributeArray().getChildesCount();
        }
        return 0;
    }
    public ResXmlAttribute getAttributeAt(int index){
        ResXmlStartElement startElement=getStartElement();
        if(startElement!=null){
            return startElement.getResXmlAttributeArray().get(index);
        }
        return null;
    }
    public Collection<ResXmlAttribute> listAttributes(){
        ResXmlStartElement startElement=getStartElement();
        if(startElement!=null){
            return startElement.listResXmlAttributes();
        }
        return new ArrayList<>();
    }
    public ResXmlStringPool getStringPool(){
        Block parent=getParent();
        while (parent!=null){
            if(parent instanceof ResXmlDocument){
                return ((ResXmlDocument)parent).getStringPool();
            }
            if(parent instanceof ResXmlElement){
                return ((ResXmlElement)parent).getStringPool();
            }
            parent=parent.getParent();
        }
        return null;
    }
    public ResXmlIDMap getResXmlIDMap(){
        ResXmlDocument resXmlDocument = getParentDocument();
        if(resXmlDocument!=null){
            return resXmlDocument.getResXmlIDMap();
        }
        return null;
    }
    public ResXmlDocument getParentDocument(){
        return getParentInstance(ResXmlDocument.class);
    }

    @Override
    public int getDepth(){
        ResXmlElement parent = getParentResXmlElement();
        if(parent != null){
            return parent.getDepth() + 1;
        }
        return 0;
    }
    @Override
    void addEvents(ParserEventList parserEventList){
        String comment = getStartComment();
        if(comment!=null){
            parserEventList.add(
                    new ParserEvent(ParserEvent.COMMENT, this, comment, false));
        }
        parserEventList.add(new ParserEvent(ParserEvent.START_TAG, this));
        for(ResXmlNode xmlNode: getXmlNodeList()){
            xmlNode.addEvents(parserEventList);
        }
        comment = getEndComment();
        if(comment!=null){
            parserEventList.add(
                    new ParserEvent(ParserEvent.COMMENT, this, comment, true));
        }
        parserEventList.add(new ParserEvent(ParserEvent.END_TAG, this));
    }
    public void addElement(ResXmlElement element){
        addNode(element);
    }
    public boolean removeAttribute(ResXmlAttribute resXmlAttribute){
        if(resXmlAttribute != null){
            resXmlAttribute.onRemoved();
        }
        return getStartElement().getResXmlAttributeArray().remove(resXmlAttribute);
    }
    public boolean removeSelf(){
        ResXmlElement parent = getParentResXmlElement();
        if(parent != null){
            return parent.removeElement(this);
        }
        return false;
    }
    public boolean removeElement(ResXmlElement element){
        if(element !=null && element.getParent()!=null){
            element.onRemoved();
        }
        return mBody.remove(element);
    }
    public boolean removeNode(ResXmlNode node){
        if(node instanceof ResXmlElement){
            return removeElement((ResXmlElement) node);
        }
        return mBody.remove(node);
    }
    public int countElements(){
        int result = 0;
        for(ResXmlNode xmlNode: getXmlNodeList()){
            if(xmlNode instanceof ResXmlElement){
                result++;
            }
        }
        return result;
    }
    public void clearChildes(){
        ResXmlNode[] copyOfNodeList=mBody.getChildes().toArray(new ResXmlNode[0]);
        for(ResXmlNode xmlNode:copyOfNodeList){
            if(xmlNode==null){
                continue;
            }
            xmlNode.onRemoved();
            mBody.remove(xmlNode);
        }
    }
    public ResXmlNode getResXmlNode(int position){
        return mBody.get(position);
    }
    public int countResXmlNodes(){
        return mBody.size();
    }
    public boolean hasText(){
        for(ResXmlNode xmlNode : getXmlNodeList()){
            if(xmlNode instanceof ResXmlTextNode){
                return true;
            }
        }
        return false;
    }
    public boolean hasElement(){
        for(ResXmlNode xmlNode : getXmlNodeList()){
            if(xmlNode instanceof ResXmlElement){
                return true;
            }
        }
        return false;
    }
    public List<ResXmlNode> listXmlNodes(){
        return new ArrayList<>(getXmlNodeList());
    }
    public Iterator<ResXmlNode> getResXmlNodes(){
        return mBody.iterator();
    }
    public Iterator<ResXmlNode> getResXmlNodes(Predicate<? super ResXmlNode> predicate){
        return mBody.iterator(predicate);
    }
    private List<ResXmlNode> getXmlNodeList(){
        return mBody.getChildes();
    }
    public int removeNodes(Predicate<? super ResXmlNode> predicate){
        List<ResXmlNode> removeList = CollectionUtil.toList(getResXmlNodes(predicate));
        Iterator<ResXmlNode> iterator = removeList.iterator();
        int count = 0;
        while (iterator.hasNext()){
            boolean removed = removeNode(iterator.next());
            if(removed){
                count ++;
            }
        }
        return count;
    }
    public Iterator<ResXmlTextNode> getResXmlTextNodes(){
        return InstanceIterator.of(getResXmlNodes(), ResXmlTextNode.class);
    }
    public List<ResXmlTextNode> listXmlTextNodes(){
        return CollectionUtil.toList(getResXmlTextNodes());
    }
    public int removeElements(Predicate<? super ResXmlElement> predicate){
        List<ResXmlElement> removeList = CollectionUtil.toList(getElements(predicate));
        Iterator<ResXmlElement> iterator = removeList.iterator();
        int count = 0;
        while (iterator.hasNext()){
            boolean removed = removeElement(iterator.next());
            if(removed){
                count ++;
            }
        }
        return count;
    }
    public Iterator<ResXmlElement> getElements(){
        return InstanceIterator.of(getResXmlNodes(), ResXmlElement.class);
    }
    public Iterator<ResXmlElement> getElements(Predicate<? super ResXmlElement> filter){
        return InstanceIterator.of(getResXmlNodes(), ResXmlElement.class, filter);
    }
    public Iterator<ResXmlElement> getElements(String name){
        return getElements(element -> element.equalsName(name));
    }
    public List<ResXmlElement> listElements(){
        return CollectionUtil.toList(getElements());
    }
    public List<ResXmlElement> listElements(String name){
        return CollectionUtil.toList(getElements(name));
    }
    public ResXmlElement getRootResXmlElement(){
        ResXmlElement parent = getParentResXmlElement();
        if(parent != null){
            return parent.getRootResXmlElement();
        }
        return this;
    }
    public ResXmlElement getParentResXmlElement(){
        return getParentInstance(ResXmlElement.class);
    }
    ResXmlStartNamespace getStartNamespaceByUriRef(int uriRef){
        if(uriRef<0){
            return null;
        }
        for(ResXmlStartNamespace ns:mStartNamespaceList.getChildes()){
            if(uriRef==ns.getUriReference()){
                return ns;
            }
        }
        ResXmlElement xmlElement=getParentResXmlElement();
        if(xmlElement!=null){
            return xmlElement.getStartNamespaceByUriRef(uriRef);
        }
        return null;
    }
    ResXmlStartNamespace getXmlStartNamespace(String uri, String prefix){
        if(uri == null || prefix == null){
            return null;
        }
        for(ResXmlStartNamespace ns : mStartNamespaceList.getChildes()){
            if(uri.equals(ns.getUri()) && prefix.equals(ns.getPrefix())){
                return ns;
            }
        }
        ResXmlElement xmlElement = getParentResXmlElement();
        if(xmlElement != null){
            return xmlElement.getXmlStartNamespace(uri, prefix);
        }
        return null;
    }
    private ResXmlStartNamespace getOrCreateXmlStartNamespace(String uri, String prefix){
        ResXmlStartNamespace exist = getXmlStartNamespace(uri, prefix);
        if(exist != null){
            return exist;
        }
        return getRootResXmlElement().createXmlStartNamespace(uri, prefix);
    }
    private ResXmlStartNamespace createXmlStartNamespace(String uri, String prefix){
        ResXmlStartNamespace startNamespace = new ResXmlStartNamespace();
        ResXmlEndNamespace endNamespace = new ResXmlEndNamespace();
        startNamespace.setEnd(endNamespace);

        addStartNamespace(startNamespace);
        addEndNamespace(endNamespace, true);
        ResXmlStringPool stringPool = getStringPool();
        ResXmlString xmlString = stringPool.createNew(uri);
        startNamespace.setUriReference(xmlString.getIndex());
        startNamespace.setPrefix(prefix);

        return startNamespace;
    }
    ResXmlStartNamespace getStartNamespaceByUri(String uri){
        if(uri==null){
            return null;
        }
        for(ResXmlStartNamespace ns:mStartNamespaceList.getChildes()){
            if(uri.equals(ns.getUri())){
                return ns;
            }
        }
        ResXmlElement xmlElement=getParentResXmlElement();
        if(xmlElement!=null){
            return xmlElement.getStartNamespaceByUri(uri);
        }
        return null;
    }
    private ResXmlStartNamespace getStartNamespaceByPrefix(String prefix, ResXmlStartNamespace result){
        if(prefix == null){
            return result;
        }
        for(ResXmlStartNamespace ns:getStartNamespaceList()){
            if(!prefix.equals(ns.getPrefix())){
                continue;
            }
            String uri = ns.getUri();
            if(uri != null && uri.length() != 0){
                return ns;
            }
            result = ns;
        }
        ResXmlElement xmlElement = getParentResXmlElement();
        if(xmlElement != null){
            return xmlElement.getStartNamespaceByPrefix(prefix, result);
        }
        return result;
    }
    private List<ResXmlStartNamespace> getStartNamespaceList(){
        return mStartNamespaceList.getChildes();
    }
    private void addStartNamespace(ResXmlStartNamespace item){
        mStartNamespaceList.add(item);
    }
    private void addEndNamespace(ResXmlEndNamespace item, boolean at_first){
        if(at_first){
            mEndNamespaceList.add(0, item);
        }else {
            mEndNamespaceList.add(item);
        }
    }
    void removeNamespace(ResXmlStartNamespace startNamespace){
        if(startNamespace == null){
            return;
        }
        startNamespace.onRemoved();
        mStartNamespaceList.remove(startNamespace);
        mEndNamespaceList.remove(startNamespace.getEnd());
    }

    ResXmlStartElement newStartElement(int lineNo){
        ResXmlStartElement startElement=new ResXmlStartElement();
        setStartElement(startElement);

        ResXmlEndElement endElement=new ResXmlEndElement();
        startElement.setResXmlEndElement(endElement);

        setEndElement(endElement);
        endElement.setResXmlStartElement(startElement);

        startElement.setLineNumber(lineNo);
        endElement.setLineNumber(lineNo);

        return startElement;
    }
    private ResXmlAttributeArray getAttributeArray(){
        ResXmlStartElement startElement = getStartElement();
        if(startElement != null){
            return startElement.getResXmlAttributeArray();
        }
        return null;
    }

    private ResXmlStartElement getStartElement(){
        return mStartElementContainer.getItem();
    }
    private void setStartElement(ResXmlStartElement item){
        mStartElementContainer.setItem(item);
    }

    private ResXmlEndElement getEndElement(){
        return mEndElementContainer.getItem();
    }
    private void setEndElement(ResXmlEndElement item){
        mEndElementContainer.setItem(item);
    }

    private ResXmlTextNode getOrCreateResXmlText(){
        ResXmlNode last = getResXmlNode(countResXmlNodes() - 1);
        if(last instanceof ResXmlTextNode){
            return (ResXmlTextNode) last;
        }
        return createResXmlTextNode();
    }
    public void addNode(ResXmlNode xmlNode){
        mBody.add(xmlNode);
    }
    public void addNode(int position, ResXmlNode xmlNode){
        mBody.add(position, xmlNode);
    }

    private boolean isBalanced(){
        return isElementBalanced() && isNamespaceBalanced();
    }
    private boolean isNamespaceBalanced(){
        return (mStartNamespaceList.size()==mEndNamespaceList.size());
    }
    private boolean isElementBalanced(){
        return (hasStartElement() && hasEndElement());
    }
    private boolean hasStartElement(){
        return mStartElementContainer.hasItem();
    }
    private boolean hasEndElement(){
        return mEndElementContainer.hasItem();
    }

    private void linkStartEnd(){
        linkStartEndElement();
        linkStartEndNameSpaces();
    }
    private void linkStartEndElement(){
        ResXmlStartElement start=getStartElement();
        ResXmlEndElement end=getEndElement();
        if(start==null || end==null){
            return;
        }
        start.setResXmlEndElement(end);
        end.setResXmlStartElement(start);
    }
    private void ensureStartEndElement(){
        ResXmlStartElement start=getStartElement();
        ResXmlEndElement end=getEndElement();
        if(start!=null && end!=null){
            return;
        }
        if(start==null){
            start=new ResXmlStartElement();
            setStartElement(start);
        }
        if(end==null){
            end=new ResXmlEndElement();
            setEndElement(end);
        }
        linkStartEndElement();
    }
    private void linkStartEndNameSpaces(){
        if(!isNamespaceBalanced()){
            return;
        }
        int max=mStartNamespaceList.size();
        for(int i=0;i<max;i++){
            ResXmlStartNamespace start=mStartNamespaceList.get(i);
            ResXmlEndNamespace end=mEndNamespaceList.get(max-i-1);
            start.setEnd(end);
        }
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int pos = reader.getPosition();
        while (readNext(reader)){
            if(pos==reader.getPosition()){
                break;
            }
            pos=reader.getPosition();
        }
    }
    private boolean readNext(BlockReader reader) throws IOException {
        int pos = reader.getPosition();
        if(isBalanced()){
            return false;
        }
        HeaderBlock headerBlock=reader.readHeaderBlock();
        if(headerBlock==null){
            return false;
        }
        ChunkType chunkType=headerBlock.getChunkType();
        if(chunkType==null){
            unknownChunk(reader, headerBlock);
            return false;
        }
        if(chunkType==ChunkType.XML_START_ELEMENT){
            onStartElement(reader);
        }else if(chunkType==ChunkType.XML_END_ELEMENT){
            onEndElement(reader);
        }else if(chunkType==ChunkType.XML_START_NAMESPACE){
            onStartNamespace(reader);
        }else if(chunkType==ChunkType.XML_END_NAMESPACE){
            onEndNamespace(reader);
        }else if(chunkType==ChunkType.XML_CDATA){
            onXmlText(reader);
        }else{
            unexpectedChunk(reader, headerBlock);
        }
        if(!isBalanced()){
            if(!reader.isAvailable()){
                unBalancedFinish(reader);
            }else if(pos!=reader.getPosition()){
                return true;
            }
        }
        linkStartEnd();
        onFinishedRead(reader);
        return false;
    }
    private void onFinishedRead(BlockReader reader) throws IOException{
        if(reader.available() > 3 && getParentResXmlElement() == null){
            onFinishedUnexpected(reader);
        }
    }
    private void onFinishedUnexpected(BlockReader reader) throws IOException{
        StringBuilder builder=new StringBuilder();
        builder.append("Unexpected finish reading: reader=").append(reader.toString());
        HeaderBlock header = reader.readHeaderBlock();
        if(header!=null){
            builder.append(", next header=");
            builder.append(header.toString());
        }
        throw new IOException(builder.toString());
    }
    private void onStartElement(BlockReader reader) throws IOException{
        if(hasStartElement()){
            ResXmlElement childElement=new ResXmlElement();
            addElement(childElement);
            childElement.readBytes(reader);
        }else{
            ResXmlStartElement startElement=new ResXmlStartElement();
            setStartElement(startElement);
            startElement.readBytes(reader);
        }
    }
    private void onEndElement(BlockReader reader) throws IOException{
        if(hasEndElement()){
            multipleEndElement(reader);
            return;
        }
        ResXmlEndElement endElement=new ResXmlEndElement();
        setEndElement(endElement);
        endElement.readBytes(reader);
    }
    private void onStartNamespace(BlockReader reader) throws IOException{
        ResXmlStartNamespace startNamespace=new ResXmlStartNamespace();
        addStartNamespace(startNamespace);
        startNamespace.readBytes(reader);
    }
    private void onEndNamespace(BlockReader reader) throws IOException{
        ResXmlEndNamespace endNamespace=new ResXmlEndNamespace();
        addEndNamespace(endNamespace, false);
        endNamespace.readBytes(reader);
    }
    private void onXmlText(BlockReader reader) throws IOException{
        ResXmlTextNode textNode = createResXmlTextNode();
        textNode.getResXmlText().readBytes(reader);
    }

    private void unknownChunk(BlockReader reader, HeaderBlock headerBlock) throws IOException{
        throw new IOException("Unknown chunk: "+headerBlock.toString());
    }
    private void multipleEndElement(BlockReader reader) throws IOException{
        throw new IOException("Multiple end element: "+reader.toString());
    }
    private void unexpectedChunk(BlockReader reader, HeaderBlock headerBlock) throws IOException{
        throw new IOException("Unexpected chunk: "+headerBlock.toString());
    }
    private void unBalancedFinish(BlockReader reader) throws IOException{
        if(!isNamespaceBalanced()){
            throw new IOException("Unbalanced namespace: start="
                    +mStartNamespaceList.size()+", end="+mEndNamespaceList.size());
        }

        if(!isElementBalanced()){
            // Should not happen unless corrupted file, auto corrected above
            StringBuilder builder=new StringBuilder();
            builder.append("Unbalanced element: start=");
            ResXmlStartElement startElement=getStartElement();
            if(startElement!=null){
                builder.append(startElement);
            }else {
                builder.append("null");
            }
            builder.append(", end=");
            ResXmlEndElement endElement=getEndElement();
            if(endElement!=null){
                builder.append(endElement);
            }else {
                builder.append("null");
            }
            throw new IOException(builder.toString());
        }
    }
    @Override
    public void serialize(XmlSerializer serializer) throws IOException {
        int count = getNamespaceCount();
        for(int i = 0; i < count; i++){
            ResXmlNamespace namespace = getNamespaceAt(i);
            serializer.setPrefix(namespace.getPrefix(),
                    namespace.getUri());
        }
        String comment = getStartComment();
        if(comment != null){
            serializer.comment(comment);
        }
        boolean indent = getFeatureSafe(serializer, FEATURE_INDENT_OUTPUT);
        setIndent(serializer, indent);
        boolean indentChanged = indent;
        serializer.startTag(getUri(), getName());
        count = getAttributeCount();
        for(int i = 0; i < count; i++){
            ResXmlAttribute attribute = getAttributeAt(i);
            attribute.serialize(serializer);
        }
        for(ResXmlNode xmlNode : getXmlNodeList()){
            if(indentChanged && xmlNode instanceof ResXmlTextNode){
                indentChanged = false;
                setIndent(serializer, false);
            }
            xmlNode.serialize(serializer);
        }
        serializer.endTag(getUri(), getName());
        if(indent != indentChanged){
            setIndent(serializer, true);
        }
        serializer.flush();
    }
    @Override
    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        if(parser.getEventType() != XmlPullParser.START_TAG){
            throw new XmlPullParserException("Invalid state START_TAG != "
                    + parser.getEventType());
        }
        String name = parser.getName();
        String prefix = splitPrefix(name);
        name = splitName(name);
        setName(name);
        String uri = parser.getNamespace();
        if(prefix == null){
            prefix = parser.getPrefix();
        }
        parseNamespaces(parser);
        setLineNumber(parser.getLineNumber());
        parseAttributes(parser);
        parseChildes(parser);
        if(prefix != null){
            if(uri == null || uri.length() == 0){
                ResXmlNamespace ns = getNamespaceByPrefix(prefix);
                if(ns != null){
                    uri = ns.getUri();
                }
            }
            setTagNamespace(uri, prefix);
        }
        clearNullNodes(false);
        calculateAttributesOrder();
    }
    private void parseChildes(XmlPullParser parser) throws IOException, XmlPullParserException {
        ResXmlElement currentElement = this;
        int event = parser.next();
        while (event != XmlPullParser.END_TAG && event != XmlPullParser.END_DOCUMENT){
            if(event == XmlPullParser.START_TAG){
                ResXmlElement element = createChildElement();
                element.parse(parser);
                currentElement = element;
            }else if(ResXmlTextNode.isTextEvent(event)){
                ResXmlTextNode textNode = getOrCreateResXmlText();
                textNode.parse(parser);
            }else if(event == XmlPullParser.COMMENT){
                currentElement.setComment(parser.getText());
            }
            event = parser.next();
        }
    }
    private void parseNamespaces(XmlPullParser parser) throws XmlPullParserException {
        int count = parser.getNamespaceCount(parser.getDepth());
        for(int i = 0; i < count; i++){
            ResXmlStartNamespace namespace = createXmlStartNamespace(
                    parser.getNamespaceUri(i),
                    parser.getNamespacePrefix(i));
            namespace.setLineNumber(parser.getLineNumber());
        }
        count = parser.getAttributeCount();
        for(int i = 0; i < count; i++){
            String name = parser.getAttributeName(i);
            String prefix = splitPrefix(name);
            name = splitName(name);
            String value = parser.getAttributeValue(i);
            if(looksNamespace(value, prefix)){
                getOrCreateNamespace(value, name);
            }
        }
    }
    private void parseAttributes(XmlPullParser parser) throws IOException {
        int count = parser.getAttributeCount();
        for(int i = 0; i < count; i++){
            String name = parser.getAttributeName(i);
            String prefix = splitPrefix(name);
            name = splitName(name);
            String value = parser.getAttributeValue(i);
            if(looksNamespace(value, prefix)){
                continue;
            }
            if(prefix == null){
                prefix = parser.getAttributePrefix(i);
                if(prefix != null && prefix.length() == 0){
                    prefix = null;
                }
            }
            String uri;
            if(prefix != null){
                uri = parser.getAttributeNamespace(i);
                if(uri.length() == 0){
                    ResXmlNamespace ns = getNamespaceByPrefix(prefix);
                    if(ns != null){
                        uri = ns.getUri();
                    }
                }
            }else {
                uri = null;
            }
            ResXmlAttribute attribute = newAttribute();
            attribute.encode(false, uri, prefix, name, value);
        }
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(NAME_node_type, NAME_element);
        jsonObject.put(NAME_name, getName(false));
        jsonObject.put(NAME_namespace_uri, getUri());
        jsonObject.put(NAME_namespace_prefix, getPrefix());
        int lineStart = getStartLineNumber();
        int lineEnd = getEndLineNumber();
        jsonObject.put(NAME_line, lineStart);
        if(lineStart != lineEnd){
            jsonObject.put(NAME_line_end, lineEnd);
        }
        JSONArray nsList = new JSONArray();
        for(ResXmlStartNamespace namespace : getStartNamespaceList()){
            JSONObject ns=new JSONObject();
            ns.put(NAME_namespace_uri, namespace.getUri());
            ns.put(NAME_namespace_prefix, namespace.getPrefix());
            nsList.put(ns);
        }
        if(!nsList.isEmpty()){
            jsonObject.put(NAME_namespaces, nsList);
        }
        jsonObject.put(NAME_comment, getStartComment());
        ResXmlAttributeArray attributeArray = getAttributeArray();
        if(attributeArray != null){
            JSONArray attrArray = attributeArray.toJson();
            if(!attrArray.isEmpty()){
                jsonObject.put(NAME_attributes, attrArray);
            }
        }
        JSONArray childes = new JSONArray();
        for(ResXmlNode xmlNode : getXmlNodeList()){
            childes.put(xmlNode.toJson());
        }
        if(!childes.isEmpty()){
            jsonObject.put(NAME_childes, childes);
        }
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        ensureStartEndElement();
        int startLineNumber = json.optInt(NAME_line, 0);
        int endLineNo = json.optInt(NAME_line_end, 0);
        if(endLineNo == 0 && startLineNumber != 0){
            endLineNo = startLineNumber;
        }
        setStartLineNumber(startLineNumber);
        setEndLineNumber(endLineNo);
        for(ResXmlStartNamespace startNamespace : getStartNamespaceList()){
            startNamespace.setLineNumber(startLineNumber);
        }
        JSONArray nsArray = json.optJSONArray(NAME_namespaces);
        if(nsArray != null){
            int length = nsArray.length();
            for(int i=0; i<length; i++){
                JSONObject nsObject = nsArray.getJSONObject(i);
                String uri = nsObject.optString(NAME_namespace_uri, "");
                String prefix = nsObject.optString(NAME_namespace_prefix, "");
                newNamespace(uri, prefix);
            }
        }
        setName(json.getString(NAME_name));
        setTagNamespace(json.optString(NAME_namespace_uri, null),
                json.optString(NAME_namespace_prefix, null));
        setComment(json.optString(NAME_comment));
        addResXmlText(json.optString(NAME_text, null));
        ResXmlAttributeArray attributeArray = getAttributeArray();
        if(attributeArray != null){
            attributeArray.fromJson(json.optJSONArray(NAME_attributes));
        }
        JSONArray childArray = json.optJSONArray(NAME_childes);
        if(childArray != null){
            int length = childArray.length();
            for(int i = 0; i < length; i++){
                JSONObject childObject = childArray.getJSONObject(i);
                if(isTextNode(childObject)){
                    createResXmlTextNode().fromJson(childObject);
                }else {
                    createChildElement().fromJson(childObject);
                }
            }
        }
        getStartElement().calculatePositions();
    }
    private boolean isTextNode(JSONObject childObject){
        String type=childObject.optString(NAME_node_type, null);
        if(ResXmlTextNode.NAME_text.equals(type)){
            return true;
        }
        if(NAME_element.equals(type)){
            return false;
        }
        // support older ARSCLib versions
        return childObject.has(NAME_text);
    }

    public XMLElement decodeToXml() {
        return decodeToXml(null);
    }
    private XMLElement decodeToXml(XMLElement parent) {
        XMLElement xmlElement = new XMLElement(getName(false));
        if(parent != null){
            parent.add(xmlElement);
        }
        xmlElement.setLineNumber(getStartElement().getLineNumber());
        for(ResXmlStartNamespace startNamespace:getStartNamespaceList()){
            xmlElement.addNamespace(startNamespace.decodeToXml());
        }
        xmlElement.setNamespace(getTagNamespace());
        for(ResXmlAttribute resXmlAttribute:listAttributes()){
            XMLAttribute xmlAttribute =
                    resXmlAttribute.decodeToXml();
            xmlElement.addAttribute(xmlAttribute);
        }
        String comment=getStartComment();
        if(comment!=null){
            xmlElement.add(new XMLComment(comment));
        }
        comment=getEndComment();
        if(comment!=null){
            xmlElement.add(new XMLComment(comment));
        }
        for(ResXmlNode xmlNode: getXmlNodeList()){
            if(xmlNode instanceof ResXmlElement){
                ResXmlElement childResXmlElement=(ResXmlElement)xmlNode;
                        childResXmlElement.decodeToXml(xmlElement);
            }else if(xmlNode instanceof ResXmlTextNode){
                ResXmlTextNode childResXmlTextNode=(ResXmlTextNode)xmlNode;
                XMLText xmlText = childResXmlTextNode.decodeToXml();
                xmlElement.add(xmlText);
            }
        }
        return xmlElement;
    }
    @Override
    public int compare(ResXmlNode node1, ResXmlNode node2) {
        return Integer.compare(node1.getIndex(), node2.getIndex());
    }
    @Override
    public String toString(){
        ResXmlStartElement start = getStartElement();
        if(start!=null){
            StringBuilder builder=new StringBuilder();
            builder.append("(");
            builder.append(getStartLineNumber());
            builder.append(":");
            builder.append(getEndLineNumber());
            builder.append(") ");
            builder.append("<");
            builder.append(start.toString());
            if(hasText() && !hasElement()){
                builder.append(">");
                for(ResXmlTextNode textNode : listXmlTextNodes()){
                    builder.append(textNode.getText());
                }
                builder.append("</");
                builder.append(start.getTagName());
                builder.append(">");
            }else {
                builder.append("/>");
            }
            return builder.toString();
        }
        return "NULL";
    }

    private static boolean looksNamespace(String uri, String prefix){
        return uri.length() != 0 && "xmlns".equals(prefix);
    }
    private static boolean getFeatureSafe(XmlSerializer serializer, String name){
        try{
            return serializer.getFeature(name);
        }catch (Throwable ignored){
            return false;
        }
    }
    private static String splitPrefix(String name){
        int i = name.indexOf(':');
        if(i >= 0){
            return name.substring(0, i);
        }
        return null;
    }
    private static String splitName(String name){
        int i = name.indexOf(':');
        if(i >= 0){
            return name.substring(i + 1);
        }
        return name;
    }
    static void setIndent(XmlSerializer serializer, boolean state){
        setFeatureSafe(serializer, FEATURE_INDENT_OUTPUT, state);
    }
    private static void setFeatureSafe(XmlSerializer serializer, String name, boolean state){
        try{
            serializer.setFeature(name, state);
        }catch (Throwable ignored){
        }
    }
    private static final Function<ResXmlElement, Iterator<ResXmlElement>> RECURSIVE_ELEMENTS = ResXmlElement::recursiveElements;
    private static final Function<ResXmlElement, Iterator<ResXmlAttribute>> RECURSIVE_ATTRIBUTES = ResXmlElement::getAttributes;

    static final String NAME_element = "element";
    static final String NAME_name = "name";
    static final String NAME_comment = "comment";
    static final String NAME_text = "text";
    static final String NAME_namespaces = "namespaces";
    static final String NAME_namespace_uri = "namespace_uri";
    static final String NAME_namespace_prefix = "namespace_prefix";
    private static final String NAME_line = "line";
    private static final String NAME_line_end = "line_end";
    static final String NAME_attributes = "attributes";
    static final String NAME_childes = "childes";

    private static final String FEATURE_INDENT_OUTPUT = "http://xmlpull.org/v1/doc/features.html#indent-output";

    private static final int MAX_INDENT_DEPTH = 25;
}
