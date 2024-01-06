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

public class SmaliRegisterSet extends SmaliSet<SmaliRegister> implements
        Iterable<SmaliRegister>{

    private boolean range;

    public SmaliRegisterSet(){
        super();
    }

    public boolean isRange() {
        return range;
    }
    public void setRange(boolean range) {
        this.range = range;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        boolean appendOnce = false;
        String separator;
        if(isRange()){
            separator = " .. ";
        }else {
            separator = ", ";
        }
        for(SmaliRegister register : this){
            if(appendOnce){
                writer.append(separator);
            }
            register.append(writer);
            appendOnce = true;
        }
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        reader.skipSpaces();
        boolean brace = reader.get() == '{';
        if(brace){
            reader.skip(1);
        }
        boolean first = true;
        while (skipNext(reader, first)){
            SmaliRegister register = new SmaliRegister();
            add(register);
            register.parse(reader);
            first = false;
        }
        if(brace){
            char ch = reader.readASCII();
            if(ch != '}'){
                reader.skip(-1);
                throw new SmaliParseException("Invalid character '" + ch + "'", reader);
            }
            reader.skipSpaces();
            ch = reader.readASCII();
            if(ch != ','){
                reader.skip(-1);
                throw new SmaliParseException("Invalid character '" + ch + "'", reader);
            }
            reader.skipSpaces();
        }
    }
    private boolean skipNext(SmaliReader reader, boolean first) throws IOException{
        reader.skipSpaces();
        if(reader.isLineEnd()){
            return false;
        }
        byte b = reader.get();
        if(b == ','){
            reader.offset(1);
            reader.skipSpaces();
            b = reader.get();
        }else if(b == '}'){
            return false;
        }else if(reader.startsWith(new byte[]{'.', '.'})){
            setRange(true);
            reader.offset(2);
            reader.skipSpaces();
            b = reader.get();
        }else if(!first){
            throw new SmaliParseException("Invalid character, expecting ','", reader);
        }
        return b == 'v' || b == 'p';
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        boolean appendOnce = false;
        String separator;
        if(isRange()){
            separator = " .. ";
        }else {
            separator = ", ";
        }
        for(SmaliRegister register : this){
            if(appendOnce){
                builder.append(separator);
            }
            builder.append(register);
            appendOnce = true;
        }
        return builder.toString();
    }
}
