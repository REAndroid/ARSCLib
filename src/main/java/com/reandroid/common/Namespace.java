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
package com.reandroid.common;

public interface Namespace {
    String getPrefix();
    String getUri();
    static boolean isValidPrefix(String prefix){
        if(prefix == null || prefix.length() == 0){
            return false;
        }
        char[] chars = prefix.toCharArray();
        if(!isValidPrefixChar(chars[0])){
            return false;
        }
        for(int i = 1; i < chars.length; i++){
            char ch = chars[i];
            if(isValidPrefixChar(ch) || isValidPrefixSymbol(ch)){
                continue;
            }
            return false;
        }
        return true;
    }
    static boolean isValidPrefixChar(char ch){
        return (ch <= 'Z' && ch >= 'A')
                || (ch <= 'z' && ch >= 'a');
    }
    static boolean isValidPrefixSymbol(char ch){
        return (ch <= '9' && ch >= '0')
                || ch == '_';
    }
}
