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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class XMLDocType extends XMLNode {

    private String mName;

    public XMLDocType(String text) {
        this.mName = text;
    }

    public XMLDocType() {
        this(null);
    }

    public String getName() {
        return mName;
    }
    public void setName(String name) {
        this.mName = name;
    }

    @Override
    void write(Appendable writer, boolean xml, boolean escapeXmlText) throws IOException {
        String type = getName();
        if (type != null) {
            writer.append("<!DOCTYPE");
            writer.append(' ');
            writer.append(type);
            writer.append('>');
        }
    }

    @Override
    public void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        XMLUtil.expectEvent(parser, XmlPullParser.DOCDECL);
        String type = StringsUtil.trimStart(parser.getText(), ' ');
        setName(type);
        if (type != null && type.contains("html")) {
            XMLUtil.setFeatureRelaxed(parser, true);
        }
        parser.nextToken();
    }

    @Override
    public void serialize(XmlSerializer serializer) throws IOException {
        String type = getName();
        if (type != null) {
            serializer.docdecl(" " + type);
        }
    }

    @Override
    public String toString() {
        return "<!DOCTYPE " + getName() + ">";
    }
}
