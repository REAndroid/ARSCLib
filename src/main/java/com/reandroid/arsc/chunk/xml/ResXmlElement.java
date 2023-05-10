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
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.SingleBlockContainer;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ResXmlString;
import com.reandroid.arsc.pool.ResXmlStringPool;
import com.reandroid.common.EntryStore;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONObject;
import com.reandroid.xml.*;


import java.io.IOException;
import java.util.*;

public class ResXmlElement extends ResXmlNode implements JSONConvert<JSONObject>,
        Comparator<ResXmlNode> {
    private final BlockList<ResXmlStartNamespace> mStartNamespaceList;
    private final SingleBlockContainer<ResXmlStartElement> mStartElementContainer;
    private final BlockList<ResXmlNode> mBody;
    private final SingleBlockContainer<ResXmlEndElement> mEndElementContainer;
    private final BlockList<ResXmlEndNamespace> mEndNamespaceList;
    private int mLevel;
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
    public int lastIndexOf(String tagName){
        List<ResXmlElement> elementList = listElements(tagName);
        int i = elementList.size();
        if(i==0){
            return -1;
        }
        i--;
        return elementList.get(i).getIndex();
    }
    public int indexOf(String tagName){
        ResXmlElement element = getElementByTagName(tagName);
        if(element!=null){
            return element.getIndex();
        }
        return -1;
    }
    public int indexOf(ResXmlElement element){
        int index = 0;
        for(ResXmlNode xmlNode:mBody.getChildes()){
            if(xmlNode==element){
                return index;
            }
            index++;
        }
        return -1;
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
    public void setComment(String comment){
        getStartElement().setComment(comment);
    }
    public void calculatePositions(){
        ResXmlStartElement start = getStartElement();
        if(start!=null){
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
        for(ResXmlNode xmlNode : getXmlNodes()){
            xmlNode.linkStringReferences();
        }
    }
    public ResXmlElement createChildElement(){
        return createChildElement(null);
    }
    public ResXmlElement createChildElement(String tag){
        int lineNo=getStartElement().getLineNumber()+1;
        ResXmlElement resXmlElement=new ResXmlElement();
        resXmlElement.newStartElement(lineNo);

        addElement(resXmlElement);

        if(tag!=null){
            resXmlElement.setTag(tag);
        }
        return resXmlElement;
    }
    public ResXmlAttribute getOrCreateAndroidAttribute(String name, int resourceId){
        return getOrCreateAttribute(NS_ANDROID_URI, NS_ANDROID_PREFIX, name, resourceId);
    }
    public ResXmlAttribute getOrCreateAttribute(String uri, String prefix, String name, int resourceId){
        ResXmlAttribute attribute=searchAttribute(name, resourceId);
        if(attribute==null){
            attribute = createAttribute(name, resourceId);
            if(uri!=null){
                ResXmlElement root = getRootResXmlElement();
                ResXmlStartNamespace ns = root.getOrCreateNamespace(uri, prefix);
                attribute.setNamespaceReference(ns.getUriReference());
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
        ResXmlStartNamespace ns = getOrCreateNamespace(NS_ANDROID_URI, NS_ANDROID_PREFIX);
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
        if(name==null){
            return null;
        }
        for(ResXmlElement child:listElements()){
            if(name.equals(child.getTag())||name.equals(child.getTagName())){
                return child;
            }
        }
        return null;
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
    public void setTag(String tag){
        ResXmlStringPool pool = getStringPool();
        if(pool==null){
            return;
        }
        ensureStartEndElement();
        ResXmlStartElement start=getStartElement();
        String prefix=null;
        String name=tag;
        int i=tag.lastIndexOf(':');
        if(i>=0){
            prefix=tag.substring(0,i);
            i++;
            name=tag.substring(i);
        }
        start.setName(name);
        ResXmlStartNamespace ns = getStartNamespaceByPrefix(prefix);
        if(ns!=null){
            start.setNamespaceReference(ns.getUriReference());
        }
    }
    public String getTagName(){
        ResXmlStartElement startElement=getStartElement();
        if(startElement!=null){
            return startElement.getTagName();
        }
        return null;
    }
    public String getTag(){
        ResXmlStartElement startElement=getStartElement();
        if(startElement!=null){
            return startElement.getName();
        }
        return null;
    }
    public String getTagUri(){
        ResXmlStartElement startElement=getStartElement();
        if(startElement!=null){
            return startElement.getUri();
        }
        return null;
    }
    public String getTagPrefix(){
        ResXmlStartElement startElement=getStartElement();
        if(startElement!=null){
            return startElement.getPrefix();
        }
        return null;
    }
    public int getAttributeCount() {
        ResXmlStartElement startElement=getStartElement();
        if(startElement!=null){
            return startElement.getResXmlAttributeArray().childesCount();
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
        for(ResXmlNode xmlNode:getXmlNodes()){
            xmlNode.addEvents(parserEventList);
        }
        comment = getEndComment();
        if(comment!=null){
            parserEventList.add(
                    new ParserEvent(ParserEvent.COMMENT, this, comment, true));
        }
        parserEventList.add(new ParserEvent(ParserEvent.END_TAG, this));
    }
    public int getLevel(){
        return mLevel;
    }
    private void setLevel(int level){
        mLevel = level;
    }
    public void addElement(ResXmlElement element){
        mBody.add(element);
    }
    public boolean removeAttribute(ResXmlAttribute resXmlAttribute){
        if(resXmlAttribute != null){
            resXmlAttribute.onRemoved();
        }
        return getStartElement().getResXmlAttributeArray().remove(resXmlAttribute);
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
        for(ResXmlNode xmlNode: getXmlNodes()){
            if(xmlNode instanceof ResXmlElement){
                result++;
            }
        }
        return result;
    }
    public void clearChildes(){
        List<ResXmlNode> copyOfNodeList=new ArrayList<>(mBody.getChildes());
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
        for(ResXmlNode xmlNode : getXmlNodes()){
            if(xmlNode instanceof ResXmlTextNode){
                return true;
            }
        }
        return false;
    }
    public boolean hasElement(){
        for(ResXmlNode xmlNode : getXmlNodes()){
            if(xmlNode instanceof ResXmlElement){
                return true;
            }
        }
        return false;
    }
    public List<ResXmlNode> listXmlNodes(){
        return new ArrayList<>(getXmlNodes());
    }
    private List<ResXmlNode> getXmlNodes(){
        return mBody.getChildes();
    }
    public List<ResXmlText> listXmlText(){
        List<ResXmlText> results=new ArrayList<>();
        for(ResXmlNode xmlNode: getXmlNodes()){
            if(xmlNode instanceof ResXmlTextNode){
                results.add(((ResXmlTextNode) xmlNode).getResXmlText());
            }
        }
        return results;
    }
    public List<ResXmlTextNode> listXmlTextNodes(){
        List<ResXmlTextNode> results=new ArrayList<>();
        for(ResXmlNode xmlNode: getXmlNodes()){
            if(xmlNode instanceof ResXmlTextNode){
                results.add((ResXmlTextNode) xmlNode);
            }
        }
        return results;
    }
    public List<ResXmlElement> listElements(){
        List<ResXmlElement> results=new ArrayList<>();
        for(ResXmlNode xmlNode: getXmlNodes()){
            if(xmlNode instanceof ResXmlElement){
                results.add((ResXmlElement) xmlNode);
            }
        }
        return results;
    }
    public List<ResXmlElement> listElements(String name){
        List<ResXmlElement> results=new ArrayList<>();
        if(name==null){
            return results;
        }
        for(ResXmlElement element:listElements()){
            if(name.equals(element.getTag())||name.equals(element.getTagName())){
                results.add(element);
            }
        }
        return results;
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
    public ResXmlStartNamespace getStartNamespaceByUriRef(int uriRef){
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
    public ResXmlStartNamespace getNamespace(String uri, String prefix){
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
            return xmlElement.getNamespace(uri, prefix);
        }
        return null;
    }
    public ResXmlStartNamespace getOrCreateNamespace(String uri, String prefix){
        ResXmlStartNamespace exist = getNamespace(uri, prefix);
        if(exist != null){
            return exist;
        }
        return getRootResXmlElement().createNamespace(uri, prefix);
    }
    public ResXmlStartNamespace createNamespace(String uri, String prefix){
        ResXmlStartNamespace startNamespace = new ResXmlStartNamespace();
        ResXmlEndNamespace endNamespace = new ResXmlEndNamespace();
        startNamespace.setEnd(endNamespace);

        addStartNamespace(startNamespace);
        addEndNamespace(endNamespace);
        ResXmlStringPool stringPool = getStringPool();
        ResXmlString xmlString = stringPool.createNew(uri);
        startNamespace.setUriReference(xmlString.getIndex());
        startNamespace.setPrefix(prefix);

        return startNamespace;
    }
    public ResXmlStartNamespace getStartNamespaceByUri(String uri){
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
    public ResXmlStartNamespace getStartNamespaceByPrefix(String prefix){
        if(prefix==null){
            return null;
        }
        for(ResXmlStartNamespace ns:mStartNamespaceList.getChildes()){
            if(prefix.equals(ns.getPrefix())){
                return ns;
            }
        }
        ResXmlElement xmlElement=getParentResXmlElement();
        if(xmlElement!=null){
            return xmlElement.getStartNamespaceByPrefix(prefix);
        }
        return null;
    }
    public List<ResXmlStartNamespace> getStartNamespaceList(){
        return mStartNamespaceList.getChildes();
    }
    public int getNamespaceCount(){
        return mStartNamespaceList.size();
    }
    public ResXmlStartNamespace getNamespace(int index){
        return mStartNamespaceList.get(index);
    }
    public void addStartNamespace(ResXmlStartNamespace item){
        mStartNamespaceList.add(item);
    }
    private List<ResXmlEndNamespace> getEndNamespaceList(){
        return mEndNamespaceList.getChildes();
    }
    public void addEndNamespace(ResXmlEndNamespace item){
        mEndNamespaceList.add(item);
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

    public ResXmlStartElement getStartElement(){
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

    public void addResXmlTextNode(ResXmlTextNode xmlTextNode){
        mBody.add(xmlTextNode);
    }
    public void addResXmlText(ResXmlText xmlText){
        if(xmlText!=null){
            addResXmlTextNode(new ResXmlTextNode(xmlText));
        }
    }
    public void addResXmlText(String text){
        if(text==null){
            return;
        }
        ResXmlTextNode xmlTextNode=new ResXmlTextNode();
        addResXmlTextNode(xmlTextNode);
        xmlTextNode.setText(text);
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
        onFinishedRead(reader, headerBlock);
        return false;
    }
    private void onFinishedRead(BlockReader reader, HeaderBlock headerBlock) throws IOException{
        int avail=reader.available();
        if(avail>0 && getLevel()==0){
            onFinishedUnexpected(reader);
            return;
        }
        onFinishedSuccess(reader, headerBlock);
    }
    private void onFinishedSuccess(BlockReader reader, HeaderBlock headerBlock) throws IOException{

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
            childElement.setLevel(getLevel()+1);
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
        addEndNamespace(endNamespace);
        endNamespace.readBytes(reader);
    }
    private void onXmlText(BlockReader reader) throws IOException{
        ResXmlText xmlText=new ResXmlText();
        addResXmlText(xmlText);
        xmlText.readBytes(reader);
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
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(NAME_node_type, NAME_element);
        ResXmlStartElement start = getStartElement();
        jsonObject.put(NAME_line, start.getLineNumber());
        int i=0;
        JSONArray nsList=new JSONArray();
        for(ResXmlStartNamespace namespace:getStartNamespaceList()){
            JSONObject ns=new JSONObject();
            ns.put(NAME_namespace_uri, namespace.getUri());
            ns.put(NAME_namespace_prefix, namespace.getPrefix());
            nsList.put(i, ns);
            i++;
        }
        if(i>0){
            jsonObject.put(NAME_namespaces, nsList);
        }
        jsonObject.put(NAME_name, start.getName());
        String comment=start.getComment();
        if(comment!=null){
            jsonObject.put(NAME_comment, comment);
        }
        String uri=start.getUri();
        if(uri!=null){
            jsonObject.put(NAME_namespace_uri, uri);
        }
        JSONArray attrArray=start.getResXmlAttributeArray().toJson();
        jsonObject.put(NAME_attributes, attrArray);
        i=0;
        JSONArray childes=new JSONArray();
        for(ResXmlNode xmlNode: getXmlNodes()){
            childes.put(i, xmlNode.toJson());
            i++;
        }
        if(i>0){
            jsonObject.put(NAME_childes, childes);
        }
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        ResXmlStartElement start = getStartElement();
        int lineNo=json.optInt(NAME_line, 1);
        if(start==null){
            start = newStartElement(lineNo);
        }else {
            start.setLineNumber(lineNo);
        }
        JSONArray nsArray = json.optJSONArray(NAME_namespaces);
        if(nsArray!=null){
            int length=nsArray.length();
            for(int i=0;i<length;i++){
                JSONObject nsObject=nsArray.getJSONObject(i);
                String uri=nsObject.optString(NAME_namespace_uri, "");
                String prefix=nsObject.optString(NAME_namespace_prefix, "");
                getOrCreateNamespace(uri,prefix);
            }
        }
        setTag(json.getString(NAME_name));
        start.setComment(json.optString(NAME_comment, null));
        String text= json.optString(NAME_text, null);
        if(text!=null){
            addResXmlText(text);
        }
        String uri = json.optString(NAME_namespace_uri, null);
        if(uri!=null){
            ResXmlStartNamespace ns = getStartNamespaceByUri(uri);
            if(ns==null){
                throw new IllegalArgumentException("Undefined uri: "
                        +uri+", must be defined in array: "+NAME_namespaces);
            }
            start.setNamespaceReference(ns.getUriReference());
        }
        JSONArray attributes = json.optJSONArray(NAME_attributes);
        if(attributes!=null){
            start.getResXmlAttributeArray().fromJson(attributes);
        }
        JSONArray childArray= json.optJSONArray(NAME_childes);
        if(childArray!=null){
            int length=childArray.length();
            for(int i=0;i<length;i++){
                JSONObject childObject=childArray.getJSONObject(i);
                if(isTextNode(childObject)){
                    ResXmlTextNode xmlTextNode=new ResXmlTextNode();
                    addResXmlTextNode(xmlTextNode);
                    xmlTextNode.fromJson(childObject);
                }else {
                    ResXmlElement childElement = createChildElement();
                    childElement.fromJson(childObject);
                }
            }
        }
        start.calculatePositions();
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

    /**
     * Decodes binary {@link ResXmlElement} to readable {@link XMLElement}
     * @param entryStore : used for decoding attribute name and values
     * @param currentPackageId : is id of current package defining this xml, used for
     *                         decoding reference names e.g @{package.name}:string/entry_name
     * */
    public XMLElement decodeToXml(EntryStore entryStore, int currentPackageId) throws XMLException {
        XMLElement xmlElement = new XMLElement(getTagName());
        xmlElement.setLineNumber(getStartElement().getLineNumber());
        for(ResXmlStartNamespace startNamespace:getStartNamespaceList()){
            xmlElement.addAttribute(startNamespace.decodeToXml());
        }
        for(ResXmlAttribute resXmlAttribute:listAttributes()){
            XMLAttribute xmlAttribute =
                    resXmlAttribute.decodeToXml(entryStore, currentPackageId);
            xmlElement.addAttribute(xmlAttribute);
        }
        String comment=getStartComment();
        if(comment!=null){
            xmlElement.addComment(new XMLComment(comment));
        }
        comment=getEndComment();
        if(comment!=null){
            xmlElement.addComment(new XMLComment(comment));
        }
        for(ResXmlNode xmlNode: getXmlNodes()){
            if(xmlNode instanceof ResXmlElement){
                ResXmlElement childResXmlElement=(ResXmlElement)xmlNode;
                XMLElement childXMLElement =
                        childResXmlElement.decodeToXml(entryStore, currentPackageId);
                xmlElement.addChild(childXMLElement);
            }else if(xmlNode instanceof ResXmlTextNode){
                ResXmlTextNode childResXmlTextNode=(ResXmlTextNode)xmlNode;
                XMLText xmlText = childResXmlTextNode.decodeToXml();
                xmlElement.addText(xmlText);
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
            builder.append("<");
            builder.append(start.toString());
            if(hasText() && !hasElement()){
                builder.append(">");
                for(ResXmlText xmlText : listXmlText()){
                    builder.append(xmlText.getText());
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
    static ResXmlElement newResXmlElement(String tag){
        ResXmlElement resXmlElement=new ResXmlElement();
        ResXmlStartElement startElement=new ResXmlStartElement();
        resXmlElement.setStartElement(startElement);
        ResXmlEndElement endElement=new ResXmlEndElement();
        resXmlElement.setEndElement(endElement);
        resXmlElement.setTag(tag);
        return resXmlElement;
    }

    public static final String NS_ANDROID_URI = "http://schemas.android.com/apk/res/android";
    public static final String NS_ANDROID_PREFIX = "android";

    static final String NAME_element = "element";
    static final String NAME_name = "name";
    static final String NAME_comment = "comment";
    static final String NAME_text = "text";
    static final String NAME_namespaces = "namespaces";
    static final String NAME_namespace_uri = "namespace_uri";
    static final String NAME_namespace_prefix = "namespace_prefix";
    private static final String NAME_line = "line";
    static final String NAME_attributes = "attributes";
    static final String NAME_childes = "childes";
}
