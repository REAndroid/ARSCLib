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

import com.reandroid.utils.StringsUtil;
import com.reandroid.xml.base.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class XMLText extends XMLNode implements Text {

    private String text;

    public XMLText(String text) {
        this.text = text;
    }
    public XMLText() {
        this(null);
    }

    @Override
    public XMLNodeTree getParentNode() {
        return (XMLNodeTree) super.getParentNode();
    }

    public String getText() {
        return getText(false);
    }
    public String getText(boolean escapeXmlChars) {
        if (escapeXmlChars) {
            return XMLUtil.escapeXmlChars(text);
        }
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public void appendText(char ch) {
        if (ch == 0) {
            return;
        }
        appendText(String.valueOf(ch));
    }
    public void appendText(String text) {
        if (text == null) {
            return;
        }
        if (this.text == null || this.text.length() == 0) {
            this.text = text;
            return;
        }
        this.text = this.text + text;
    }
    @Override
    public void serialize(XmlSerializer serializer) throws IOException {
        serializer.text(getText());
    }
    @Override
    public void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        int event = parser.getEventType();
        if (!isTextEvent(event)) {
            throw new XmlPullParserException("Not TEXT event");
        }
        while (isTextEvent(event)) {
            appendText(parser.getText());
            event = parser.nextToken();
        }
    }
    @Override
    void write(Appendable appendable, boolean xml, boolean escapeXmlText) throws IOException {
        String text = getText(escapeXmlText);
        if (text != null) {
            appendable.append(text);
        }
    }

    public boolean isIndent() {
        String text = getText();
        return isEmptyOrNewlineBlank(text);
    }
    public boolean isBlank() {
        return StringsUtil.isBlank(getText());
    }

    @Override
    public String toString() {
        return getText();
    }

    static boolean isTextEvent(int event) {
        return event == XmlPullParser.TEXT
                || event == XmlPullParser.ENTITY_REF || event == XmlPullParser.IGNORABLE_WHITESPACE;
    }
    private static boolean isEmptyOrNewlineBlank(String text) {
        if (text == null) {
            return true;
        }
        if (text.length() == 0) {
            return false;
        }
        if (text.indexOf('\n') < 0) {
            return false;
        }
        return StringsUtil.isBlank(text);
    }
}
