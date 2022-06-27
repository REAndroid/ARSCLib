package com.reandroid.lib.arsc.pool.builder;

import java.util.List;

public class SpannedText {
    private String mLeftText;
    private String mTag;
    private String mText;
    private String mRightText;
    private List<SpannedText> mChildes;
    public SpannedText(){
    }
    public void parse(int start, String text){
        int i=text.indexOf('<', start);
    }
}
