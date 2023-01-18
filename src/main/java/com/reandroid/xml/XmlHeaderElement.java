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

 public class XmlHeaderElement extends XMLElement {
    private static final String ATTR_VERSION="version";
    private static final String ATTR_ENCODING="encoding";
    private static final String ATTR_STANDALONE="standalone";
    public XmlHeaderElement(XmlHeaderElement element){
        this();
        copyAll(element);
    }
    public XmlHeaderElement(){
        super();
        initializeStartEnd();
        setDefaultAttr();
    }
    private void copyAll(XmlHeaderElement element){
        if(element==null){
            return;
        }
        for(XMLAttribute exist : element.listAttributes()){
            setAttribute(exist.getName(), exist.getValue());
        }
    }
    private void initializeStartEnd(){
        setTagName("xml");
        setStart("<?");
        setEnd("?>");
        setStartPrefix("");
        setEndPrefix("");
    }
    private void setDefaultAttr(){
        setVersion("1.0");
        setEncoding("utf-8");
        setStandalone(null);
    }
    public Object getProperty(String name){
        XMLAttribute attr=getAttribute(name);
        if(attr==null){
            return null;
        }
        String val=attr.getValue();
        if(ATTR_STANDALONE.equalsIgnoreCase(name)){
            boolean res=false;
            if("true".equals(val)){
                res=true;
            }
            return res;
        }
        return val;
    }
    public void setProperty(String name, Object o){
        if(ATTR_STANDALONE.equalsIgnoreCase(name)){
            if(o instanceof Boolean){
                setStandalone((Boolean)o);
                return;
            }
        }
        String val=null;
        if(o!=null){
            val=o.toString();
        }
        setAttribute(name, val);
    }
    public void setVersion(String version){
        setAttribute(ATTR_VERSION, version);
    }
    public void setEncoding(String encoding){
        setAttribute(ATTR_ENCODING, encoding);
    }
    public void setStandalone(Boolean flag){
        if(flag==null){
            removeAttribute(ATTR_STANDALONE);
            return;
        }
        String str=flag?"yes":"no";
        setAttribute(ATTR_STANDALONE, str);
    }
    @Override
    int getChildIndent(){
        return 0;
    }
    @Override
    int getIndent(){
        return 0;
    }
    @Override
    void buildTextContent(Writer writer, boolean unEscape) throws IOException {

    }
}
