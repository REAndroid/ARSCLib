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
package com.reandroid.arsc.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StyledStringBuilder {

    public static String build(String text, Collection<StyleSpanInfo> spanInfoList, boolean xml){
        if(isEmpty(spanInfoList)){
            return text;
        }
        CharPiece[] charPieceArray = toCharPieceArray(text);
        boolean spansOk = initializeTags(charPieceArray, spanInfoList, xml);
        if(!spansOk){
            // TODO: should throw here ?
            return text;
        }
        if(xml){
            escapeXmlChars(charPieceArray);
        }
        StringBuilder builder = new StringBuilder();
        int length = charPieceArray.length;
        for(int i = 0; i < length; i++){
            CharPiece charPiece = charPieceArray[i];
            charPiece.append(builder);
        }
        return builder.toString();
    }

    private static boolean isEmpty(Collection<StyleSpanInfo> spanInfoList){
        if(spanInfoList == null || spanInfoList.size()==0){
            return true;
        }
        for(StyleSpanInfo spanInfo:spanInfoList){
            if(spanInfo != null){
                return false;
            }
        }
        return true;
    }
    private static boolean initializeTags(CharPiece[] charPieceArray, Collection<StyleSpanInfo> spanInfoList, boolean xml){
        for(StyleSpanInfo spanInfo : spanInfoList){
            if(spanInfo == null){
                continue;
            }
            boolean spanOk = initializeTag(charPieceArray, spanInfo, xml);
            if(!spanOk){
                return false;
            }
        }
        return true;
    }
    private static boolean initializeTag(CharPiece[] charPieceArray, StyleSpanInfo spanInfo, boolean xml){
        int length = charPieceArray.length;
        int pos = spanInfo.getFirst();
        if(pos < 0 || pos >= length){
            return false;
        }
        CharPiece charPiece = charPieceArray[pos];
        charPiece.addFirstTag(spanInfo.getStartTag(xml));

        pos = spanInfo.getLast();
        if(pos < 0 || pos >= length){
            return false;
        }
        charPiece = charPieceArray[pos];
        charPiece.addLastTag(spanInfo.getEndTag());
        return true;
    }

    private static void escapeXmlChars(CharPiece[] charPieceArray){
        int length = charPieceArray.length;
        for(int i = 0; i < length; i++){
            CharPiece charPiece = charPieceArray[i];
            if(isSpecialXmlChar(charPiece.mChar) && !isAlreadyEscaped(charPieceArray, i)){
                charPiece.escapedXml = escapeXmlChar(charPiece.mChar);
            }
        }
    }
    private static boolean isAlreadyEscaped(CharPiece[] charPieceArray, int position){
        if(charPieceArray[position].mChar != '&'){
            return false;
        }
        if((position + 3) >= charPieceArray.length){
            return false;
        }
        if(charPieceArray[position + 3].mChar == ';'){
            char ch = charPieceArray[position + 1].mChar;
            return charPieceArray[position + 2].mChar == 't'
                    && (ch == 'l' || ch == 'g') ;
        }
        if((position + 4) >= charPieceArray.length){
            return false;
        }
        if(charPieceArray[position + 4].mChar == ';'){
            return charPieceArray[position + 1].mChar == 'a'
                    && charPieceArray[position + 2].mChar == 'm'
                    && charPieceArray[position + 3].mChar == 'p';
        }
        return false;
    }
    private static String escapeXmlChar(char ch){
        switch (ch){
            case '&':
                return "&amp;";
            case '<':
                return "&lt;";
            case '>':
                return "&gt;";
            default:
                throw new IllegalArgumentException("Not special xml char: '" + ch + "'");
        }
    }
    private static boolean isSpecialXmlChar(char ch){
        switch (ch){
            case '&':
            case '<':
            case '>':
                return true;
            default:
                return false;
        }
    }
    private static CharPiece[] toCharPieceArray(String text){
        char[] chars = text.toCharArray();
        int length = chars.length;
        CharPiece[] results = new CharPiece[length];
        for(int i = 0; i < length; i++){
            results[i] = new CharPiece(i, chars[i]);
        }
        return results;
    }

    static class CharPiece{
        final int position;
        private List<String> firstTagList;
        final char mChar;
        private List<String> lastTagList;
        String escapedXml;
        CharPiece(int position, char ch){
            this.position = position;
            this.mChar = ch;
        }
        void append(StringBuilder builder){
            if(firstTagList != null){
                for(String tag : firstTagList){
                    builder.append(tag);
                }
            }
            if(escapedXml != null){
                builder.append(escapedXml);
            }else {
                builder.append(mChar);
            }
            if(lastTagList != null){
                for(String tag : lastTagList){
                    builder.append(tag);
                }
            }
        }
        void addFirstTag(String tag){
            if(tag == null){
                return;
            }
            if(this.firstTagList == null){
                this.firstTagList = new ArrayList<>(2);
            }
            this.firstTagList.add(tag);
        }
        void addLastTag(String tag){
            if(tag == null){
                return;
            }
            if(this.lastTagList == null){
                this.lastTagList = new ArrayList<>(2);
            }
            this.lastTagList.add(0, tag);
        }
    }
}
