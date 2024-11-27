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
package com.reandroid.dex.smali.model;

import com.reandroid.dex.key.*;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;

import java.io.IOException;

public class SmaliValueFactory {

    public static SmaliValue create(SmaliReader reader) throws IOException {
        reader.skipSpaces();
        int position = reader.position();
        byte b = reader.get(position);
        if(b == '.'){
            SmaliDirective directive = SmaliDirective.parse(reader, false);
            if(directive == SmaliDirective.ENUM){
                return new SmaliValueEnum();
            }
            if(directive == SmaliDirective.SUB_ANNOTATION){
                return new SmaliValueAnnotation();
            }
            throw new SmaliParseException("Unrecognized value", reader);
        }
        if(b == '{'){
            return new SmaliValueArray();
        }
        if(b == '\''){
            return new SmaliValueChar();
        }
        if(b == 'n'){
            return new SmaliValueNull();
        }
        if(b == 't' || b == 'f'){
            return new SmaliValueBoolean();
        }
        if(b == '-' || b == '+'){
            position ++;
            b = reader.get(position);
        }
        if(b == '0' && reader.get(position + 1) == 'x'){
            b = reader.get(reader.indexOfWhiteSpaceOrComment() - 1);
            if(b == 't'){
                return new SmaliValueByte();
            }
            if(b == 'S' || b == 's'){
                return new SmaliValueShort();
            }
            if(b == 'L'){
                return new SmaliValueLong();
            }
            return new SmaliValueInteger();
        }
        byte[] infinity = new byte[]{'I', 'n', 'f', 'i', 'n', 'i', 't', 'y'};
        if(reader.startsWith(infinity, position)){
            b = reader.get(infinity.length + position);
            if(b == 'f'){
                return new SmaliValueFloat();
            }
            return new SmaliValueDouble();
        }
        byte[] nan = new byte[]{'N', 'a', 'N'};
        if(reader.startsWith(nan, position)){
            b = reader.get(nan.length + position);
            if(b == 'f'){
                return new SmaliValueFloat();
            }
            return new SmaliValueDouble();
        }
        if(b <= '9' && b >= '0'){
            b = reader.get(reader.indexOfWhiteSpaceOrComment() - 1);
            if(b == 'f'){
                return new SmaliValueFloat();
            }
            return new SmaliValueDouble();
        }
        return new SmaliValueSectionData();
    }

    public static SmaliValue createForField(TypeKey typeKey){
        SmaliValue smaliValue;
        if(typeKey.isTypeArray() || !typeKey.isPrimitive()) {
            smaliValue = new SmaliValueNull();
        }else if(TypeKey.TYPE_I.equals(typeKey)) {
            smaliValue = new SmaliValueInteger();
        } else if(TypeKey.TYPE_J.equals(typeKey)) {
            smaliValue = new SmaliValueLong();
        } else if(TypeKey.TYPE_D.equals(typeKey)) {
            smaliValue = new SmaliValueDouble();
        } else if(TypeKey.TYPE_F.equals(typeKey)) {
            smaliValue = new SmaliValueFloat();
        } else if(TypeKey.TYPE_S.equals(typeKey)) {
            smaliValue = new SmaliValueShort();
        } else if(TypeKey.TYPE_B.equals(typeKey)) {
            smaliValue = new SmaliValueByte();
        } else if(TypeKey.TYPE_C.equals(typeKey)) {
            smaliValue = new SmaliValueChar();
        } else if(TypeKey.TYPE_Z.equals(typeKey)) {
            smaliValue = new SmaliValueBoolean();
        }else {
            throw new IllegalArgumentException("Undefined: " + typeKey);
        }
        return smaliValue;
    }

    public static SmaliValue createForValue(Key key) {
        SmaliValue smaliValue = initializeForValue(key);
        if (smaliValue != null) {
            smaliValue.setKey(key);
        }
        return smaliValue;
    }
    public static SmaliValue initializeForValue(Key key) {
        SmaliValue smaliValue;
        if (key == null) {
            smaliValue = null;
        } else if (key instanceof PrimitiveKey) {
            PrimitiveKey primitiveKey = (PrimitiveKey) key;
            if (primitiveKey.isX()) {
                smaliValue = new SmaliValueX();
            }else if (primitiveKey.isBoolean()) {
                smaliValue = new SmaliValueBoolean();
            } else if (primitiveKey.isByte()) {
                smaliValue = new SmaliValueByte();
            } else if (primitiveKey.isChar()) {
                smaliValue = new SmaliValueChar();
            } else if (primitiveKey.isDouble()) {
                smaliValue = new SmaliValueDouble();
            } else if (primitiveKey.isFloat()) {
                smaliValue = new SmaliValueFloat();
            } else if (primitiveKey.isInteger()) {
                smaliValue = new SmaliValueInteger();
            } else if (primitiveKey.isLong()) {
                smaliValue = new SmaliValueLong();
            } else if (primitiveKey.isShort()) {
                smaliValue = new SmaliValueShort();
            } else {
                throw new IllegalArgumentException("Unknown primitive key: " + key.getClass() + ", " + key);
            }
        } else if(key instanceof StringKey ||
                key instanceof TypeKey ||
                key instanceof MethodKey ||
                key instanceof MethodHandleKey) {
            smaliValue = new SmaliValueSectionData();
        } else if(key instanceof FieldKey) {
            smaliValue = new SmaliValueEnum();
        } else if(key instanceof ArrayKey) {
            smaliValue = new SmaliValueArray();
        } else if(key instanceof AnnotationItemKey) {
            smaliValue = new SmaliValueAnnotation();
        } else if(key instanceof NullKey) {
            smaliValue = new SmaliValueNull();
        } else {
            throw new IllegalArgumentException("Undefined key: " + key.getClass() + ", " + key);
        }
        return smaliValue;
    }

    public static SmaliValueNumber<?> valueNumberFor(PrimitiveKey key) {
        if (key.isByte()) {
            return new SmaliValueByte();
        }
        if (key.isShort()) {
            return new SmaliValueShort();
        }
        if (key.isInteger()) {
            return new SmaliValueInteger();
        }
        if (key.isLong()) {
            return new SmaliValueLong();
        }
        if (key.isX()) {
            return new SmaliValueLong();
        }
        throw new IllegalArgumentException("Unknown primitive key type: "
                + key.getClass() + ", " + key);
    }
    public static SmaliValueNumber<?> valueNumberForWidth(int width) {
        if (width == 1) {
            return new SmaliValueByte();
        }
        if (width == 2) {
            return new SmaliValueShort();
        }
        if (width == 4) {
            return new SmaliValueInteger();
        }
        if (width == 8) {
            return new SmaliValueLong();
        }
        if (width >= 0 && width < 8) {
            return new SmaliValueX();
        }
        throw new IllegalArgumentException("Unknown width: " + width);
    }
}
