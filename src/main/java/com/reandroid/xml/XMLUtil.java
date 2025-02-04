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

import com.reandroid.utils.ObjectsUtil;
import com.reandroid.xml.kxml2.KXmlSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.Closeable;
import java.io.IOException;

public class XMLUtil {

    public static void expectEvent(XmlPullParser parser, int expect) throws XmlPullParserException {
        int event = parser.getEventType();
        if (event != expect) {
            throw new XmlPullParserException("Expecting event: " + toEventName(expect) +
                    ", but found: " + toEventName(event));
        }
    }
    public static String decodeEntityRef(String entityRef) {
        if (entityRef == null || entityRef.length() == 0) {
            return entityRef;
        }
        String decode;
        if (entityRef.equals("lt")) {
            decode = "<";
        } else if (entityRef.equals("gt")) {
            decode = ">";
        } else if (entityRef.equals("amp")) {
            decode = "&";
        } else if (entityRef.equals("quote")) {
            decode = "\"";
        } else if (entityRef.equals("apos")) {
            decode = "'";
        } else if (entityRef.startsWith("#")) {
            int code;
            if (entityRef.startsWith("#x")) {
                code = Integer.parseInt(entityRef.substring(2), 16);
            } else {
                code = Integer.parseInt(entityRef.substring(1));
            }
            decode = new StringBuilder().appendCodePoint(code).toString();
        } else {
            decode = entityRef;
        }
        return decode;
    }

    public static String splitName(String name) {
        if (name == null) {
            return null;
        }
        int i = name.lastIndexOf(':');
        if (i >= 0) {
            i++;
            name = name.substring(i);
        }
        name = name.trim();
        if (name.length() == 0) {
            return null;
        }
        return name;
    }
    public static String splitPrefix(String name) {
        if (name == null) {
            return null;
        }
        int i = name.indexOf(':');
        if (i > 0) {
            return name.substring(0, i);
        }
        return null;
    }
    public static int ensureStartTag(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        int event = parser.getEventType();
        while (event != XmlPullParser.START_TAG
                && event != XmlPullParser.END_DOCUMENT) {
            event = parser.next();
        }
        return event;
    }
    public static int ensureTag(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        int event = parser.getEventType();
        while (event != XmlPullParser.START_TAG &&
                event != XmlPullParser.END_TAG  &&
                event != XmlPullParser.END_DOCUMENT) {
            event = parser.next();
        }
        return event;
    }
    public static boolean isEmpty(String s) {
        if (s == null) {
            return true;
        }
        return s.length() == 0;
    }
    public static String escapeXmlChars(String str) {
        return escapeXmlChars(str, false);
    }
    public static String escapeXmlChars(String str, boolean attribute) {
        if (str == null) {
            return null;
        }
        int length = str.length();
        if (length == 0) {
            return str;
        }
        StringBuilder builder = new StringBuilder(length + 16);
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if (c == '\n' || c == '\r' || c == '\t') {
                if (attribute) {
                    appendCodePoint(builder, c);
                } else {
                    builder.append(c);
                }
            } else if (c == '&') {
                builder.append("&amp;");
            } else if (c == '<') {
                builder.append("&lt;");
            } else if (c == '>') {
                builder.append("&gt;");
            } else {
                if ((c >= 0x20 && c <= 0xd7ff) || (c >= 0xe000 && c <= 0xfffd)) {
                    builder.append(c);
                } else if (Character.isHighSurrogate(c) && i < length - 1) {
                    i ++;
                    appendCodePoint(builder, Character.toCodePoint(c, str.charAt(i)));
                } else {
                    appendCodePoint(builder, c);
                }
            }
        }
        return builder.toString();
    }
    private static void appendCodePoint(StringBuilder builder, int codePoint) {
        builder.append("&#");
        builder.append(codePoint);
        builder.append(";");
    }
    public static String toEventName(int eventType) {
        String[] types = EVENT_TYPES;
        if (eventType < 0 || eventType >= types.length) {
            return String.valueOf(eventType);
        }
        return types[eventType];
    }
    public static boolean getFeatureSafe(XmlPullParser parser, String name, boolean def) {
        try {
            return parser.getFeature(name);
        } catch (Throwable ignored) {
            return def;
        }
    }
    public static void setFeatureSafe(XmlPullParser parser, String name, boolean state) {
        try {
            parser.setFeature(name, state);
        } catch (Throwable ignored) {
        }
    }
    public static void setFeatureSafe(XmlSerializer serializer, String name, boolean state) {
        try {
            serializer.setFeature(name, state);
        } catch (Throwable ignored) {
        }
    }
    public static Object getPropertySafe(XmlPullParser parser, String name) {
        try {
            return parser.getProperty(name);
        } catch (Throwable ignored) {
            return null;
        }
    }
    public static void close(XmlSerializer serializer) {
        if (serializer instanceof XmlSerializerWrapper) {
            close(((XmlSerializerWrapper) serializer).getBaseSerializer());
        } else if (serializer != null) {
            try {
                serializer.flush();
            } catch (IOException ignored) {}
            if (serializer instanceof Closeable) {
                try {
                    ((Closeable) serializer).close();
                } catch (IOException ignored) {}
            }
        }
    }

    public static Object getLocation(XmlPullParser parser) {
        try {
            return parser.getProperty(XMLUtil.PROPERTY_LOCATION);
        } catch (Throwable ignored) {
            return null;
        }
    }
    public static void setLocation(XmlPullParser parser, Object location) {
        try {
            parser.setProperty(XMLUtil.PROPERTY_LOCATION, location);
        } catch (Throwable ignored) {
        }
    }
    public static boolean hasFeatureRelaxed(XmlPullParser parser) {
        return getFeatureSafe(parser, FEATURE_RELAXED, false);
    }
    public static void setFeatureRelaxed(XmlPullParser parser, boolean value) {
        setFeatureSafe(parser, FEATURE_RELAXED, value);
    }
    public static KXmlSerializer getKXmlSerializer(XmlSerializer serializer) {
        if (serializer instanceof KXmlSerializer) {
            return (KXmlSerializer) serializer;
        }
        if (serializer instanceof XmlSerializerWrapper) {
            XmlSerializerWrapper wrapper = (XmlSerializerWrapper) serializer;
            return getKXmlSerializer(wrapper.getBaseSerializer());
        }
        return null;
    }

    public static final String PROPERTY_XMLDECL_VERSION = ObjectsUtil.of(
            "http://xmlpull.org/v1/doc/properties.html#xmldecl-version");
    public static final String PROPERTY_XMLDECL_STANDALONE = ObjectsUtil.of(
            "http://xmlpull.org/v1/doc/properties.html#xmldecl-standalone");

    public static final String FEATURE_INDENT_OUTPUT = ObjectsUtil.of(
            "http://xmlpull.org/v1/doc/features.html#indent-output");
    public static final String PROPERTY_LOCATION = ObjectsUtil.of(
            "http://xmlpull.org/v1/doc/properties.html#location");
    public static final String FEATURE_RELAXED = ObjectsUtil.of(
            "http://xmlpull.org/v1/doc/features.html#relaxed");

    public static String [] EVENT_TYPES = {
            "START_DOCUMENT",
            "END_DOCUMENT",
            "START_TAG",
            "END_TAG",
            "TEXT",
            "CDSECT",
            "ENTITY_REF",
            "IGNORABLE_WHITESPACE",
            "PROCESSING_INSTRUCTION",
            "COMMENT",
            "DOCDECL"
    };

}
