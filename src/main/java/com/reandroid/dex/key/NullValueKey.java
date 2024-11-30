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

import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public class NullValueKey implements Key {

    public static NullValueKey INSTANCE = new NullValueKey();

    private NullValueKey() {
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == null) {
            return -1;
        }
        return 0;
    }
    @Override
    public int hashCode() {
        return 0;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        return getClass() == obj.getClass();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append("null");
    }

    @Override
    public String toString() {
        return "null";
    }

    public static NullValueKey read(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        SmaliParseException.expect(reader, 'n');
        SmaliParseException.expect(reader, 'u');
        SmaliParseException.expect(reader, 'l');
        SmaliParseException.expect(reader, 'l');
        return INSTANCE;
    }
}
