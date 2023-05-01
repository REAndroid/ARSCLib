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


import com.reandroid.xml.parser.XMLSpanParser;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

public class XMLElement extends XMLNode{
    static final long DEBUG_TO_STRING=500;
    private String mTagName;
    private final LinkedHashMap<String, XMLAttribute> mAttributes = new LinkedHashMap<>();
    private final List<XMLElement> mChildElements = new ArrayList<>();
    private final List<XMLComment> mComments = new ArrayList<>();
    private final List<XMLText> mTexts = new ArrayList<>();
    private XMLElement mParent;
    private int mIndent;
    private float mAttributeIndentScale = 1.0f;
    private Object mTag;
    private int mResId;
    private float mIndentScale;
    private String mStart;
    private String mStartPrefix;
    private String mEnd;
    private String mEndPrefix;
    private Set<NameSpaceItem> nameSpaceItems;
    public XMLElement(String tagName){
        this();
        setTagName(tagName);
    }
    public XMLElement(){
        setDefaultStartEnd();
    }

    public void addText(XMLText text){
        addTextInternal(text, true);
    }
    private void addTextInternal(XMLText text, boolean addSupper){
        if(text==null){
            return;
        }
        mTexts.add(text);
        if(addSupper){
            super.addChildNodeInternal(text);
        }
    }
    private void appendText(String text){
        if(text==null || text.length()==0){
            return;
        }
        addText(new XMLText(text));
    }
    public String getTagNamePrefix(){
        int i=mTagName.indexOf(":");
        if(i>0){
            return mTagName.substring(0,i);
        }
        return null;
    }
    public String getTagNameWoPrefix(){
        int i=mTagName.indexOf(":");
        if(i>0){
            return mTagName.substring(i+1);
        }
        return mTagName;
    }
    private void setDefaultStartEnd(){
        this.mStart="<";
        this.mEnd=">";
        this.mStartPrefix="/";
        this.mEndPrefix="/";
    }
    public void applyNameSpaceItems(){
        if(nameSpaceItems!=null){
            for(NameSpaceItem nsItem:nameSpaceItems){
                SchemaAttr schemaAttr=nsItem.toSchemaAttribute();
                XMLAttribute exist=getAttribute(schemaAttr.getName());
                if(exist!=null){
                    exist.setValue(schemaAttr.getValue());
                }else {
                    addAttribute(schemaAttr);
                }
            }
        }
        if(mParent!=null){
            mParent.applyNameSpaceItems();
        }
    }
    public void addNameSpace(NameSpaceItem nsItem){
        if(nsItem==null){
            return;
        }
        if(mParent!=null){
            mParent.addNameSpace(nsItem);
            return;
        }
        if(nameSpaceItems==null){
            nameSpaceItems=new HashSet<>();
        }
        nameSpaceItems.add(nsItem);
    }
    public NameSpaceItem getNameSpaceItemForUri(String uri){
        if(nameSpaceItems!=null){
            for(NameSpaceItem ns:nameSpaceItems){
                if(ns.isUriEqual(uri)){
                    return ns;
                }
            }
        }
        if(mParent!=null){
            return mParent.getNameSpaceItemForUri(uri);
        }
        return null;
    }
    public NameSpaceItem getNameSpaceItemForPrefix(String prefix){
        if(nameSpaceItems!=null){
            for(NameSpaceItem ns:nameSpaceItems){
                if(ns.isPrefixEqual(prefix)){
                    return ns;
                }
            }
        }
        if(mParent!=null){
            return mParent.getNameSpaceItemForPrefix(prefix);
        }
        return null;
    }
    void setStart(String start) {
        this.mStart = start;
    }
    void setEnd(String end) {
        this.mEnd = end;
    }
    void setStartPrefix(String pfx) {
        if(pfx==null){
            pfx="";
        }
        this.mStartPrefix = pfx;
    }
    void setEndPrefix(String pfx) {
        if(pfx==null){
            pfx="";
        }
        this.mEndPrefix = pfx;
    }
    public void setIndentScale(float scale){
        mIndentScale=scale;
    }
    private float getIndentScale(){
        XMLElement parent=getParent();
        if(parent==null){
            return mIndentScale;
        }
        return parent.getIndentScale();
    }
    public int getResourceId(){
        return mResId;
    }
    public void setResourceId(int id){
        mResId=id;
    }
    public XMLElement createElement(String tag) {
        XMLElement baseElement=new XMLElement(tag);
        addChildNoCheck(baseElement, true);
        return baseElement;
    }
    public void addChild(Collection<XMLElement> elements) {
        if(elements==null){
            return;
        }
        for(XMLElement element:elements){
            addChild(element);
        }
    }
    public void addChild(XMLElement child) {
        addChildNoCheck(child, true);
    }
    private void clearChildElements(){
        mChildElements.clear();
    }
    private void clearTexts(){
        mTexts.clear();
    }
    public XMLComment getCommentAt(int index){
        if(index<0){
            return null;
        }
        if(index>=mComments.size()){
            return null;
        }
        return mComments.get(index);
    }
    public void hideComments(boolean recursive, boolean hide){
        hideComments(hide);
        if(recursive){
            for(XMLElement child: mChildElements){
                child.hideComments(recursive, hide);
            }
        }
    }
    private void hideComments(boolean hide){
        for(XMLComment ce:mComments){
            ce.setHidden(hide);
        }
    }
    public int getCommentsCount(){
        return mComments.size();
    }
    public void addComments(Collection<XMLComment> commentElements){
        if(commentElements==null){
            return;
        }
        for(XMLComment ce:commentElements){
            addComment(ce);
        }
    }
    public void clearComments(){
        mComments.clear();
    }
    public void addComment(XMLComment commentElement) {
        addCommentInternal(commentElement, true);
    }
    void addCommentInternal(XMLComment commentElement, boolean addSuper) {
        if(commentElement==null){
            return;
        }
        mComments.add(commentElement);
        commentElement.setIndent(getIndent());
        commentElement.setParent(this);
        if(addSuper){
            super.addChildNodeInternal(commentElement);
        }
    }
    @Override
    void clearChildNodesInternal(){
        super.clearChildNodesInternal();
        mChildElements.clear();
        mComments.clear();
        mTexts.clear();
    }
    public Collection<XMLAttribute> listAttributes(){
        return mAttributes.values();
    }
    public int getChildesCount(){
        return mChildElements.size();
    }
    public List<XMLElement> listChildElements(){
        return mChildElements;
    }
    public XMLElement getChildAt(int index){
        if(index<0 || index>= mChildElements.size()){
            return null;
        }
        return mChildElements.get(index);
    }
    public int getAttributeCount(){
        return mAttributes.size();
    }
    public String getAttributeValue(String name){
        XMLAttribute attr=getAttribute(name);
        if (attr==null){
            return null;
        }
        return attr.getValue();
    }
    public int getAttributeValueInt(String name, int def){
        XMLAttribute attr=getAttribute(name);
        if (attr==null){
            return def;
        }
        return attr.getValueInt();
    }
    public int getAttributeValueInt(String name) throws XMLException {
        XMLAttribute attr=getAttribute(name);
        if (attr==null){
            throw new XMLException("Expecting integer for attr <"+name+ "> at '"+toString()+"'");
        }
        try{
            return attr.getValueInt();
        }catch (NumberFormatException ex){
            throw new XMLException(ex.getMessage()+": "+" '"+toString()+"'");
        }
    }
    public XMLAttribute getAttribute(String name){
        return mAttributes.get(name);
    }
    public XMLAttribute removeAttribute(String name){
        XMLAttribute attribute = mAttributes.remove(name);
        if(attribute!=null){
            attribute.setParent(null);
        }
        return attribute;
    }
    public XMLAttribute setAttribute(String name, int value){
        return setAttribute(name, String.valueOf(value));
    }
    public XMLAttribute setAttribute(String name, boolean value){
        String v=value?"true":"false";
        return setAttribute(name, v);
    }
    public XMLAttribute setAttribute(String name, String value){
        if(XMLUtil.isEmpty(name)){
            return null;
        }
        XMLAttribute attr=getAttribute(name);
        if(attr==null){
            if(SchemaAttr.looksSchema(name, value)){
                attr=new SchemaAttr(name, value);
            }else{
                attr=new XMLAttribute(name,value);
            }
            addAttribute(attr);
        }else {
            attr.setValue(value);
        }
        return attr;
    }
    public void addAttributes(Collection<XMLAttribute> attrs){
        if(attrs==null){
            return;
        }
        for(XMLAttribute a:attrs){
            addAttribute(a);
        }
    }
    public void addAttribute(XMLAttribute attr){
        if(attr==null){
            return;
        }
        String name = attr.getName();
        if(XMLUtil.isEmpty(name)){
            return;
        }
        XMLAttribute exist = mAttributes.get(name);
        if(exist!=null){
            return;
        }
        mAttributes.put(name, attr);
        attr.setParent(this);
    }
    public void sortChildes(Comparator<XMLElement> comparator){
        if(comparator==null){
            return;
        }
        mChildElements.sort(comparator);
    }
    public XMLElement getParent(){
        return mParent;
    }
    void setParent(XMLElement baseElement){
        mParent=baseElement;
    }
    @Override
    void onChildAdded(XMLNode xmlNode){
        if(xmlNode instanceof XMLComment){
            addCommentInternal((XMLComment) xmlNode, false);
        }else if(xmlNode instanceof XMLElement){
            addChildNoCheck((XMLElement) xmlNode, false);
        }else if(xmlNode instanceof XMLText){
            addTextInternal((XMLText) xmlNode, false);
        }
    }
    private void addChildNoCheck(XMLElement child, boolean addSupper){
        if(child==null || child == this){
            return;
        }
        child.setParent(this);
        child.setIndent(getChildIndent());
        mChildElements.add(child);
        if(addSupper){
            super.addChildNodeInternal(child);
        }
    }
    public int getLevel(){
        int rs=0;
        XMLElement parent=getParent();
        if(parent!=null){
            rs=rs+1;
            rs+=parent.getLevel();
        }
        return rs;
    }
    int getIndent(){
        XMLElement parent = getParent();
        if(parent!=null && parent.hasTextContent()){
            return 0;
        }
        return mIndent;
    }
    int getChildIndent(){
        if(mIndent<=0 || hasTextContent()){
            return 0;
        }
        int rs=mIndent+1;
        String tag= getTagName();
        if(tag!=null){
            int i=tag.length();
            if(i>10){
                i=10;
            }
            rs+=i;
        }
        return rs;
    }
    public void setIndent(int indent){
        mIndent=indent;
        int chIndent=getChildIndent();
        for(XMLElement child: mChildElements){
            child.setIndent(chIndent);
        }
        if(mComments!=null){
            for(XMLComment ce:mComments){
                ce.setIndent(indent);
            }
        }
    }

    /**
     * @param indentScale scale of attributes indent relative to element tag start
     */
    public void setAttributesIndentScale(float indentScale){
        setAttributesIndentScale(indentScale, true);
    }
    public void setAttributesIndentScale(float indentScale, boolean setToChildes){
        this.mAttributeIndentScale = indentScale;
        if(!setToChildes){
            return;
        }
        for(XMLElement child:listChildElements()){
            child.setAttributesIndentScale(indentScale, true);
        }
    }
    private float getAttributeIndentScale(){
        return mAttributeIndentScale;
    }
    private int calculateAttributesIndent(){
        float scale = getAttributeIndentScale();
        int indent = 0;
        String tagName = getTagName();
        if(tagName!=null){
            indent += tagName.length();
        }
        indent += 2;
        if(indent>MAX_ATTRIBUTE_INDENT){
            indent = MAX_ATTRIBUTE_INDENT;
        }
        int baseIndent = getIndentWidth();
        indent = (int) (scale * indent);
        indent = baseIndent + indent;
        if(indent<0){
            indent = 0;
        }
        return indent;
    }
    private boolean appendAttributesIndentText(Writer writer, boolean appendOnce, int indent) throws IOException {
        if(indent<=0){
            return false;
        }
        if(appendOnce){
            writer.write(XMLUtil.NEW_LINE);
            for(int i=0;i<indent;i++){
                writer.write(' ');
            }
        }
        return true;
    }
    boolean appendIndentText(Writer writer) throws IOException {
        int max=getIndentWidth();
        int i=0;
        while (i<max){
            writer.write(' ');
            i++;
        }
        return true;
    }
    private int getIndentWidth(){
        float scale=getIndentScale();
        scale = scale * (float) getIndent();
        int i=(int)scale;
        if(i<=0){
            i=0;
        }
        if(i>40){
            i=40;
        }
        return i;
    }
    public String getTagName(){
        return mTagName;
    }
    public void setTagName(String tag){
        mTagName =tag;
    }
    public Object getTag(){
        return mTag;
    }
    public void setTag(Object tag){
        mTag =tag;
    }
    public String getTextContent(){
        if(!hasTextContent()){
            return null;
        }
        return buildTextContent(true);
    }
    public String buildTextContent(boolean unEscape){
        StringWriter writer=new StringWriter();
        try {
            for(XMLNode node:getChildNodes()){
                node.buildTextContent(writer, unEscape);
            }
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
        }
        return writer.toString();
    }
    void buildTextContent(Writer writer, boolean unEscape) throws IOException {
        writer.write("<");
        writer.write(getTagName());
        appendAttributes(writer, false);
        if(!hasChildNodes()){
            writer.write("/>");
            return;
        }
        writer.write('>');
        for(XMLNode node:getChildNodes()){
            node.buildTextContent(writer, unEscape);
        }
        if(hasChildNodes()){
            writer.write("</");
            writer.write(getTagName());
            writer.write('>');
        }
    }
    private void appendTextContent(Writer writer) throws IOException {
        for(XMLNode child:getChildNodes()){
            if(child instanceof XMLElement){
                ((XMLElement)child).setIndent(0);
            }
            child.write(writer, false);
        }
    }
    public boolean hasChildElements(){
        return mChildElements.size()>0;
    }
    public boolean hasTextContent() {
        return mTexts.size()>0;
    }
    public String getText(){
        if(mTexts.size()==0){
            return null;
        }
        return mTexts.get(0).getText();
    }
    public void setSpannableText(String text){
        clearChildNodes();
        XMLElement element = parseSpanSafe(text);
        if(element==null){
            addText( new XMLText(text));
            return;
        }
        for(XMLNode xmlNode:element.getChildNodes()){
            super.addChildNode(xmlNode);
        }
    }
    public void setTextContent(String text){
        setTextContent(text, true);
    }
    public void setTextContent(String text, boolean escape){
        clearChildElements();
        clearTexts();
        super.getChildNodes().clear();
        if(escape){
            text=XMLUtil.escapeXmlChars(text);
        }
        appendText(text);
    }
    private boolean appendAttributes(Writer writer, boolean newLineAttributes) throws IOException {
        boolean addedOnce=false;
        int attributesIndent = calculateAttributesIndent();
        boolean indentAppend = false;
        for(XMLAttribute attr:listAttributes()){
            if(newLineAttributes){
                indentAppend = appendAttributesIndentText(writer, indentAppend, attributesIndent);
            }
            if(addedOnce){
                if(!indentAppend){
                    writer.write(' ');
                }
            }else {
                writer.write(' ');
            }
            attr.write(writer);
            addedOnce=true;
        }
        return addedOnce;
    }
    boolean isEmpty(){
        if(mTagName!=null){
            return false;
        }
        if(mAttributes.size()>0){
            return false;
        }
        if(mComments!=null && mComments.size()>0){
            return false;
        }
        if(mTexts.size()>0){
            return false;
        }
        return true;
    }
    private boolean canAppendChildes(){
        for(XMLElement child: mChildElements){
            if (!child.isEmpty()){
                return true;
            }
        }
        return false;
    }
    boolean appendComments(Writer writer) throws IOException {
        if(mComments==null){
            return false;
        }
        boolean appendPrevious=false;
        boolean addedOnce=false;
        for(XMLComment ce:mComments){
            if(ce.isEmpty()){
                continue;
            }
            if(appendPrevious){
                writer.write(XMLUtil.NEW_LINE);
            }
            appendPrevious=ce.write(writer, false);
            if(appendPrevious){
                addedOnce=true;
            }
        }
        return addedOnce;
    }
    private boolean appendChildes(Writer writer, boolean newLineAttributes) throws IOException {
        boolean appendPrevious=true;
        boolean addedOnce=false;
        for(XMLElement child: mChildElements){
            if(stopWriting(writer)){
                break;
            }
            if(child.isEmpty()){
                continue;
            }
            if(appendPrevious){
                writer.write(XMLUtil.NEW_LINE);
            }
            appendPrevious=child.write(writer, newLineAttributes);
            if(!addedOnce && appendPrevious){
                addedOnce=true;
            }
        }
        return addedOnce;
    }
    private boolean stopWriting(Writer writer){
        if(!(writer instanceof ElementWriter)){
            return false;
        }
        ElementWriter elementWriter=(ElementWriter)writer;
        if(elementWriter.isFinished()){
            elementWriter.writeInterrupted();
            return true;
        }
        return false;
    }
    @Override
    public boolean write(Writer writer, boolean newLineAttributes) throws IOException {
        if(isEmpty()){
            return false;
        }
        if(stopWriting(writer)){
            return false;
        }
        boolean appendOnce=appendComments(writer);
        if(appendOnce){
            writer.write(XMLUtil.NEW_LINE);
        }
        appendIndentText(writer);
        writer.write(mStart);
        String tagName=getTagName();
        if(tagName!=null){
            writer.write(tagName);
        }
        appendAttributes(writer, newLineAttributes);
        boolean useEndTag=false;
        boolean hasTextCon=hasTextContent();
        if(hasTextCon){
            writer.write(mEnd);
            appendTextContent(writer);
            useEndTag=true;
        }else if(canAppendChildes()){
            writer.write(mEnd);
            appendChildes(writer, newLineAttributes);
            useEndTag=true;
        }
        if(useEndTag){
            if(!hasTextCon){
                writer.write(XMLUtil.NEW_LINE);
                appendIndentText(writer);
            }
            writer.write(mStart);
            writer.write(mStartPrefix);
            writer.write(getTagName());
        }else {
            writer.write(mEndPrefix);
        }
        writer.write(mEnd);
        return true;
    }
    @Override
    public String toText(int indent, boolean newLineAttributes){
        StringWriter writer=new StringWriter();
        setIndent(indent);
        try {
            write(writer, newLineAttributes);
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
        }
        return writer.toString();
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
        builder.append(getTagName());
        for(XMLAttribute attribute:listAttributes()){
            builder.append(' ');
            builder.append(attribute.toText(0, false));
        }
        return builder.toString();
    }
    @Override
    public String toString(){
        StringWriter strWriter=new StringWriter();
        ElementWriter writer=new ElementWriter(strWriter, DEBUG_TO_STRING);
        try {
            write(writer, false);
        } catch (IOException ignored) {
        }
        strWriter.flush();
        return strWriter.toString();
    }

    private static XMLElement parseSpanSafe(String spanText){
        if(spanText==null){
            return null;
        }
        try {
            XMLSpanParser spanParser = new XMLSpanParser();
            return spanParser.parse(spanText);
        } catch (XMLException ignored) {
            return null;
        }
    }

    private static final int MAX_ATTRIBUTE_INDENT = 20;
}
