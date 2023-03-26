/*
 *  This class is taken from org.xmlpull.*
 *
 *  Check license: http://xmlpull.org
 *
 */

/*This package is renamed from org.xmlpull.* to avoid conflicts*/
package com.reandroid.xml.parser;

@Deprecated
public class XmlPullParserException extends Exception {
    protected Throwable detail;
    protected int row = -1;
    protected int column = -1;

    public XmlPullParserException(String s) {
        super(s);
    }
    public XmlPullParserException(String msg, XmlPullParser parser, Throwable chain) {
        super(buildMessage(msg, parser));
        if (parser != null) {
            this.row = parser.getLineNumber();
            this.column = parser.getColumnNumber();
        }
        this.detail = chain;
    }
    public Throwable getDetail() { return detail; }
    public int getLineNumber() { return row; }
    public int getColumnNumber() { return column; }
    private static String buildMessage(String msg, XmlPullParser parser){
        StringBuilder builder=new StringBuilder();
        if(parser!=null){
            builder.append("[line=");
            builder.append(parser.getLineNumber());
            builder.append(", col=");
            builder.append(parser.getColumnNumber());
            builder.append("] ");
        }
        if(msg!=null){
            builder.append(msg);
        }
        return builder.toString();
    }
}

