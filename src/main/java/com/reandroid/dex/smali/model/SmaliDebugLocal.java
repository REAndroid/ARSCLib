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

import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.*;

import java.io.IOException;

public class SmaliDebugLocal extends SmaliDebug implements SmaliRegion {

    private final SmaliRegisterSet registerSet;
    private StringKey name;
    private TypeKey type;
    private StringKey signature;

    public SmaliDebugLocal(){
        super();
        this.registerSet = new SmaliRegisterSet();
        registerSet.setParent(this);
    }

    public SmaliRegisterSet getRegisterSet() {
        return registerSet;
    }

    public StringKey getName() {
        return name;
    }
    public void setName(StringKey name) {
        this.name = name;
    }
    public TypeKey getType() {
        return type;
    }
    public void setType(TypeKey type) {
        this.type = type;
    }

    public StringKey getSignature() {
        return signature;
    }
    public void setSignature(StringKey signature) {
        this.signature = signature;
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.LOCAL;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getSmaliDirective().append(writer);
        getRegisterSet().append(writer);
        writer.append(", ");
        writer.appendOptional(getName());
        writer.append(':');
        writer.appendOptional(getType());
        StringKey signature = getSignature();
        if(signature != null){
            writer.append(", ");
            signature.append(writer);
        }
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        reader.skipSpaces();
        SmaliParseException.expect(reader, getSmaliDirective());
        getRegisterSet().parse(reader);
        setName(StringKey.read(reader));
        reader.skipSpaces();
        if(reader.readASCII() != ':'){
            reader.skip(-1);
            throw new SmaliParseException("Expecting ':'", reader);
        }
        reader.skipSpaces();
        setType(TypeKey.read(reader));
        reader.skipSpaces();
        if(reader.get() == ','){
            reader.skip(1);
            reader.skipSpaces();
            setSignature(StringKey.read(reader));
        }
    }
}
