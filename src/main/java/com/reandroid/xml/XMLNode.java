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
    private final List<XMLNode> mChildNodes = new ArrayList<>();

    void addChildNode(XMLNode xmlNode){
        if(xmlNode!=null && canAdd(xmlNode)){
            mChildNodes.add(xmlNode);
        }
    }
    boolean canAdd(XMLNode xmlNode){
        return !mChildNodes.contains(xmlNode);
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
    public List<XMLNode> getChildNodes() {
        return mChildNodes;
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
