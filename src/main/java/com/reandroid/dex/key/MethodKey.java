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
package com.reandroid.dex.key;

import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Iterator;
import java.util.function.Function;

public class MethodKey implements ProgramKey {

    private final TypeKey declaring;
    private final StringKey nameKey;
    private final ProtoKey proto;

    public MethodKey(TypeKey declaring, StringKey name, ProtoKey proto){
        this.declaring = declaring;
        this.nameKey = name;
        this.proto = proto;
    }
    public MethodKey(TypeKey declaring, String name, ProtoKey proto){
        this(declaring, StringKey.create(name), proto);
    }

    @Override
    public ElementType getElementType() {
        return ElementType.METHOD;
    }

    public int getRegister(int index) {
        return getProto().getRegister(index);
    }
    public int getParameterIndex(int index) {
        return getProto().getParameterIndex(index);
    }
    public MethodKey changeDeclaring(TypeKey typeKey) {
        if (getDeclaring().equals(typeKey)) {
            return this;
        }
        return new MethodKey(typeKey, getNameKey(), getProto());
    }
    public MethodKey changeName(String name) {
        return changeName(StringKey.create(name));
    }
    public MethodKey changeName(StringKey name) {
        if (name.equals(getNameKey())) {
            return this;
        }
        return new MethodKey(getDeclaring(), name, getProto());
    }
    public MethodKey changeParameters(TypeListKey parameters) {
        return changeProto(getProto().changeParameters(parameters));
    }
    public MethodKey changeParameter(int index, TypeKey parameter) {
        return changeProto(getProto().changeParameter(index, parameter));
    }
    public MethodKey changeProto(ProtoKey protoKey) {
        if (protoKey.equals(getProto())) {
            return this;
        }
        return create(getDeclaring(), getNameKey(), protoKey);
    }
    public MethodKey changeReturnType(TypeKey typeKey) {
        return changeProto(getProto().changeReturnType(typeKey));
    }
    public MethodKey removeParameter(int index) {
        return changeProto(getProto().removeParameter(index));
    }
    @Override
    public TypeKey getDeclaring() {
        return declaring;
    }
    public StringKey getNameKey() {
        return nameKey;
    }
    public ProtoKey getProto() {
        return proto;
    }
    public TypeKey getReturnType() {
        return getProto().getReturnType();
    }
    public TypeListKey getParameters() {
        return getProto().getParameters();
    }
    public String getName() {
        return getNameKey().getString();
    }
    public Iterator<TypeKey> parameters() {
        return getParameters().iterator();
    }
    public int getParametersCount() {
        return getParameters().size();
    }
    public TypeKey getParameter(int i) {
        return getParameters().get(i);
    }
    public int getParameterRegistersCount(){
        return getProto().getParameterRegistersCount();
    }
    public boolean isObjectMethod() {
        return isObjectMethod(this);
    }
    @Override
    public Iterator<Key> contents() {
        return CombiningIterator.singleTwo(
                MethodKey.this,
                CombiningIterator.singleOne(getDeclaring(), SingleIterator.of(getNameKey())),
                getProto().contents());
    }

    public MethodKey replaceTypes(Function<TypeKey, TypeKey> function) {
        MethodKey result = this;
        TypeKey typeKey = getDeclaring();
        typeKey = typeKey.changeTypeName(function.apply(typeKey));

        result = result.changeDeclaring(typeKey);

        typeKey = getReturnType();
        typeKey = typeKey.changeTypeName(function.apply(typeKey));

        result = result.changeReturnType(typeKey);

        int count = getParametersCount();
        for(int i = 0; i < count; i++){
            typeKey = getParameter(i);
            typeKey = typeKey.changeTypeName(function.apply(typeKey));
            result = result.changeParameter(i, typeKey);
        }
        return result;
    }

    @Override
    public MethodKey replaceKey(Key search, Key replace) {
        MethodKey result = this;
        if (search.equals(result)) {
            return (MethodKey) replace;
        }
        result = result.changeDeclaring(getDeclaring().replaceKey(search, replace));
        result = result.changeProto(getProto().replaceKey(search, replace));
        if (replace instanceof StringKey && search.equals(result.getNameKey())) {
            result = result.changeName((StringKey) replace);
        }
        return result;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getDeclaring().append(writer);
        writer.append("->");
        appendDefinition(writer);
    }
    public void appendDefinition(SmaliWriter writer) throws IOException {
        getNameKey().appendSimpleName(writer);
        getProto().append(writer);
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == this) {
            return 0;
        }
        if (!(obj instanceof MethodKey)) {
            return StringsUtil.compareToString(this, obj);
        }
        MethodKey key = (MethodKey) obj;
        int i = CompareUtil.compare(getDeclaring(), key.getDeclaring());
        if (i != 0) {
            return i;
        }
        i = CompareUtil.compare(getNameKey(), key.getNameKey());
        if(i != 0) {
            return i;
        }
        return CompareUtil.compare(getProto(), key.getProto());
    }
    public int compareTo(Object obj, boolean checkDefining) {
        if (obj == null) {
            return -1;
        }
        if (obj == this) {
            return 0;
        }
        MethodKey key = (MethodKey) obj;
        int i;
        if(checkDefining){
            i = CompareUtil.compare(getDeclaring(), key.getDeclaring());
            if(i != 0) {
                return i;
            }
        }
        i = CompareUtil.compare(getNameKey(), key.getNameKey());
        if(i != 0) {
            return i;
        }
        return CompareUtil.compare(getProto(), key.getProto());
    }

    public boolean equalsIgnoreDeclaring(MethodKey other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        return ObjectsUtil.equals(getNameKey(), other.getNameKey()) &&
                ObjectsUtil.equals(getProto(), other.getProto());
    }
    public boolean equalsIgnoreReturnType(MethodKey other){
        if(other == null) {
            return false;
        }
        if(other == this) {
            return true;
        }
        if(!ObjectsUtil.equals(getDeclaring(), other.getDeclaring())){
            return false;
        }
        if(!ObjectsUtil.equals(getNameKey(), other.getNameKey())){
            return false;
        }
        return getProto().equalsParameters(other.getProto());
    }
    public boolean equalsNameAndParameters(MethodKey other){
        if(other == null){
            return false;
        }
        if(other == this){
            return true;
        }
        if(!KeyUtil.matches(getName(), other.getName())){
            return false;
        }
        return TypeListKey.equalsIgnoreEmpty(getParameters(), other.getParameters());
    }
    public boolean equalsIgnoreName(MethodKey other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if(!ObjectsUtil.equals(getDeclaring(), other.getDeclaring())){
            return false;
        }
        return ObjectsUtil.equals(getProto(), other.getProto());
    }
    public boolean equalsName(MethodKey other) {
        if(other == null){
            return false;
        }
        if(other == this){
            return true;
        }
        return KeyUtil.matches(getName(), other.getName());
    }
    public boolean equalsName(String name){
        return KeyUtil.matches(getName(), name);
    }
    public boolean equalsProto(MethodKey other){
        if(other == null){
            return false;
        }
        if(other == this){
            return true;
        }
        return ObjectsUtil.equals(getProto(), other.getProto());
    }
    public boolean equalsDeclaring(TypeKey declaring) {
        return ObjectsUtil.equals(getDeclaring(), declaring);
    }
    public boolean equalsDeclaring(MethodKey other) {
        if(other == null) {
            return false;
        }
        if(other == this) {
            return true;
        }
        return ObjectsUtil.equals(getDeclaring(), other.getDeclaring());
    }
    public boolean equalsReturnType(TypeKey returnType) {
        return getProto().equalsReturnType(returnType);
    }
    public boolean equalsReturnType(MethodKey other){
        if(other == null) {
            return false;
        }
        if(other == this){
            return true;
        }
        return getProto().equalsReturnType(other.getProto());
    }
    public boolean equals(Object obj, boolean checkDefining, boolean checkType) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MethodKey)) {
            return false;
        }
        MethodKey methodKey = (MethodKey) obj;
        if(!ObjectsUtil.equals(getNameKey(), methodKey.getNameKey())){
            return false;
        }
        if(!TypeListKey.equalsIgnoreEmpty(getParameters(), methodKey.getParameters())) {
            return false;
        }
        if(checkDefining){
            if(!ObjectsUtil.equals(getDeclaring(), methodKey.getDeclaring())){
                return false;
            }
        }
        if(checkType){
            return ObjectsUtil.equals(getReturnType(), methodKey.getReturnType());
        }
        return true;
    }

    public boolean equals(TypeKey declaring, StringKey name, ProtoKey proto) {
        return getDeclaring().equals(declaring) &&
                getNameKey().equals(name) &&
                getProto().equals(proto);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MethodKey)) {
            return false;
        }
        MethodKey methodKey = (MethodKey) obj;
        return ObjectsUtil.equals(getNameKey(), methodKey.getNameKey()) &&
                ObjectsUtil.equals(getDeclaring(), methodKey.getDeclaring()) &&
                ObjectsUtil.equals(getProto(), methodKey.getProto());
    }
    @Override
    public int hashCode() {
        return ObjectsUtil.hash(getDeclaring(), getNameKey(), getProto());
    }
    @Override
    public String toString() {
        return getDeclaring() + "->" + getNameKey().getAsSimpleName() + getProto();
    }

    public static MethodKey parse(String text) {
        return parse(text, 0);
    }

    public static MethodKey parse(String text, int start) {
        if(text.length() - start < 6 || (text.charAt(start) != 'L' && text.charAt(start) != '[')){
            return null;
        }
        int i = text.indexOf("->", start);
        if(i < 0){
            return null;
        }
        TypeKey defining = TypeKey.parseBinaryType(text, start, i);
        if (defining == null) {
            return null;
        }
        start = start + defining.getTypeName().length() + 2;
        i = text.indexOf('(', start);
        if (i < 0) {
            return null;
        }
        String name = text.substring(start, i);
        start = i;
        i = text.indexOf(')', start);
        if (i < 0) {
            return null;
        }
        ProtoKey protoKey = ProtoKey.parse(text, start);
        if (protoKey == null) {
            return null;
        }
        return create(defining, StringKey.create(name), protoKey);
    }
    public static MethodKey convert(Method method) {
        TypeKey declaring = TypeKey.convert(method.getDeclaringClass());
        TypeKey returnType = TypeKey.convert(method.getReturnType());
        Parameter[] parameters = method.getParameters();
        int length = parameters.length;
        TypeKey[] typeKeys = new TypeKey[length];
        for (int i = 0; i < length; i++) {
            typeKeys[i] = TypeKey.convert(parameters[i].getType());
        }
        ProtoKey protoKey = ProtoKey.create(returnType, typeKeys);
        return create(declaring, StringKey.create(method.getName()), protoKey);
    }

    public static MethodKey create(TypeKey declaring, StringKey name, ProtoKey protoKey) {
        if (declaring == null || name == null || protoKey == null) {
            return null;
        }
        return new MethodKey(declaring, name, protoKey);
    }
    public static MethodKey create(TypeKey declaring, String name, ProtoKey protoKey) {
        return create(declaring, StringKey.create(name), protoKey);
    }

    public static MethodKey read(SmaliReader reader) throws IOException {
        TypeKey declaring = TypeKey.read(reader);
        reader.skipWhitespacesOrComment();
        SmaliParseException.expect(reader, '-');
        SmaliParseException.expect(reader, '>');
        reader.skipWhitespacesOrComment();
        StringKey name = StringKey.readSimpleName(reader, '(');
        ProtoKey protoKey = ProtoKey.read(reader);
        return create(declaring, name, protoKey);
    }

    public static boolean isObjectMethod(MethodKey methodKey) {
        if (methodKey == null) {
            return false;
        }
        int p = methodKey.getParametersCount();
        if (p > 2) {
            return false;
        }
        if (p == 2) {
            return WAIT_J_I.equalsNameAndParameters(methodKey);
        }
        if (p == 1) {
            return EQUALS.equalsNameAndParameters(methodKey) ||
                    WAIT_J.equalsNameAndParameters(methodKey);
        }
        String name = methodKey.getName();
        return TO_STRING.equalsName(name) ||
                HASHCODE.equalsName(name) ||
                GET_CLASS.equalsName(name) ||
                CLONE.equalsName(name) ||
                CONSTRUCTOR.equalsName(name) ||
                CONSTRUCTOR_STATIC.equalsName(name) ||
                FINALIZE.equalsName(name) ||
                WAIT.equalsName(name) ||
                NOTIFY.equalsName(name) ||
                NOTIFY_ALL.equalsName(name);
    }


    public static final MethodKey CLONE = new MethodKey(
            TypeKey.OBJECT, "clone",
            ProtoKey.emptyParameters(TypeKey.OBJECT));

    public static final MethodKey CONSTRUCTOR = new MethodKey(
            TypeKey.OBJECT, "<init>",
            ProtoKey.emptyParameters(TypeKey.TYPE_V));

    public static final MethodKey CONSTRUCTOR_STATIC = new MethodKey(
            TypeKey.OBJECT, "<clinit>",
            ProtoKey.emptyParameters(TypeKey.TYPE_V));

    public static final MethodKey EQUALS = new MethodKey(
            TypeKey.OBJECT, "equals",
            ProtoKey.create(TypeKey.TYPE_Z, TypeKey.OBJECT));

    public static final MethodKey FINALIZE = new MethodKey(
            TypeKey.OBJECT, "finalize",
            ProtoKey.emptyParameters(TypeKey.TYPE_V));

    public static final MethodKey GET_CLASS = new MethodKey(
            TypeKey.OBJECT, "getClass",
            ProtoKey.emptyParameters(TypeKey.CLASS));

    public static final MethodKey HASHCODE = new MethodKey(
            TypeKey.OBJECT, "hashCode",
            ProtoKey.emptyParameters(TypeKey.TYPE_I));

    public static final MethodKey NOTIFY = new MethodKey(
            TypeKey.OBJECT, "notify",
            ProtoKey.emptyParameters(TypeKey.TYPE_V));

    public static final MethodKey NOTIFY_ALL = new MethodKey(
            TypeKey.OBJECT, "notifyAll",
            ProtoKey.emptyParameters(TypeKey.TYPE_V));

    public static final MethodKey TO_STRING = new MethodKey(
            TypeKey.OBJECT, "toString",
            ProtoKey.emptyParameters(TypeKey.STRING));

    public static final MethodKey WAIT = new MethodKey(
            TypeKey.OBJECT, "wait",
            ProtoKey.emptyParameters(TypeKey.TYPE_V));

    public static final MethodKey WAIT_J = new MethodKey(
            TypeKey.OBJECT, "wait",
            ProtoKey.create(TypeKey.TYPE_V, TypeKey.TYPE_J));

    public static final MethodKey WAIT_J_I = new MethodKey(
            TypeKey.OBJECT, "wait",
            ProtoKey.create(TypeKey.TYPE_V, TypeKey.TYPE_J, TypeKey.TYPE_I));
}
