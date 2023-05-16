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
package com.reandroid.xml.parser;

import com.android.org.kxml2.io.KXmlParser;
import com.reandroid.common.FileChannelInputStream;
import com.reandroid.xml.*;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class XMLDocumentParser {
    private final XmlPullParser mParser;
    private XMLDocument mResDocument;
    private XMLElement mCurrentElement;
    private boolean mNameSpaceCreated;
    private StringBuilder mCurrentText;
    private List<XMLComment> mComments;
    private int mIndent;
    public XMLDocumentParser(XmlPullParser parser){
        this.mParser=parser;
    }
    public XMLDocumentParser(InputStream in) throws XMLParseException {
        this(createParser(in));
    }
    public XMLDocumentParser(File file) throws XMLParseException {
        this(createParser(file));
    }
    public XMLDocumentParser(String text) throws XMLParseException {
        this(createParser(text));
    }

    public XMLDocument parse() throws XMLParseException {
        try {
            XMLDocument document= parseDocument();
            close();
            return document;
        } catch (XmlPullParserException | IOException e) {
            XMLParseException ex=new XMLParseException(e.getMessage());
            ex.setStackTrace(e.getStackTrace());
            throw ex;
        }
    }
    private void close(){
        closeParser();
        closeReader();
        closeFileInputStream();
        mResDocument=null;
        mCurrentElement=null;
        mCurrentText=null;
        mComments=null;
    }
    private void closeFileInputStream(){
        if(!(mParser instanceof MXParser)){
            return;
        }
        MXParser parser=(MXParser) mParser;
        InputStream inputStream = parser.getInputStream();
        if(!(inputStream instanceof FileInputStream)){
            return;
        }
        try {
            inputStream.close();
        } catch (IOException ignored) {
        }
    }
    private void closeReader(){
        if(!(mParser instanceof MXParser)){
            return;
        }
        MXParser parser=(MXParser) mParser;
        Reader reader = parser.getReader();
        if(reader!=null){
            try {
                reader.close();
            } catch (IOException ignored) {
            }
        }
    }
    private void closeParser(){
        if(!(mParser instanceof Closeable)){
            return;
        }
        Closeable closeable = (Closeable) mParser;
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }

    private XMLDocument parseDocument() throws XmlPullParserException, IOException {
        mResDocument=null;
        int type;
        while ((type=mParser.nextToken()) !=XmlPullParser.END_DOCUMENT){
            event(type);
        }
        event(XmlPullParser.END_DOCUMENT);
        if(mResDocument==null){
            throw new XmlPullParserException("Failed to parse/empty document");
        }
        return mResDocument;
    }
    private void event(int type) {
        if (type == XmlPullParser.START_DOCUMENT){
            onStartDocument();
        }else if (type == XmlPullParser.END_DOCUMENT){
            onEndDocument();
        }else if (type == XmlPullParser.START_TAG){
            onStartTag();
        }else if (type == XmlPullParser.END_TAG){
            onEndTag();
        }else if (type == XmlPullParser.TEXT){
            onText();
        }else if (type == XmlPullParser.ENTITY_REF){
            onEntityRef();
        }else if (type == XmlPullParser.COMMENT){
            onComment();
        }else if (type == XmlPullParser.IGNORABLE_WHITESPACE){
            onIgnorableWhiteSpace();
        }else {
            onUnknownType(type);
        }
    }
    private void onStartDocument(){
        mResDocument=new XMLDocument();
        mIndent=-1;
    }
    private void onEndDocument(){
        flushComments(null);
        applyIndent(mResDocument);
    }
    private void onStartTag(){
        String name=mParser.getName();
        flushTextContent();
        if(mCurrentElement==null){
            if(mResDocument==null){
                onStartDocument();
            }
            mCurrentElement=new XMLElement(name);
            mResDocument.setDocumentElement(mCurrentElement);
        }else {
            mCurrentElement=mCurrentElement.createElement(name);
        }
        mCurrentElement.setColumnNumber(mParser.getColumnNumber());
        mCurrentElement.setLineNumber(mParser.getLineNumber());
        checkIndent();
        flushComments(mCurrentElement);
        String ns=mParser.getNamespace();
        if(!XMLUtil.isEmpty(ns)){
            String prefix=mParser.getPrefix();
            if(!XMLUtil.isEmpty(prefix)){
                String tagName=appendPrefix(prefix,name);
                mCurrentElement.setTagName(tagName);
                checkNamespace(prefix, ns);
            }
        }
        loadAttributes();
    }
    private void loadAttributes(){
        int max=mParser.getAttributeCount();
        for(int i=0; i<max; i++){
            onAttribute(i);
        }
    }
    private void onAttribute(int i){
        String attrName=mParser.getAttributeName(i);

        String attrValue=mParser.getAttributeValue(i);
        String prefix=mParser.getAttributePrefix(i);
        if(!XMLUtil.isEmpty(prefix)){
            attrName=appendPrefix(prefix, attrName);
            checkNamespace(prefix, i);
        }
        mCurrentElement.setAttribute(attrName, attrValue);
    }
    private String appendPrefix(String prefix, String attrName){
        if(!prefix.endsWith(":")){
            prefix=prefix+":";
        }
        if(!attrName.startsWith(prefix)){
            attrName=prefix+attrName;
        }
        return attrName;
    }
    private void checkNamespace(String prefix, int i){
        NameSpaceItem nsItem=mCurrentElement.getNameSpaceItemForPrefix(prefix);
        if(nsItem!=null){
            return;
        }
        String nsUri=null;
        try {
            nsUri=mParser.getNamespaceUri(i);
        } catch (XmlPullParserException e) {
        }
        checkNamespace(prefix, nsUri);
    }
    private void checkNamespace(String prefix, String nsUri){
        NameSpaceItem nsItem=mCurrentElement.getNameSpaceItemForPrefix(prefix);
        if(nsItem!=null){
            return;
        }
        nsItem=new NameSpaceItem(prefix, nsUri);
        mCurrentElement.addNameSpace(nsItem);
        mNameSpaceCreated=true;
    }
    private void onEndTag(){
        flushTextContent();
        if(mNameSpaceCreated){
            mCurrentElement.applyNameSpaceItems();
            mNameSpaceCreated=false;
        }
        mCurrentElement=mCurrentElement.getParent();
    }
    private void onText(){
        String textContent=mParser.getText();
        appendText(textContent);
    }
    private void appendText(String text){
        if(text==null){
            return;
        }
        if(mCurrentText==null){
            mCurrentText=new StringBuilder();
        }
        mCurrentText.append(text);
    }
    private void flushTextContent(){
        if(mCurrentText==null){
            return;
        }
        String text=mCurrentText.toString();
        mCurrentText=null;
        if(text.trim().length()==0 && !mCurrentElement.hasTextContent()){
            return;
        }
        mCurrentElement.addText(new XMLText(text));
    }
    private void onEntityRef(){
        String name=mParser.getName();
        if(XMLUtil.isEmpty(name)){
            return;
        }
        appendText("&");
        appendText(name);
        appendText(";");
    }
    private void onComment(){
        String commentText=mParser.getText();
        addComment(commentText);
    }
    private void addComment(String commentText){
        if(XMLUtil.isEmpty(commentText)){
            return;
        }
        XMLComment commentElement=new XMLComment();
        commentElement.setCommentText(commentText);
        commentElement.setColumnNumber(mParser.getColumnNumber());
        commentElement.setLineNumber(mParser.getLineNumber());
        addComment(commentElement);
    }
    private void addComment(XMLComment ce){
        if(ce==null){
            return;
        }
        if(mComments==null){
            mComments=new ArrayList<>();
        }
        mComments.add(ce);
    }
    private void flushComments(XMLElement element){
        if(mComments==null){
            return;
        }
        if(element!=null){
            element.addComments(mComments);
        }
        mComments.clear();
        mComments=null;
    }
    private void onIgnorableWhiteSpace(){
    }
    private void onIgnore(int type){

    }
    private void onUnknownType(int type){
        String typeName=toTypeName(type);
        //System.err.println("Unknown TYPE = "+typeName+" "+type);
    }
    private String toTypeName(int type){
        String[] allTypes=XmlPullParser.TYPES;
        if(type<0 || type>=allTypes.length){
            return "type:"+type;
        }
        return allTypes[type];
    }

    private void checkIndent(){
        if(mIndent>=0){
            return;
        }
        String txt=mParser.getText();
        if(txt==null){
            return;
        }
        int len=txt.length();
        int col=mParser.getColumnNumber();
        mIndent=col-len;
        if(mIndent<0){
            mIndent=0;
        }
    }
    private void applyIndent(XMLDocument resDocument){
        if(mIndent<=0 || mIndent>5 || resDocument==null){
            mIndent=-1;
            return;
        }
        resDocument.setIndent(mIndent);
        mIndent=-1;
    }

    private static XmlPullParser createParser(String text) throws XMLParseException {
        if(text == null){
            throw new XMLParseException("Text is null, failed to create XmlPullParser");
        }
        InputStream in = new ByteArrayInputStream(text.getBytes());
        return createParser(in);
    }
    private static XmlPullParser createParser(File file) throws XMLParseException {
        if(file == null){
            throw new XMLParseException("File is null, failed to create XmlPullParser");
        }
        if(!file.isFile()){
            throw new XMLParseException("No such file : "+file.getAbsolutePath());
        }
        InputStream in;
        try {
            in=new FileChannelInputStream(file);
            return createParser(in);
        } catch (IOException e) {
            throw new XMLParseException(e.getMessage());
        }
    }
    private static XmlPullParser createParser(InputStream in) throws XMLParseException {
        try {
            XmlPullParser parser = new KXmlParser();
            parser.setInput(in, null);
            return parser;
        } catch (XmlPullParserException e) {
            throw new XMLParseException(e.getMessage());
        }
    }

    private static boolean isAndroid(int id){
        int pkgId=toPackageId(id);
        return pkgId>0 && pkgId<=ANDROID_PACKAGE_MAX;
    }
    private static boolean isResourceId(int id){
        int pkgId=toPackageId(id);
        return pkgId>0 && pkgId<128;
    }
    private static int toPackageId(int id){
        if(id<=0xff){
            return id;
        }
        return ((id >> 24) & 0xff);
    }
    private static final int ANDROID_PACKAGE_MAX=3;
}
