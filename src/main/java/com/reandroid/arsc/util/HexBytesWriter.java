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
package com.reandroid.arsc.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class HexBytesWriter {
    private final byte[] byteArray;
    private final int width;
    private final int columns;
    private final int indent;
    private boolean mAppendString = true;
    private String mEncoding;
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

    public void setAppendString(boolean appendString) {
        this.mAppendString = appendString;
    }
    public void setEncoding(String encoding){
        if(!"UTF-16".equals(encoding)){
            encoding = "UTF-8";
        }
        this.mEncoding = encoding;
    }
    private String getEncoding(){
        if(mEncoding==null){
            mEncoding="UTF-8";
        }
        return mEncoding;
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
                    writeString(writer, x, i);
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
        if(x>1){
            writeString(writer,x , length);
        }
    }
    private void writeHex(Writer writer, byte b) throws IOException {
        String hex = String.format("%02x", (0xff & b)).toUpperCase();
        writer.write(hex);
    }
    private void writeString(Writer writer, int width, int position) throws IOException {
        if(!mAppendString){
            return;
        }
        int start = position - width;
        if(start<0){
            start=0;
        }
        int rem = this.width - width;
        if(rem > 0){
            fillLastRow(writer, position);
        }
        writer.write(' ');
        writer.write(' ');
        writer.write(' ');
        writer.write(' ');
        String text = new String(this.byteArray, start, width, getEncoding());
        for(char ch:text.toCharArray()){
            printChar(writer, ch);
        }
    }
    private void fillLastRow(Writer writer, int position) throws IOException {
        int rem = width - position % width;
        for(int i=0; i<rem; i++){
            if(i%columns == 0){
                writer.write(' ');
            }
            writer.write(' ');
            writer.write(' ');
            writer.write(' ');
        }
    }
    private void printChar(Writer writer, char ch) throws IOException {
        switch (ch){
            case '\n':
                writer.write('\\');
                writer.write('n');
                break;
            case '\r':
                writer.write('\\');
                writer.write('r');
                break;
            case '\t':
                writer.write('\\');
                writer.write('t');
                break;
            default:
                writer.write(ch);
                break;
        }
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
