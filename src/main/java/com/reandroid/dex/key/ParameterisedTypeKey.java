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
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;

public class ParameterisedTypeKey implements Key {

    private final ParameterName parameterName;
    private final ParameterisedProtoKey protoKey;

    private ParameterisedTypeKey(ParameterName parameterName, ParameterisedProtoKey protoKey) {
        this.parameterName = parameterName;
        this.protoKey = protoKey;
    }

    public String getName() {
        ParameterName name = getParameterName();
        if (name != null) {
            return name.getName();
        }
        return null;
    }
    public ParameterisedProtoKey getProtoKey() {
        return protoKey;
    }
    public ParameterisedTypeKey changeProtoKey(ParameterisedProtoKey protoKey) {
        if (ObjectsUtil.equals(getProtoKey(), protoKey)) {
            return this;
        }
        return create(getParameterName(), protoKey);
    }

    ParameterName getParameterName() {
        return parameterName;
    }
    @Override
    public TypeKey getDeclaring() {
        TypeKey typeKey = getInnerClassKey();
        if (typeKey == null) {
            typeKey = getNameTypeKey();
        }
        return typeKey;
    }

    private TypeKey getNameTypeKey() {
        ParameterName name = getParameterName();
        if (name != null) {
            return name.getDeclaring();
        }
        return null;
    }
    private ParameterisedTypeKey changeParameterName(ParameterName name) {
        if (ObjectsUtil.equals(getParameterName(), name)) {
            return this;
        }
        return create(name, getProtoKey());
    }
    private ParameterisedTypeKey changeParameterName(TypeKey typeKey) {
        TypeKey key = getNameTypeKey();
        if (key == null || ObjectsUtil.equals(key, typeKey)) {
            return this;
        }
        return changeParameterName(getParameterName().changeName(typeKey));
    }
    private TypeKey getInnerClassKey() {
        ParameterisedTypeKey key = getProtoKey().getReturnType();
        if (key == null) {
            return null;
        }
        ParameterName innerName = key.getParameterName();
        if (innerName == null || !innerName.isInnerName()) {
            return null;
        }
        ParameterName parameterName = getParameterName();
        if (parameterName == null) {
            return null;
        }
        String name = parameterName.getName();
        String inner = innerName.getName().replace('.', '$');
        return TypeKey.create(name + inner + ";");
    }
    private ParameterisedTypeKey changeInnerKey(TypeKey innerKey) {
        TypeKey key = getInnerClassKey();
        if (key == null || key.equals(innerKey)) {
            return this;
        }
        ParameterisedProtoKey protoKey = getProtoKey();
        ParameterisedTypeKey returnType = protoKey.getReturnType();
        if (returnType == null) {
            return this;
        }
        ParameterName name = returnType.getParameterName();
        if (!(name instanceof InnerClassName)) {
            return this;
        }
        InnerClassName innerClassName = (InnerClassName) name;
        String inner = innerClassName.createInnerName(innerKey);
        if (inner == null) {
            return this;
        }
        TypeKey outerKey = innerClassName.createOuterKey(innerKey);
        if (outerKey == null) {
            return this;
        }
        innerClassName = innerClassName.changeName(inner);
        returnType = returnType.changeParameterName(innerClassName);
        protoKey = protoKey.changeReturnType(returnType);
        ParameterisedTypeKey result = this.changeProtoKey(protoKey);
        result = result.changeParameterName(outerKey);
        return result;
    }

    public Iterator<TypeKey> getTypes() {
        return CombiningIterator.singleTwo(getNameTypeKey(),
                getProtoKey().getTypes(),
                SingleIterator.of(getInnerClassKey()));
    }

    void buildSignature(SignatureStringsBuilder builder) {
        ParameterName name = getParameterName();
        boolean needSemicolon = false;
        if (name != null) {
            name.buildSignature(builder);
            needSemicolon = name.isTypeUse();
        }

        getProtoKey().buildSignature(builder);

        if (needSemicolon) {
            builder.append(';');
            builder.flushPending();
        }
    }

    @Override
    public ParameterisedTypeKey replaceKey(Key search, Key replace) {
        if (search.equals(this)) {
            return (ParameterisedTypeKey) replace;
        }
        ParameterisedTypeKey result = this;
        if (ObjectsUtil.equals(result.getInnerClassKey(), search)) {
            result = result.changeInnerKey((TypeKey) replace);
        }
        if (ObjectsUtil.equals(result.getNameTypeKey(), search)) {
            result = result.changeParameterName((TypeKey) replace);
        }
        ParameterisedProtoKey protoKey = result.getProtoKey();
        protoKey = protoKey.replaceKey(search, replace);
        result = result.changeProtoKey(protoKey);
        return result;
    }

    @Override
    public Iterator<? extends Key> mentionedKeys() {
        return getTypes();
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == this) {
            return 0;
        }
        if (obj == null) {
            return -1;
        }
        if (!(obj instanceof ParameterisedTypeKey)) {
            return StringsUtil.compareToString(this, obj);
        }
        ParameterisedTypeKey key = (ParameterisedTypeKey) obj;
        int i = CompareUtil.compare(getParameterName(), key.getParameterName());
        if (i == 0) {
            i = CompareUtil.compare(getProtoKey(), key.getProtoKey());
        }
        return i;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ParameterisedTypeKey)) {
            return false;
        }
        ParameterisedTypeKey key = (ParameterisedTypeKey) obj;
        return ObjectsUtil.equals(getParameterName(), key.getParameterName()) &&
                ObjectsUtil.equals(getProtoKey(), key.getProtoKey());
    }

    @Override
    public int hashCode() {
        return ObjectsUtil.hash(getParameterName(), getProtoKey());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        appendString(builder);
        return builder.toString();
    }
    void appendString(StringBuilder builder) {
        ParameterName name = getParameterName();
        boolean needSemicolon = false;
        if (name != null) {
            name.appendString(builder);
            needSemicolon = name.isTypeUse();
        }
        getProtoKey().appendString(builder);
        if (needSemicolon) {
            builder.append(';');
        }
    }

    public static ParameterisedTypeKey create(ParameterName name, ParameterisedProtoKey protoKey) {
        if (protoKey == null || protoKey.isBlank()) {
            if (name == null) {
                return null;
            }
            protoKey = ParameterisedProtoKey.EMPTY;
        }
        return new ParameterisedTypeKey(name,protoKey);
    }
    public static ParameterisedTypeKey read(SmaliReader reader) throws IOException {
        ParameterName name = readName(reader);
        ParameterisedProtoKey protoKey = null;
        if (!reader.finished()) {
            int c = reader.get() & 0xff;
            if (c == '<' || (c == '(' && name == null)) {
                protoKey = ParameterisedProtoKey.read(reader);
            }
        }
        if (name != null && name.isTypeUse()) {
            SmaliParseException.expect(reader, ';');
        }
        ParameterisedTypeKey typeKey = create(name, protoKey);
        if (typeKey == null) {
            throw new SmaliParseException("Invalid ParameterisedTypeKey", reader);
        }
        return typeKey;
    }

    private static ParameterName readName(SmaliReader reader) {
        char c = reader.getASCII(reader.position());
        if (isNameStop(c)) {
            return null;
        }
        ParameterName name = Wild.readWild(reader);
        if (name == null) {
            name = InnerClassName.readInnerClassName(reader);
        }
        if (name == null) {
            name = Definition.readDefinition(reader);
        }
        if (name == null) {
            name = TypeUsePrimitive.readTypeUsePrimitive(reader);
        }
        if (name == null) {
            name = TypeUse.readTypeUse(reader);
        }
        return name;
    }
    static boolean isNameStop(char c) {
        return c == '<' ||
                c == '>' ||
                c == '(' ||
                c == ')' ||
                c == ';';
    }
    static boolean isWild(char c) {
        return c == ':' ||
                c == '-' ||
                c == '+' ||
                c == '*';
    }

    static abstract class ParameterName implements Key {

        private final String name;

        ParameterName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
        public ParameterName changeName(TypeKey typeKey) {
            return this;
        }
        public abstract ParameterName changeName(String name);
        boolean isTypeUse() {
            return false;
        }
        boolean isClassType() {
            return false;
        }
        boolean isInnerName() {
            return false;
        }
        @Override
        public TypeKey getDeclaring() {
            return null;
        }

        public void buildSignature(SignatureStringsBuilder builder) {
            appendString(builder.getStringBuilder());
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.append(getName());
        }
        public void appendString(StringBuilder builder) {
            builder.append(getName());
        }

        @Override
        public int compareTo(Object obj) {
            if (obj == this) {
                return 0;
            }
            if (obj == null) {
                return -1;
            }
            if (!(getClass().isInstance(obj))) {
                return StringsUtil.compareToString(this, obj);
            }
            ParameterName name = (ParameterName) obj;
            return CompareUtil.compare(getName(), name.getName());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!getClass().isInstance(obj)) {
                return false;
            }
            ParameterName name = (ParameterName) obj;
            return ObjectsUtil.equals(getName(), name.getName());
        }

        @Override
        public int hashCode() {
            return ObjectsUtil.hash(getName());
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    static class Wild extends ParameterName {

        static final Wild SUPER = new Wild("-");
        static final Wild EXTENDS = new Wild("+");
        static final Wild ANY = new Wild("*");

        Wild(String name) {
            super(name);
        }

        @Override
        public Wild changeName(String name) {
            if (name.length() != 1 || getName().equals(name)) {
                return this;
            }
            Wild result = getWild(name.charAt(0));
            if (result == null) {
                result = this;
            }
            return result;
        }
        static Wild readWild(SmaliReader reader) {
            int position = reader.position();
            Wild wild = getWild(reader.getASCII(position));
            if (wild != null) {
                reader.position(position + 1);
                return wild;
            }
            return null;
        }
        static Wild getWild(char c) {
            if (c == '-') {
                return SUPER;
            }
            if (c == '+') {
                return EXTENDS;
            }
            if (c == '*') {
                return ANY;
            }
            return null;
        }
    }

    static class Definition extends ParameterName {

        private final int colons;

        Definition(String name, int colons) {
            super(name);
            this.colons = colons;
        }

        @Override
        public Definition changeName(String name) {
            if (getName().equals(name)) {
                return this;
            }
            return new Definition(name, colons);
        }

        @Override
        public void appendString(StringBuilder builder) {
            builder.append(getName());
            int colons = this.colons;
            for (int i = 0; i < colons; i++) {
                builder.append(':');
            }
        }
        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.append(getName());
            int colons = this.colons;
            for (int i = 0; i < colons; i++) {
                writer.append(':');
            }
        }
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            appendString(builder);
            return builder.toString();
        }

        static Definition readDefinition(SmaliReader reader) {
            int colons = 0;
            int start = reader.position();
            int end = start + reader.available();
            int i = start;
            while (i < end) {
                char c = reader.getASCII(i);
                if (c == ':') {
                    colons ++;
                } else if (colons != 0 || isDefinitionStop(c)) {
                    break;
                }
                i ++;
            }
            if (colons == 0) {
                return null;
            }
            int length = i - start - colons;
            String name = reader.readString(length);
            reader.position(reader.position() + colons);
            return new Definition(name, colons);
        }
        static boolean isDefinitionStop(char c) {
            return ParameterisedTypeKey.isNameStop(c) ||
                    ParameterisedTypeKey.isWild(c) ||
                    c == '/' ||
                    c == '.';
        }
    }

    static class InnerClassName extends ParameterName {

        InnerClassName(String name) {
            super(name);
        }

        @Override
        boolean isInnerName() {
            return true;
        }

        @Override
        public InnerClassName changeName(String name) {
            if (getName().equals(name)) {
                return this;
            }
            return new InnerClassName(name);
        }
        public String createInnerName(TypeKey typeKey) {
            String[] simpleNames = StringsUtil.split(typeKey.getSimpleName(), '$');
            String name = getName();
            String[] current = StringsUtil.split(name.substring(1), '.');
            int length = current.length;
            int simpleNamesLength = simpleNames.length;
            int index = simpleNamesLength - length;
            if (index <= 0) {
                return null;
            }
            StringBuilder builder = new StringBuilder();
            for (int i = index; i < simpleNamesLength; i++) {
                builder.append('.');
                builder.append(simpleNames[i]);
            }
            return builder.toString();
        }
        public TypeKey createOuterKey(TypeKey typeKey) {
            String[] simpleNames = StringsUtil.split(typeKey.getSimpleName(), '$');
            String name = getName();
            String[] current = StringsUtil.split(name.substring(1), '.');
            int length = current.length;
            int simpleNamesLength = simpleNames.length;
            int end = simpleNamesLength - length;
            if (end <= 0) {
                return null;
            }
            StringBuilder builder = new StringBuilder();
            builder.append(typeKey.getPackageName());
            for (int i = 0; i < end; i++) {
                if (i != 0) {
                    builder.append('$');
                }
                builder.append(simpleNames[i]);
            }
            builder.append(';');
            return TypeKey.create(builder.toString());
        }

        static InnerClassName readInnerClassName(SmaliReader reader) {
            int start = reader.position();
            int end = start + reader.available();
            int i = start;
            boolean first = true;
            while (i < end) {
                char c = reader.getASCII(i);
                if (c == '[') {
                    if (!first) {
                        break;
                    }
                    i ++;
                    continue;
                }
                if (first && c != '.') {
                    return null;
                }
                if (isNameStop(c) || isWild(c)) {
                    break;
                }
                first = false;
                i ++;
            }
            if (i == start) {
                return null;
            }
            String name = reader.readString(i - reader.position());
            return new InnerClassName(name);
        }
    }
    static class TypeUse extends ParameterName {

        TypeUse(String name) {
            super(name);
        }

        @Override
        public TypeUse changeName(TypeKey typeKey) {
            String name = typeKey.getTypeName();
            name = name.substring(0, name.length() - 1);
            return changeName(name);
        }

        @Override
        public TypeUse changeName(String name) {
            if (getName().equals(name)) {
                return this;
            }
            return new TypeUse(name);
        }

        @Override
        boolean isTypeUse() {
            return true;
        }

        @Override
        boolean isClassType() {
            String name = getName();
            int length = name.length();
            int i = 0;
            while (i < length && name.charAt(i) == '[') {
                i ++;
            }
            if (i + 2 >= length) {
                return false;
            }
            return name.charAt(i) == 'L';
        }

        @Override
        public TypeKey getDeclaring() {
            if (isClassType()) {
                return TypeKey.create(getName() + ";");
            }
            return null;
        }

        @Override
        public void buildSignature(SignatureStringsBuilder builder) {
            boolean flushed = false;
            if (isClassType()) {
                builder.flush();
                flushed = true;
            }
            builder.append(getName());
            if (flushed) {
                builder.markFlush();
            }
        }

        static TypeUse readTypeUse(SmaliReader reader) {
            int start = reader.position();
            int end = start + reader.available();
            int i = start;
            boolean first = true;
            while (i < end) {
                char c = reader.getASCII(i);
                if (c == '[') {
                    if (!first) {
                        break;
                    }
                    i ++;
                    continue;
                }
                if (isNameStop(c) || isWild(c)) {
                    break;
                }
                first = false;
                i ++;
            }
            if (i == start) {
                return null;
            }
            String name = reader.readString(i - start);
            return new TypeUse(name);
        }
    }
    static class TypeUsePrimitive extends ParameterName {

        TypeUsePrimitive(String name) {
            super(name);
        }

        @Override
        public TypeUsePrimitive changeName(String name) {
            if (getName().equals(name)) {
                return this;
            }
            return new TypeUsePrimitive(name);
        }

        static TypeUsePrimitive readTypeUsePrimitive(SmaliReader reader) {
            int start = reader.position();
            int end = start + reader.available();
            int i = start;
            while (i < end) {
                char c = reader.getASCII(i);
                if (c == '[') {
                    i ++;
                    continue;
                }
                if (!TypeKey.isPrimitive(c)) {
                    return null;
                }
                int i2 = i + 1;
                if (i2 < end) {
                    char next = reader.getASCII(i2);
                    if (next == ';' || next == '<') {
                        return null;
                    }
                }
                i ++;
                String name = reader.readString(i - start);
                return new TypeUsePrimitive(name);
            }
            return null;
        }
    }
}
