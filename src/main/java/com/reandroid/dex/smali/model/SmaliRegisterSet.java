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

import com.reandroid.dex.common.RegisterFormat;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;

public class SmaliRegisterSet extends SmaliSet<SmaliRegister> implements
        Iterable<SmaliRegister>{

    private final RegisterFormat format;

    public SmaliRegisterSet(RegisterFormat format){
        super();
        this.format = format;
    }
    public SmaliRegisterSet(){
        this(RegisterFormat.READ);
    }

    public RegisterFormat getFormat() {
        return format;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        boolean appendOnce = false;
        RegisterFormat format = getFormat();
        String separator = format.isRange() ? " .. ": ", ";
        if(format.isOut()){
            writer.append('{');
        }
        for(SmaliRegister register : this){
            if(appendOnce){
                writer.append(separator);
            }
            register.append(writer);
            appendOnce = true;
        }
        if(format.isOut()){
            writer.append('}');
        }
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        RegisterFormat format = getFormat();
        if(format.isRange()){
            parseOutRangeRegisters(reader);
        }else if(format.isOut()){
            parseOutRegisters(reader);
        }else {
            parseRegisters(reader);
        }
    }
    private void parseRegisters(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        int count = getFormat().getCount();
        for(int i = 0; i < count; i++){
            if(i != 0){
                SmaliParseException.expect(reader, ',');
                reader.skipWhitespacesOrComment();
            }
            SmaliRegister register = new SmaliRegister();
            add(register);
            register.parse(reader);
            reader.skipWhitespacesOrComment();
        }
    }
    private void parseOutRegisters(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        SmaliParseException.expect(reader, '{');
        reader.skipWhitespacesOrComment();
        boolean parsedOnce = false;
        while (!reader.finished() && reader.get() != '}'){
            if(parsedOnce){
                SmaliParseException.expect(reader, ',');
                reader.skipWhitespacesOrComment();
            }
            SmaliRegister register = new SmaliRegister();
            add(register);
            register.parse(reader);
            reader.skipWhitespacesOrComment();
            parsedOnce = true;
        }
        SmaliParseException.expect(reader, '}');
    }
    private void parseOutRangeRegisters(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        SmaliParseException.expect(reader, '{');
        reader.skipWhitespacesOrComment();

        SmaliRegister register1 = new SmaliRegister();
        add(register1);
        register1.parse(reader);

        reader.skipWhitespacesOrComment();
        SmaliParseException.expect(reader, '.');
        SmaliParseException.expect(reader, '.');
        reader.skipWhitespacesOrComment();

        SmaliRegister register2 = new SmaliRegister();
        add(register2);
        register2.parse(reader);
        reader.skipWhitespacesOrComment();

        SmaliParseException.expect(reader, '}');
    }

    public static final SmaliRegisterSet NO_REGISTER_SET = new SmaliRegisterSet(RegisterFormat.NONE){
        @Override
        public boolean add(SmaliRegister smali) {
            throw new RuntimeException("NO_REGISTER_SET");
        }
        @Override
        public Iterator<SmaliRegister> iterator() {
            return EmptyIterator.of();
        }
        @Override
        public int size() {
            return 0;
        }
        @Override
        public void append(SmaliWriter writer) {
        }
        @Override
        public void parse(SmaliReader reader) {
        }
        @Override
        public String toString() {
            return "";
        }
    };
}
