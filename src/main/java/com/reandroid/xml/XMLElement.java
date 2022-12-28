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


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

public class XMLElement {
    static final long DEBUG_TO_STRING=500;
    private String mTagName;
    private XMLTextAttribute mTextAttribute;
    private List<XMLAttribute> mAttributes;
    private List<XMLElement> mChildes;
    private List<XMLComment> mComments;
    private XMLElement mParent;
    private int mIndent;
    private Object mTag;
    private int mResId;
    private float mIndentScale;
    private String mUniqueAttrName;
    private List<String> mUniqueNameValues;
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
    public XMLElement removeChild(XMLElement element){
        if(mChildes==null || element==null){
            return null;
        }
        int i=mChildes.indexOf(element);
        if(i<0){
            return null;
        }
        XMLElement result=mChildes.get(i);
        mChildes.remove(i);
        result.setParent(null);
        return result;
    }
    public XMLElement getFirstChildWithAttrName(String name){
        if(mChildes==null || name==null){
            return null;
        }
        for(XMLElement element:mChildes){
            XMLAttribute attr=element.getAttribute(name);
            if(attr!=null){
                return element;
            }
        }
        return null;
    }
    public XMLElement getFirstChildWithAttr(String attrName, String attrValue){
        if(mChildes==null || attrName==null || attrValue==null){
            return null;
        }
        for(XMLElement element:mChildes){
            XMLAttribute attr=element.getAttribute(attrName);
            if(attr!=null){
                if(attrValue.equals(attr.getValue())){
                    return element;
                }
            }
        }
        return null;
    }
    public void applyNameSpaceItems(){
        if(nameSpaceItems!=null){
            for(NameSpaceItem nsItem:nameSpaceItems){
                SchemaAttr schemaAttr=nsItem.toSchemaAttribute();
                XMLAttribute exist=getAttribute(schemaAttr.getName());
                if(exist!=null){
                    exist.setValue(schemaAttr.getValue());
                }else {
                    addAttributeNoCheck(schemaAttr);
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
    public void setStart(String start) {
        this.mStart = start;
    }
    public void setEnd(String end) {
        this.mEnd = end;
    }
    public void setStartPrefix(String pfx) {
        if(pfx==null){
            pfx="";
        }
        this.mStartPrefix = pfx;
    }
    public void setEndPrefix(String pfx) {
        if(pfx==null){
            pfx="";
        }
        this.mEndPrefix = pfx;
    }

    public void setUniqueAttrName(String attrName){
        clearUniqueNameValues();
        if(XMLUtil.isEmpty(attrName)){
            mUniqueAttrName=null;
            return;
        }
        mUniqueAttrName=attrName;
    }
    public String getUniqueAttrName(){
        return mUniqueAttrName;
    }
    private void clearUniqueNameValues(){
        if(mUniqueNameValues!=null){
            mUniqueNameValues.clear();
            mUniqueNameValues=null;
        }
    }
    private void addUniqueNameValues(XMLElement element){
        if(element==null){
            return;
        }
        XMLAttribute baseAttr=element.getAttribute(getUniqueAttrName());
        if(baseAttr==null){
            return;
        }
        addUniqueNameValues(baseAttr.getValue());
    }
    private void addUniqueNameValues(String nameVal){
        if(XMLUtil.isEmpty(nameVal)){
            return;
        }
        if(mUniqueNameValues==null){
            mUniqueNameValues=new ArrayList<>();
        }
        mUniqueNameValues.add(nameVal);
    }
    private boolean shouldCheckUniqueElement(){
        return mUniqueAttrName!=null;
    }
    private boolean containsUniqueNameValue(XMLElement element){
        if(element==null){
            return false;
        }
        return containsUniqueNameValue(element.getAttribute(getUniqueAttrName()));
    }
    private boolean containsUniqueNameValue(XMLAttribute baseAttr){
        if(baseAttr==null){
            return false;
        }
        return containsUniqueNameValue(baseAttr.getValue());
    }
    private boolean containsUniqueNameValue(String nameVal){
        if(mUniqueNameValues==null|| XMLUtil.isEmpty(nameVal)){
            return false;
        }
        return mUniqueNameValues.contains(nameVal);
    }
    void setIndentScale(float scale){
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
    public boolean containsSameChild(XMLElement baseElement){
        if(baseElement==null||mChildes==null){
            return false;
        }
        for(XMLElement ch:mChildes){
            if(baseElement.isSame(ch)){
                return true;
            }
        }
        return false;
    }
    private String getUniqueId(){
        StringBuilder builder=new StringBuilder(getTagName());
        builder.append(attributesToString(false));
        return builder.toString();
    }
    private boolean isSame(XMLElement baseElement){
        if(baseElement==null){
            return false;
        }
        String s1=getUniqueId();
        String s2=baseElement.getUniqueId();
        return XMLUtil.isStringEqual(s1,s2);
    }
    public XMLElement cloneElement(){
        XMLElement baseElement=onCloneElement();
        baseElement.setTag(getTag());
        cloneAllAttributes(baseElement);
        if(mChildes!=null){
            for(XMLElement element:mChildes){
                baseElement.addChildNoCheck(element.cloneElement());
            }
        }
        if(mComments!=null){
            for(XMLComment ce:mComments){
                baseElement.addComment((XMLComment) ce.cloneElement());
            }
        }
        return baseElement;
    }
    void cloneAllAttributes(XMLElement element){
        if(mAttributes!=null){
            for(XMLAttribute attr:mAttributes){
                element.addAttributeNoCheck(attr.cloneAttr());
            }
        }
        XMLTextAttribute textAttribute=mTextAttribute;
        if(textAttribute!=null){
            element.mTextAttribute=mTextAttribute.cloneAttr();
        }
    }
    XMLElement onCloneElement(){
        return new XMLElement(getTagName());
    }
    public XMLElement createElement(String tag) {
        XMLElement baseElement=new XMLElement(tag);
        addChildNoCheck(baseElement);
        return baseElement;
    }
    public boolean containsChild(XMLElement baseElement){
        if(baseElement==null||mChildes==null){
            return false;
        }
        return mChildes.contains(baseElement);
    }

    public void addChild(XMLElement[] elements) {
        if(elements==null){
            return;
        }
        int max=elements.length;
        for(int i=0;i<max;i++){
            XMLElement baseElement=elements[i];
            addChild(baseElement);
        }
    }
    public void addChild(Collection<XMLElement> elements) {
        if(elements==null){
            return;
        }
        for(XMLElement element:elements){
            addChild(element);
        }
    }
    public void addChild(XMLElement baseElement) {
        addChildNoCheck(baseElement);
    }
    public XMLComment getCommentAt(int index){
        if(mComments==null || index<0){
            return null;
        }
        if(index>=mComments.size()){
            return null;
        }
        return mComments.get(index);
    }
    public void hideComments(boolean recursive, boolean hide){
        hideComments(hide);
        if(recursive && mChildes!=null){
            for(XMLElement child:mChildes){
                child.hideComments(recursive, hide);
            }
        }
    }
    private void hideComments(boolean hide){
        if(mComments==null){
            return;
        }
        for(XMLComment ce:mComments){
            ce.setHidden(hide);
        }
    }
    public int getCommentsCount(){
        if(mComments==null){
            return 0;
        }
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
        if(mComments==null){
            return;
        }
        mComments.clear();
        mComments=null;
    }
    public void addComment(XMLComment commentElement) {
        if(commentElement==null){
            return;
        }
        if(mComments==null){
            mComments=new ArrayList<>();
        }
        mComments.add(commentElement);
        commentElement.setIndent(getIndent());
        commentElement.setParent(this);
    }
    public void removeAllChildes(){
        if(mChildes==null){
            return;
        }
        mChildes.clear();
    }
    public int getChildesCount(){
        if(mChildes==null){
            return 0;
        }
        return mChildes.size();
    }
    public XMLElement getFirstElementByTagName(String tagName){
        if(tagName==null||mChildes==null){
            return null;
        }
        for(XMLElement element:mChildes){
            if(tagName.equals(element.getTagName())){
                return element;
            }
        }
        return null;
    }
    public XMLElement[] getElementsByTagName(String tagName){
        if(tagName==null||mChildes==null){
            return null;
        }
        List<XMLElement> results=new ArrayList<>();
        for(XMLElement element:mChildes){
            if(tagName.equals(element.getTagName())){
                results.add(element);
            }
        }
        int max=results.size();
        if(max==0){
            return null;
        }
        return results.toArray(new XMLElement[max]);
    }
    public XMLElement[] getAllChildes(){
        if(mChildes==null){
            return null;
        }
        int max=mChildes.size();
        if(max==0){
            return null;
        }
        return mChildes.toArray(new XMLElement[max]);
    }
    public XMLElement getChildAt(int index){
        if(mChildes==null||index<0){
            return null;
        }
        if(index>=mChildes.size()){
            return null;
        }
        return mChildes.get(index);
    }
    public int getAttributeCount(){
        if(mAttributes==null){
            return 0;
        }
        return mAttributes.size();
    }
    public XMLAttribute getAttributeAt(int index){
        if(mAttributes==null||index<0){
            return null;
        }
        if(index>=mAttributes.size()){
            return null;
        }
        return mAttributes.get(index);
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
    public boolean getAttributeValueBool(String name, boolean def){
        XMLAttribute attr=getAttribute(name);
        if (attr==null){
            return def;
        }
        if(!attr.isValueBool()){
            return def;
        }
        return attr.getValueBool();
    }
    public boolean getAttributeValueBool(String name) throws XMLException {
        XMLAttribute attr=getAttribute(name);
        if (attr==null || !attr.isValueBool()){
            throw new XMLException("Expecting boolean for attr <"+name+ "> at '"+toString()+"'");
        }
        return attr.getValueBool();
    }
    public XMLAttribute getAttribute(String name){
        if(mAttributes==null){
            return null;
        }
        if(XMLUtil.isEmpty(name)){
            return null;
        }
        for(XMLAttribute attr:mAttributes){
            if(name.equals(attr.getName())){
                return attr;
            }
        }
        return null;
    }
    public XMLAttribute removeAttribute(String name){
        if(XMLUtil.isEmpty(name)){
            return null;
        }
        XMLAttribute attr=getAttribute(name);
        if(attr==null){
            return null;
        }
        int i=mAttributes.indexOf(attr);
        if(i<0){
            return null;
        }
        mAttributes.remove(i);
        return attr;
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
            addAttributeNoCheck(attr);
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
        if(XMLUtil.isEmpty(attr.getName())){
            return;
        }
        XMLAttribute exist=getAttribute(attr.getName());
        if(exist!=null){
            return;
        }
        if(mAttributes==null){
            mAttributes=new ArrayList<>();
        }
        mAttributes.add(attr);
    }
    private void addAttributeNoCheck(XMLAttribute attr){
        if(attr==null){
            return;
        }
        if(XMLUtil.isEmpty(attr.getName())){
            return;
        }
        if(mAttributes==null){
            mAttributes=new ArrayList<>();
        }
        mAttributes.add(attr);
    }
    public void sortChildes(Comparator<XMLElement> comparator){
        if(mChildes==null||comparator==null){
            return;
        }
        mChildes.sort(comparator);
    }
    public void sortAttributesWithChildes(Comparator<XMLAttribute> comparator){
        if(comparator==null){
            return;
        }
        sortAttributes(comparator);
        if(mChildes!=null){
            for(XMLElement element:mChildes){
                element.sortAttributesWithChildes(comparator);
            }
        }
    }
    public void sortAttributes(Comparator<XMLAttribute> comparator){
        if(mAttributes==null || comparator==null){
            return;
        }
        mAttributes.sort(comparator);
    }
    public XMLElement getParent(){
        return mParent;
    }
    void setParent(XMLElement baseElement){
        mParent=baseElement;
    }
    private void addChildNoCheck(XMLElement baseElement){
        if(baseElement==null){
            return;
        }
        if(mChildes==null){
            mChildes=new ArrayList<>();
        }
        baseElement.setParent(this);
        baseElement.setIndent(getChildIndent());
        mChildes.add(baseElement);
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
        return mIndent;
    }
    int getChildIndent(){
        if(mIndent<=0){
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
        if(mChildes!=null){
            int chIndent=getChildIndent();
            for(XMLElement be:mChildes){
                be.setIndent(chIndent);
            }
        }
        if(mComments!=null){
            for(XMLComment ce:mComments){
                ce.setIndent(indent);
            }
        }
    }
    private String getAttributesIndentText(){
        int i=getLevel()+1;
        String tagName=getTagName();
        if(tagName!=null){
            i+=tagName.length();
        }
        if(i>12){
            i=12;
        }
        tagName="";
        while (tagName.length()<i){
            tagName+=" ";
        }
        String rs=getIndentText();
        return rs+tagName;
    }
    private boolean appendAttributesIndentText(Writer writer) throws IOException {
        int i=0;
        String tagName=getTagName();
        if(tagName!=null){
            i+=tagName.length();
        }
        i+=2;
        if(i>15){
            i=15;
        }
        i+=getIndentWidth();
        int j=0;
        while (j<i){
            writer.write(' ');
            j++;
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
    private String getIndentText(){
        float scale=getIndentScale();
        scale = scale * (float) getIndent();
        int i=(int)scale;
        if(i<=0){
            return "";
        }
        if(i>40){
            i=40;
        }
        StringBuilder builder=new StringBuilder();
        int max=i;
        i=0;
        while (i<max){
            builder.append(" ");
            i++;
        }
        return builder.toString();
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
        return getTextContent(false);
    }
    public String getTextContent(boolean unEscape){
        XMLTextAttribute textAttribute= getTextAttr();
        return textAttribute.getText(unEscape);
    }
    public XMLTextAttribute getTextAttribute(){
        String txt=getTextContent();
        if(txt==null){
            return null;
        }
        return mTextAttribute;
    }
    XMLTextAttribute getTextAttr(){
        if(mTextAttribute==null){
            mTextAttribute=new XMLTextAttribute();
        }
        return mTextAttribute;
    }
    private boolean appendTextContent(Writer writer) throws IOException {
        if(mTextAttribute==null){
            return false;
        }
        return mTextAttribute.write(writer);
    }
    private boolean hasTextContent() {
        if(mTextAttribute==null){
            return false;
        }
        return !mTextAttribute.isEmpty();
    }
    private void appendText(String text){
        String old= getTextContent();
        if (old!=null){
            if(text!=null){
                setTextContent(old+text);
                return;
            }
        }
        setTextContent(text);
    }
    public XMLAttribute setTextContent(String text){
        return setTextContent(text, true);
    }
    public XMLAttribute setTextContent(String text, boolean escape){
        XMLTextAttribute textAttribute= getTextAttr();
        if(XMLUtil.isEmpty(text)){
            textAttribute.setText(null);
            //textAttribute.setText(text);
        }else {
            if(escape){
                text= XMLUtil.escapeXmlChars(text);
            }
            textAttribute.setText(text);
        }
        return textAttribute;
    }
    private boolean appendAttributes(Writer writer, boolean newLineAttributes) throws IOException {
        if(mAttributes==null){
            return false;
        }
        boolean addedOnce=false;
        for(XMLAttribute attr:mAttributes){
            if(attr.isEmpty()){
                continue;
            }
            if(addedOnce){
                if(newLineAttributes){
                    writer.write(XMLUtil.NEW_LINE);
                    appendAttributesIndentText(writer);
                }else{
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
    private String attributesToString(boolean newLineAttributes){
        if(mAttributes==null){
            return null;
        }
        StringBuilder builder=new StringBuilder();
        boolean addedOnce=false;
        for(XMLAttribute attr:mAttributes){
            if(attr.isEmpty()){
                continue;
            }
            if(addedOnce){
                if(newLineAttributes){
                    builder.append("\n");
                    builder.append(getAttributesIndentText());
                }else{
                    builder.append(" ");
                }
            }
            builder.append(attr.toString());
            addedOnce=true;
        }
        if(addedOnce){
            return builder.toString();
        }
        return null;
    }
    boolean isEmpty(){
        if(mTagName!=null){
            return false;
        }
        if(mAttributes!=null && mAttributes.size()>0){
            return false;
        }
        if(mComments!=null && mComments.size()>0){
            return false;
        }
        return getTextContent()==null;
    }
    private boolean canAppendChildes(){
        if(mChildes==null){
            return false;
        }
        for(XMLElement be:mChildes){
            if (!be.isEmpty()){
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
        if(mChildes==null){
            return false;
        }
        boolean appendPrevious=true;
        boolean addedOnce=false;
        for(XMLElement be:mChildes){
            if(stopWriting(writer)){
                break;
            }
            if(be.isEmpty()){
                continue;
            }
            if(appendPrevious){
                writer.write(XMLUtil.NEW_LINE);
            }
            appendPrevious=be.write(writer, newLineAttributes);
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
        if(canAppendChildes()){
            writer.write(mEnd);
            appendChildes(writer, newLineAttributes);
            useEndTag=true;
        }
        boolean hasTextCon=hasTextContent();
        if(hasTextCon){
            if(!useEndTag){
                writer.write(mEnd);
            }else {
                writer.write(XMLUtil.NEW_LINE);
            }
            appendTextContent(writer);
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
            writer.write(mEnd);
        }else {
            writer.write(mEndPrefix);
            writer.write(mEnd);
        }
        return true;
    }
    public String toText(){
        return toText(1, false);
    }
    public String toText(boolean newLineAttributes){
        return toText(1, newLineAttributes);
    }
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
    @Override
    public String toString(){
        StringWriter strWriter=new StringWriter();
        ElementWriter writer=new ElementWriter(strWriter, DEBUG_TO_STRING);
        try {
            write(writer, false);
        } catch (IOException e) {
        }
        strWriter.flush();
        return strWriter.toString();
    }

    public static List<XMLElement> getAllElementsRecursive(XMLElement parent){
        List<XMLElement> results=new ArrayList<>();
        if(parent==null){
            return results;
        }
        XMLElement[] allChildes=parent.getAllChildes();
        if(allChildes==null){
            return results;
        }
        int max=allChildes.length;
        for(int i=0;i<max;i++){
            XMLElement child=allChildes[i];
            results.add(child);
            results.addAll(getAllElementsRecursive(child));
        }
        return results;
    }


}
