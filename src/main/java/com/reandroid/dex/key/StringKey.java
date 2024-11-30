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
package com.reandroid.dex.key;

import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;

public class StringKey implements Key{

    private final String text;
    private boolean mSignature;

    public StringKey(String text) {
        this.text = text;
    }

    public String getString() {
        return text;
    }
    public String getEncodedString() {
        return DexUtils.encodeString(getString());
    }
    public String getQuoted() {
        return DexUtils.quoteString(getString());
    }
    public String getAsSimpleName() {
        return encodeSimpleName(getString());
    }

    public boolean isSignature() {
        return mSignature;
    }
    public void setSignature(boolean signature) {
        this.mSignature = signature;
    }

    @Override
    public TypeKey getDeclaring() {
        if(!isSignature()){
            return null;
        }
        return TypeKey.parseSignature(getString());
    }
    @Override
    public Iterator<Key> mentionedKeys() {
        return CombiningIterator.singleOne(
                getDeclaring(),
                SingleIterator.of(this));
    }
    @Override
    public Key replaceKey(Key search, Key replace) {
        if(search.equals(this)){
            return replace;
        }
        return this;
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        append(writer, false);
    }
    public void append(SmaliWriter writer, boolean enableComment) throws IOException {
        writer.append('"');
        boolean unicodeDetected = DexUtils.encodeString(writer, getString());
        writer.append('"');
        if(enableComment && unicodeDetected && writer.isCommentUnicodeStrings()) {
            DexUtils.appendCommentString(250, writer.getCommentAppender(), getString());
        }
    }
    public void appendSimpleName(SmaliWriter writer) throws IOException {
        writer.append(getAsSimpleName());
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == this) {
            return 0;
        }
        if (!(obj instanceof StringKey)) {
            return StringsUtil.compareToString(this, obj);
        }
        StringKey key = (StringKey) obj;
        return CompareUtil.compare(getString(), key.getString());
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StringKey)) {
            return false;
        }
        StringKey stringKey = (StringKey) obj;
        return ObjectsUtil.equals(getString(), stringKey.getString());
    }
    @Override
    public int hashCode() {
        return getString().hashCode();
    }
    @Override
    public String toString() {
        return getQuoted();
    }

    public static StringKey create(String text){
        if(text == null){
            return null;
        }
        if(text.length() == 0){
            return EMPTY;
        }
        return new StringKey(text);
    }
    public static StringKey parseQuotedString(String quotedString) {
        if(quotedString == null || quotedString.length() < 2) {
            return null;
        }
        SmaliReader reader = SmaliReader.of(quotedString);
        if(reader.get() != '\"') {
            return null;
        }
        String str;
        try {
            reader.skip(1);
            str = reader.readEscapedString('"');
            if(reader.available() != 1 || reader.get() != '\"') {
                return null;
            }
        } catch (IOException ignored) {
            return null;
        }
        return create(str);
    }
    public static StringKey read(SmaliReader reader) throws IOException{
        reader.skipSpaces();
        SmaliParseException.expect(reader, '\"');
        String str = reader.readEscapedString('"');
        SmaliParseException.expect(reader, '\"');
        return create(str);
    }

    public static String encodeSimpleName(String name) {
        StringBuilder builder = null;
        int length = name.length();
        for (int i = 0; i < length; i++) {
            char c = name.charAt(i);
            String encoded = encodeSimpleName(c);
            if (encoded != null) {
                if (builder == null) {
                    builder = new StringBuilder();
                    builder.append(name, 0, i);
                }
                builder.append(encoded);
            } else if (builder != null) {
                builder.append(c);
            }
        }
        if (builder == null) {
            return name;
        }
        return builder.toString();
    }
    private static String encodeSimpleName(char c) {
        String encoded;
        if (c == ' ') {
            encoded = HexUtil.toHex( "\\u", c, 4);
        } else if (c == '\n') {
            encoded = "\\n";
        } else if (c == '\r') {
            encoded = "\\r";
        } else if (c == '\t') {
            encoded = "\\t";
        } else if (c == '\b') {
            encoded = "\\b";
        } else if (c == '\f') {
            encoded = "\\f";
        } else {
            encoded = null;
        }
        return encoded;
    }

    public static String decodeEscapedString(String encoded) {
        return decodeEscapedString(encoded, 0);
    }
    public static String decodeEscapedString(String encoded, int start) {
        StringBuilder builder = null;
        int length = encoded.length();
        boolean escaped = false;
        for (int i = start; i < length; i++) {
            char c = encoded.charAt(i);
            if (escaped) {
                if (builder == null) {
                    builder = new StringBuilder();
                    builder.append(encoded, start, i - 1);
                }
                if (c == 'u') {
                    Character hex = decodeNextHex(encoded, i + 1);
                    if (hex != null) {
                        builder.append(hex.charValue());
                        i = i + 4;
                    } else {
                        builder.append(c);
                    }
                } else {
                    builder.append(getEscaped(c));
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (builder != null) {
                builder.append(c);
            }
        }
        if (builder == null) {
            return encoded;
        }
        return builder.toString();
    }
    private static char getEscaped(char c) {
        if (c == 'n') {
            return '\n';
        }
        if (c == 'b') {
            return '\b';
        }
        if (c == 'f') {
            return '\f';
        }
        if (c == 'r') {
            return '\r';
        }
        if (c == 't') {
            return '\t';
        }
        return c;
    }
    private static Character decodeNextHex(String text, int start) {
        int length = text.length();
        int end = start + 4;
        if (end > length) {
            return null;
        }
        int value = 0;
        for (int i = start; i < end; i++) {
            int v = HexUtil.decodeHexChar(text.charAt(i));
            if (v == -1) {
                return null;
            }
            value = value << 4;
            value = value | v;
        }
        return (char) value;
    }
    public static StringKey readSimpleName(SmaliReader reader, char stopChar) throws IOException {
        int position = reader.position();
        String name = reader.readEscapedString(stopChar);
        SmaliParseException.expect(reader, stopChar);
        reader.skip(-1);
        String error = validateSimpleName(name);
        if (error != null) {
            reader.position(position);
            throw new SmaliParseException(error, reader);
        }
        return create(name);
    }

    private static String validateSimpleName(String name) {
        int length = name.length();
        if (length == 0) {
            return "Invalid name";
        }
        for (int i = 0; i < length; i++) {
            char c = name.charAt(i);
            if (isInvalidSimpleName(c)) {
                return "Invalid name character '" + c + "'";
            }
        }
        return null;
    }
    private static boolean isInvalidSimpleName(char c) {
        return c == '(' ||
                c == ')' ||
                c == '[' ||
                c == ']' ||
                c == '{' ||
                c == '}' ||
                c == ';' ||
                c == '/' ||
                c == '\\' ||
                c == ':' ||
                c == '.' ||
                c == '*' ||
                c == '%' ||
                c == '|' ||
                c == '^';
    }

    public static final StringKey EMPTY = new StringKey(StringsUtil.EMPTY);
}
