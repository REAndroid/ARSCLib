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

import com.reandroid.dex.key.*;
import com.reandroid.dex.program.MethodParameterProgram;
import com.reandroid.dex.smali.*;
import com.reandroid.utils.StringsUtil;

import java.io.IOException;

public class SmaliMethodParameter extends SmaliDebug implements MethodParameterProgram, SmaliRegion {

    private final SmaliRegister smaliRegister;
    private StringKey name;
    private SmaliAnnotationSet annotationSet;

    public SmaliMethodParameter() {
        super();
        SmaliRegister register = new SmaliRegister();
        register.setParameter(true);
        register.setNumber(-1);
        this.smaliRegister = register;
    }

    @Override
    public TypeKey getKey() {
        SmaliMethod smaliMethod = getParentInstance(SmaliMethod.class);
        if (smaliMethod == null) {
            return null;
        }
        ProtoKey protoKey = smaliMethod.getProtoKey();
        if (protoKey == null) {
            return null;
        }
        SmaliRegister smaliRegister = getSmaliRegister();
        int index = smaliRegister.getNumber();
        if (!smaliMethod.isStatic()) {
            index = index - 1;
        }
        return protoKey.getParameter(protoKey.getParameterIndex(index));
    }

    public SmaliRegister getSmaliRegister() {
        return smaliRegister;
    }

    public StringKey getNameKey() {
        return name;
    }
    public void setName(StringKey name) {
        this.name = name;
    }
    @Override
    public String getDebugName() {
        StringKey key = getNameKey();
        if (key != null) {
            return key.getString();
        }
        return null;
    }
    @Override
    public void setDebugName(String name) {
        if (StringsUtil.isEmpty(name)) {
            name = null;
        }
        StringKey key = name == null ? null : StringKey.create(name);
        setName(key);
    }

    @Override
    public boolean hasAnnotations() {
        SmaliAnnotationSet annotationSet = getSmaliAnnotationSet();
        return annotationSet != null && !annotationSet.isEmpty();
    }

    @Override
    public AnnotationSetKey getAnnotation() {
        SmaliAnnotationSet annotationSet = getSmaliAnnotationSet();
        if (annotationSet != null) {
            return annotationSet.getKey();
        }
        return AnnotationSetKey.empty();
    }
    @Override
    public void setAnnotation(AnnotationSetKey annotation) {
        if (annotation == null || annotation.isEmpty()) {
            setSmaliAnnotationSet(null);
        } else {
            getOrCreateSmaliAnnotationSet().setKey(annotation);
        }
    }
    @Override
    public void clearAnnotations() {
        setSmaliAnnotationSet(null);
    }

    public SmaliAnnotationSet getSmaliAnnotationSet() {
        return annotationSet;
    }
    public SmaliAnnotationSet getOrCreateSmaliAnnotationSet() {
        SmaliAnnotationSet annotationSet = getSmaliAnnotationSet();
        if (annotationSet == null) {
            annotationSet = new SmaliAnnotationSet();
            setSmaliAnnotationSet(annotationSet);
        }
        return annotationSet;
    }
    public void setSmaliAnnotationSet(SmaliAnnotationSet annotationSet) {
        this.annotationSet = annotationSet;
        if (annotationSet != null) {
            annotationSet.setParent(this);
        }
    }
    public int getDefinitionIndex() {
        SmaliMethod smaliMethod = getParentInstance(SmaliMethod.class);
        if (smaliMethod == null) {
            return -1;
        }
        ProtoKey protoKey = smaliMethod.getProtoKey();
        if (protoKey == null) {
            return -1;
        }
        return getDefinitionIndex(smaliMethod.isStatic(), protoKey);
    }
    public int getDefinitionIndex(boolean is_static, ProtoKey protoKey) {
        int index = getRegisterNumber();
        if (!is_static) {
            index = index - 1;
        }
        return protoKey.getParameterIndex(index);
    }
    public int getRegisterNumber() {
        return getSmaliRegister().getNumber();
    }
    public void setRegisterNumber(int number) {
        getSmaliRegister().setNumber(number);
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.PARAM;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        SmaliDirective directive = getSmaliDirective();
        directive.append(writer);
        getSmaliRegister().append(writer);
        StringKey name = getNameKey();
        if (name != null) {
            writer.append(", ");
            name.append(writer);
        }
        SmaliAnnotationSet annotationSet = getSmaliAnnotationSet();
        if (annotationSet != null) {
            writer.indentPlus();
            writer.newLine();
            annotationSet.append(writer);
            writer.indentMinus();
            directive.appendEnd(writer);
        }
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        super.parse(reader);
        getSmaliRegister().parse(reader);
        validateRegister(reader);
        reader.skipWhitespacesOrComment();
        if (reader.get() == ',') {
            reader.skip(1);
            reader.skipWhitespacesOrComment();
        }
        parseName(reader);
        parseAnnotationSet(reader);
        AnnotationItemKey duplicate = getAnnotation().getDuplicate();
        if (duplicate != null) {
            throw new SmaliParseException("Multiple annotation of type: "
                    + duplicate.getType() + "\n", reader);
        }
    }
    private void validateRegister(SmaliReader reader) throws IOException {
        SmaliRegister register = getSmaliRegister();
        if (!register.isParameter()) {
            throw new SmaliParseException("Unexpected parameter register type: "
                    + register + "\n", reader);
        }
        SmaliParamSet paramSet = getParentInstance(SmaliParamSet.class);
        if (paramSet != null && paramSet.isDuplicateRegister(this)) {
            throw new SmaliParseException("Duplicate parameter register: "
                    + register + "\n", reader);
        }
        SmaliMethod smaliMethod = getParentInstance(SmaliMethod.class);
        if (smaliMethod == null) {
            return;
        }
        ProtoKey protoKey = smaliMethod.getProtoKey();
        if (protoKey == null) {
            return;
        }
        boolean is_static = smaliMethod.isStatic();
        int max = protoKey.getParameterRegistersCount();
        if (is_static) {
            max = max - 1;
        }
        if (register.getNumber() > max) {
            throw new SmaliParseException("Register "
                    + register + " is larger than the maximum register p"
                    + max + " for this method", reader);
        }
        int index = getDefinitionIndex(is_static, protoKey);
        if (index < 0) {
            index = register.getNumber();
            if (index == 0 && !is_static) {
                throw new SmaliParseException("Register "
                        + register + " is not a parameter register.", reader);
            }
            index = index - 1;
            if (!is_static) {
                index = index - 1;
            }
            TypeKey typeKey = protoKey.getParameter(protoKey.getParameterIndex(index));
            if (typeKey != null && typeKey.isWide()) {
                throw new SmaliParseException("Register "
                        + register + " is the second half of a wide parameter "
                        + typeKey, reader);
            }
            throw new SmaliParseException("Parameter register out of range: "
                    + register + "\n", reader);
        }
    }
    private void parseName(SmaliReader reader) throws IOException {
        reader.skipSpaces();
        if (reader.get() == '"') {
            setName(StringKey.read(reader));
        }
    }
    private void parseAnnotationSet(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        if (directive != SmaliDirective.ANNOTATION) {
            return;
        }
        int position = reader.position();
        SmaliAnnotationSet annotationSet = new SmaliAnnotationSet();
        annotationSet.parse(reader);
        reader.skipWhitespacesOrComment();
        if (getSmaliDirective().isEnd(reader)) {
            setSmaliAnnotationSet(annotationSet);
            SmaliDirective.parse(reader);
        } else {
            // put back, it is method annotation
            reader.position(position);
        }
    }
}
