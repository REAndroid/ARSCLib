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

import com.reandroid.arsc.coder.XmlSanitizer;
import com.reandroid.arsc.refactor.ResourceMergeOption;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.SingleIterator;
import com.reandroid.xml.XMLComment;
import com.reandroid.xml.XMLNode;
import com.reandroid.xml.XMLText;
import com.reandroid.xml.XMLUtil;
import com.reandroid.xml.base.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Iterator;

public class ResXmlTextNode extends ResXmlNode implements Text {

    private String mIndentText;
    
    public ResXmlTextNode() {
        super(new ResXmlTextChunk());
    }

    public boolean isEmpty() {
        return StringsUtil.isEmpty(getText());
    }
    public boolean isBlank() {
        return StringsUtil.isBlank(getText());
    }

    @Override
    ResXmlTextChunk getChunk() {
        return (ResXmlTextChunk) super.getChunk();
    }

    public boolean isIndent() {
        return !hasComment() && isIndent(getText());
    }

    @Override
    int autoSetLineNumber(int start) {
        int lineNumber = start;
        if (isComment()) {
            start ++;
        } else {
            String text = getText();
            if (isIndent(text) && isNextElement()) {
                lineNumber ++;
            } else {
                start += StringsUtil.countChar(text, '\n');
            }
        }
        setLineNumber(lineNumber);
        return start;
    }
    private boolean isNextElement() {
        ResXmlNodeTree parent = getParentNode();
        if (parent != null) {
            return parent.get(getIndex() + 1) instanceof ResXmlElement;
        }
        return false;
    }
    public String getComment() {
        return getChunk().getComment();
    }
    public void setComment(String comment) {
        getChunk().setComment(comment);
    }

    @Override
    Iterator<ResXmlEvent> getParserEvents() {
        if (isComment()) {
            return SingleIterator.of(ResXmlEvent.comment(this));
        }
        if (hasComment()) {
            return CombiningIterator.singleOne(ResXmlEvent.comment(this),
                    SingleIterator.of(ResXmlEvent.text(this)));
        }
        return SingleIterator.of(ResXmlEvent.text(this));
    }

    @Override
    public ResXmlNodeTree getParentNode() {
        return (ResXmlNodeTree) super.getParentNode();
    }

    @Override
    public int getStartLineNumber() {
        return getChunk().getLineNumber();
    }
    @Override
    public int getEndLineNumber() {
        int line = getStartLineNumber();
        if (!isIndent()) {
            line += StringsUtil.countChar(getText(), '\n');
        }
        return line;
    }

    @Override
    public void setLineNumber(int lineNumber) {
        getChunk().setLineNumber(lineNumber);
    }
    @Override
    public String getText() {
        return getChunk().getText();
    }
    @Override
    public void setText(String text) {
        getChunk().setText(text);
        mIndentText = null;
    }
    public void append(String text) {
        String exist = getText();
        if (exist == null || exist.length() == 0) {
            exist = mIndentText;
        }
        if (exist == null && isIndent(text)) {
            mIndentText = text;
            return;
        }
        if (exist != null) {
            text = exist + text;
        }
        setText(text);
    }

    @Override
    public boolean isNull() {
        return getChunk().isNull();
    }
    @Override
    public boolean removeSelf() {
        ResXmlNodeTree parentNode = getParentNode();
        if (parentNode != null) {
            return parentNode.remove(this);
        }
        return false;
    }

    @Override
    void onPreRemove() {
        getChunk().onPreRemove();
    }
    @Override
    void linkStringReferences() {
        getChunk().linkStringReferences();
    }
    @Override
    public void serialize(XmlSerializer serializer, boolean decode) throws IOException {
        serializeComment(serializer, getComment());
        if (isText()) {
            String text = getText();
            if (text != null) {
                serializer.text(text);
            }
        }
    }

    @Override
    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {

        setLineNumber(parser.getLineNumber());

        if (parser.getEventType() == XmlPullParser.COMMENT) {
            setComment(parser.getText());
            parser.nextToken();
        } else {
            if (!isTextEvent(parser.getEventType())) {
                throw new XmlPullParserException("Expecting text events, but found: "
                        + XMLUtil.toEventName(parser.getEventType()) + ", "
                        + parser.getPositionDescription());
            }

            while (isTextEvent(parser.getEventType())) {
                append(XmlSanitizer.unEscapeUnQuote(parser.getText()));
                parser.nextToken();
            }

            if (isNull()) {
                removeSelf();
            }
        }
    }

    @Override
    public XMLNode toXml(boolean decode) {
        if (isComment()) {
            return new XMLComment(getComment());
        }
        return new XMLText(getText());
    }

    @Override
    public void merge(ResXmlNode xmlNode) {
        if (xmlNode == this) {
            return;
        }
        ResXmlTextNode coming = (ResXmlTextNode) xmlNode;
        setText(coming.getText());
        setComment(coming.getComment());
        setLineNumber(coming.getLineNumber());
    }

    public void mergeWithName(ResourceMergeOption mergeOption, ResXmlNode xmlNode) {
        this.merge(xmlNode);
    }

    @Override
    public boolean isText() {
        return !isComment();
    }
    public boolean isComment() {
        return hasComment() && isEmpty();
    }
    public boolean hasComment() {
        return !StringsUtil.isEmpty(getComment());
    }

    @Override
    String nodeTypeName() {
        if (isComment()) {
            return JSON_node_type_comment;
        }
        return JSON_node_type_text;
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_node_type, nodeTypeName());
        jsonObject.put(JSON_line, getLineNumber());
        if (isText()) {
            jsonObject.put(JSON_value, getText());
        }
        jsonObject.put(JSON_comment, getComment());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setText(json.optString(JSON_value, null));
        setComment(json.optString(JSON_comment, null));
        setLineNumber(json.optInt(JSON_line));
    }

    @Override
    public String toString() {
        if (isText()) {
            String text = getText();
            return text == null ? "null" : text;
        }
        return "<!--" + getComment() + "-->";
    }
    private static boolean isIndent(String text) {
        if (text == null) {
            return true;
        }
        int length = text.length();
        if (length == 0) {
            return true;
        }
        if (text.charAt(0) != '\n') {
            return false;
        }
        for (int i = 1; i < length; i++) {
            if (!StringsUtil.isWhiteSpace(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
