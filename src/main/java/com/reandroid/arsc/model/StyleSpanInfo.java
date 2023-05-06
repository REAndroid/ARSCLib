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

import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

public class StyleSpanInfo implements JSONConvert<JSONObject> {
    private String mTag;
    private int mFirst;
    private int mLast;
    public StyleSpanInfo(String tag, int first, int last){
        this.mTag = tag;
        this.mFirst = first;
        this.mLast = last;
    }
    public boolean isValid(){
        return mFirst < mLast;
    }
    public int getFirst() {
        return mFirst;
    }
    public void setFirst(int first) {
        this.mFirst = first;
    }
    public int getLast() {
        return mLast;
    }
    public void setLast(int last) {
        this.mLast = last;
    }
    public String getTag() {
        return mTag;
    }
    public void setTag(String tag) {
        this.mTag = tag;
    }

    public String getStartTag(boolean xml){
        int i= mTag.indexOf(';');
        StringBuilder builder=new StringBuilder();
        builder.append('<');
        if(i<0){
            builder.append(mTag);
        }else {
            builder.append(mTag, 0, i);
            builder.append(' ');
            String attrs = mTag.substring(i+1);
            if(xml){
                appendXmlAttrs(builder, attrs);
            }else {
                builder.append(attrs);
            }
        }
        builder.append('>');
        return builder.toString();
    }
    private void appendXmlAttrs(StringBuilder builder, String rawAttr){
        String[] split=rawAttr.split("(\\s*;\\s*)");
        for(int i=0;i<split.length; i++){
            String attr=split[i];
            if(i!=0){
                builder.append(' ');
            }
            int index=attr.indexOf('=')+1;
            builder.append(attr, 0, index);
            builder.append('"');
            builder.append(attr.substring(index));
            builder.append('"');
        }
    }

    public String getEndTag(){
        int i= mTag.indexOf(';');
        if(i < 0){
            i = mTag.indexOf(' ');
        }
        StringBuilder builder=new StringBuilder();
        builder.append('<');
        builder.append('/');
        if(i<0){
            builder.append(mTag);
        }else {
            builder.append(mTag, 0, i);
        }
        builder.append('>');
        return builder.toString();
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(NAME_tag, mTag);
        jsonObject.put(NAME_first, mFirst);
        jsonObject.put(NAME_last, mLast);
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setTag(json.getString(NAME_tag));
        setFirst(json.getInt(NAME_first));
        setLast(json.getInt(NAME_last));
    }
    @Override
    public String toString(){
        return mTag +" ("+ mFirst +", "+ mLast +")";
    }

    public static final String NAME_tag="tag";
    public static final String NAME_first="first";
    public static final String NAME_last="last";
}
