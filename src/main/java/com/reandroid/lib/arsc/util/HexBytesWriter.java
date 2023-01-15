package com.reandroid.lib.arsc.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class HexBytesWriter {
    private final byte[] byteArray;
    private final int width;
    private final int columns;
    private final int indent;
    public HexBytesWriter(byte[] byteArray, int width, int columns, int indent){
        this.byteArray = byteArray;
        this.width = (width <= 0) ? DEFAULT_WIDTH : width;
        this.columns = (columns <= 0) ? width : columns;
        this.indent = indent;
    }
    public HexBytesWriter(byte[] byteArray, int width){
        this(byteArray, width, DEFAULT_COLUMNS, DEFAULT_INDENT);
    }
    public HexBytesWriter(byte[] byteArray){
        this(byteArray, DEFAULT_WIDTH, DEFAULT_COLUMNS, DEFAULT_INDENT);
    }
    public void write(Writer writer) throws IOException{
        if(byteArray==null){
            return;
        }
        write(writer, 0, byteArray.length);
    }
    public void write(Writer writer, int offset, int length) throws IOException {
        byte[] byteArray = this.byteArray;
        if(byteArray==null){
            return;
        }
        int width = this.width;
        int columns = this.columns;
        int x = 0;
        boolean newLineAppended = false;
        for(int i=0; i < length; i++){
            if((i%width)==0){
                if(i!=0){
                    writeNewLine(writer);
                    newLineAppended=true;
                }
                writeIndent(writer);
                x=0;
            }else if(x%columns==0){
                writer.write(' ');
            }
            if(!newLineAppended && i!=0){
                writer.write(' ');
            }
            x++;
            newLineAppended=false;
            writeHex(writer, byteArray[offset+i]);
        }
    }
    private void writeHex(Writer writer, byte b) throws IOException {
        String hex = String.format("%02x", (0xff & b)).toUpperCase();
        writer.write(hex);
    }
    private void writeIndent(Writer writer) throws IOException {
        for(int i=0;i<this.indent;i++){
            writer.write(' ');
        }
    }
    private void writeNewLine(Writer writer) throws IOException {
        writer.write('\n');
    }

    public static String toHex(byte[] byteArray){
        StringWriter writer=new StringWriter();
        HexBytesWriter hexBytesWriter = new HexBytesWriter(byteArray);
        try {
            hexBytesWriter.write(writer);
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
        }
        return writer.toString();
    }

    private static final int DEFAULT_WIDTH = 16;
    private static final int DEFAULT_COLUMNS = 4;
    private static final int DEFAULT_INDENT = 0;
}
