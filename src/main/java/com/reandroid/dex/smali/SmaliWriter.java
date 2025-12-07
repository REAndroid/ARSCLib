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
package com.reandroid.dex.smali;

import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.common.Modifier;
import com.reandroid.dex.common.Register;
import com.reandroid.dex.common.RegistersTable;
import com.reandroid.dex.ins.Label;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.formatters.SequentialLabelFactory;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.io.FileUtil;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class SmaliWriter implements Appendable, Closeable {

    private Writer writer;
    private OutputStream outputStream;
    private int indent;
    private int lineNumber;
    private int columnNumber;
    private boolean state_new_line;
    private boolean indentRequested;
    private StringBuilder comment;

    private RegistersTable currentRegistersTable;
    private boolean stateWritingFields;
    private boolean stateWritingInstructions;

    private SmaliWriterSetting writerSetting;
    private SequentialLabelFactory sequentialLabelFactory;

    public SmaliWriter(Writer writer) {
        this();
        this.writer = writer;
    }
    public SmaliWriter(SmaliWriterSetting setting) {
        this();
        this.writerSetting = setting;
    }
    public SmaliWriter() {
        this.lineNumber = 1;
        this.state_new_line = true;
    }


    public void onWriteClass(TypeKey typeKey) throws IOException {
        SmaliWriterSetting setting = getWriterSetting();
        if (setting != null && setting.isEnableComments()) {
            setting.writeClassComment(this, typeKey);
        }
    }
    public void onWriteMethod(MethodKey methodKey) throws IOException {
        SmaliWriterSetting setting = getWriterSetting();
        if (setting != null && setting.isEnableComments()) {
            setting.writeMethodComment(this, methodKey);
        }
    }
    public void setWriter(File file) throws IOException {
        setWriter(FileUtil.outputStream(file));
    }
    public void setWriter(OutputStream outputStream) {
        setWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        this.outputStream = outputStream;
    }
    public void setWriter(Writer writer) {
        this.reset();
        if (writer != this.writer) {
            this.outputStream = null;
        }
        this.writer = writer;
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
    public void appendRegister(int registerValue) throws IOException {
        RegistersTable registersTable = getCurrentRegistersTable();
        if (registersTable == null) {
            throw new IOException("Current registers table not set");
        }
        Register register = registersTable.getRegisterFor(registerValue);
        register.append(this);
    }

    public void appendAllWithDoubleNewLine(Iterator<? extends SmaliFormat> iterator) throws IOException {
        while (iterator.hasNext()) {
            if (!state_new_line) {
                newLine();
            }
            if (indent == 0) {
                newLine();
            }
            iterator.next().append(this);
        }
    }
    public void appendAllWithIndent(Iterator<? extends SmaliFormat> iterator) throws IOException {
        boolean appendOnce = false;
        while (iterator.hasNext()) {
            if (!appendOnce) {
                indentPlus();
            }
            newLine();
            iterator.next().append(this);
            appendOnce = true;
        }
        if (appendOnce) {
            indentMinus();
        }
    }
    public void appendAll(Iterator<? extends SmaliFormat> iterator) throws IOException {
        appendAll(iterator, true);
    }
    public void appendAll(Iterator<? extends SmaliFormat> iterator, boolean newLine) throws IOException {
        boolean appendOnce = false;
        while (iterator.hasNext()) {
            if (newLine && appendOnce) {
                newLine();
            }
            iterator.next().append(this);
            appendOnce = true;
        }
    }
    public void appendModifiers(Iterator<? extends Modifier> iterator) throws IOException {
        while (iterator.hasNext()) {
            iterator.next().append(this);
        }
    }
    public boolean appendOptional(SmaliFormat smaliFormat) throws IOException {
        if (smaliFormat != null) {
            smaliFormat.append(this);
            return true;
        }
        return false;
    }
    public boolean appendOptional(SmaliFormat smaliFormat, String comment) throws IOException {
        if (smaliFormat == null) {
            return false;
        }
        newLine();
        appendCommentNewLine(comment);
        smaliFormat.append(this);
        return true;
    }
    public void appendRequired(SmaliFormat smaliFormat) throws IOException {
        if (smaliFormat == null) {
            throw new IOException("Null SmaliFormat");
        }
        smaliFormat.append(this);
    }
    public void append(double d) throws IOException {
        append(Double.toString(d));
    }
    public void append(float f) throws IOException {
        append(Float.toString(f));
        append('f');
    }
    public void appendInteger(int i) throws IOException {
        append(Integer.toString(i));
    }
    public void appendHex(byte b) throws IOException {
        append(HexUtil.toSignedHex(b));
        append('t');
    }
    public void appendHex(short s) throws IOException {
        append(HexUtil.toSignedHex(s));
        append('S');
    }
    public void appendHex(int i) throws IOException {
        append(HexUtil.toSignedHex(i));
        appendResourceIdComment(i);
    }
    public void appendHex(long l) throws IOException {
        append(HexUtil.toSignedHex(l));
        append('L');
        appendResourceIdComment(l);
    }
    public void appendHex(int width, long l) throws IOException {
        String hex = HexUtil.toSignedHex(l);
        append(hex);
        if (width == 1) {
            append('t');
        } else if (width == 2) {
            append('S');
        } else if (width == 8 && (l & 0xffffffff80000000L) != 0) {
            append('L');
        }
        appendResourceIdComment(l);
    }
    public void appendResourceIdComment(long l) throws IOException {
        SmaliWriterSetting setting = getWriterSetting();
        if (setting != null) {
            setting.writeResourceIdComment(this, l);
        }
    }
    public void appendResourceIdComment(int i) throws IOException {
        SmaliWriterSetting setting = getWriterSetting();
        if (setting != null) {
            setting.writeResourceIdComment(this, i);
        }
    }
    public void appendLabelName(String name) throws IOException {
        SequentialLabelFactory labelFactory = getSequentialLabelFactory();
        if (labelFactory != null) {
            name = labelFactory.get(name);
        }
        append(name);
    }
    public void newLineDouble() throws IOException {
        newLine(2);
    }
    public void newLine() throws IOException {
        newLine(1);
    }
    public void newLine(int amount) throws IOException {
        if (lineNumber == 1 && columnNumber == 0 || amount == 0) {
            return;
        }
        flushComment();
        for (int i = 0; i < amount; i++) {
            writer.append('\n');
        }
        columnNumber = 0;
        lineNumber += amount;
        state_new_line = true;
        indentRequested = true;
    }
    private void flushIndent() throws IOException {
        if (indentRequested) {
            writeIndent();
        }
    }
    private void writeIndent() throws IOException {
        indentRequested = false;
        Writer writer = this.writer;
        int length = this.indent;
        for (int i = 0; i < length; i++) {
            writer.append(' ');
        }
    }
    public void appendCommentNewLine(String text) {
        appendComment(text, true);
    }
    public void appendComment(String text) {
        appendComment(text, false);
    }
    public void appendComment(String text, boolean newLine) {
        if (StringsUtil.isEmpty(text) || !isEnableComments()) {
            return;
        }
        if (newLine) {
            try {
                newLine();
            } catch (IOException ignored) {}
        }
        StringBuilder comment = this.comment;
        if (comment == null || comment.length() == 0) {
            comment = new StringBuilder();
            this.comment = comment;
            if (this.indent != 0 || this.columnNumber != 0) {
                comment.append("    ");
            }
            comment.append('#');
            comment.append(' ');
            columnNumber += 2;
        } else {
            comment.append(' ');
            columnNumber += 1;
        }
        int column = comment.length();
        escapeCommentCharacters(comment, text);
        column = comment.length() - column;
        columnNumber += column;
    }
    private void escapeCommentCharacters(StringBuilder builder, String text) {
        int length = text.length();
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                builder.append('\\');
                builder.append('n');
            } else if (c == '\t') {
                builder.append('\\');
                builder.append('t');
            } else if (c == '\r') {
                builder.append('\\');
                builder.append('r');
            } else if (c == '\b') {
                builder.append('\\');
                builder.append('b');
            } else if (c == '\f') {
                builder.append('\\');
                builder.append('f');
            } else if (c == ' ') {
                builder.append(' ');
            } else if (c < ' ' || !Character.isDefined(c)) {
                DexUtils.encodeToHexChar(builder, c);
            } else if (c == '\u0085' || c == '\u2028' || c == '\u2029') {
                DexUtils.encodeToHexChar(builder, c);
            } else {
                builder.append(c);
            }
        }
    }
    public Appendable getCommentAppender() {
        if (!isEnableComments()) {
            return null;
        }
        StringBuilder comment = this.comment;
        if (comment == null) {
            comment = new StringBuilder();
            this.comment = comment;
            if (this.indent != 0 || this.columnNumber != 0) {
                comment.append("    ");
            }
            comment.append('#');
            comment.append(' ');
            columnNumber += 2;
        }
        return comment;
    }
    private void flushComment() throws IOException {
        StringBuilder comment = this.comment;
        if (comment == null) {
            return;
        }
        append(comment.toString());
        this.comment = null;
    }

    public int getLineNumber() {
        return lineNumber;
    }
    public int getColumnNumber() {
        return columnNumber;
    }

    public void indentPlus() {
        indent += INDENT_STEP;
    }
    public void indentMinus() {
        indent -= INDENT_STEP;
        if (indent < 0) {
            indent = 0;
        }
    }
    public void indentReset() {
        indent = 0;
    }


    private void write(CharSequence text, int start, int length) throws IOException {
        for (int i = start; i < length; i++) {
            write(text.charAt(i));
        }
    }
    private void write(char ch) throws IOException {
        flushIndent();
        this.writer.append(ch);
        this.columnNumber ++;
        this.state_new_line = false;
    }

    public SmaliWriterSetting getWriterSetting() {
        return writerSetting;
    }
    public void setWriterSetting(SmaliWriterSetting writerSetting) {
        this.writerSetting = writerSetting;
    }

    public boolean isLocalRegistersCount() {
        SmaliWriterSetting setting = getWriterSetting();
        if (setting != null) {
            return setting.isLocalRegistersCount();
        }
        return true;
    }
    public boolean isEnableComments() {
        SmaliWriterSetting setting = getWriterSetting();
        if (setting != null) {
            return setting.isEnableComments();
        }
        return true;
    }
    public SequentialLabelFactory getSequentialLabelFactory() {
        return sequentialLabelFactory;
    }
    private SequentialLabelFactory getOrCreateSequentialLabelFactory() {
        SequentialLabelFactory labelFactory = this.sequentialLabelFactory;
        SmaliWriterSetting setting = getWriterSetting();
        if (setting != null) {
            if (setting.isSequentialLabel()) {
                if (labelFactory == null) {
                    labelFactory = new SequentialLabelFactory();
                    this.sequentialLabelFactory = labelFactory;
                }
            } else {
                labelFactory = null;
            }
        }
        return labelFactory;
    }
    public void setSequentialLabelFactory(SequentialLabelFactory sequentialLabelFactory) {
        this.sequentialLabelFactory = sequentialLabelFactory;
    }
    public boolean isCommentUnicodeStrings() {
        SmaliWriterSetting setting = getWriterSetting();
        if (setting != null && setting.isEnableComments() && setting.isCommentUnicodeStrings()) {
            return stateWritingFields || stateWritingInstructions;
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        Writer writer = this.writer;
        if (writer == null) {
            return;
        }
        flushComment();
        this.writer = null;
        writer.close();
        OutputStream outputStream = this.outputStream;
        this.outputStream = null;
        if (outputStream != null) {
            outputStream.close();
        }
    }
    public void reset() {
        this.indent = 0;
        this.lineNumber = 1;
        this.columnNumber = 0;
        this.comment = null;
        this.indentRequested = false;
    }

    public RegistersTable getCurrentRegistersTable() {
        return currentRegistersTable;
    }
    public void setCurrentRegistersTable(RegistersTable currentRegistersTable) {
        this.currentRegistersTable = currentRegistersTable;
        if (currentRegistersTable == null) {
            clearSequentialLabels();
        }
    }
    public void setStateWritingFields(boolean stateWritingFields) {
        this.stateWritingFields = stateWritingFields;
    }
    public void setStateWritingInstructions(boolean stateWritingInstructions) {
        this.stateWritingInstructions = stateWritingInstructions;
    }

    public void buildLabels(Iterator<? extends Label> iterator) {
        SequentialLabelFactory labelFactory = getOrCreateSequentialLabelFactory();
        if (labelFactory != null) {
            labelFactory.build(iterator);
        }
    }
    public void clearSequentialLabels() {
        SequentialLabelFactory labelFactory = getSequentialLabelFactory();
        if (labelFactory != null) {
            labelFactory.reset();
        }
    }

    @Override
    public String toString() {
        return "line = " + getLineNumber() + ", column = " + getColumnNumber();
    }

    public static String toString(SmaliFormat smaliFormat) throws IOException {
        return toString(new SmaliWriter(), smaliFormat);
    }
    public static String toString(SmaliWriter writer, SmaliFormat smaliFormat) throws IOException {
        StringWriter stringWriter = new StringWriter();
        writer.setWriter(stringWriter);
        smaliFormat.append(writer);
        writer.close();
        return stringWriter.toString();
    }
    public static String toStringSafe(SmaliFormat smaliFormat) {
        return toStringSafe(smaliFormat, true);
    }
    public static String toStringSafe(SmaliFormat smaliFormat, boolean comment) {
        if (smaliFormat == null) {
            return "# null";
        }
        StringWriter stringWriter = new StringWriter();
        SmaliWriter writer = new SmaliWriter(stringWriter);
        if (!comment) {
            SmaliWriterSetting setting = writer.getWriterSetting();
            if (setting == null) {
                setting = new SmaliWriterSetting();
                setting.setEnableComments(false);
                writer.setWriterSetting(setting);
            }
        }
        try {
            smaliFormat.append(writer);
            writer.close();
            return stringWriter.toString();
        } catch (IOException exception) {
            return "# " + exception.toString();
        }
    }

    private static final int INDENT_STEP = 4;
}
