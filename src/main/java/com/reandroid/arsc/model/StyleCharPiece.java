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
import java.util.List;

class StyleCharPiece {
    final char mChar;
    private List<StyleSpanInfo> firstList;
    private List<StyleSpanInfo> endList;

    StyleCharPiece(char ch) {
        this.mChar = ch;
    }
    StyleSpanInfo getFirst(){
        if(firstList == null || firstList.size() == 0){
            return null;
        }
        StyleSpanInfo spanInfo = firstList.get(0);
        firstList.remove(0);
        return spanInfo;
    }
    StyleSpanInfo getEnd(String name){
        if(endList == null){
            return null;
        }
        int i;
        StyleSpanInfo result = null;
        for(i = 0; i < endList.size(); i++){
            StyleSpanInfo spanInfo = endList.get(i);
            if(name.equals(spanInfo.getName())){
                result = spanInfo;
                break;
            }
        }
        if(result == null){
            return null;
        }
        endList.remove(i);
        return result;
    }
    void addFirst(StyleSpanInfo spanInfo){
        if(firstList == null){
            firstList = new ArrayList<>(1);
        }
        firstList.add(spanInfo);
    }
    void addEnd(StyleSpanInfo spanInfo){
        if(endList == null){
            endList = new ArrayList<>(1);
        }
        endList.add(spanInfo);
    }

    static StyleCharPiece[] toCharPieceArray(String text){
        char[] chars = text.toCharArray();
        int length = chars.length;
        StyleCharPiece[] results = new StyleCharPiece[length + 1];
        for(int i = 0; i < length; i++){
            results[i] = new StyleCharPiece(chars[i]);
        }
        results[length] = new StyleCharPiece((char) 0);
        return results;
    }
}
