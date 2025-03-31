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

import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;

public class KeyUtil {

    public static final String ANY_NAME = ObjectsUtil.of("*");

    public static boolean matches(String name1, String name2){
        if(name1 == null){
            if(name2 == null){
                return true;
            }
            return name2.equals(ANY_NAME);
        }
        if(name2 == null){
            return false;
        }
        return name1.equals(name2) ||
                name1.equals(ANY_NAME) ||
                name2.equals(ANY_NAME);
    }

    public static Key readKey(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        if (directive != null) {
            if (directive == SmaliDirective.SUB_ANNOTATION) {
                return AnnotationItemKey.read(reader);
            }
            if (directive == SmaliDirective.ENUM) {
                return EnumKey.read(reader);
            }
            throw new SmaliParseException("Unexpected directive", reader);
        }
        char first = reader.getASCII(reader.position());
        if (first == '{') {
            return ArrayValueKey.read(reader);
        }
        if (first == '"') {
            return StringKey.read(reader);
        }
        if (first == '(') {
            return ProtoKey.read(reader);
        }
        if (first == 'n') {
            return NullValueKey.read(reader);
        }
        Key key = PrimitiveKey.readSafe(reader);
        if (key != null) {
            return key;
        }
        key = MethodHandleKey.read(reader);
        if (key != null) {
            return key;
        }
        key = TypeKey.primitiveType(first);
        if (key != null) {
            reader.readASCII();
            return key;
        }
        if (first == 'L' || first == '[') {
            TypeKey typeKey = TypeKey.read(reader);
            if (reader.finished() || reader.get() != '-') {
                return typeKey;
            }
            reader.readASCII();
            SmaliParseException.expect(reader, '>');
            reader.skipWhitespacesOrComment();
            StringKey name = StringKey.readSimpleName(reader);
            reader.skipWhitespacesOrComment();
            char c = SmaliParseException.expect(reader, '(', ':');
            if (c == '(') {
                reader.skip(-1);
                ProtoKey protoKey = ProtoKey.read(reader);
                return MethodKey.create(typeKey, name, protoKey);
            }
            return FieldKey.create(typeKey, name, TypeKey.read(reader));
        }
        throw new SmaliParseException("Unrecognized value", reader);
    }

    public static TypeKey getReturnTypeForValue(Key value) {
        return getReturnTypeForValue(value, 0);
    }
    private static TypeKey getReturnTypeForValue(Key value, int arrayDimension) {
        if (value instanceof StringKey) {
            return TypeKey.STRING.setArrayDimension(arrayDimension);
        }
        if (value instanceof TypeKey) {
            return TypeKey.CLASS.setArrayDimension(arrayDimension);
        }
        if (value instanceof PrimitiveKey) {
            return ((PrimitiveKey) value).valueType().setArrayDimension(arrayDimension);
        }
        if (value instanceof EnumKey) {
            return ((EnumKey) value).getType().setArrayDimension(arrayDimension);
        }
        if (value instanceof AnnotationItemKey) {
            return ((AnnotationItemKey) value).getType().setArrayDimension(arrayDimension);
        }
        if (value instanceof KeyList<?>) {
            KeyList<?> keyList = (KeyList<?>) value;
            return getReturnTypeForValue(keyList.get(0), arrayDimension + 1);
        }
        return null;
    }

}
