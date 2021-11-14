package com.reandroid.lib.arsc.model;

public class StyleSpanInfo {
    public final String TAG;
    public final int FIRST;
    public final int LAST;
    public StyleSpanInfo(String tag, int first, int last){
        this.TAG=tag;
        this.FIRST=first;
        this.LAST=last;
    }
    public String getStartTag(){
        int i=TAG.indexOf(';');
        StringBuilder builder=new StringBuilder();
        builder.append('<');
        if(i<0){
            builder.append(TAG);
        }else {
            builder.append(TAG, 0, i);
            builder.append(' ');
            builder.append(TAG.substring(i+1));
        }
        builder.append('>');
        return builder.toString();
    }

    public String getEndTag(){
        int i=TAG.indexOf(';');
        StringBuilder builder=new StringBuilder();
        builder.append('<');
        builder.append('/');
        if(i<0){
            builder.append(TAG);
        }else {
            builder.append(TAG, 0, i);
        }
        builder.append('>');
        return builder.toString();
    }
    @Override
    public String toString(){
        return TAG+" ("+FIRST+", "+LAST+")";
    }
}
