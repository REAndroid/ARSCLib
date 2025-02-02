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

import com.reandroid.xml.base.XmlReader;
import com.reandroid.xml.base.XmlSerializable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class XMLDocDeclaration implements XmlReader, XmlSerializable {

    private String version;
    private String encoding;
    private Boolean standalone;

    public XMLDocDeclaration() {
    }

    public Object get(String nameOrUri) {
        if ("version".equals(nameOrUri) || XMLUtil.PROPERTY_XMLDECL_VERSION.equals(nameOrUri)) {
            return getVersion();
        }
        if ("encoding".equals(nameOrUri)) {
            return encoding();
        }
        if ("standalone".equals(nameOrUri) || XMLUtil.PROPERTY_XMLDECL_STANDALONE.equals(nameOrUri)) {
            return standalone();
        }
        return null;
    }
    public void set(String nameOrUri, Object value) {
        if ("version".equals(nameOrUri) || XMLUtil.PROPERTY_XMLDECL_VERSION.equals(nameOrUri)) {
            version((String) value);
        } else if ("encoding".equals(nameOrUri)) {
            encoding((String) value);
        } else if ("standalone".equals(nameOrUri) || XMLUtil.PROPERTY_XMLDECL_STANDALONE.equals(nameOrUri)) {
            standalone((Boolean) value);
        }
    }

    public String version() {
        return version;
    }
    public void version(String version) {
        this.version = version;
    }

    public String getVersion() {
        String version = version();
        if (version == null && isValid()) {
            version = "1.0";
        }
        return version;
    }

    public String encoding() {
        return encoding;
    }
    public void encoding(String encoding) {
        this.encoding = encoding;
    }

    public Boolean standalone() {
        return standalone;
    }
    public void standalone(Boolean standalone) {
        this.standalone = standalone;
    }

    public boolean isValid() {
        return version() != null ||
                encoding() != null ||
                standalone() != null;
    }
    public void clear() {
        version(null);
        encoding(null);
        standalone(null);
    }



    @Override
    public void serialize(XmlSerializer serializer) throws IOException {
        if (isValid()) {
            serializer.startDocument(encoding(), standalone());
        }
    }
    @Override
    public void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        clear();
        if (parser.getEventType() == XmlPullParser.START_DOCUMENT) {
            parser.nextToken();
        }
        version((String) XMLUtil.getPropertySafe(parser, XMLUtil.PROPERTY_XMLDECL_VERSION));
        encoding(parser.getInputEncoding());
        standalone((Boolean) XMLUtil.getPropertySafe(parser, XMLUtil.PROPERTY_XMLDECL_STANDALONE));
    }

    @Override
    public String toString() {
        if (!isValid()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version='");
        builder.append(getVersion());
        builder.append("'");

        String encoding = encoding();
        if (encoding != null) {
            builder.append(" encoding='");
            builder.append(encoding);
            builder.append("'");
        }
        Boolean standalone = standalone();
        if (standalone != null) {
            builder.append(" standalone='");
            builder.append(standalone);
            builder.append("'");
        }
        builder.append(" ?>");
        return builder.toString();
    }
}
