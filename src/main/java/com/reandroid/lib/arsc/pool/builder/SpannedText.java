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
package com.reandroid.lib.arsc.pool.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SpannedText {
    private String mTag;
    private String mText;
    private int mStart;
    private int mEnd;
    private List<SpannedText> mChildes=new ArrayList<>();
    public SpannedText(){
    }
    public void parse(String text){
        char[] allChars=text.toCharArray();
        int len=allChars.length;
        String firstTag=null;
        String endTag=null;
        StringBuilder tag=null;
        boolean firstTagFound=false;
        int posFirst=0;
        int posSecond=0;
        for(int i=0;i<len;i++){
            char ch=allChars[i];
            if(tag!=null){
                tag.append(ch);
                if(ch=='>'){
                    if(!firstTagFound){
                        firstTag=tag.toString();
                        firstTagFound=true;
                        tag=null;
                    }else {
                        endTag=tag.toString();
                        if(isTagsMatch(firstTag, endTag)){
                            break;
                        }
                        endTag=null;
                        tag=null;
                        continue;
                    }
                }
                continue;
            }
            if(ch=='<'){
                if(isClosing(allChars, i+1)){
                    if(!firstTagFound){
                        tag=null;
                        continue;
                    }
                }else if(firstTagFound){
                    firstTagFound=false;
                    firstTag=null;
                }
                tag=new StringBuilder();
                tag.append(ch);
                if(!firstTagFound){
                    posFirst=i;
                }else {
                    posSecond=i;
                }
            }
        }
        if(firstTag==null || endTag==null){
            return;
        }
        mStart=posFirst;
        mEnd=posSecond;
        StringBuilder builder=new StringBuilder();
        builder.append(text, 0, posFirst);
        builder.append(text, posFirst+firstTag.length(), posSecond);
        builder.append(text.substring(posSecond+endTag.length()));
        mText=builder.toString();
    }
    private boolean isClosing(char[] allChars, int pos){
        for(int i=pos;i<allChars.length;i++){
            char ch=allChars[i];
            if(ch=='/'){
                return true;
            }
            if(ch!=' '){
                return false;
            }
        }
        return false;
    }
    private boolean isTagsMatch(String start, String end){
        start=trimStart(start);
        end=trimEndTag(end);
        return start.equals(end);
    }
    private String trimStart(String start){
        start=start.substring(1, start.length()-1);
        int i=start.indexOf(' ');
        if(i>0){
            start=start.substring(0,i);
        }
        start=start.trim();
        return start;
    }
    private String trimEndTag(String end){
        end=end.substring(1, end.length()-1).trim();
        end=end.substring(1).trim();
        return end;
    }
    private static final Pattern PATTERN_TAG=Pattern.compile("^(.*)(<[^/<>]+>)([^<]+)(</[^<>]+>)(.*)$");
}
