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
import com.reandroid.xml.base.Document;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.*;
import java.util.Iterator;

public class XMLDocument extends XMLNodeTree implements Document<XMLElement> {

    private final XMLDocDeclaration declaration;

    public XMLDocument(String elementName) {
        this();
        XMLElement docElem = new XMLElement(elementName);
        setDocumentElement(docElem);
    }
    public XMLDocument() {
        super();
        this.declaration = new XMLDocDeclaration();
    }

    public XMLElement getDocumentElement() {
        return CollectionUtil.getFirst(iterator(XMLElement.class));
    }
    public void setDocumentElement(XMLElement element) {
        clear();
        add(element);
    }

    public XMLDocDeclaration getDeclaration() {
        return declaration;
    }

    public void setEncoding(String encoding) {
        getDeclaration().encoding(encoding);
    }
    public void setStandalone(Boolean standalone) {
        getDeclaration().standalone(standalone);
    }

    @Override
    public XMLElement newElement() {
        XMLElement element = new XMLElement();
        add(element);
        return element;
    }
    @Override
    public XMLText newText() {
        XMLText xmlText = new XMLText();
        add(xmlText);
        return xmlText;
    }
    @Override
    public XMLText newText(String text) {
        return super.newText(text);
    }
    @Override
    public XMLComment newComment() {
        XMLComment comment = new XMLComment();
        add(comment);
        return comment;
    }

    public XMLDocType getDocType() {
        return CollectionUtil.getFirst(iterator(XMLDocType.class));
    }
    public XMLDocType getOrCreateDocType() {
        XMLDocType docType = getDocType();
        if (docType == null) {
            docType = newDocType();
            move(docType, 0);
        }
        return docType;
    }

    @Override
    protected void onStartParse(XmlPullParser parser) throws XmlPullParserException, IOException {
        int event = parser.getEventType();
        if (event == XmlPullParser.START_DOCUMENT) {
            clear();
            getDeclaration().parse(parser);
        } else if (!XMLUtil.hasFeatureRelaxed(parser)) {
            throw new XmlPullParserException("Unexpected event, expecting = "
                    + XMLUtil.toEventName(XmlPullParser.START_DOCUMENT) + ", found = "
                    + XMLUtil.toEventName(event));
        }
    }
    @Override
    protected void onEndParse(XmlPullParser parser) throws XmlPullParserException, IOException {
    }
    @Override
    void startSerialize(XmlSerializer serializer) throws IOException {
        getDeclaration().serialize(serializer);
    }
    @Override
    void endSerialize(XmlSerializer serializer) {
        if (getDeclaration().isValid()) {
            try {
                serializer.endDocument();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
    @Override
    void write(Appendable appendable, boolean xml, boolean escapeXmlText) throws IOException {
        XMLDocDeclaration declaration = getDeclaration();
        if (declaration.isValid()) {
            appendable.append(declaration.toString());
        }
        Iterator<XMLNode> iterator = iterator();
        while (iterator.hasNext()) {
            iterator.next().write(appendable, xml, escapeXmlText);
        }
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
}
