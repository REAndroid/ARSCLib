
package com.reandroid.xml.base;

public interface Text extends Node{
    String getText();
    void setText(String text);
    @Override
    Element getParentNode();
}
