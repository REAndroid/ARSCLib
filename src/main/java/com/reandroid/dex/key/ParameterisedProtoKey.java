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
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.*;

import java.io.IOException;
import java.util.Iterator;

public class ParameterisedProtoKey extends ArrayKey<ParameterisedTypeKey> {

    public static final ParameterisedProtoKey EMPTY = new ParameterisedProtoKey(
            false, null, EMPTY_ARRAY);


    private final boolean method;
    private final ParameterisedTypeKey returnType;

    private ParameterisedProtoKey(boolean method, ParameterisedTypeKey returnType,
                                 Key[] elements) {
        super(elements);
        this.method = method;
        this.returnType = returnType;
    }

    public boolean isMethod() {
        return method;
    }
    public ParameterisedTypeKey getReturnType() {
        return returnType;
    }
    public ParameterisedProtoKey changeReturnType(ParameterisedTypeKey returnType) {
        if (ObjectsUtil.equals(getReturnType(), returnType)) {
            return this;
        }
        return create(isMethod(), returnType, getElements());
    }
    public boolean isBlank() {
        return isEmpty() && getReturnType() == null;
    }

    public Iterator<TypeKey> getTypes() {
        Iterator<TypeKey> iterator2;
        ParameterisedTypeKey returnType = getReturnType();
        if (returnType != null) {
            iterator2 = returnType.getTypes();
        } else {
            iterator2 = EmptyIterator.of();
        }
        return CombiningIterator.two(
                new IterableIterator<ParameterisedTypeKey, TypeKey>(iterator()) {
                    @Override
                    public Iterator<TypeKey> iterator(ParameterisedTypeKey element) {
                        return element.getTypes();
                    }
                },
                iterator2
        );
    }

    @Override
    public ParameterisedProtoKey replaceKey(Key search, Key replace) {
        ParameterisedProtoKey result = (ParameterisedProtoKey) super.replaceKey(search, replace);
        ParameterisedTypeKey returnType = result.getReturnType();
        if (returnType != null) {
            result = result.changeReturnType(returnType.replaceKey(search, replace));
        }
        return result;
    }

    @Override
    public Iterator<? extends Key> mentionedKeys() {
        return super.mentionedKeys();
    }

    void buildSignature(SignatureStringsBuilder builder) {
        ParameterisedTypeKey returnType = getReturnType();
        if (isBlank()) {
            return;
        }
        boolean method = isMethod();
        builder.append(method? '(' : '<');
        builder.flushPending();
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).buildSignature(builder);
        }
        builder.append(method? ')' : '>');
        if (returnType != null) {
            returnType.buildSignature(builder);
        }
    }

    void appendString(StringBuilder builder) {
        ParameterisedTypeKey returnType = getReturnType();
        if (isBlank()) {
            return;
        }
        boolean method = isMethod();
        builder.append(method? '(' : '<');
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).appendString(builder);
        }
        builder.append(method? ')' : '>');
        if (returnType != null) {
            returnType.appendString(builder);
        }
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == this) {
            return 0;
        }
        if (obj == null) {
            return -1;
        }
        if (!(obj instanceof ParameterisedProtoKey)) {
            return StringsUtil.compareToString(this, obj);
        }
        ParameterisedProtoKey key = (ParameterisedProtoKey) obj;
        int i = CompareUtil.compare(getReturnType(), key.getReturnType());
        if (i == 0) {
            i = super.compareElements(key);
        }
        return i;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ParameterisedProtoKey)) {
            return false;
        }
        ParameterisedProtoKey key = (ParameterisedProtoKey) obj;
        return ObjectsUtil.equals(getReturnType(), key.getReturnType()) &&
                equalsElements(key);
    }

    @Override
    public int hashCode() {
        return ObjectsUtil.hash(getReturnType()) * 31 + getHashCode();
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        appendString(builder);
        return builder.toString();
    }

    @Override
    ParameterisedProtoKey newInstance(Key[] elements) {
        boolean method = isMethod();
        ParameterisedTypeKey returnType = getReturnType();
        if (elements.length == 0) {
            if (!method && returnType == null) {
                return EMPTY;
            }
            elements = EMPTY_ARRAY;
        }
        return new ParameterisedProtoKey(method, returnType, elements);
    }

    public static ParameterisedProtoKey create(boolean method, ParameterisedTypeKey returnType, Key[] elements) {
        if (elements == null || elements.length == 0) {
            if (returnType == null) {
                return EMPTY;
            }
            elements = EMPTY_ARRAY;
        }
        return new ParameterisedProtoKey(method, returnType, elements);
    }

    public static ParameterisedProtoKey read(SmaliReader reader) throws IOException {
        char first = reader.readASCII();
        if (first != '<' && first != '(') {
            reader.position(reader.position() - 1);
            throw new SmaliParseException("Expecting '<' or '(", reader);
        }
        char endChar = first == '<' ? '>' : ')';
        ArrayCollection<ParameterisedTypeKey> results = null;
        while (!reader.finished() && reader.get() != endChar) {
            if (results == null) {
                results = new ArrayCollection<>();
            }
            results.add(ParameterisedTypeKey.read(reader));
        }
        SmaliParseException.expect(reader, endChar);
        ParameterisedTypeKey returnType;
        if (endChar == ')' || (!reader.finished() && reader.get() == '.')) {
            returnType = ParameterisedTypeKey.read(reader);
        } else {
            returnType = null;
        }
        Key[] elements;
        if (results != null) {
            elements = results.toArrayFill(new Key[results.size()]);
        } else {
            elements = null;
        }
        return create(endChar == ')', returnType, elements);
    }
}
