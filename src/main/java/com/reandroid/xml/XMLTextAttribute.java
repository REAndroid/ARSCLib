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

public class XMLTextAttribute extends XMLAttribute {
    private String mText;
    public XMLTextAttribute(){
        super(null,null);
    }
    public void setText(String txt){
        mText=txt;
    }
    public String getText(){
        return mText;
    }
    public String getText(boolean unEscape){
        if(unEscape){
            return XMLUtil.unEscapeXmlChars(mText);
        }
        if(mText!=null){
            String junk= XMLUtil.unEscapeXmlChars(mText);
            if(!mText.equals(junk)){
                junk.trim();
            }
        }
        return mText;
    }
    @Override
    public XMLTextAttribute cloneAttr(){
        XMLTextAttribute textAttribute=new XMLTextAttribute();
        textAttribute.setText(getText());
        textAttribute.setValueId(getValueId());
        return textAttribute;
    }
    @Override
    public String getValue(){
        return getText();
    }
    @Override
    public void setValue(String val){
        setText(val);
    }
    @Override
    public boolean equals(Object obj){
        if(obj instanceof XMLTextAttribute){
            XMLTextAttribute attr=(XMLTextAttribute)obj;
            String s=getText();
            if(s==null){
                return attr.getText()==null;
            }
            return s.equals(attr.getText());
        }
        if(obj instanceof String){
            String s2=(String)obj;
            return s2.equals(getText());
        }
        return false;
    }
    @Override
    public boolean isEmpty(){
        //return XMLUtil.isEmpty(getText());
        return getText()==null;
    }
    @Override
    public boolean write(Writer writer) throws IOException {
        if(isEmpty()){
            return false;
        }
        writer.append(getText());
        return true;
    }
    @Override
    public String toString(){
        return getText();
    }
}
