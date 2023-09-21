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
package com.reandroid.dex.value;

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class CharValue extends PrimitiveValue {

    public CharValue(){
        super();
    }

    public char getChar(){
        return (char) getNumberValue();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
    }
    @Override
    public String getAsString() {
        StringBuilder builder = new StringBuilder();
        char ch = getChar();
        if ((ch >= ' ') && (ch < 0x7f)) {
            builder.append('\'');
            if ((ch == '\'') || (ch == '\"') || (ch == '\\')) {
                builder.append('\\');
            }
            builder.append(ch);
            builder.append('\'');
            return builder.toString();
        } else if (ch <= 0x7f) {
            switch (ch) {
                case '\n':
                    builder.append("'\\n'");
                    return builder.toString();
                case '\r':
                    builder.append("'\\r'");
                    return builder.toString();
                case '\t':
                    builder.append("'\\t'");
                    return builder.toString();
            }
        }

        builder.append('\'');
        builder.append("\\u");
        builder.append(Character.forDigit(ch >> 12, 16));
        builder.append(Character.forDigit((ch >> 8) & 0x0f, 16));
        builder.append(Character.forDigit((ch >> 4) & 0x0f, 16));
        builder.append(Character.forDigit(ch & 0x0f, 16));
        builder.append('\'');
        return builder.toString();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getAsString());
    }
    @Override
    public String toString() {
        return getAsString();
    }
}
