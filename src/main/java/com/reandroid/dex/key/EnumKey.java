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
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public class EnumKey extends FieldKey {

    EnumKey(FieldKey fieldKey) {
        super(fieldKey.getDeclaring(), fieldKey.getNameKey(), fieldKey.getType());
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        SmaliDirective.ENUM.append(writer);
        super.append(writer);
    }

    @Override
    public String toString() {
        return SmaliDirective.ENUM.getName() + " " + super.toString();
    }

    public static EnumKey create(FieldKey fieldKey) {
        if (fieldKey == null) {
            return null;
        }
        if (fieldKey instanceof EnumKey) {
            return (EnumKey) fieldKey;
        }
        return new EnumKey(fieldKey);
    }

    public static EnumKey parse(String text) {
        if (text == null) {
            return null;
        }
        String prefix = ".enum ";
        if (!text.startsWith(prefix)) {
            return null;
        }
        text = text.substring(prefix.length()).trim();
        return create(FieldKey.parse(text));
    }

    public static EnumKey read(SmaliReader reader) throws IOException {
        SmaliParseException.expect(reader, SmaliDirective.ENUM);
        reader.skipWhitespacesOrComment();
        return create(FieldKey.read(reader));
    }
}
