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
package com.reandroid.arsc.coder;

public class XmlSanitizer {
    public static String escapeSpecialCharacter(String text){
        if(text==null || text.length()==0){
            return text;
        }
        if(isSpecialCharacter(text.charAt(0))){
            return '\\' +text;
        }
        return text;
    }
    public static String unEscapeUnQuote(String text){
        if(text==null || text.length()<2){
            return text;
        }
        char first = text.charAt(0);
        if(first == '"'){
            return unQuoteWhitespace(text);
        }
        if(first != '\\' || !isSpecialCharacter(text.charAt(1))){
            return text;
        }
        return text.substring(1);
    }
    public static String quoteWhitespace(String text){
        if(!isWhiteSpace(text)){
            return text;
        }
        return "\"" + text + "\"";
    }
    public static String unQuoteWhitespace(String text){
        if(text == null || text.length() < 3){
            return text;
        }
        if(text.charAt(0) != '"' || text.charAt(text.length()-1) != '"'){
            return text;
        }
        String unQuoted = text.substring(1, text.length()-1);
        if(!isWhiteSpace(unQuoted)){
            return text;
        }
        return unQuoted;
    }
    private static boolean isWhiteSpace(String text){
        if(text == null || text.length() == 0){
            return false;
        }
        char[] chars = text.toCharArray();
        for(int i = 0; i < chars.length; i++){
            if(!isWhiteSpace(chars[i])){
                return false;
            }
        }
        return true;
    }
    private static boolean isWhiteSpace(char ch){
        switch (ch){
            case ' ':
            case '\n':
            case '\r':
            case '\t':
                return true;
            default:
                return false;
        }
    }
    private static boolean isSpecialCharacter(char ch){
        switch (ch){
            case '@':
            case '?':
            case '#':
                return true;
            default:
                return false;
        }
    }
}
