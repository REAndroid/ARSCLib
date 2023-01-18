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

public class XMLText extends XMLNode{
    private String text;
    public XMLText(String text){
        this.text=XMLUtil.escapeXmlChars(text);
    }
    public XMLText(){
        this(null);
    }

    @Override
    public void addChildNode(XMLNode xmlNode){
        throw new IllegalArgumentException("Can not add xml node on text: "+xmlNode);
    }
    public String getText(){
        return getText(true);
    }
    public String getText(boolean unEscape){
        if(unEscape){
            return XMLUtil.unEscapeXmlChars(text);
        }
        return text;
    }
    public void setText(String text){
        this.text=XMLUtil.escapeXmlChars(text);
    }
    @Override
    void buildTextContent(Writer writer, boolean unEscape) throws IOException{
        writer.write(getText(unEscape));
    }
    @Override
    public boolean write(Writer writer, boolean newLineAttributes) throws IOException {
        if(!XMLUtil.isEmpty(this.text)){
            writer.write(this.text);
            return true;
        }
        return false;
    }
    @Override
    public String toText(int indent, boolean newLineAttributes) {
        return getText(false);
    }
    @Override
    public String toString(){
        return getText();
    }
}
