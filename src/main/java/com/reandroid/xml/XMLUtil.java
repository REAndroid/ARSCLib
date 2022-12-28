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

public class XMLUtil {
    public static String NEW_LINE="\n";
    public static boolean isEmpty(String s){
        if(s==null){
            return true;
        }
        //String txt=s.trim();
        String txt=s;
        return txt.length()==0;
    }
    public static String escapeXmlChars(String str){
        if(str==null){
            return null;
        }
        str=str.replaceAll("&amp;", "&");
        str=str.replaceAll("&lt;", "<");
        str=str.replaceAll("&gt;", ">");
        str=str.replaceAll("&", "&amp;");
        str=str.replaceAll("<", "&lt;");
        str=str.replaceAll(">", "&gt;");
        return str;
    }
    public static String escapeQuote(String str){
        if(str==null){
            return null;
        }
        str=str.replaceAll("\"", "&quot;");
        return str;
    }
    public static String unEscapeXmlChars(String str){
        if(str==null){
            return null;
        }
        str=str.replaceAll("&amp;", "&");
        str=str.replaceAll("&lt;", "<");
        str=str.replaceAll("&gt;", ">");
        str=str.replaceAll("&quot;", "\"");
        if(str.startsWith("\"")&&str.endsWith("\"")){
           // str=str.substring(1, str.length()-1);
        }
        return str;
    }
    public static String intToHex(int val){
        return String.format("0x%08x", val);
    }
    public static int hexToInt(String hexStr, int def){
        if(hexStr==null){
            return def;
        }
        Matcher matcher=PATTERN_HEX.matcher(hexStr);
        if(!matcher.find()){
            return def;
        }
        hexStr=matcher.group("A");
        return Integer.parseInt(hexStr, 16);
    }

    public static String trimQuote(String txt){
        if(txt==null){
            return null;
        }
        String tmp=txt.trim();
        if(tmp.length()==0){
            return txt;
        }
        char c1=tmp.charAt(0);
        if(c1!='"'){
            return txt;
        }
        int end=tmp.length()-1;
        c1=tmp.charAt(end);
        if(c1!='"'){
            return txt;
        }
        if(end<=1){
            return "";
        }
        return tmp.substring(1,end);
    }
    public static boolean isStringEqual(String s1, String s2) {
        if(s1==null&&s2==null){
            return true;
        }
        if(s1==null||s2==null){
            return false;
        }
        return s1.equals(s2);
    }

    static String htmlToXml(String htmlString){
        if(htmlString==null){
            return null;
        }
        int i=0;
        htmlString=htmlString.trim();
        String result=htmlString;
        Matcher matcher=PATTERN_HTML_HEADER_CHILDES.matcher(htmlString);
        while (matcher.find()){
            String openedTag=matcher.group("Element");
            if(openedTag.contains("https://github.githubassets.com")){
                openedTag.trim();
            }
            int len=openedTag.length();
            String tagName=matcher.group("Tag");
            if(isOpenHtmlTag(tagName) && !openedTag.endsWith("/>")&& !openedTag.endsWith("/ >")){
                String rep=openedTag.substring(0, len-1);
                rep=rep+"/>";
                result=result.replace(openedTag, rep);
                result=result.replace(" crossorigin/>", "/>");
                result=result.replace(" data-pjax-transient/>", "/>");
            }
            i=htmlString.indexOf(openedTag);
            i=i+len;
            htmlString=htmlString.substring(i);
            htmlString=htmlString.trim();
            matcher=PATTERN_HTML_HEADER_CHILDES.matcher(htmlString);
        }
        result="<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+result;
        return result;
    }
    private static boolean isOpenHtmlTag(String tagName){
        if("link".equals(tagName)){
            return true;
        }
        if("meta".equals(tagName)){
            return true;
        }
        if("img".equals(tagName)){
            return true;
        }
        if("style".equals(tagName)){
            return true;
        }
        return false;
    }

    private static Pattern PATTERN_HEX=Pattern.compile("^\\s*(0x)?(?<A>[a-f0-9]+)\\s*$");

    //<link rel="preconnect" href="https://avatars.githubusercontent.com">
    private static Pattern PATTERN_HTML_HEADER_CHILDES=Pattern.compile("(?<Element><\\s*(?<Tag>[a-zA-Z]+)\\s*[^<>]+>)");

}
