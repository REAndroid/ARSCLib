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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SchemaAttr extends XMLAttribute {
    private static final String DEFAULT_XMLNS="xmlns";
    private String mXmlns;
    private String mPrefix;
    public SchemaAttr(String prefix, String uri) {
        this(DEFAULT_XMLNS, prefix, uri);
    }
    public SchemaAttr(String xmlns, String prefix, String uri) {
        super(prefix, uri);
        this.set(xmlns, prefix, uri);
    }
    private void set(String xmlns, String prefix, String uri){
        setXmlns(xmlns);
        if(XMLUtil.isEmpty(prefix)){
            prefix=null;
        }
        setName(prefix);
        setUri(uri);
    }
    @Override
    public void setName(String fullName){
        if(fullName==null){
            setPrefix(null);
            return;
        }
        int i=fullName.indexOf(':');
        if(i>0 && i<fullName.length()){
            mXmlns=fullName.substring(0, i);
            mPrefix=fullName.substring(i+1);
        }else {
            setPrefix(fullName);
        }
    }
    public String getXmlns(){
        return mXmlns;
    }
    public String getPrefix(){
        return mPrefix;
    }
    public void setPrefix(String prefix){
        mPrefix=prefix;
    }
    public void setXmlns(String xmlns){
        if(XMLUtil.isEmpty(xmlns)){
            xmlns=DEFAULT_XMLNS;
        }
        mXmlns=xmlns;
    }
    public String getUri(){
        return super.getValue();
    }
    public void setUri(String uri){
        if(uri==null){
            super.setValue(null);
            return;
        }
        Matcher matcher=PATTERN_URI.matcher(uri);
        if(!matcher.find()){
            super.setValue(uri);
            return;
        }
        String prf=matcher.group(3);
        if(!XMLUtil.isEmpty(prf)){
            setPrefix(prf);
        }
        uri=matcher.group(1);
        super.setValue(uri);
    }

    @Override
    public XMLAttribute cloneAttr(){
        SchemaAttr attr=new SchemaAttr(getXmlns(), getPrefix(), getUri());
        attr.setNameId(getNameId());
        attr.setValueId(getValueId());
        return attr;
    }
    @Override
    public String getName(){
        StringBuilder builder=new StringBuilder();
        builder.append(getXmlns());
        builder.append(':');
        String prf=getPrefix();
        if(prf==null){
            prf="NULL";
        }
        builder.append(prf);
        return builder.toString();
    }
    public static boolean looksSchema(String name, String value){
        if(value==null || !name.startsWith("xmlns:")){
            return false;
        }
        return true;
    }
    public static String getPrefix(String xmlnsName){
        String start="xmlns:";
        if(!xmlnsName.startsWith("xmlns:")){
            return null;
        }
        return xmlnsName.substring(start.length());
    }
    private static final Pattern PATTERN_URI=Pattern.compile("^\\s*(https?://[^:\\s]+)(:([^:/\\s]+))?\\s*$");
}
