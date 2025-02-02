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
import com.reandroid.arsc.value.ValueType;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.xml.XMLUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;

public class ResXmlEventParser implements XmlPullParser {

    private final Iterator<ResXmlEvent> eventIterator;
    private ResXmlEvent mCurrent;
    private boolean mFirstPulled;
    private boolean mFinished;

    private boolean processNamespaces;
    private boolean reportNamespaceAttrs;

    private Object location;

    public ResXmlEventParser(Iterator<ResXmlEvent> eventIterator) {
        this.eventIterator = eventIterator;

        this.processNamespaces = true;
        this.reportNamespaceAttrs = true;
    }

    public ResXmlEvent getCurrent() {
        if (mFinished) {
            return null;
        }
        if (!mFirstPulled) {
            try {
                nextParserEvent();
            } catch (Throwable ignored) {
            }
        }
        return mCurrent;
    }
    private void nextParserEvent() throws XmlPullParserException {
        mFirstPulled = true;
        if (!mFinished && eventIterator.hasNext()) {
            mCurrent = eventIterator.next();
        } else {
            if (mFinished) {
                throw new XmlPullParserException("Finished", this, null);
            }
            mFinished = true;
        }
    }
    private ResXmlNode getXmlNode() {
        ResXmlEvent event = getCurrent();
        if (event != null) {
            return event.getXmlNode();
        }
        return null;
    }
    public ResXmlElement getCurrentElement() {
        ResXmlNode xmlNode = getXmlNode();
        if (xmlNode instanceof ResXmlElement) {
            return (ResXmlElement) xmlNode;
        }
        return ObjectsUtil.getNull();
    }
    public ResXmlDocument getDocument() {
        ResXmlNode xmlNode = getXmlNode();
        if (xmlNode instanceof ResXmlDocument) {
            return (ResXmlDocument) xmlNode;
        }
        if (xmlNode instanceof ResXmlElement) {
            return ((ResXmlElement) xmlNode).getParentDocument();
        }
        if (xmlNode instanceof ResXmlTextNode) {
            ResXmlNodeTree parent = ((ResXmlTextNode) xmlNode).getParentNode();
            if (parent instanceof ResXmlDocument) {
                return ((ResXmlDocument) parent);
            }
            if (parent instanceof ResXmlElement) {
                return ((ResXmlElement) parent).getParentDocument();
            }
        }
        return ObjectsUtil.getNull();
    }
    private boolean isCountNamespacesAsAttribute(){
        return isProcessNamespaces() & isReportNamespaceAttrs();
    }

    public boolean isProcessNamespaces() {
        return processNamespaces;
    }
    public boolean isReportNamespaceAttrs() {
        return reportNamespaceAttrs;
    }

    private int getRealAttributeIndex(int index){
        if(isCountNamespacesAsAttribute()){
            index = index - getNamespaceCountInternal();
        }
        return index;
    }
    private int getNamespaceCountInternal(){
        ResXmlElement element = getCurrentElement();
        if(element != null){
            return element.getNamespaceCount();
        }
        return 0;
    }
    private String getNamespaceAttributeName(int index) {
        ResXmlNamespace namespace = getCurrentElement()
                .getNamespaceAt(index);
        return "xmlns:" + namespace.getPrefix();
    }
    private String decodeAttributeName(ResXmlAttribute attribute){
        if (attribute != null) {
            if (attribute.getPackageBlock() == null) {
                return attribute.getName(isProcessNamespaces());
            }
            return attribute.decodeName(isProcessNamespaces());
        }
        return null;
    }
    public ResXmlAttribute getResXmlAttributeAt(int index) {
        index = getRealAttributeIndex(index);
        ResXmlElement element = getCurrentElement();
        if (element == null) {
            return null;
        }
        return element.getAttributeAt(index);
    }
    private String decodeAttributeValue(ResXmlAttribute attribute) {
        if(attribute != null) {
            String value = attribute.decodeValue();
            if (attribute.getValueType() == ValueType.STRING) {
                value = XmlSanitizer.escapeSpecialCharacter(value);
            }
            return value;
        }
        return null;
    }
    public ResXmlAttribute getAttribute(String namespace, String name) {
        ResXmlElement element = getCurrentElement();
        if(element != null){
            return element.searchAttribute(namespace, name);
        }
        return null;
    }
    private String getNamespaceAttributeValue(int index){
        ResXmlNamespace namespace = getCurrentElement()
                .getNamespaceAt(index);
        return namespace.getUri();
    }

    @Override
    public void setFeature(String name, boolean state) throws XmlPullParserException {
        if (FEATURE_PROCESS_NAMESPACES.equals(name)) {
            processNamespaces = state;
        } else if (FEATURE_REPORT_NAMESPACE_ATTRIBUTES.equals(name)) {
            reportNamespaceAttrs = state;
        } else {
            throw new XmlPullParserException("Unsupported feature: " + name);
        }
    }
    @Override
    public boolean getFeature(String name) {
        if (FEATURE_PROCESS_NAMESPACES.equals(name)) {
            return processNamespaces;
        } else if(FEATURE_REPORT_NAMESPACE_ATTRIBUTES.equals(name)) {
            return reportNamespaceAttrs;
        }
        return false;
    }

    @Override
    public void setProperty(String name, Object value) throws XmlPullParserException {
        if (XMLUtil.PROPERTY_LOCATION.equals(name)) {
            location = value;
        } else {
            throw new XmlPullParserException("unsupported property: " + name);
        }
    }

    @Override
    public Object getProperty(String name) {
        if (XMLUtil.PROPERTY_LOCATION.equals(name)) {
            return location;
        }
        return null;
    }

    @Override
    public void setInput(Reader in) throws XmlPullParserException {

    }

    @Override
    public void setInput(InputStream inputStream, String inputEncoding) throws XmlPullParserException {

    }

    @Override
    public String getInputEncoding() {
        ResXmlDocument document = getDocument();
        if (document != null) {
            return document.getEncoding();
        }
        return null;
    }

    @Override
    public void defineEntityReplacementText(String entityName, String replacementText) {

    }

    @Override
    public int getNamespaceCount(int depth) throws XmlPullParserException {
        if (isCountNamespacesAsAttribute()) {
            return 0;
        }
        ResXmlElement element = getCurrentElement();
        while(element != null && element.getDepth() > depth) {
            element=element.getParentElement();
        }
        if (element != null) {
            return element.getNamespaceCount();
        }
        return 0;
    }

    @Override
    public String getNamespacePrefix(int pos) throws XmlPullParserException {
        ResXmlElement element = getCurrentElement();
        if (element != null) {
            return element.getNamespaceAt(pos).getPrefix();
        }
        return null;
    }

    @Override
    public String getNamespaceUri(int pos) {
        ResXmlElement element = getCurrentElement();
        if (element != null) {
            return element.getNamespaceAt(pos).getUri();
        }
        return null;
    }

    @Override
    public String getNamespace(String prefix) {
        ResXmlElement element = getCurrentElement();
        if(element != null){
            ResXmlNamespace namespace = element.getNamespaceForPrefix(prefix);
            if(namespace != null){
                return namespace.getUri();
            }
        }
        return null;
    }

    @Override
    public int getDepth() {
        ResXmlEvent xmlEvent = getCurrent();
        if (xmlEvent != null) {
            return xmlEvent.getDepth();
        }
        return 0;
    }

    @Override
    public String getPositionDescription() {
        StringBuilder builder = new StringBuilder();
        Object location = XMLUtil.getLocation(this);
        if (location != null) {
            builder.append(" at ");
            builder.append(location);
        }
        builder.append(" Binary XML file line #");
        builder.append(this.getLineNumber());
        ResXmlElement element = getCurrentElement();
        if(element != null) {
            if (getCurrent().getType() == START_TAG) {
                builder.append(" START_TAG ");
            } else {
                builder.append(" END_TAG ");
            }
            builder.append('<');
            builder.append(element.getName(true));
            builder.append('>');
        }
        return builder.toString();
    }

    @Override
    public int getLineNumber() {
        ResXmlEvent xmlEvent = getCurrent();
        if (xmlEvent != null) {
            return xmlEvent.getLineNumber();
        }
        return 0;
    }

    @Override
    public int getColumnNumber() {
        return 0;
    }

    @Override
    public boolean isWhitespace() throws XmlPullParserException {
        ResXmlNode xmlNode = getXmlNode();
        if (xmlNode instanceof ResXmlTextNode) {
            return ((ResXmlTextNode) xmlNode).isBlank();
        }
        return false;
    }

    @Override
    public String getText() {
        ResXmlEvent xmlEvent = getCurrent();
        if (xmlEvent != null) {
            return xmlEvent.getText();
        }
        return null;
    }

    @Override
    public char[] getTextCharacters(int[] holderForStartAndLength) {
        String text = getText();
        if (text == null) {
            holderForStartAndLength[0] = -1;
            holderForStartAndLength[1] = -1;
            return null;
        }
        char[] result = text.toCharArray();
        holderForStartAndLength[0] = 0;
        holderForStartAndLength[1] = result.length;
        return result;
    }

    @Override
    public String getNamespace() {
        ResXmlElement element = getCurrentElement();
        if(element != null) {
            return element.getUri();
        }
        return null;
    }
    @Override
    public String getName() {
        ResXmlElement element = getCurrentElement();
        if (element != null) {
            return element.getName();
        }
        return null;
    }
    @Override
    public String getPrefix() {
        ResXmlElement element = getCurrentElement();
        if (element != null) {
            return element.getPrefix();
        }
        return null;
    }
    @Override
    public boolean isEmptyElementTag() {
        ResXmlElement element = getCurrentElement();
        if (element != null) {
            return element.size() == 0 && element.getAttributeCount()==0;
        }
        return true;
    }

    @Override
    public int getAttributeCount() {
        int count = 0;
        ResXmlElement element = getCurrentElement();
        if(element != null) {
            count = element.getAttributeCount();
            if(isCountNamespacesAsAttribute()) {
                count += element.getNamespaceCount();
            }
        }
        return count;
    }

    @Override
    public String getAttributeNamespace(int index) {
        if (!isProcessNamespaces()) {
            ResXmlAttribute attribute = getResXmlAttributeAt(index);
            if(attribute != null) {
                return attribute.getUri();
            }
        }
        return null;
    }

    @Override
    public String getAttributeName(int index) {
        if(isCountNamespacesAsAttribute()){
            int nsCount = getNamespaceCountInternal();
            if(index < nsCount){
                return getNamespaceAttributeName(index);
            }
        }
        return decodeAttributeName(getResXmlAttributeAt(index));
    }

    @Override
    public String getAttributePrefix(int index) {
        if (!isProcessNamespaces()) {
            ResXmlAttribute attribute = getResXmlAttributeAt(index);
            if (attribute != null) {
                return attribute.getPrefix();
            }
        }
        return null;
    }

    @Override
    public String getAttributeType(int index) {
        return "CDATA";
    }

    @Override
    public boolean isAttributeDefault(int index) {
        return false;
    }

    @Override
    public String getAttributeValue(int index) {
        if(isCountNamespacesAsAttribute()){
            int nsCount = getNamespaceCountInternal();
            if (index < nsCount) {
                return getNamespaceAttributeValue(index);
            }
        }
        return decodeAttributeValue(getResXmlAttributeAt(index));
    }

    @Override
    public String getAttributeValue(String namespace, String name) {
        return decodeAttributeValue(getAttribute(namespace, name));
    }

    @Override
    public int getEventType() throws XmlPullParserException {
        ResXmlEvent resXmlEvent = getCurrent();
        if (resXmlEvent != null) {
            return resXmlEvent.getType();
        }
        return -1;
    }

    @Override
    public int next() throws XmlPullParserException, IOException {
        nextParserEvent();
        return getEventType();
    }

    @Override
    public int nextToken() throws XmlPullParserException, IOException {
        nextParserEvent();
        return getEventType();
    }

    @Override
    public void require(int type, String namespace, String name) throws XmlPullParserException, IOException {
        if (type != this.getEventType()
                || (namespace != null && !namespace.equals(getNamespace()))
                || (name != null && !name.equals(getName()))) {
            throw new XmlPullParserException(
                    "expected: " + TYPES[type] + " {" + namespace + "}" + name, this, null);
        }
    }

    @Override
    public String nextText() throws XmlPullParserException, IOException {
        int event = getEventType();
        if (event != START_TAG) {
            throw new XmlPullParserException("precondition: START_TAG", this, null);
        }
        while (event != TEXT && event!=END_TAG && event!=END_DOCUMENT) {
            event = next();
        }
        if (event == TEXT) {
            return getText();
        }
        return "";
    }

    @Override
    public int nextTag() throws XmlPullParserException, IOException {
        int event = getEventType();
        if (event != START_TAG) {
            throw new XmlPullParserException("precondition: START_TAG", this, null);
        }
        event = next();
        while (event != START_TAG && event != END_DOCUMENT){
            event = next();
        }
        return event;
    }
}
