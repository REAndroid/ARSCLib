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
package com.reandroid.apk.xmldecoder;

import com.reandroid.xml.XMLComment;
import com.reandroid.xml.XMLElement;

import java.io.IOException;

public class EntryWriterElement implements EntryWriter<XMLElement> {
    private XMLElement mCurrentElement;
    private XMLElement mResult;
    private boolean mEnableIndent;

    public EntryWriterElement(){
    }

    public XMLElement getElement() {
        return mResult;
    }
    @Override
    public void setFeature(String name, Object value) {
        if(!FEATURE_INDENT.equals(name)){
            return;
        }
        boolean state = false;
        if(value instanceof Boolean){
            state = (Boolean)value;
        }
        mEnableIndent = state;
    }
    @Override
    public XMLElement startTag(String name) throws IOException {
        XMLElement xmlElement = new XMLElement(name);
        XMLElement current = mCurrentElement;
        if(current != null){
            current.addChild(xmlElement);
        }else {
            mResult = null;
        }
        mCurrentElement = xmlElement;
        if(mEnableIndent){
            xmlElement.setIndent(2);
            xmlElement.setIndentScale(1.0f);
        }else {
            xmlElement.setIndent(0);
            xmlElement.setIndentScale(0.0f);
        }
        return xmlElement;
    }
    @Override
    public XMLElement endTag(String name) throws IOException {
        XMLElement current = mCurrentElement;
        if(current == null){
            throw new IOException("endTag called before startTag");
        }
        if(!name.equals(current.getTagName())){
            throw new IOException("Mismatch endTag = "
                    + name + ", expect = " + current.getTagName());
        }
        XMLElement parent = current.getParent();
        if(parent == null){
            mResult = current;
        }else {
            current = parent;
        }
        mCurrentElement = parent;
        return current;
    }
    @Override
    public XMLElement attribute(String name, String value) {
        mCurrentElement.setAttribute(name, value);
        return mCurrentElement;
    }
    @Override
    public XMLElement text(String text) throws IOException {
        mCurrentElement.setTextContent(text, false);
        return mCurrentElement;
    }
    @Override
    public void comment(String comment) throws IOException {
        if(comment != null){
            mCurrentElement.addComment(new XMLComment(comment));
        }
    }
    @Override
    public void flush() throws IOException {
    }
    @Override
    public void enableIndent(boolean enable){
        setFeature(FEATURE_INDENT, enable);
    }

    private static final String FEATURE_INDENT = "http://xmlpull.org/v1/doc/features.html#indent-output";
}
