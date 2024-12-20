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

import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.smali.*;
import com.reandroid.dex.value.DexValueType;

import java.io.IOException;

public class SmaliValueEnum extends SmaliValueSectionData implements SmaliRegion {

    public SmaliValueEnum(){
        super();
    }

    @Override
    public FieldKey getValue() {
        return (FieldKey) super.getValue();
    }

    @Override
    public FieldKey getKey() {
        return getValue();
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.ENUM;
    }
    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.ENUM;
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        getSmaliDirective().append(writer);
        super.append(writer);
    }
    @Override
    public void parse(SmaliReader reader) throws IOException {
        SmaliParseException.expect(reader, getSmaliDirective());
        setValue(FieldKey.read(reader));
    }
}
