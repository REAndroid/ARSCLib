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

import com.reandroid.xml.parser.XMLSpanParser;

import java.util.*;

public class XMLSpannable implements Comparable<XMLSpannable>{
    private XMLElement mElement;
    private String mText;
    private List<XMLSpanInfo> mSpanInfoList;
    public XMLSpannable(XMLElement element){
        this.mElement=element;
    }
    public boolean isValid(){
        List<XMLSpanInfo> spanInfoList = getSpanInfoList();
        if(spanInfoList.size()==0){
            return false;
        }
        for(XMLSpanInfo spanInfo:spanInfoList){
            if(spanInfo.end<spanInfo.start){
                return false;
            }
        }
        return true;
    }
    public String getXml(){
        StringBuilder builder = new StringBuilder();
        for(XMLNode xmlNode: mElement.getChildNodes()){
            if(xmlNode instanceof XMLElement){
                appendXml(builder, (XMLElement) xmlNode);
            }else if(xmlNode instanceof XMLText){
                appendXml(builder, (XMLText) xmlNode);
            }
        }
        return builder.toString();
    }
    private void appendXml(StringBuilder builder, XMLElement element){
        builder.append('<');
        builder.append(element.getTagName());
        appendXmlAttributes(builder, element);
        builder.append('>');
        for(XMLNode xmlNode: element.getChildNodes()){
            if(xmlNode instanceof XMLElement){
                appendXml(builder, (XMLElement) xmlNode);
            }else if(xmlNode instanceof XMLText){
                appendXml(builder, (XMLText) xmlNode);
            }
        }
        builder.append('<');
        builder.append('/');
        builder.append(element.getTagName());
        builder.append('>');
    }
    private void appendXmlAttributes(StringBuilder builder, XMLElement element){
        for(XMLAttribute xmlAttribute : element.listAttributes()){
            builder.append(' ');
            builder.append(xmlAttribute.getName());
            builder.append('=');
            builder.append('"');
            builder.append(xmlAttribute.getValue());
            builder.append('"');
        }
    }
    private void appendXml(StringBuilder builder, XMLText xmlText){
        builder.append(xmlText.getText(true));
    }
    public String getText(){
        if(mText==null){
            buildSpanInfo();
        }
        return mText;
    }
    public List<XMLSpanInfo> getSpanInfoList(){
        if(mSpanInfoList==null){
            buildSpanInfo();
        }
        return mSpanInfoList;
    }
    private void buildSpanInfo(){
        mSpanInfoList=new ArrayList<>();
        StringBuilder builder=new StringBuilder();
        buildSpanInfo(mElement, builder);
        mText=builder.toString();
        mElement=null;
    }
    private void buildSpanInfo(XMLElement element, StringBuilder builder){
        XMLSpanInfo info = null;
        for(XMLNode node:element.listSpannable()){
            if(info != null){
                int pos = builder.length();
                if(pos > 0){
                    pos = pos - 1;
                }
                info.end = pos;
                info = null;
            }
            if(node instanceof XMLText){
                builder.append(((XMLText)node).getText());
                continue;
            }
            XMLElement child = (XMLElement) node;
            info=new XMLSpanInfo(
                    child.getSpannableText(),
                    builder.length(), 0);
            mSpanInfoList.add(info);
            buildSpanInfo(child, builder);
        }
        if(info!=null){
            int pos = builder.length();
            if(pos > 0){
                pos = pos - 1;
            }
            info.end = pos;
        }
    }
    @Override
    public int compareTo(XMLSpannable xmlSpannable) {
        return getText().compareTo(xmlSpannable.getText());
    }

    public static XMLSpannable parse(String text){
        if(!hasStyle(text)){
            return null;
        }
        try {
            XMLSpannable spannable=new XMLSpannable(PARSER.parse(text));
            if(spannable.isValid()){
                return spannable;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
    public static Set<String> tagList(Collection<XMLSpannable> spannableList){
        Set<String> results=new HashSet<>();
        for(XMLSpannable xmlSpannable:spannableList){
            for(XMLSpanInfo spanInfo: xmlSpannable.getSpanInfoList()){
                results.add(spanInfo.tag);
            }
        }
        return results;
    }
    public static List<String> toTextList(Collection<XMLSpannable> spannableList){
        List<String> results=new ArrayList<>(spannableList.size());
        for(XMLSpannable xmlSpannable:spannableList){
            results.add(xmlSpannable.getText());
        }
        return results;
    }
    public static void sort(List<XMLSpannable> spannableList){
        Comparator<XMLSpannable> cmp=new Comparator<XMLSpannable>() {
            @Override
            public int compare(XMLSpannable s1, XMLSpannable s2) {
                return s1.compareTo(s2);
            }
        };
        spannableList.sort(cmp);
    }
    private static boolean hasStyle(String text){
        if(text==null){
            return false;
        }
        int i=text.indexOf('<');
        if(i<0){
            return false;
        }
        i=text.indexOf('>');
        return i>1;
    }

    private static final XMLSpanParser PARSER=new XMLSpanParser();

}
