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

import android.content.res.XmlResourceParser;
import com.reandroid.arsc.value.ValueType;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ResXmlPullParser implements XmlResourceParser {
    private ResXmlDocument mDocument;
    private boolean mDocumentCreatedHere;
    private ResXmlElement mCurrentElement;
    private ResXmlTextNode mCurrentText;
    private int mEvent = -1;


    public ResXmlPullParser(){
    }

    public void setResXmlDocument(ResXmlDocument xmlDocument){
        closeDocument();
        this.mDocument = xmlDocument;
    }
    public ResXmlDocument getResXmlDocument() {
        return mDocument;
    }

    public void closeDocument(){
        mCurrentElement = null;
        mCurrentText = null;
        mEvent = -1;
        destroyDocument();
    }
    private void destroyDocument(){
        if(!mDocumentCreatedHere){
            return;
        }
        mDocumentCreatedHere = false;
        if(this.mDocument == null){
            return;
        }
        this.mDocument.destroy();
        this.mDocument = null;
    }

    @Override
    public void close(){
        closeDocument();
    }
    @Override
    public int getAttributeCount() {
        return mCurrentElement.getAttributeCount();
    }
    @Override
    public String getAttributeName(int index) {
        ResXmlAttribute xmlAttribute = mCurrentElement.getAttributeAt(index);
        if(xmlAttribute!=null){
            return xmlAttribute.getName();
        }
        return null;
    }
    @Override
    public String getAttributeValue(int index) {
        ResXmlAttribute xmlAttribute = geResXmlAttributeAt(index);
        if(xmlAttribute!=null){
            return xmlAttribute.getValueString();
        }
        return null;
    }
    @Override
    public String getAttributeValue(String namespace, String name) {
        ResXmlAttribute attribute = getAttribute(namespace, name);
        if(attribute != null){
            return attribute.getValueString();
        }
        return null;
    }
    @Override
    public String getPositionDescription() {
        return null;
    }
    @Override
    public int getAttributeNameResource(int index) {
        ResXmlAttribute attribute = geResXmlAttributeAt(index);
        if(attribute!=null){
            return attribute.getNameResourceID();
        }
        return 0;
    }
    @Override
    public int getAttributeListValue(String namespace, String attribute, String[] options, int defaultValue) {
        ResXmlAttribute xmlAttribute = getAttribute(namespace, attribute);
        if(xmlAttribute == null){
            return 0;
        }
        List<String> list = Arrays.asList(options);
        int index = list.indexOf(xmlAttribute.getValueString());
        if(index==-1){
            return defaultValue;
        }
        return index;
    }
    @Override
    public boolean getAttributeBooleanValue(String namespace, String attribute, boolean defaultValue) {
        ResXmlAttribute xmlAttribute = getAttribute(namespace, attribute);
        if(xmlAttribute == null || xmlAttribute.getValueType() != ValueType.INT_BOOLEAN){
            return defaultValue;
        }
        return xmlAttribute.getValueAsBoolean();
    }
    @Override
    public int getAttributeResourceValue(String namespace, String attribute, int defaultValue) {
        ResXmlAttribute xmlAttribute = getAttribute(namespace, attribute);
        if(xmlAttribute == null){
            return 0;
        }
        ValueType valueType=xmlAttribute.getValueType();
        if(valueType==ValueType.ATTRIBUTE
                ||valueType==ValueType.REFERENCE
                ||valueType==ValueType.DYNAMIC_ATTRIBUTE
                ||valueType==ValueType.DYNAMIC_REFERENCE){
            return xmlAttribute.getData();
        }
        return defaultValue;
    }
    @Override
    public int getAttributeIntValue(String namespace, String attribute, int defaultValue) {
        ResXmlAttribute xmlAttribute = getAttribute(namespace, attribute);
        if(xmlAttribute == null){
            return 0;
        }
        ValueType valueType=xmlAttribute.getValueType();
        if(valueType==ValueType.INT_DEC
                ||valueType==ValueType.INT_HEX){
            return xmlAttribute.getData();
        }
        return defaultValue;
    }
    @Override
    public int getAttributeUnsignedIntValue(String namespace, String attribute, int defaultValue) {
        ResXmlAttribute xmlAttribute = getAttribute(namespace, attribute);
        if(xmlAttribute == null){
            return 0;
        }
        ValueType valueType=xmlAttribute.getValueType();
        if(valueType==ValueType.INT_DEC){
            return xmlAttribute.getData();
        }
        return defaultValue;
    }
    @Override
    public float getAttributeFloatValue(String namespace, String attribute, float defaultValue) {
        ResXmlAttribute xmlAttribute = getAttribute(namespace, attribute);
        if(xmlAttribute == null){
            return 0;
        }
        ValueType valueType=xmlAttribute.getValueType();
        if(valueType==ValueType.FLOAT){
            return Float.intBitsToFloat(xmlAttribute.getData());
        }
        return defaultValue;
    }

    @Override
    public int getAttributeListValue(int index, String[] options, int defaultValue) {
        ResXmlAttribute xmlAttribute = geResXmlAttributeAt(index);
        if(xmlAttribute == null){
            return 0;
        }
        List<String> list = Arrays.asList(options);
        int i = list.indexOf(xmlAttribute.getValueString());
        if(i==-1){
            return defaultValue;
        }
        return index;
    }
    @Override
    public boolean getAttributeBooleanValue(int index, boolean defaultValue) {
        ResXmlAttribute xmlAttribute = geResXmlAttributeAt(index);
        if(xmlAttribute == null || xmlAttribute.getValueType() != ValueType.INT_BOOLEAN){
            return defaultValue;
        }
        return xmlAttribute.getValueAsBoolean();
    }
    @Override
    public int getAttributeResourceValue(int index, int defaultValue) {
        ResXmlAttribute xmlAttribute = geResXmlAttributeAt(index);
        if(xmlAttribute == null){
            return 0;
        }
        ValueType valueType=xmlAttribute.getValueType();
        if(valueType==ValueType.ATTRIBUTE
                ||valueType==ValueType.REFERENCE
                ||valueType==ValueType.DYNAMIC_ATTRIBUTE
                ||valueType==ValueType.DYNAMIC_REFERENCE){
            return xmlAttribute.getData();
        }
        return defaultValue;
    }
    @Override
    public int getAttributeIntValue(int index, int defaultValue) {
        ResXmlAttribute xmlAttribute = geResXmlAttributeAt(index);
        if(xmlAttribute == null){
            return 0;
        }
        ValueType valueType=xmlAttribute.getValueType();
        if(valueType==ValueType.INT_DEC
                ||valueType==ValueType.INT_HEX){
            return xmlAttribute.getData();
        }
        return defaultValue;
    }
    @Override
    public int getAttributeUnsignedIntValue(int index, int defaultValue) {
        ResXmlAttribute xmlAttribute = geResXmlAttributeAt(index);
        if(xmlAttribute == null){
            return 0;
        }
        ValueType valueType=xmlAttribute.getValueType();
        if(valueType==ValueType.INT_DEC){
            return xmlAttribute.getData();
        }
        return defaultValue;
    }
    @Override
    public float getAttributeFloatValue(int index, float defaultValue) {
        ResXmlAttribute xmlAttribute = geResXmlAttributeAt(index);
        if(xmlAttribute == null){
            return 0;
        }
        ValueType valueType=xmlAttribute.getValueType();
        if(valueType==ValueType.FLOAT){
            return Float.intBitsToFloat(xmlAttribute.getData());
        }
        return defaultValue;
    }

    @Override
    public String getIdAttribute() {
        ResXmlStartElement startElement = getResXmlStartElement();
        if(startElement!=null){
            ResXmlAttribute attribute = startElement.getIdAttribute();
            if(attribute!=null){
                return attribute.getName();
            }
        }
        return null;
    }
    @Override
    public String getClassAttribute() {
        ResXmlStartElement startElement = getResXmlStartElement();
        if(startElement!=null){
            ResXmlAttribute attribute = startElement.getClassAttribute();
            if(attribute!=null){
                return attribute.getName();
            }
        }
        return null;
    }
    @Override
    public int getIdAttributeResourceValue(int defaultValue) {
        ResXmlStartElement startElement = getResXmlStartElement();
        if(startElement!=null){
            ResXmlAttribute attribute = startElement.getIdAttribute();
            if(attribute!=null){
                return attribute.getNameResourceID();
            }
        }
        return 0;
    }
    @Override
    public int getStyleAttribute() {
        ResXmlStartElement startElement = getResXmlStartElement();
        if(startElement!=null){
            ResXmlAttribute attribute = startElement.getStyleAttribute();
            if(attribute!=null){
                return attribute.getNameResourceID();
            }
        }
        return 0;
    }

    @Override
    public void setFeature(String name, boolean state) throws XmlPullParserException {
    }
    @Override
    public boolean getFeature(String name) {
        return false;
    }
    @Override
    public void setProperty(String name, Object value) throws XmlPullParserException {
    }
    @Override
    public Object getProperty(String name) {
        return null;
    }
    @Override
    public void setInput(Reader in) throws XmlPullParserException {
        InputStream inputStream = getFromLock(in);
        if(inputStream == null){
            throw new XmlPullParserException("Can't parse binary xml from reader");
        }
        setInput(inputStream, null);
    }
    @Override
    public void setInput(InputStream inputStream, String inputEncoding) throws XmlPullParserException {
        ResXmlDocument xmlDocument = new ResXmlDocument();
        try {
            xmlDocument.readBytes(inputStream);
        } catch (IOException exception) {
            XmlPullParserException pullParserException = new XmlPullParserException(exception.getMessage());
            pullParserException.initCause(exception);
            throw pullParserException;
        }
        setResXmlDocument(xmlDocument);
        this.mDocumentCreatedHere = true;
    }
    @Override
    public String getInputEncoding() {
        return null;
    }
    @Override
    public void defineEntityReplacementText(String entityName, String replacementText) throws XmlPullParserException {
    }
    @Override
    public int getNamespaceCount(int depth) throws XmlPullParserException {
        ResXmlElement element = getCurrentElement();
        while(element!=null && element.getDepth()>depth){
            element=element.getParentResXmlElement();
        }
        if(element!=null){
            return element.getStartNamespaceList().size();
        }
        return 0;
    }
    @Override
    public String getNamespacePrefix(int pos) throws XmlPullParserException {
        ResXmlAttribute attribute = mCurrentElement.getAttributeAt(pos);
        if(attribute!=null){
            return attribute.getNamePrefix();
        }
        return null;
    }
    @Override
    public String getNamespaceUri(int pos) throws XmlPullParserException {
        ResXmlAttribute attribute = mCurrentElement.getAttributeAt(pos);
        if(attribute!=null){
            return attribute.getUri();
        }
        return null;
    }
    @Override
    public String getNamespace(String prefix) {
        ResXmlStartNamespace startNamespace = mCurrentElement.getStartNamespaceByPrefix(prefix);
        if(startNamespace!=null){
            return startNamespace.getUri();
        }
        return null;
    }
    @Override
    public int getDepth() {
        int event = mEvent;
        if(event == START_TAG || event == END_TAG){
            return mCurrentElement.getDepth();
        }
        if(event == TEXT){
            return mCurrentText.getDepth();
        }
        return 0;
    }
    @Override
    public int getLineNumber() {
        int event = mEvent;
        if(event == START_TAG){
            ResXmlStartElement startElement = mCurrentElement.getStartElement();
            if(startElement!=null){
                return startElement.getLineNumber();
            }
            return 0;
        }
        if(event == END_TAG){
            ResXmlEndElement endElement = mCurrentElement.getEndElement();
            if(endElement!=null){
                return endElement.getLineNumber();
            }
            return 0;
        }
        if(event == TEXT){
            return mCurrentText.getLineNumber();
        }
        return 0;
    }
    @Override
    public int getColumnNumber() {
        return 0;
    }
    @Override
    public boolean isWhitespace() throws XmlPullParserException {
        return false;
    }
    @Override
    public String getText() {
        int event = mEvent;
        if(event == TEXT){
            return mCurrentText.getText();
        }
        if(event == START_TAG || event == END_TAG){
            return mCurrentElement.getTag();
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
        if(element!=null){
            return element.getTagUri();
        }
        return null;
    }
    @Override
    public String getName() {
        ResXmlElement element = getCurrentElement();
        if(element!=null){
            return element.getTag();
        }
        return null;
    }
    @Override
    public String getPrefix() {
        ResXmlElement element = getCurrentElement();
        if(element!=null){
            return element.getTagPrefix();
        }
        return null;
    }
    @Override
    public boolean isEmptyElementTag() throws XmlPullParserException {
        ResXmlElement element = getCurrentElement();
        if(element!=null){
            return element.countResXmlNodes() == 0 && element.getAttributeCount()==0;
        }
        return false;
    }
    @Override
    public String getAttributeNamespace(int index) {
        ResXmlAttribute attribute = geResXmlAttributeAt(index);
        if(attribute != null){
            return attribute.getUri();
        }
        return null;
    }
    @Override
    public String getAttributePrefix(int index) {
        ResXmlAttribute attribute = geResXmlAttributeAt(index);
        if(attribute != null){
            return attribute.getNamePrefix();
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
    private ResXmlAttribute geResXmlAttributeAt(int index){
        ResXmlElement element = getCurrentElement();
        if(element == null){
            return null;
        }
        return element.getAttributeAt(index);
    }
    private ResXmlAttribute getAttribute(String namespace, String name) {
        ResXmlElement element = getCurrentElement();
        if(element == null){
            return null;
        }
        for(ResXmlAttribute attribute:element.listAttributes()){
            if(Objects.equals(namespace, attribute.getUri())
                    && Objects.equals(name, attribute.getName())){
                return attribute;
            }
        }
        return null;
    }
    private ResXmlStartElement getResXmlStartElement(){
        ResXmlElement element = getCurrentElement();
        if(element!=null){
            return element.getStartElement();
        }
        return null;
    }
    private ResXmlElement getCurrentElement() {
        int event = mEvent;
        if(event!=START_TAG && event!=END_TAG){
            return null;
        }
        return mCurrentElement;
    }
    @Override
    public int getEventType() throws XmlPullParserException {
        return mEvent;
    }
    @Override
    public int next() throws XmlPullParserException, IOException {
        checkNotEnded();
        int event = calculateNextEvent(mEvent);
        if(event == START_DOCUMENT){
            onStartDocument();
        }else if(event == END_DOCUMENT){
            onEndDocument();
        }else if(event == START_TAG){
            onStartTag();
        }else if(event == END_TAG){
            onEndTag();
        }else if(event == TEXT){
            onText();
        }
        this.mEvent = event;
        return event;
    }
    private void onEndTag() throws XmlPullParserException {
        int previous = mEvent;
        if(previous == END_TAG){
            mCurrentElement = mCurrentElement.getParentResXmlElement();
        }
        mCurrentText = null;
    }
    private void onText() throws XmlPullParserException {
        int previous = mEvent;
        if(previous == END_TAG){
            int position = mCurrentElement.getIndex();
            ResXmlElement parent = mCurrentElement.getParentResXmlElement();
            position++;
            mCurrentText = (ResXmlTextNode) parent.getResXmlNode(position);
            mCurrentElement = parent;
        }else if(previous == START_TAG){
            ResXmlElement parent = mCurrentElement;
            mCurrentText = (ResXmlTextNode) parent.getResXmlNode(0);
        }else if(previous == TEXT){
            int position = mCurrentText.getIndex();
            ResXmlElement parent = mCurrentElement;
            position++;
            mCurrentText = (ResXmlTextNode) parent.getResXmlNode(position);
            mCurrentText = (ResXmlTextNode) parent.getResXmlNode(0);
        }else {
            throw new XmlPullParserException("Unknown state at onText() prev="+previous);
        }
    }
    private void onStartTag() throws XmlPullParserException {
        int previous = mEvent;
        if(previous == START_DOCUMENT){
            mCurrentElement = mDocument.getResXmlElement();
            mCurrentText = null;
        }else if(previous == END_TAG){
            int position = mCurrentElement.getIndex();
            ResXmlElement parent = mCurrentElement.getParentResXmlElement();
            position++;
            mCurrentElement = (ResXmlElement) parent.getResXmlNode(position);
        }else if(previous == TEXT){
            int position = mCurrentText.getIndex();
            ResXmlElement parent = mCurrentText.getResXmlText().getParentResXmlElement();
            position++;
            mCurrentElement = (ResXmlElement) parent.getResXmlNode(position);
        }else if(previous == START_TAG){
            mCurrentElement = (ResXmlElement) mCurrentElement.getResXmlNode(0);
        }else {
            throw new XmlPullParserException("Unknown state at onStartTag() prev="+previous);
        }
        mCurrentText = null;

    }
    private void onStartDocument(){
    }
    private void onEndDocument() throws XmlPullParserException {
        mCurrentElement = null;
        mCurrentText = null;
        close();
    }
    private void checkNotEnded() throws XmlPullParserException {
        if(mEvent == END_DOCUMENT){
            throw new XmlPullParserException("Document reached to end");
        }
    }
    private int calculateNextEvent(int previous) throws XmlPullParserException {
        if(previous < 0){
            if(mDocument == null){
                return previous;
            }
            return START_DOCUMENT;
        }
        if(previous == START_DOCUMENT){
            ResXmlElement element = mDocument.getResXmlElement();
            if(element==null){
                return END_DOCUMENT;
            }
            return START_TAG;
        }
        if(previous == END_DOCUMENT){
            return END_DOCUMENT;
        }
        if(previous == START_TAG){
            ResXmlElement element = mCurrentElement;
            ResXmlNode firstChild = element.getResXmlNode(0);
            if(firstChild == null){
                return END_TAG;
            }
            if(firstChild instanceof ResXmlTextNode){
                return TEXT;
            }
            return START_TAG;
        }
        if(previous == END_TAG || previous==TEXT){
            ResXmlElement element = mCurrentElement;
            ResXmlElement parent = element.getParentResXmlElement();
            if(parent == null){
                return END_DOCUMENT;
            }
            int position = element.getIndex() + 1;
            ResXmlNode nextNode = parent.getResXmlNode(position);
            if(nextNode==null){
                return END_TAG;
            }
            if(nextNode instanceof ResXmlTextNode){
                return TEXT;
            }
            return START_TAG;
        }
        throw new XmlPullParserException("Unknown state at calculateNextEvent() prev="+previous);
    }
    @Override
    public int nextToken() throws XmlPullParserException, IOException {
        return next();
    }
    @Override
    public void require(int type, String namespace, String name) throws XmlPullParserException, IOException {
    }
    @Override
    public String nextText() throws XmlPullParserException, IOException {
        return null;
    }
    @Override
    public int nextTag() throws XmlPullParserException, IOException {
        return 0;
    }

    private static InputStream getFromLock(Reader reader){
        try{
            Field field = Reader.class.getDeclaredField("lock");
            field.setAccessible(true);
            Object obj = field.get(reader);
            if(obj instanceof InputStream){
                return (InputStream) obj;
            }
        }catch (Throwable ignored){
        }
        return null;
    }

    /**
     * This non-final re-declaration is to force compiler from using literal int value on this class
     * */


}
