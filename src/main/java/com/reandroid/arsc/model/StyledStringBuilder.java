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

import com.reandroid.utils.StringsUtil;
import com.reandroid.xml.*;

public class StyledStringBuilder {

    private final String text;
    private final StyleSpanInfo[] spanInfoList;
    public StyledStringBuilder(String text, StyleSpanInfo[] spanInfoList){
        this.text = text;
        this.spanInfoList = spanInfoList;
    }
    public StyleDocument build(){
        StyleCharPiece[] charPieceList = StyleCharPiece.toCharPieceArray(text);
        boolean initOk = initialize(charPieceList, spanInfoList);
        if(!initOk){
            return null;
        }
        StyleDocument document = new StyleDocument();
        StyleNode styleNode = document;
        for(int i = 0; i < charPieceList.length; i++){
            StyleCharPiece charPiece = charPieceList[i];
            styleNode = buildStarts(styleNode, charPiece);
            styleNode.appendChar(charPiece.mChar);
            styleNode = buildEnd(styleNode, charPiece);
        }
        return document;
    }
    private StyleNode buildStarts(StyleNode styleNode, StyleCharPiece charPiece){
        StyleSpanInfo info = charPiece.getFirst();
        while (info != null){
            StyleElement styleElement = new StyleElement(info.getName());
            info.serializeAttributes(styleElement);
            styleNode.addStyleNode(styleElement);
            styleNode = styleElement;
            info = charPiece.getFirst();
            styleNode = buildEnd(styleNode, charPiece);
        }
        return styleNode;
    }
    private StyleNode buildEnd(StyleNode styleNode, StyleCharPiece charPiece){
        if(!(styleNode instanceof StyleElement)){
            return styleNode;
        }
        StyleElement element = (StyleElement) styleNode;
        StyleSpanInfo info = charPiece.getEnd(element.getName());
        while (info != null){
            styleNode = element.getParentStyle();
            if(!(styleNode instanceof StyleElement)){
                break;
            }
            element = (StyleElement) styleNode;
            info = charPiece.getEnd(element.getName());
        }
        return styleNode;
    }

    private static boolean initialize(StyleCharPiece[] styleCharPieceArray, StyleSpanInfo[] spanInfoList){
        for(StyleSpanInfo spanInfo : spanInfoList){
            if(spanInfo == null){
                continue;
            }
            boolean spanOk = initialize(styleCharPieceArray, spanInfo);
            if(!spanOk){
                return false;
            }
        }
        return true;
    }
    private static boolean initialize(StyleCharPiece[] styleCharPieceArray, StyleSpanInfo spanInfo){
        int length = styleCharPieceArray.length;
        int pos = spanInfo.getFirst();
        if(pos < 0 || pos >= length){
            return false;
        }
        StyleCharPiece styleCharPiece = styleCharPieceArray[pos];
        styleCharPiece.addFirst(spanInfo);

        pos = spanInfo.getLast();
        if(pos < 0 || pos >= length){
            return false;
        }
        styleCharPiece = styleCharPieceArray[pos];
        styleCharPiece.addEnd(spanInfo);
        return true;
    }
}
