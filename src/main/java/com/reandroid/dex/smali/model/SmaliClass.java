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
import com.reandroid.dex.common.Modifier;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.key.TypeListKey;
import com.reandroid.dex.program.ClassProgram;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.Iterator;

public class SmaliClass extends SmaliDef implements ClassProgram {

    private TypeKey superClass;
    private StringKey sourceFile;

    private final SmaliInterfaceSet interfaces;
    private final SmaliFieldSet  fields;
    private final SmaliMethodSet methods;

    public SmaliClass() {
        super();

        this.interfaces = new SmaliInterfaceSet();
        this.fields = new SmaliFieldSet();
        this.methods = new SmaliMethodSet();

        interfaces.setParent(this);
        fields.setParent(this);
        methods.setParent(this);
    }

    @Override
    public ElementType getElementType() {
        return ElementType.TYPE;
    }

    @Override
    public TypeKey getKey() {
        return TypeKey.create(getName());
    }
    public void setKey(TypeKey key) {
        String name;
        if (key != null) {
            name = key.getTypeName();
        } else {
            name = null;
        }
        setName(name);
    }

    @Override
    public TypeKey getSuperClassKey() {
        return superClass;
    }
    public void setSuperClass(TypeKey typeKey) {
        this.superClass = typeKey;
    }
    @Override
    public String getSourceFileName() {
        StringKey key = getSourceFileKey();
        if (key != null) {
            return key.getString();
        }
        return null;
    }
    public StringKey getSourceFileKey() {
        return sourceFile;
    }
    public void setSourceFile(String sourceFile) {
        StringKey key = sourceFile == null ? null : StringKey.create(sourceFile);
        setSourceFile(key);
    }
    public void setSourceFile(StringKey sourceFile) {
        this.sourceFile = sourceFile;
    }
    public SmaliInterfaceSet getInterfaces() {
        return interfaces;
    }
    @Override
    public TypeListKey getInterfacesKey() {
        return getInterfaces().getKey();
    }
    public void setInterfaces(TypeListKey key) {
        getInterfaces().setKey(key);
    }
    public boolean hasClassData() {
        return !fields.isEmpty() || !methods.isEmpty();
    }
    @Override
    public Iterator<SmaliField> getStaticFields() {
        return fields.getStaticFields();
    }
    @Override
    public Iterator<SmaliField> getInstanceFields() {
        return fields.getInstanceFields();
    }
    public void addFields(Iterator<SmaliField> iterator) {
        fields.addAll(iterator);
    }
    @Override
    public Iterator<SmaliMethod> getDirectMethods() {
        return methods.getDirectMethods();
    }
    @Override
    public Iterator<SmaliMethod> getVirtualMethods() {
        return methods.getVirtualMethods();
    }
    public void addMethods(Iterator<SmaliMethod> iterator) {
        methods.addAll(iterator);
    }

    public SmaliMethod getStaticConstructor() {
        Iterator<SmaliMethod> iterator = getDirectMethods();
        while (iterator.hasNext()) {
            SmaliMethod method = iterator.next();
            if (method.isConstructor() && method.isStatic() &&
                    MethodKey.CONSTRUCTOR_STATIC.equalsIgnoreDeclaring(method.getKey())) {
                return method;
            }
        }
        return null;
    }
    void fixUninitializedFinalFields() {
        Iterator<SmaliField> iterator = getStaticFields();
        while (iterator.hasNext()) {
            iterator.next().fixUninitializedFinalValue();
        }
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.CLASS;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getSmaliDirective().append(writer);
        Modifier.append(writer, getAccessFlags());
        writer.appendOptional(getKey());
        writer.newLine();
        SmaliDirective.SUPER.append(writer);
        writer.appendOptional(getSuperClassKey());
        StringKey source = getSourceFileKey();
        if (source != null) {
            writer.newLine();
            SmaliDirective.SOURCE.append(writer);
            source.append(writer);
        }
        getInterfaces().append(writer);
        if (hasAnnotation()) {
            writer.newLine();
            writer.appendCommentNewLine("annotations");
            writer.appendAllWithDoubleNewLine(getAnnotationSet().iterator());
        }
        fields.append(writer);
        methods.append(writer);
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        SmaliParseException.expect(reader, SmaliDirective.CLASS);
        setAccessFlags(AccessFlag.parse(reader));
        setKey(TypeKey.read(reader));
        while (parseNext(reader)) {
            reader.skipWhitespacesOrComment();
        }
        reader.skipWhitespacesOrComment();
        if (!reader.finished() && !SmaliDirective.CLASS.is(
                SmaliDirective.parse(reader, false))) {
            throw new SmaliParseException("Unexpected character", reader);
        }
        fixUninitializedFinalFields();
    }
    private boolean parseNext(SmaliReader reader) throws IOException {
        if (reader.finished()) {
            return false;
        }
        reader.skipWhitespacesOrComment();
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        if (directive == SmaliDirective.CLASS) {
            return false;
        }
        if (directive == SmaliDirective.SUPER) {
            parseSuper(reader);
            return true;
        }
        if (directive == SmaliDirective.SOURCE) {
            parseSource(reader);
            return true;
        }
        if (directive == SmaliDirective.ANNOTATION) {
            getOrCreateSmaliAnnotationSet().parse(reader);
            return true;
        }
        if (directive == SmaliDirective.FIELD) {
            fields.parse(reader);
            return true;
        }
        if (directive == SmaliDirective.METHOD) {
            methods.parse(reader);
            return true;
        }
        if (directive == SmaliDirective.IMPLEMENTS) {
            interfaces.parse(reader);
            return true;
        }
        return false;
    }
    private void parseSuper(SmaliReader reader) throws IOException {
        SmaliParseException.expect(reader, SmaliDirective.SUPER);
        setSuperClass(TypeKey.read(reader));
    }
    private void parseSource(SmaliReader reader) throws IOException{
        SmaliParseException.expect(reader, SmaliDirective.SOURCE);
        reader.skipSpaces();
        setSourceFile(StringKey.read(reader));
    }

    public SmaliField parseField(SmaliReader reader) throws IOException {
        return fields.parseNext(reader);
    }
    public SmaliMethod parseMethod(SmaliReader reader) throws IOException {
        return methods.parseNext(reader);
    }

    @Override
    public String toDebugString() {
        return "class = " + getKey();
    }
}
