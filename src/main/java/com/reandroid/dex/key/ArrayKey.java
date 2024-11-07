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
import com.reandroid.utils.collection.ArrayCollection;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.function.Predicate;

public class ArrayKey extends KeyList<Key> {

    private static final Key[] EMPTY = new Key[0];

    public ArrayKey(Key[] elements) {
        super(checkNullOrEmpty(elements));
    }

    @Override
    public ArrayKey add(Key item) {
        return (ArrayKey) super.add(item);
    }
    @Override
    public ArrayKey remove(Key itemKey) {
        return (ArrayKey) super.remove(itemKey);
    }
    @Override
    public ArrayKey remove(int index) {
        return (ArrayKey) super.remove(index);
    }
    @Override
    public ArrayKey removeIf(Predicate<? super Key> predicate) {
        return (ArrayKey) super.removeIf(predicate);
    }
    @Override
    public ArrayKey set(int i, Key item) {
        return (ArrayKey) super.set(i, item);
    }

    @Override
    ArrayKey newInstance(Key[] elements) {
        return new ArrayKey(elements);
    }
    @Override
    Key[] newArray(int length) {
        if (length == 0) {
            return EMPTY;
        }
        return new Key[length];
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        append(writer, ", ");
    }

    public void append(SmaliWriter writer, String separator) throws IOException {
        boolean appendOnce = false;
        int size = this.size();
        for (int i = 0; i < size; i++) {
            Key key = get(i);
            if (appendOnce) {
                writer.append(separator);
            }
            if (key == null) {
                writer.append("# null");
            } else {
                key.append(writer);
            }
            appendOnce = true;
        }
    }

    @Override
    public String toString() {
        return toString(", ");
    }

    public String toString(String separator) {
        StringWriter stringWriter = new StringWriter();
        SmaliWriter writer = new SmaliWriter(stringWriter);
        try {
            this.append(writer, separator);
            writer.close();
            return stringWriter.toString();
        } catch (IOException exception) {
            return "# " + exception.toString();
        }
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == this) {
            return 0;
        }
        if (!(obj instanceof ArrayKey)) {
            return 0;
        }
        return compareElements((ArrayKey) obj);
    }

    @Override
    public int hashCode() {
        return getHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ArrayKey)) {
            return false;
        }
        return equalsElements((ArrayKey) obj);
    }

    public static ArrayKey read(SmaliReader reader, char end) throws IOException {
        reader.skipWhitespacesOrComment();
        if (reader.getASCII(reader.position()) == end) {
            reader.readASCII();
            return new ArrayKey(new Key[0]);
        }
        List<Key> results = new ArrayCollection<>();
        while (true) {
            Key key = readNext(reader);
            results.add(key);
            reader.skipWhitespacesOrComment();
            if (reader.getASCII(reader.position()) == end) {
                break;
            }
            SmaliParseException.expect(reader, ',');
        }
        SmaliParseException.expect(reader, end);
        return new ArrayKey(results.toArray(new Key[results.size()]));
    }
    private static Key readNext(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        char c = reader.getASCII(reader.position());
        if (c == '"') {
            return StringKey.read(reader);
        }
        Key key = MethodHandleKey.read(reader);
        if (key != null) {
            return key;
        }
        key = TypeKey.primitiveType(c);
        if (key != null) {
            reader.readASCII();
            return key;
        }
        int lineEnd = reader.indexOfBeforeLineEnd(',');
        if (lineEnd < 0) {
            lineEnd = reader.indexOfLineEnd();
        }
        if (c == 'L' || c == '[') {
            int i = reader.indexOfBeforeLineEnd('>');
            if (i < 0 || i > lineEnd) {
                return TypeKey.read(reader);
            }
            c = reader.getASCII(i + 1);
            if (c == '(') {
                return MethodKey.read(reader);
            }
            if (c != ':') {
                throw new SmaliParseException("Expecting ':'", reader);
            }
            return FieldKey.read(reader);
        }
        return StringKey.read(reader);
    }

    private static Key[] checkNullOrEmpty(Key[] elements) {
        if (elements == null || elements.length == 0) {
            return EMPTY;
        }
        return elements;
    }
}
