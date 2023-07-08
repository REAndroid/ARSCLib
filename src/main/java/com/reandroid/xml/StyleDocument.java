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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

public class StyleDocument extends XMLDocument{
    public StyleDocument(){
        super();
    }
    public boolean hasElements(){
        return getElements().hasNext();
    }
    public Iterator<StyleElement> getElements(){
        return iterator(StyleElement.class);
    }


    public String getText(boolean xml){
        StringWriter writer = new StringWriter();
        try {
            write(writer, xml);
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
        }
        return writer.toString();
    }
    public String getStyledText(){
        StringWriter writer = new StringWriter();
        try {
            writeStyledText(writer);
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
        }
        return writer.toString();
    }
    void writeStyledText(Appendable appendable) throws IOException {
        Iterator<XMLNode> iterator = iterator();
        while (iterator.hasNext()){
            XMLNode xmlNode = iterator.next();
            if(xmlNode instanceof StyleText){
                StyleText styleText = (StyleText) xmlNode;
                styleText.writeStyledText(appendable);
            } else if(xmlNode instanceof StyleElement){
                StyleElement element = (StyleElement) xmlNode;
                element.writeStyledText(appendable);
            }
        }
    }
    @Override
    void write(Appendable appendable, boolean xml) throws IOException {
        appendDocument(appendable, xml);
        appendChildes(iterator(), appendable, xml);
    }
    private void appendChildes(Iterator<XMLNode> iterator, Appendable appendable, boolean xml) throws IOException {
        while (iterator.hasNext()){
            iterator.next().write(appendable, xml);
        }
    }
    @Override
    StyleElement newElement(){
        return new StyleElement();
    }
    @Override
    StyleText newText(){
        return new StyleText();
    }
    @Override
    XMLComment newComment(){
        return null;
    }
    @Override
    StyleAttribute newAttribute(){
        return new StyleAttribute();
    }

    public static StyleDocument parseNext(XmlPullParser parser) throws IOException, XmlPullParserException {
        StyleDocument styleDocument = new StyleDocument();
        styleDocument.parseInner(parser);
        return styleDocument;
    }
}
