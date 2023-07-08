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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.*;
import java.util.Iterator;

public class XMLDocument extends XMLNodeTree{
    private String encoding;
    private Boolean standalone;
    public XMLDocument(String elementName){
        this();
        XMLElement docElem=new XMLElement(elementName);
        setDocumentElement(docElem);
    }
    public XMLDocument(){
        super();
    }

    @Override
    XMLDocument clone(XMLNode parent) {

        XMLDocument document = new XMLDocument();
        document.encoding = encoding;
        document.standalone = standalone;
        Iterator<XMLNode> iterator = iterator();
        while(iterator.hasNext()){
            iterator.next().clone(document);
        }
        return document;
    }

    public XMLElement getDocumentElement(){
        return CollectionUtil.getFirst(iterator(XMLElement.class));
    }
    public void setDocumentElement(XMLElement element){
        clear();
        add(element);
    }
    public void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        encoding = null;
        standalone = null;
        clear();
        int event = parser.getEventType();
        if(event == XmlPullParser.START_DOCUMENT){
            encoding = parser.getInputEncoding();
            XMLUtil.findStartTag(parser);
        }else if(event == XmlPullParser.END_TAG || event == XmlPullParser.START_TAG){
            parser.next();
        }else if(event == XmlPullParser.END_DOCUMENT){
            return;
        }
        XMLUtil.findStartTag(parser);
        XMLElement element = newElement();
        add(element);
        element.parse(parser);
    }
    public void parseInner(XmlPullParser parser) throws XmlPullParserException, IOException {
        encoding = null;
        standalone = null;
        clear();
        int event = parser.getEventType();
        if(event == XmlPullParser.START_DOCUMENT){
            encoding = parser.getInputEncoding();
            event = XMLUtil.findStartTag(parser);
        }
        if(event == XmlPullParser.END_DOCUMENT){
            return;
        }
        if(event != XmlPullParser.START_TAG){
            throw new XmlPullParserException("Invalid document event: " + event);
        }
        event = parser.next();
        while (event != XmlPullParser.END_TAG && event != XmlPullParser.END_DOCUMENT){
            XMLNode node = createChildNode(event);
            if(node != null){
                add(node);
                node.parse(parser);
                event = parser.getEventType();
            }else {
                event = parser.nextToken();
            }
        }
    }
    @Override
    void startSerialize(XmlSerializer serializer) throws IOException {
        if(encoding == null){
            return;
        }
        serializer.startDocument(encoding, standalone);
    }
    @Override
    void endSerialize(XmlSerializer serializer) {
    }
    void appendDocument(Appendable appendable, boolean xml) throws IOException {
        if(encoding == null || !xml){
            return;
        }
        appendable.append("<?xml version='1.0' encoding='");
        appendable.append(encoding);
        appendable.append("'?>");
    }
    @Override
    void write(Appendable writer) throws IOException{

    }
    @Override
    public String toText(int indent, boolean newLineAttributes){
        StringWriter writer=new StringWriter();
        try {
            write(writer, newLineAttributes);
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
        }
        return writer.toString();
    }
    public static XMLDocument load(String text) throws XmlPullParserException, IOException {
        XMLDocument document = new XMLDocument();
        document.parse(XMLFactory.newPullParser(text));
        return document;
    }
    public static XMLDocument load(InputStream inputStream) throws XmlPullParserException, IOException {
        XMLDocument document = new XMLDocument();
        document.parse(XMLFactory.newPullParser(inputStream));
        return document;
    }
    public static XMLDocument load(File file) throws XmlPullParserException, IOException {
        XMLDocument document = new XMLDocument();
        document.parse(XMLFactory.newPullParser(file));
        return document;
    }
    XMLNode createChildNode(int event){
        if(event == XmlPullParser.START_TAG){
            return newElement();
        }
        if(XMLText.isTextEvent(event)){
            return newText();
        }
        return null;
    }
}
