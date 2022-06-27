package com.reandroid.lib.arsc.pool.builder;

import com.reandroid.lib.arsc.item.StringItem;

import java.util.regex.Pattern;

public class StyleBuilder {
    public static void buildStyle(StringItem stringItem){
        System.out.println(stringItem.toString());
    }
    public static boolean hasStyle(StringItem stringItem){
        if(stringItem==null){
            return false;
        }
        return hasStyle(stringItem.getHtml());
    }
    public static boolean hasStyle(String text){
        if(text==null){
            return false;
        }
        int i=text.indexOf('<');
        if(i<0){
            return false;
        }
        i=text.indexOf('>');
        return i>1;
    }
    private static final Pattern PATTERN_STYLE=Pattern.compile("");
}
