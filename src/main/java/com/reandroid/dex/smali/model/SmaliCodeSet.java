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

import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.InstanceIterator;

import java.io.IOException;
import java.util.Iterator;

public class SmaliCodeSet extends SmaliSet<SmaliCode>{

    public SmaliCodeSet(){
        super();
    }

    public void updateAddresses(){
        int address = 0;
        Iterator<SmaliInstruction> iterator = getInstructions();
        while (iterator.hasNext()) {
            SmaliInstruction ins = iterator.next();
            ins.setAddress(address);
            address += ins.getCodeUnits();
        }
    }
    public Iterator<SmaliInstruction> getInstructions() {
        return InstanceIterator.of(iterator(), SmaliInstruction.class);
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        if(isEmpty()){
            return;
        }
        writer.newLine();
        writer.appendAll(iterator());
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        SmaliCode code;
        while ((code = createNext(reader)) != null){
            add(code);
            code.parse(reader);
            reader.skipWhitespacesOrComment();
        }
        updateAddresses();
    }
    private SmaliCode createNext(SmaliReader reader){
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        if(directive != null){
            return createFor(directive);
        }
        reader.skipWhitespaces();
        if(reader.get() == ':'){
            return new SmaliLabel();
        }
        Opcode<?> opcode = Opcode.parseSmali(reader, false);
        if(opcode != null){
            return new SmaliInstruction();
        }
        return null;
    }
    public static SmaliCode createFor(SmaliDirective directive){
        if(directive == SmaliDirective.LINE){
            return new SmaliLineNumber();
        }
        if(directive == SmaliDirective.CATCH || directive == SmaliDirective.CATCH_ALL){
            return new SmaliCodeTryItem();
        }
        if(directive == SmaliDirective.PARAM){
            return new SmaliMethodParameter();
        }
        if(directive == SmaliDirective.END_LOCAL){
            return new SmaliDebugEndLocal();
        }
        if(directive == SmaliDirective.LOCAL){
            return new SmaliDebugLocal();
        }
        if(directive == SmaliDirective.RESTART_LOCAL){
            return new SmaliDebugRestartLocal();
        }
        if(directive == SmaliDirective.ARRAY_DATA){
            return new SmaliPayloadArray();
        }
        if(directive == SmaliDirective.PACKED_SWITCH){
            return new SmaliPayloadPackedSwitch();
        }
        if(directive == SmaliDirective.SPARSE_SWITCH){
            return new SmaliPayloadSparseSwitch();
        }
        return null;
    }
}
