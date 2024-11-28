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

import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.HiddenApiFlag;
import com.reandroid.dex.common.Modifier;
import com.reandroid.dex.common.RegistersTable;
import com.reandroid.dex.key.*;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.SmaliWriterSetting;
import com.reandroid.dex.smali.fix.SmaliGotoFix;

import java.io.IOException;
import java.util.Iterator;

public class SmaliMethod extends SmaliDef implements RegistersTable{

    private ProtoKey protoKey;

    private final SmaliParamSet paramSet;
    private final SmaliRegistersCount smaliRegistersCount;
    private final SmaliCodeSet codeSet;

    public SmaliMethod(){
        super();
        this.paramSet = new SmaliParamSet();
        this.smaliRegistersCount = new SmaliRegistersCount();
        this.codeSet = new SmaliCodeSet();

        this.paramSet.setParent(this);
        this.smaliRegistersCount.setParent(this);
        this.codeSet.setParent(this);
    }

    @Override
    public MethodKey getKey(){
        TypeKey typeKey = getDefining();
        if(typeKey != null) {
            return getKey(typeKey);
        }
        return null;
    }
    public void setKey(Key key) {
        MethodKey methodKey = (MethodKey) key;
        setName(methodKey.getNameKey());
        setProtoKey(methodKey.getProto());
        setDefining(methodKey.getDeclaring());
    }
    public MethodKey getKey(TypeKey declaring) {
        return MethodKey.create(declaring, getName(), getProtoKey());
    }

    public boolean hasInstructions(){
        return getInstructions().hasNext();
    }
    public Iterator<SmaliInstruction> getInstructions(){
        return getCodeSet().getInstructions();
    }
    public boolean hasDebugElements(){
        return getDebugElements().hasNext();
    }
    public Iterator<SmaliDebugElement> getDebugElements(){
        return getCodeSet().getDebugElements();
    }
    public SmaliRegistersCount getSmaliRegistersCount() {
        return smaliRegistersCount;
    }
    public ProtoKey getProtoKey() {
        return protoKey;
    }
    public void setProtoKey(ProtoKey protoKey) {
        this.protoKey = protoKey;
    }

    public SmaliParamSet getParamSet() {
        return paramSet;
    }
    public Iterator<SmaliMethodParameter> getParameters(){
        return getParamSet().iterator();
    }
    public SmaliCodeSet getCodeSet() {
        return codeSet;
    }
    public Iterator<SmaliCodeTryItem> getTryItems(){
        return getCodeSet().getTryItems();
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.METHOD;
    }


    public boolean isConstructor(){
        return Modifier.contains(getAccessFlags(), AccessFlag.CONSTRUCTOR);
    }
    public boolean isDirect(){
        return isConstructor() || isStatic() || isPrivate();
    }
    public boolean isVirtual(){
        return !isDirect();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getSmaliDirective().append(writer);
        Modifier.append(writer, getAccessFlags());
        Modifier.append(writer, hiddenApiFlags());
        writer.append(getName());
        getProtoKey().append(writer);
        writer.indentPlus();
        if (hasInstructions()) {
            writer.newLine();
            getSmaliRegistersCount().append(writer);
        }
        getParamSet().append(writer);
        if(hasAnnotation()){
            writer.newLine();
            getAnnotationSet().append(writer);
        }
        getCodeSet().append(writer);
        writer.indentMinus();
        getSmaliDirective().appendEnd(writer);
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        SmaliParseException.expect(reader, getSmaliDirective());
        setAccessFlags(AccessFlag.parse(reader));
        setHiddenApiFlags(HiddenApiFlag.parse(reader));
        setName(StringKey.readSimpleName(reader, '('));
        parseProto(reader);
        reader.skipWhitespacesOrComment();
        while (parseNoneCode(reader)){
            reader.skipWhitespacesOrComment();
        }
        getCodeSet().parse(reader);
        SmaliParseException.expect(reader, getSmaliDirective(), true);
        runFixes();
    }
    private void runFixes() {
        new SmaliGotoFix(this).apply();
    }
    private boolean parseNoneCode(SmaliReader reader) throws IOException {
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        if(directive == SmaliDirective.LOCALS ||
                directive == SmaliDirective.REGISTERS) {
            getSmaliRegistersCount().parse(reader);
            return true;
        }
        if(directive == SmaliDirective.ANNOTATION){
            getOrCreateAnnotation().parse(reader);
            return true;
        }
        if(directive == SmaliDirective.PARAM){
            getParamSet().parse(reader);
            return true;
        }
        return false;
    }
    private void parseProto(SmaliReader reader) throws IOException {
        reader.skipWhitespaces();
        setProtoKey(ProtoKey.read(reader));
    }

    @Override
    public int getRegistersCount() {
        return getSmaliRegistersCount().getRegisters();
    }

    @Override
    public int getParameterRegistersCount() {
        int count = isStatic() ? 0 : 1;
        ProtoKey protoKey = getProtoKey();
        if(protoKey != null){
            count += protoKey.getParameterRegistersCount();
        }
        return count;
    }
    @Override
    public void setRegistersCount(int count) {
    }
    @Override
    public void setParameterRegistersCount(int count) {
    }
    @Override
    public boolean ensureLocalRegistersCount(int count) {
        // FIXME
        return true;
    }
    @Override
    public int getLocalRegistersCount() {
        return getSmaliRegistersCount().getLocals();
    }

    @Override
    public String toDebugString() {
        StringBuilder builder = new StringBuilder();
        TypeKey typeKey = getDefining();
        if(typeKey != null){
            builder.append(typeKey);
            builder.append(", ");
        }
        builder.append("method = ");
        builder.append(getName());
        builder.append(getProtoKey());
        return builder.toString();
    }

    public static class SmaliRegistersCount extends Smali implements SmaliRegion {

        private SmaliDirective directive;
        private int value;

        public SmaliRegistersCount() {
            this.directive = SmaliDirective.LOCALS;
        }

        public int getLocals() {
            int value = getValue();
            if (!isLocalsMode()) {
                SmaliMethod method = getParent(SmaliMethod.class);
                value -= method.getParameterRegistersCount();
            }
            return value;
        }
        public int getRegisters() {
            int value = getValue();
            if (isLocalsMode()) {
                SmaliMethod method = getParent(SmaliMethod.class);
                value += method.getParameterRegistersCount();
            }
            return value;
        }
        public int getValue() {
            return value;
        }
        public void setValue(int value) {
            this.value = value;
        }

        public boolean isLocalsMode() {
            return getSmaliDirective() == SmaliDirective.LOCALS;
        }
        public void setLocalsMode(boolean localsMode) {
            if (localsMode == isLocalsMode()) {
                return;
            }
            SmaliDirective directive;
            int value;
            if (localsMode) {
                value = getLocals();
                directive = SmaliDirective.LOCALS;
            } else {
                value = getRegisters();
                directive = SmaliDirective.REGISTERS;
            }
            setDirective(directive);
            setValue(value);
        }

        private void setDirective(SmaliDirective directive) {
            this.directive = directive;
        }

        @Override
        public void parse(SmaliReader reader) throws IOException {
            SmaliDirective directive = SmaliDirective.parse(reader);
            if (directive != SmaliDirective.LOCALS && directive != SmaliDirective.REGISTERS) {
                throw new SmaliParseException("expecting '" + SmaliDirective.LOCALS + "', or '"
                        + SmaliDirective.REGISTERS + "'", reader);
            }
            setDirective(directive);
            reader.skipSpaces();
            setValue(reader.readInteger());
        }
        @Override
        public SmaliDirective getSmaliDirective() {
            return directive;
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            SmaliWriterSetting setting = writer.getWriterSetting();
            if (setting != null) {
                setLocalsMode(setting.isLocalRegistersCount());
            }
            getSmaliDirective().append(writer);
            writer.appendInteger(getValue());
        }
    }
}
