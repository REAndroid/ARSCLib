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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public abstract class XMLNode {
    private int mLineNumber;
    private int mColumnNumber;
    private final List<XMLNode> mChildNodes = new ArrayList<>();

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

    public void addChildNode(XMLNode xmlNode){
        boolean addOk=addChildNodeInternal(xmlNode);
        if(addOk){
            onChildAdded(xmlNode);
        }
    }
    boolean addChildNodeInternal(XMLNode xmlNode){
        if(xmlNode!=null && canAdd(xmlNode)){
            return mChildNodes.add(xmlNode);
        }
        return false;
    }
    void onChildAdded(XMLNode xmlNode){

    }
    boolean canAdd(XMLNode xmlNode){
        return xmlNode!=null;
    }
    boolean contains(XMLNode xmlNode){
        return mChildNodes.contains(xmlNode);
    }
    void removeChildNode(XMLNode xmlNode){
        int i = mChildNodes.indexOf(xmlNode);
        while (i>=0){
            i = mChildNodes.indexOf(xmlNode);
        }
        mChildNodes.remove(xmlNode);
    }
    public void clearChildNodes(){
        clearChildNodesInternal();
    }
    void clearChildNodesInternal(){
        mChildNodes.clear();
    }
    public List<XMLNode> getChildNodes() {
        return mChildNodes;
    }
    boolean hasChildNodes(){
        return mChildNodes.size()>0;
    }
    void buildTextContent(Writer writer, boolean unEscape) throws IOException{

    }
    public boolean write(Writer writer) throws IOException {
        return write(writer, false);
    }
    public String toText(){
        return toText(1, false);
    }
    public String toText(boolean newLineAttributes){
        return toText(1, newLineAttributes);
    }
    public abstract boolean write(Writer writer, boolean newLineAttributes) throws IOException;
    public abstract String toText(int indent, boolean newLineAttributes);
}
