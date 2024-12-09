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
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.IterableIterator;

import java.io.IOException;
import java.util.Iterator;

public class DalvikSignatureKey extends ArrayKey<ParameterisedTypeKey> {

    private DalvikSignatureKey(Key[] elements) {
        super(elements);
    }

    public ParameterisedTypeKey getProtoParameter(int parameterIndex) {
        ParameterisedProtoKey protoKey = getMethodProto();
        if (protoKey != null) {
            return protoKey.get(parameterIndex);
        }
        return null;
    }
    public ParameterisedProtoKey getMethodProto() {
        int size = size();
        for (int i = 0; i < size; i++) {
            ParameterisedTypeKey typeKey = get(i);
            ParameterisedProtoKey protoKey = typeKey.getProtoKey();
            if (protoKey.isMethod()) {
                return protoKey;
            }
        }
        return null;
    }
    public Iterator<TypeKey> getTypes() {
        return new IterableIterator<ParameterisedTypeKey, TypeKey>(iterator()) {
            @Override
            public Iterator<TypeKey> iterator(ParameterisedTypeKey element) {
                return element.getTypes();
            }
        };
    }

    public ArrayValueKey toStringValues() {
        DalvikSignatureBuilder builder = new DalvikSignatureBuilder();
        int size  = size();
        for (int i = 0; i < size; i++) {
            get(i).buildSignature(builder);
        }
        builder.flush();
        return builder.build();
    }

    @Override
    public DalvikSignatureKey replaceKey(Key search, Key replace) {
        return (DalvikSignatureKey) super.replaceKey(search, replace);
    }

    @Override
    protected DalvikSignatureKey newInstance(Key[] elements) {
        return createKey(elements);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int size = size();
        for (int i = 0; i < size; i ++) {
            get(i).appendString(builder, false);
        }
        return builder.toString();
    }

    public static DalvikSignatureKey of(ParameterisedTypeKey ... elements) {
        if (elements == null || elements.length == 0) {
            return null;
        }
        return createKey(elements);
    }
    private static DalvikSignatureKey createKey(Key[] elements) {
        return new DalvikSignatureKey(elements);
    }
    public static DalvikSignatureKey parseAnnotationValue(ArrayValueKey stringsKey) {
        if (stringsKey != null) {
            return parse(StringsUtil.join(stringsKey.stringValuesIterator(), ""));
        }
        return null;
    }
    public static DalvikSignatureKey parse(String text) {
        try {
            return read(SmaliReader.of(text));
        } catch (IOException ignored) {
            return null;
        }
    }
    public static DalvikSignatureKey read(SmaliReader reader) throws IOException {
        reader = ensurePlainString(reader);
        ArrayCollection<Key> results = null;
        while (!reader.finished() && !reader.skipWhitespaces()) {
            ParameterisedTypeKey typeKey = ParameterisedTypeKey.read(reader);
            if (results == null) {
                results = new ArrayCollection<>();
            }
            results.add(typeKey);
        }
        if (results == null) {
            if (reader.finished()) {
                throw new IOException("EOF: Invalid signature");
            }
            reader.position(reader.position() - 1);
            throw new SmaliParseException("Whitespace detected", reader);
        }
        return createKey(results.toArrayFill(new Key[results.size()]));
    }
    private static SmaliReader ensurePlainString(SmaliReader reader) throws IOException {
        if (reader.finished()) {
            return reader;
        }
        int position = reader.position();
        reader.skipWhitespacesOrComment();
        if (reader.finished() || reader.get() != '{') {
            reader.position(position);
            return reader;
        }
        ArrayValueKey arrayKey = ArrayValueKey.read(reader);
        String plain = StringsUtil.join(arrayKey.stringValuesIterator(), "");
        return SmaliReader.of(plain);
    }
}
