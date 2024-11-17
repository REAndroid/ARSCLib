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
package com.reandroid.arsc.chunk.xml;

import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import org.xmlpull.v1.XmlPullParser;


public class ResXmlEvent {

    private final int type;
    private final ResXmlNode xmlNode;

    public ResXmlEvent(int type, ResXmlNode xmlNode) {
        this.type = type;
        this.xmlNode = xmlNode;
    }

    public int getLineNumber() {
        return 0;
    }
    public String getText() {
        return null;
    }
    public int getType() {
        return type;
    }
    public ResXmlNode getXmlNode() {
        return xmlNode;
    }
    public int getDepth() {
        return getXmlNode().getDepth();
    }

    public String getEventName() {
        int t = getType();
        String[] types = XmlPullParser.TYPES;
        if (t >= 0 && t < types.length) {
            return types[t];
        }
        return "unknown-" + t;
    }
    @Override
    public String toString() {
        String text = getText();
        if (text != null && text.length() > 100) {
            text = text.substring(0, 100) + " ...";
        }
        return getEventName() + " {" + text + "} ";
    }

    public static ResXmlEvent startComment(ResXmlElement element) {
        String comment = element.getStartComment();
        if (StringsUtil.isEmpty(comment)) {
            return null;
        }
        return new ResXmlEvent(COMMENT, element) {
            @Override
            public int getLineNumber() {
                return getXmlNode().getStartLineNumber();
            }
            @Override
            public String getText() {
                return ((ResXmlElement) getXmlNode()).getStartComment();
            }
        };
    }
    public static ResXmlEvent endComment(ResXmlElement element) {
        String comment = element.getEndComment();
        if (StringsUtil.isEmpty(comment)) {
            return null;
        }
        return new ResXmlEvent(COMMENT, element) {
            @Override
            public int getLineNumber() {
                return getXmlNode().getEndLineNumber();
            }
            @Override
            public String getText() {
                return ((ResXmlElement) getXmlNode()).getEndComment();
            }
        };
    }
    public static ResXmlEvent comment(ResXmlTextNode textNode) {
        String comment = textNode.getComment();
        if (StringsUtil.isEmpty(comment)) {
            return null;
        }
        return new ResXmlEvent(COMMENT, textNode) {
            @Override
            public String getText() {
                return ((ResXmlTextNode) getXmlNode()).getComment();
            }
            @Override
            public int getLineNumber() {
                return getXmlNode().getLineNumber();
            }
        };
    }
    public static ResXmlEvent startTag(ResXmlElement element) {
        return new ResXmlEvent(START_TAG, element) {
            @Override
            public int getLineNumber() {
                return getXmlNode().getStartLineNumber();
            }
            @Override
            public String getText() {
                return ((ResXmlElement) getXmlNode()).getName(false);
            }
        };
    }
    public static ResXmlEvent endTag(ResXmlElement element) {
        return new ResXmlEvent(END_TAG, element) {
            @Override
            public int getLineNumber() {
                return getXmlNode().getEndLineNumber();
            }
            @Override
            public String getText() {
                return ((ResXmlElement) getXmlNode()).getName(false);
            }
        };
    }
    public static ResXmlEvent text(ResXmlTextNode resXmlText) {
        if (resXmlText.isIndent()) {
            return null;
        }
        return new ResXmlEvent(TEXT, resXmlText) {
            @Override
            public int getLineNumber() {
                return getXmlNode().getLineNumber();
            }
            @Override
            public String getText() {
                return ((ResXmlTextNode) getXmlNode()).getText();
            }
        };
    }
    public static ResXmlEvent startDocument(ResXmlDocument xmlDocument) {
        return new ResXmlEvent(START_DOCUMENT, xmlDocument) {
            @Override
            public int getLineNumber() {
                return getXmlNode().getStartLineNumber();
            }
        };
    }
    public static ResXmlEvent endDocument(ResXmlDocument xmlDocument) {
        return new ResXmlEvent(END_DOCUMENT, xmlDocument) {
            @Override
            public int getLineNumber() {
                return getXmlNode().getEndLineNumber();
            }
        };
    }

    public static final int START_DOCUMENT = ObjectsUtil.of(XmlPullParser.START_DOCUMENT);
    public static final int END_DOCUMENT = ObjectsUtil.of(XmlPullParser.END_DOCUMENT);
    public static final int START_TAG = ObjectsUtil.of(XmlPullParser.START_TAG);
    public static final int END_TAG = ObjectsUtil.of(XmlPullParser.END_TAG);
    public static final int TEXT = ObjectsUtil.of(XmlPullParser.TEXT);
    public static final int CDSECT = ObjectsUtil.of(XmlPullParser.CDSECT);
    public static final int ENTITY_REF = ObjectsUtil.of(XmlPullParser.ENTITY_REF);
    public static final int IGNORABLE_WHITESPACE = ObjectsUtil.of(XmlPullParser.IGNORABLE_WHITESPACE);
    public static final int PROCESSING_INSTRUCTION = ObjectsUtil.of(XmlPullParser.PROCESSING_INSTRUCTION);
    public static final int COMMENT = ObjectsUtil.of(XmlPullParser.COMMENT);
    public static final int DOCDECL = ObjectsUtil.of(XmlPullParser.DOCDECL);
}
