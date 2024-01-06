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

import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public class SmaliSparseSwitchEntry extends Smali{

    private SmaliValueNumber<?> value;
    private final SmaliLabel label;

    public SmaliSparseSwitchEntry(){
        super();
        this.label = new SmaliLabel();
        this.label.setParent(this);
    }

    public Number getValue() {
        return getValueNumber().getNumber();
    }
    public void setValue(Number value) {
        setNumberValue(SmaliValueNumber.createFor(value));
    }

    public SmaliValueNumber<?> getValueNumber() {
        return value;
    }
    public void setNumberValue(SmaliValueNumber<?> valueNumber) {
        this.value = valueNumber;
        if(valueNumber != null){
            valueNumber.setParent(this);
        }
    }

    public SmaliLabel getLabel() {
        return label;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.appendOptional(getValueNumber());
        writer.append(" -> ");
        getLabel().append(writer);
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();

        SmaliValueNumber<?> value = SmaliValueNumber.createNumber(reader);
        setNumberValue(value);
        value.parse(reader);
        reader.skipSpaces();
        SmaliParseException.expect(reader, '-');
        SmaliParseException.expect(reader, '>');
        reader.skipSpaces();
        getLabel().parse(reader);
    }
}
