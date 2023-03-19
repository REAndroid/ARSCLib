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
import java.io.StringWriter;
import java.io.Writer;

public class XMLAttribute extends XMLNode{
    private int mNameId;
    private int mValueId;
    private String mName;
    private String mValue;
    private XMLElement mParent;
    public XMLAttribute(String name, String val){
        mName=name;
        mValue= XMLUtil.escapeXmlChars(val);
    }
    public XMLElement getParent(){
        return mParent;
    }
    void setParent(XMLElement parent){
        this.mParent = parent;
    }
    public void setNameId(int id){
        mNameId=id;
    }
    public void setValueId(int id){
        mValueId=id;
    }
    public int getNameId(){
        return mNameId;
    }
    public int getValueId(){
        return mValueId;
    }
    public XMLAttribute cloneAttr(){
        XMLAttribute baseAttr=new XMLAttribute(getName(),getValue());
        baseAttr.setNameId(getNameId());
        baseAttr.setValueId(getValueId());
        return baseAttr;
    }
    public String getName(){
        return mName;
    }
    public String getNamePrefix(){
        int i=mName.indexOf(":");
        if(i>0){
            return mName.substring(0,i);
        }
        return null;
    }
    public String getNameWoPrefix(){
        int i=mName.indexOf(":");
        if(i>0){
            return mName.substring(i+1);
        }
        return mName;
    }
    public String getValue(){
        if(mValue==null){
            mValue="";
        }
        return mValue;
    }
    public int getValueInt(){
        long l=Long.decode(getValue());
        return (int)l;
    }
    public boolean getValueBool(){
        String str=getValue().toLowerCase();
        if("true".equals(str)){
            return true;
        }
        return false;
    }
    public boolean isValueBool(){
        String str=getValue().toLowerCase();
        if("true".equals(str)){
            return true;
        }
        return "false".equals(str);
    }
    public void setName(String name){
        mName=name;
    }
    public void setValue(String val){
        mValue= XMLUtil.escapeXmlChars(val);
    }

    @Override
    public boolean write(Writer writer, boolean newLineAttributes) throws IOException {
        writer.write(getName());
        writer.write("=\"");
        String val= XMLUtil.trimQuote(getValue());
        val= XMLUtil.escapeXmlChars(val);
        val= XMLUtil.escapeQuote(val);
        writer.write(val);
        writer.write('"');
        return true;
    }
    @Override
    public String toText(int indent, boolean newLineAttributes) {
        StringWriter writer=new StringWriter();
        try {
            write(writer);
        } catch (IOException ignored) {
        }
        writer.flush();
        return writer.toString();
    }
    @Override
    public int hashCode(){
        String name=getName();
        if(name==null){
            name="";
        }
        return name.hashCode();
    }
    @Override
    public boolean equals(Object obj){
        if(obj instanceof XMLAttribute){
            XMLAttribute attr=(XMLAttribute)obj;
            return getName().equals(attr.getName());
        }
        return false;
    }
    @Override
    public String toString(){
        return toText();
    }
}
