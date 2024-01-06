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

import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public class SmaliLineNumber extends SmaliDebug implements SmaliRegion {

    private int number;

    public SmaliLineNumber(){
        super();
    }

    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.LINE;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getSmaliDirective().append(writer);
        writer.appendInteger(getNumber());
    }

    @Override
    public void parse(SmaliReader reader) throws IOException{
        reader.skipWhitespaces();
        SmaliDirective directive = SmaliDirective.parse(reader);
        if(directive != getSmaliDirective()){
            // throw
        }
        reader.skipWhitespaces();
        setNumber(reader.readInteger());
    }
}
