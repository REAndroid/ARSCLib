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

public class ParserEvent {

    private final int event;
    private final ResXmlNode xmlNode;
    private final String comment;
    private final boolean endComment;

    public ParserEvent(int event, ResXmlNode xmlNode, String comment, boolean endComment){
        this.event = event;
        this.xmlNode = xmlNode;
        this.comment = comment;
        this.endComment = endComment;
    }
    public ParserEvent(int event, ResXmlNode xmlNode){
        this(event, xmlNode, null, false);
    }
    public int getEvent() {
        return event;
    }
    public ResXmlNode getXmlNode() {
        return xmlNode;
    }
    public String getComment() {
        return comment;
    }
    public boolean isEndComment() {
        return endComment;
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

    public static ParserEvent startComment(ResXmlElement element) {
        String comment = element.getStartComment();
        if (StringsUtil.isEmpty(comment)) {
            return null;
        }
        return new ParserEvent(COMMENT, element, comment, false);
    }
    public static ParserEvent endComment(ResXmlElement element) {
        String comment = element.getEndComment();
        if (StringsUtil.isEmpty(comment)) {
            return null;
        }
        return new ParserEvent(COMMENT, element, comment, true);
    }
    public static ParserEvent startTag(ResXmlElement element) {
        String comment = element.getStartComment();
        if (StringsUtil.isEmpty(comment)) {
            return null;
        }
        return new ParserEvent(START_TAG, element);
    }
    public static ParserEvent endTag(ResXmlElement element) {
        String comment = element.getStartComment();
        if (StringsUtil.isEmpty(comment)) {
            return null;
        }
        return new ParserEvent(END_TAG, element);
    }
    public static ParserEvent text(ResXmlTextNode resXmlText) {
        if (resXmlText.isIndent()) {
            return null;
        }
        return new ParserEvent(TEXT, resXmlText);
    }
    public static ParserEvent startDocument() {
        return new ParserEvent(START_DOCUMENT, null);
    }
    public static ParserEvent endDocument() {
        return new ParserEvent(END_DOCUMENT, null);
    }

}
