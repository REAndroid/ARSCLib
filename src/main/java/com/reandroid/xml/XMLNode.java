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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public abstract class XMLNode {
    private XMLNode mParent;
    private int mLineNumber;
    private int mColumnNumber;
    public XMLNode(){

    }
    int getLength(){
        return 0;
    }
    abstract XMLNode clone(XMLNode parent);
    public XMLNode getParent(){
        return mParent;
    }
    void setParent(XMLNode parent){
        if(parent != this){
            this.mParent = parent;
        }
    }

    public int getColumnNumber() {
        return mColumnNumber;
    }
    public void setColumnNumber(int columnNumber) {
        this.mColumnNumber = columnNumber;
    }
    public int getLineNumber() {
        return mLineNumber;
    }
    public void setLineNumber(int lineNumber) {
        this.mLineNumber = lineNumber;
    }

    public abstract void serialize(XmlSerializer serializer) throws IOException;
    public void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
    }
    public String toText(){
        return toText(1, false);
    }
    public String toText(boolean newLineAttributes){
        return toText(1, newLineAttributes);
    }
    void write(Appendable writer, boolean xml) throws IOException{
        write(writer);
    }
    abstract void write(Appendable appendable) throws IOException;
    public String toText(int indent, boolean newLineAttributes){
        StringWriter writer = new StringWriter();
        try {
            write(writer);
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
        }
        return writer.toString();
    }
    @Override
    public String toString(){
        StringWriter writer = new StringWriter();
        try {
            write(writer, false);
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
        }
        return writer.toString();
    }
}
