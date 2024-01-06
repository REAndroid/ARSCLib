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
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class StringKey implements Key{

    private final String text;
    private boolean mSignature;

    public StringKey(String text) {
        this.text = text;
    }

    public String getString() {
        return text;
    }

    @Override
    public boolean isPlatform() {
        return false;
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
    public void append(SmaliWriter writer) throws IOException{
        writer.append(DexUtils.quoteString(getString()));
    }
    @Override
    public int compareTo(Object obj) {
        if(obj == null){
            return -1;
        }
        StringKey key = (StringKey) obj;
        return CompareUtil.compare(getString(), key.getString());
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj || obj == ANY) {
            return true;
        }
        if (!(obj instanceof StringKey)) {
            return false;
        }
        StringKey stringKey = (StringKey) obj;
        return Objects.equals(getString(), stringKey.getString());
    }
    @Override
    public int hashCode() {
        return getString().hashCode();
    }
    @Override
    public String toString() {
        return DexUtils.quoteString(getString());
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
    public static StringKey read(SmaliReader reader) throws IOException{
        reader.skipSpaces();
        if(reader.readASCII() != '"'){
            throw new SmaliParseException("Invalid string key, missing begin quote '\"'", reader);
        }
        int i = reader.indexOf('\n');
        if(i < 0){
            throw new SmaliParseException("Invalid string key, missing end quote '\"'", reader);
        }
        i = i - reader.position();
        StringBuilder builder = new StringBuilder(i);
        boolean skipped = false;
        while (true){
            if(reader.get() == '\n'){
                reader.skip(-1);
                throw new SmaliParseException("Invalid string key, missing end quote '\"'", reader);
            }
            char ch = reader.readASCII();
            if(skipped){
                builder.append(decodeSkipped(reader, ch));
                skipped = false;
                continue;
            }
            if(ch == '\\'){
                skipped = true;
                continue;
            }
            if(ch == '"'){
                break;
            }
            builder.append(ch);
        }
        return new StringKey(builder.toString());
    }
    private static char decodeSkipped(SmaliReader reader, char ch){
        switch (ch){
            case 'n':
                return '\n';
            case 'r':
                return  '\r';
            case 't':
                return '\t';
            case 'u':
                return decodeHex(reader);
            default:
                return ch;
        }
    }
    private static char decodeHex(SmaliReader reader){
        String hex = reader.readString(4);
        int i = HexUtil.parseHex(hex);
        return (char) i;
    }
    public static final StringKey EMPTY = new StringKey(StringsUtil.EMPTY);

    public static final StringKey ANY = new StringKey(StringsUtil.EMPTY){
        @Override
        public boolean equals(Object obj) {
            return obj == this;
        }
        @Override
        public int hashCode() {
            return -1;
        }
        @Override
        public String toString() {
            return StringsUtil.EMPTY;
        }
    };
}
