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
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class XMLProcessingInstruction extends XMLNode {

    private String mText;

    public XMLProcessingInstruction(String text) {
        this.mText = text;
    }
    public XMLProcessingInstruction() {
        this(null);
    }

    public String getText() {
        return mText;
    }
    public void setText(String text) {
        this.mText = text;
    }

    @Override
    public void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        XMLUtil.expectEvent(parser, XmlPullParser.PROCESSING_INSTRUCTION);
        setText(parser.getText());
        parser.nextToken();
    }

    @Override
    public void serialize(XmlSerializer serializer) throws IOException {
        String text = getText();
        if (text != null) {
            serializer.processingInstruction(text);
        }
    }
    @Override
    void write(Appendable writer, boolean xml, boolean escapeXmlText) throws IOException {
        String text = getText();
        if (text != null) {
            writer.append("<?");
            writer.append(text);
            writer.append("?>");
        }
    }
    @Override
    public String toString() {
        return "<?" + getText() + "?>";
    }
}
