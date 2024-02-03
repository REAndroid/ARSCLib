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

import java.io.IOException;

public class XMLUtil {

    public static String decodeEntityRef(String text) {
        if(text == null || text.length() == 0){
            return "";
        }
        if("lt".equals(text)){
            return "<";
        }else if("gt".equals(text)){
            return ">";
        }else if("amp".equals(text)){
            return  "&";
        }else if("quot".equals(text)){
            return  "\"";
        }else if("apos".equals(text)){
            return  "'";
        }
        if(text.charAt(0) == '#'){
            try{
                char ch = (char) Integer.parseInt(text.substring(1));
                return String.valueOf(ch);
            }catch (NumberFormatException ignored){
            }
        }
        return text;
    }

    public static String splitName(String name){
        if(name == null){
            return null;
        }
        int i = name.lastIndexOf(':');
        if(i >= 0){
            i++;
            name = name.substring(i);
        }
        name = name.trim();
        if(name.length() == 0){
            return null;
        }
        return name;
    }
    public static String splitPrefix(String name){
        if(name == null){
            return null;
        }
        int i = name.indexOf(':');
        if(i > 0){
            return name.substring(0, i);
        }
        return null;
    }
    public static int ensureStartTag(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        int event = parser.getEventType();
        while (event != XmlPullParser.START_TAG
                && event != XmlPullParser.END_DOCUMENT){
            event = parser.next();
        }
        return event;
    }
    public static boolean isEmpty(String s){
        if(s==null){
            return true;
        }
        return s.length()==0;
    }
    public static String escapeXmlChars(String str){
        if(str==null){
            return null;
        }
        if(str.indexOf('&')<0 && str.indexOf('<')<0 && str.indexOf('>')<0){
            return str;
        }
        str=str.replaceAll("&amp;", "&");
        str=str.replaceAll("&lt;", "<");
        str=str.replaceAll("&gt;", ">");
        str=str.replaceAll("&", "&amp;");
        str=str.replaceAll("<", "&lt;");
        str=str.replaceAll(">", "&gt;");
        return str;
    }
    public static String toEventName(int eventType){
        String[] types = EVENT_TYPES;
        if(eventType < 0 || eventType >= types.length){
            return String.valueOf(eventType);
        }
        return types[eventType];
    }

    public static String [] EVENT_TYPES = {
            "START_DOCUMENT",
            "END_DOCUMENT",
            "START_TAG",
            "END_TAG",
            "TEXT",
            "CDSECT",
            "ENTITY_REF",
            "IGNORABLE_WHITESPACE",
            "PROCESSING_INSTRUCTION",
            "COMMENT",
            "DOCDECL"
    };

}
