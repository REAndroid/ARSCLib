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
package com.reandroid.xml.parser;

import com.reandroid.xml.*;

import java.io.IOException;
import java.io.StringReader;

public class XMLSpanParser {
    private final Object mLock = new Object();
    private final XmlPullParser mParser;
    private XMLElement mCurrentElement;
    public XMLSpanParser(){
        this.mParser = new MXParserNonValidating();
    }
    public XMLElement parse(String text) throws XMLException {
        synchronized (mLock){
            try {
                text="<spannable-parser>"+text+"</spannable-parser>";
                parseString(text);
            } catch (XmlPullParserException|IOException ex) {
                throw new XMLException(ex.getMessage());
            }
            XMLElement element=mCurrentElement;
            mCurrentElement=null;
            return element;
        }
    }
    private void parseString(String text) throws XmlPullParserException, IOException {
        mCurrentElement=null;
        StringReader reader=new StringReader(text);
        this.mParser.setInput(reader);
        int type;
        while ((type=mParser.next()) !=XmlPullParser.END_DOCUMENT){
            event(type);
        }
    }
    private void event(int type) {
        if (type == XmlPullParser.START_DOCUMENT){
            onStartDocument();
        }else if (type == XmlPullParser.START_TAG){
            onStartTag();
        }else if (type == XmlPullParser.END_TAG){
            onEndTag();
        }else if (type == XmlPullParser.TEXT){
            onText();
        }else if (type == XmlPullParser.ENTITY_REF){
            onEntityRef();
        }else if (type == XmlPullParser.IGNORABLE_WHITESPACE){
            onText();
        }
    }


    private void loadAttributes(){
        int max=mParser.getAttributeCount();
        for(int i=0; i<max; i++){
            onAttribute(i);
        }
    }
    private void onAttribute(int i){
        String attrName=mParser.getAttributeName(i);
        String attrValue=mParser.getAttributeValue(i);
        mCurrentElement.setAttribute(attrName, attrValue);
    }

    private void onStartTag() {
        String name=mParser.getName();
        if(mCurrentElement==null){
            mCurrentElement=new XMLElement(name);
        }else {
            mCurrentElement=mCurrentElement.createElement(name);
        }
        loadAttributes();
    }
    private void onEndTag() {
        XMLElement parent=mCurrentElement.getParent();
        if(parent!=null){
            mCurrentElement=parent;
        }
    }
    private void onText() {
        String text=mParser.getText();
        if(text!=null && text.length()>0){
            mCurrentElement.addText(new XMLText(text));
        }
    }
    private void onEntityRef() {
        String text = getEntity(mParser.getName());
        mCurrentElement.addText(new XMLText(text));
    }
    private String getEntity(String name){
        if("amp".equals(name)){
            return "&";
        }
        if("lt".equals(name)){
            return "<";
        }
        if("gt".equals(name)){
            return ">";
        }
        return name;
    }
    private void onStartDocument() {
        this.mCurrentElement=null;
    }
}
