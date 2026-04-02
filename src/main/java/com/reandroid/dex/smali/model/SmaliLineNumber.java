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

import com.reandroid.dex.debug.DebugElementType;
import com.reandroid.dex.program.DebugLineNumber;
import com.reandroid.dex.debug.DebugLineNumberBlock;
import com.reandroid.dex.program.InstructionLabelType;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public class SmaliLineNumber extends SmaliDebugElement implements DebugLineNumber, SmaliRegion {

    private int number;

    public SmaliLineNumber(){
        super();
    }

    @Override
    public int getLineNumber() {
        return number;
    }
    public void setLineNumber(int number) {
        this.number = number;
    }

    @Override
    public DebugElementType<DebugLineNumberBlock> getDebugElementType() {
        return DebugElementType.LINE_NUMBER;
    }

    @Override
    public InstructionLabelType getLabelType() {
        return InstructionLabelType.LINE;
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        super.append(writer);
        writer.appendInteger(getLineNumber());
    }
    @Override
    public void parse(SmaliReader reader) throws IOException{
        reader.skipWhitespaces();
        SmaliParseException.expect(reader, getSmaliDirective());
        reader.skipWhitespaces();
        setLineNumber(reader.readInteger());
    }

    @Override
    public String toString() {
        return DebugLineNumber.formatToString(this);
    }
}
