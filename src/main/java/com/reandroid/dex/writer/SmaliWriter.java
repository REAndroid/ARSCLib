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
package com.reandroid.dex.writer;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;

public class SmaliWriter implements Appendable, Closeable {
    private final Writer writer;
    private int indent;
    private int lineNumber;
    private StringBuilder comment;
    public SmaliWriter(Writer writer){
        this.writer = writer;
        this.lineNumber = 1;
    }
    public void writeLine(String text) throws IOException {
        newLine();
        append(text);
    }

    @Override
    public SmaliWriter append(CharSequence charSequence) throws IOException {
        write(charSequence, 0, charSequence.length());
        return this;
    }

    @Override
    public SmaliWriter append(CharSequence charSequence, int start, int length) throws IOException {
        write(charSequence, start, length);
        return this;
    }

    @Override
    public SmaliWriter append(char ch) throws IOException {
        write(ch);
        return this;
    }
    public void append4(String text) throws IOException {
        //write(text);
    }
    public void append(int i) throws IOException {
        writer.append(Integer.toString(i));
    }
    public void newLine() throws IOException {
        flushComment();
        writer.append('\n');
        writeIndent();
        lineNumber++;
    }
    private void writeIndent() throws IOException {
        Writer writer = this.writer;
        int length = this.indent;
        for(int i = 0; i < length; i++){
            writer.append(' ');
        }
    }
    public void appendComment(String text) {
        StringBuilder comment = this.comment;
        if(comment == null){
            comment = new StringBuilder();
            this.comment = comment;
            comment.append(" #");
        }
        comment.append(' ');
        comment.append(text);
    }
    private void flushComment() throws IOException {
        StringBuilder comment = this.comment;
        if(comment == null){
            return;
        }
        append(comment.toString());
        this.comment = null;
    }

    public int getLineNumber() {
        return lineNumber;
    }
    public void indentPlus(){
        indent += INDENT_STEP;
    }
    public void indentMinus(){
        indent -= INDENT_STEP;
        if(indent < 0){
            indent = 0;
        }
    }
    public void indentReset(){
        indent = 0;
    }


    private void write(CharSequence text, int start, int length) throws IOException {
        for(int i = start; i < length; i++){
            write(text.charAt(i));
        }
    }
    private void write(char ch) throws IOException {
        Writer writer = this.writer;
        if ((ch >= ' ') && (ch < 0x7f)) {
            writer.append(ch);
            return;
        } else if (ch <= 0x7f) {
            switch (ch) {
                case '\n':
                    writer.append("\\n");
                    return;
                case '\r':
                    writer.append("\\r");
                    return;
                case '\t':
                    writer.append("\\t");
                    return;
            }
        }

        writer.append("\\u");
        writer.append(Character.forDigit(ch >> 12, 16));
        writer.append(Character.forDigit((ch >> 8) & 0x0f, 16));
        writer.append(Character.forDigit((ch >> 4) & 0x0f, 16));
        writer.append(Character.forDigit(ch & 0x0f, 16));
    }
    @Override
    public void close() throws IOException {
        writer.close();
    }
    @Override
    public String toString(){
        return "line = " + getLineNumber();
    }

    private static final int INDENT_STEP = 4;
}
