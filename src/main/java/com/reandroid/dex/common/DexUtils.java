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

// originally copied from JesusFreke/smali
package com.reandroid.dex.common;

import java.io.IOException;

public class DexUtils {

    public static String quoteString(String text){
        StringBuilder builder = new StringBuilder(text.length() + 2);
        try {
            appendQuotedString(builder, text);
        } catch (IOException ignored) {
        }
        return builder.toString();
    }
    public static void appendQuotedString(Appendable appendable, String text) throws IOException {
        appendable.append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if ((c >= ' ') && (c < 0x7f)) {
                if ((c == '\'') || (c == '\"') || (c == '\\')) {
                    appendable.append('\\');
                }
                appendable.append(c);
                continue;
            } else if (c <= 0x7f) {
                switch (c) {
                    case '\n':
                        appendable.append("\\n");
                        continue;
                    case '\r':
                        appendable.append("\\r");
                        continue;
                    case '\t':
                        appendable.append("\\t");
                        continue;
                }
            }
            appendable.append("\\u");
            appendable.append(Character.forDigit(c >> 12, 16));
            appendable.append(Character.forDigit((c >> 8) & 0x0f, 16));
            appendable.append(Character.forDigit((c >> 4) & 0x0f, 16));
            appendable.append(Character.forDigit(c & 0x0f, 16));
        }
        appendable.append('"');
    }
    public static boolean isNative(String type){
        if(type == null){
            return false;
        }
        int length = type.length();
        if(length == 0){
            return false;
        }
        int i = 0;
        while (i < length && type.charAt(i) == '['){
            i++;
        }
        if(i >= length){
            return false;
        }
        return isNative(type.charAt(i));
    }
    public static boolean isNative(char ch){
        switch (ch){
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
                return true;
            default:
                return false;
        }
    }
    public static boolean looksSignatureType(String name){
        int length = name.length();
        if(length < 3){
            return false;
        }
        return name.charAt(0) == 'L' && name.charAt(length - 1) == '<';
    }

    public static String toJavaName(String dalvikName){
        int i = dalvikName.indexOf('L');
        dalvikName = dalvikName.substring(i + 1);
        i = dalvikName.indexOf(';');
        if(i < 0){
            i = dalvikName.indexOf('<');
        }
        if(i > 0){
            dalvikName = dalvikName.substring(0, i);
        }
        return dalvikName.replace('/', '.');
    }
    public static String toDalvikName(String javaName){
        return 'L' + javaName.replace('.', '/') + ';';
    }
}
