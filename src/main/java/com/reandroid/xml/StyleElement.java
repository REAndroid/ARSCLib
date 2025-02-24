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

import com.reandroid.utils.collection.IndexIterator;
import com.reandroid.utils.collection.SizedSupplier;
import com.reandroid.xml.base.Attribute;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Iterator;

public class StyleElement extends XMLElement implements Span {

    public StyleElement() {
        super();
    }

    @Override
    public StyleElement getParentElement() {
        return (StyleElement) super.getParentElement();
    }
    @Override
    public StyleAttribute getAttributeAt(int i) {
        return (StyleAttribute) super.getAttributeAt(i);
    }
    @Override
    public void addAttribute(Attribute attribute) {
        if (!(attribute instanceof StyleAttribute)) {
            throw new ClassCastException("Incompatible attribute type: "
                    + attribute.getClass());
        }
        super.addAttribute(attribute);
    }

    @Override
    public StyleAttribute getAttribute(String name) {
        return (StyleAttribute) super.getAttribute(name);
    }
    @Override
    public Iterator<StyleElement> getElements() {
        return iterator(StyleElement.class);
    }
    @Override
    public Iterator<StyleAttribute> getAttributes() {
        return new IndexIterator<>(new SizedSupplier<StyleAttribute>() {
            @Override
            public int size() {
                return getAttributeCount();
            }
            @Override
            public StyleAttribute get(int index) {
                return getAttributeAt(index);
            }
        });
    }

    public String getTagString() {
        String tag = getTagName();
        String attributes = getSpanAttributes();
        if(attributes == null) {
            return tag;
        }
        return tag + attributes;
    }

    @Override
    public String getTagName() {
        return getName();
    }

    @Override
    public int getFirstChar() {
        XMLNode parent = getRootParentNode();
        int result = 0;
        Iterator<XMLNode> itr = ((XMLNodeTree)parent).recursiveNodes();
        while (itr.hasNext()) {
            XMLNode child = itr.next();
            if (child == this) {
                break;
            }
            result += child.getTextLength();
        }
        return result;
    }

    @Override
    public int getLastChar() {
        int result = getFirstChar() + getLength();
        if (result != 0) {
            result = result -1;
        }
        return result;
    }

    @Override
    public int getSpanOrder() {
        XMLNode parent = getRootParentNode();
        int result = 0;
        Iterator<XMLNode> iterator = ((XMLNodeTree)parent).recursiveNodes();
        while (iterator.hasNext()) {
            XMLNode child = iterator.next();
            if (child == this) {
                break;
            }
            if (child instanceof StyleElement) {
                result ++;
            }
        }
        return result;
    }

    @Override
    public String getSpanAttributes() {
        return SpanAttributesEncoder.encodeAttributes(this);
    }
    @Override
    public StyleElement toElement() {
        return this;
    }

    @Override
    int getLength() {
        int result = 0;
        Iterator<XMLNode> itr = iterator();
        while (itr.hasNext()) {
            XMLNode child = itr.next();
            result += child.getLength();
        }
        return result;
    }
    void writeStyledText(Appendable appendable) throws IOException {
        Iterator<XMLNode> iterator = iterator();
        while (iterator.hasNext()) {
            XMLNode xmlNode = iterator.next();
            if (xmlNode instanceof StyleText) {
                StyleText styleText = (StyleText) xmlNode;
                styleText.writeStyledText(appendable);
            } else if (xmlNode instanceof StyleElement) {
                StyleElement element = (StyleElement) xmlNode;
                element.writeStyledText(appendable);
            }
        }
    }

    @Override
    void startSerialize(XmlSerializer serializer) throws IOException {
        serializer.startTag(null, getName());
        Iterator<StyleAttribute> itr = getAttributes();
        while (itr.hasNext()) {
            itr.next().serialize(serializer);
        }
    }
    @Override
    void endSerialize(XmlSerializer serializer) throws IOException {
        serializer.endTag(null, getName());
    }
    @Override
    public void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        XMLUtil.expectEvent(parser, XmlPullParser.START_TAG);
        setName(parser.getName());
        int count = parser.getAttributeCount();
        for(int i = 0; i < count; i++) {
            newAttribute().set(parser.getAttributeName(i),
                    parser.getAttributeValue(i));
        }
        int event = parser.next();
        while (event != XmlPullParser.END_TAG && event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                newElement().parse(parser);
            } else if (XMLText.isTextEvent(event)) {
                newText().parse(parser);
            } else {
                parser.next();
            }
            event = parser.getEventType();
        }
        if (parser.getEventType() == XmlPullParser.END_TAG) {
            parser.next();
        }
    }

    @Override
    public StyleElement newElement() {
        StyleElement element = new StyleElement();
        add(element);
        return element;
    }
    @Override
    public StyleText newText() {
        StyleText styleText = new StyleText();
        add(styleText);
        return styleText;
    }
    public StyleText newText(String text) {
        StyleText styleText = newText();
        styleText.setText(text);
        return styleText;
    }
    public XMLComment newComment() {
        return null;
    }
    @Override
    public StyleAttribute newAttribute() {
        StyleAttribute attribute = new StyleAttribute();
        addAttribute(attribute);
        return attribute;
    }

    void copyFrom(XMLElement xmlElement) {
        setName(xmlElement.getName());
        setVoidHtml(xmlElement.isVoidHtml());
        Iterator<? extends XMLAttribute> attributes = xmlElement.getAttributes();
        while (attributes.hasNext()) {
            newAttribute().setFrom(attributes.next());
        }
        Iterator<XMLNode> iterator = xmlElement.iterator();
        while (iterator.hasNext()) {
            XMLNode xmlNode = iterator.next();
            if (xmlNode instanceof XMLElement) {
                newElement().copyFrom((XMLElement) xmlNode);
            } else if (xmlNode instanceof XMLText) {
                XMLText xmlText = (XMLText)xmlNode;
                getOrCreateLastText().appendText(xmlText.getText());
            } else if (xmlNode instanceof XMLCDSect) {
                XMLCDSect xmlcdSect = (XMLCDSect)xmlNode;
                getOrCreateLastText().appendText(xmlcdSect.getText());
            }
        }
    }
    @Override
    public String toString() {
        return "[" + getFirstChar() + ", " + getLastChar() + "] "
                + getTagString();
    }
}
