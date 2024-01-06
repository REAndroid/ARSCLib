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

import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;

public class TypeKey implements Key{

    private final String typeName;
    private String packageName;
    private String simpleName;

    public TypeKey(String typeName) {
        this.typeName = typeName;
    }

    public String[] generateTypeNames(){
        if(!isTypeObject()){
            return new String[0];
        }
        TypeKey main = getDeclaring();
        if(main != this){
            return main.generateTypeNames();
        }
        return new String[]{
                getDeclaringName(),
                getSignatureTypeName(),
                getArrayType(1),
                getArrayType(2),
                getArrayType(3)
        };
    }
    public String[] generateInnerTypePrefix(){
        if(!isTypeObject()){
            return new String[0];
        }
        TypeKey main = getDeclaring();
        if(main != this){
            return main.generateInnerTypePrefix();
        }
        String type = getTypeName();
        return new String[]{
                type.replace(';', '$'),
                getArrayType(1).replace(';', '$'),
                getArrayType(2).replace(';', '$'),
                getArrayType(3).replace(';', '$'),
        };
    }

    public String getTypeName() {
        return typeName;
    }

    public String getSourceName(){
        return DexUtils.toSourceName(getDeclaringName());
    }
    @Override
    public TypeKey getDeclaring(){
        String main = getDeclaringName();
        if(main.equals(getTypeName())){
            return this;
        }
        return TypeKey.create(main);
    }
    @Override
    public Iterator<TypeKey> mentionedKeys() {
        return SingleIterator.of(this);
    }

    @Override
    public Key replaceKey(Key search, Key replace) {
        if(search.equals(this)){
            return replace;
        }
        return this;
    }

    public String getDeclaringName() {
        return DexUtils.toDeclaringType(getTypeName());
    }
    public String getSignatureTypeName() {
        return DexUtils.toSignatureType(getTypeName());
    }
    public String getArrayType(int dimension){
        return DexUtils.makeArrayType(getTypeName(), dimension);
    }
    public int getArrayDimension(){
        return DexUtils.countArrayPrefix(getTypeName());
    }
    public boolean isTypeSignature(){
        return DexUtils.isTypeSignature(getTypeName());
    }
    public boolean isTypeArray(){
        return DexUtils.isTypeArray(getTypeName());
    }
    public boolean isTypeObject(){
        return DexUtils.isTypeObject(getTypeName());
    }
    public boolean isPrimitive(){
        return DexUtils.isPrimitive(getTypeName());
    }
    public boolean isWide(){
        String name = getTypeName();
        if(name.length() != 1){
            return false;
        }
        return name.equals(TYPE_D.getTypeName()) || name.equals(TYPE_J.getTypeName());
    }

    public TypeKey changePackageName(String packageName){
        String type = getTypeName();
        return new TypeKey(packageName + type.substring(getPackageName().length()));
    }
    public String getSimpleName() {
        if(simpleName == null){
            simpleName = DexUtils.getSimpleName(getTypeName());
        }
        return simpleName;
    }
    public String getSimpleInnerName(){
        return DexUtils.getSimpleInnerName(getTypeName());
    }
    public boolean isInnerName(){
        return !getSimpleName().equals(getSimpleInnerName());
    }
    public String getPackageName() {
        if(packageName == null){
            packageName = DexUtils.getPackageName(getTypeName());
        }
        return packageName;
    }
    public TypeKey getEnclosingClass(){
        String type = getTypeName();
        String parent = DexUtils.getParentClassName(type);
        if(type.equals(parent)){
            return this;
        }
        return new TypeKey(parent);
    }
    public TypeKey createInnerClass(String simpleName){
        String type = getTypeName();
        String child = DexUtils.createChildClass(type, simpleName);
        if(type.equals(child)){
            return this;
        }
        return new TypeKey(child);
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getTypeName());
    }

    @Override
    public int compareTo(Object obj) {
        if(obj == null){
            return -1;
        }
        if(obj == this){
            return 0;
        }
        TypeKey key = (TypeKey) obj;
        return CompareUtil.compare(getTypeName(), key.getTypeName());
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if(!(obj instanceof TypeKey)) {
            return false;
        }
        TypeKey key = (TypeKey) obj;
        return getTypeName().equals(key.getTypeName());
    }
    @Override
    public int hashCode() {
        return getTypeName().hashCode();
    }
    @Override
    public String toString() {
        return getTypeName();
    }

    public TypeKey parse(String text) {
        if(text == null){
            return null;
        }
        text = text.trim();
        if(text.length() < 3){
            return null;
        }
        if(text.charAt(0) != 'L' || text.charAt(text.length() - 1) != ';'){
            return null;
        }
        if(text.indexOf('>') > 0 || text.indexOf('(') > 0 || text.indexOf('@') > 0){
            return null;
        }
        return new TypeKey(text);
    }

    public TypeKey fromSourceName(String sourceName){
        if(sourceName == null || sourceName.length() == 0){
            return null;
        }
        if(sourceName.indexOf('.') >= 0){
            return create(DexUtils.toBinaryName(sourceName));
        }
        if(sourceName.equals(TYPE_B.getSourceName())){
            return TYPE_B;
        }
        if(sourceName.equals(TYPE_D.getSourceName())){
            return TYPE_D;
        }
        if(sourceName.equals(TYPE_F.getSourceName())){
            return TYPE_F;
        }
        if(sourceName.equals(TYPE_I.getSourceName())){
            return TYPE_I;
        }
        if(sourceName.equals(TYPE_J.getSourceName())){
            return TYPE_J;
        }
        if(sourceName.equals(TYPE_S.getSourceName())){
            return TYPE_S;
        }
        if(sourceName.equals(TYPE_V.getSourceName())){
            return TYPE_V;
        }
        if(sourceName.equals(TYPE_Z.getSourceName())){
            return TYPE_Z;
        }
        return create(DexUtils.toBinaryName(sourceName));
    }

    public static TypeKey create(String typeName){
        if(typeName == null){
            return null;
        }
        int length = typeName.length();
        if(length == 0){
            return null;
        }
        if(length != 1){
            return new TypeKey(typeName);
        }
        return primitiveType(typeName.charAt(0));
    }
    public static TypeKey read(SmaliReader reader) throws IOException {
        reader.skipSpaces();
        int position = reader.position();
        StringBuilder builder = new StringBuilder();
        while (reader.get() == '['){
            builder.append(reader.readASCII());
        }
        byte b = reader.get();
        if(b != 'L'){
            builder.append(reader.readASCII());
        }else {
            int i = reader.indexOfBeforeLineEnd(';');
            if(i < 0){
                reader.position(position);
                throw new SmaliParseException("Invalid type, missing ';'", reader);
            }
            i = i + 1;
            builder.append(reader.readString(i - reader.position()));
        }
        TypeKey typeKey = TypeKey.create(builder.toString());
        if(typeKey == null){
            reader.position(position);
            throw new SmaliParseException("Invalid type", reader);
        }
        return typeKey;
    }

    public static TypeKey primitiveType(char ch){
        switch (ch){
            case 'B':
                return TYPE_B;
            case 'C':
                return TYPE_C;
            case 'D':
                return TYPE_D;
            case 'F':
                return TYPE_F;
            case 'I':
                return TYPE_I;
            case 'J':
                return TYPE_J;
            case 'S':
                return TYPE_S;
            case 'V':
                return TYPE_V;
            case 'Z':
                return TYPE_Z;
            default:
                return null;
        }
    }


    public static TypeKey parseSignature(String type){
        if(DexUtils.isTypeOrSignature(type)){
            return new TypeKey(type);
        }
        return null;
    }
    public static final TypeKey NULL = new TypeKey("00"){
        @Override
        public boolean isPlatform() {
            return false;
        }
        @Override
        public boolean isPrimitive() {
            return false;
        }
        @Override
        public boolean isTypeArray() {
            return false;
        }
        @Override
        public boolean isTypeObject() {
            return false;
        }
        @Override
        public TypeKey getDeclaring() {
            return this;
        }
        @Override
        public String getTypeName() {
            return StringsUtil.EMPTY;
        }
        @Override
        public Iterator<TypeKey> mentionedKeys() {
            return EmptyIterator.of();
        }
        @Override
        public Key replaceKey(Key search, Key replace) {
            return this;
        }

        @Override
        public void append(SmaliWriter writer) {
        }
    };
    static class PrimitiveTypeKey extends TypeKey {
        private final String sourceName;
        public PrimitiveTypeKey(String type, String sourceName) {
            super(type);
            this.sourceName = sourceName;
        }
        @Override
        public String getSourceName() {
            return sourceName;
        }
        @Override
        public TypeKey getDeclaring() {
            return this;
        }
        @Override
        public boolean isPrimitive() {
            return true;
        }
        @Override
        public boolean isWide() {
            return false;
        }
        @Override
        public boolean isTypeObject() {
            return false;
        }
        @Override
        public boolean isTypeArray() {
            return false;
        }
        @Override
        public boolean isInnerName() {
            return false;
        }
        @Override
        public boolean isTypeSignature() {
            return false;
        }
    }

    public static final TypeKey TYPE_B = new PrimitiveTypeKey("B", "byte");
    public static final TypeKey TYPE_C = new PrimitiveTypeKey("C", "char");
    public static final TypeKey TYPE_D = new PrimitiveTypeKey("D", "double"){
        @Override
        public boolean isWide() {
            return true;
        }
    };
    public static final TypeKey TYPE_F = new PrimitiveTypeKey("F", "float");
    public static final TypeKey TYPE_I = new PrimitiveTypeKey("I", "int");
    public static final TypeKey TYPE_J = new PrimitiveTypeKey("J", "long"){
        @Override
        public boolean isWide() {
            return true;
        }
    };
    public static final TypeKey TYPE_S = new PrimitiveTypeKey("S", "short");
    public static final TypeKey TYPE_V = new PrimitiveTypeKey("V", "void");
    public static final TypeKey TYPE_Z = new PrimitiveTypeKey("Z", "boolean");

    public static final TypeKey OBJECT = new TypeKey("Ljava/lang/Object;");
    public static final TypeKey STRING = new TypeKey("Ljava/lang/String;");

    public static final TypeKey DALVIK_EnclosingClass = new TypeKey("Ldalvik/annotation/EnclosingClass;");
    public static final TypeKey DALVIK_EnclosingMethod = new TypeKey("Ldalvik/annotation/EnclosingMethod;");
    public static final TypeKey DALVIK_InnerClass = new TypeKey("Ldalvik/annotation/InnerClass;");
    public static final TypeKey DALVIK_MemberClass = new TypeKey("Ldalvik/annotation/MemberClasses;");
    public static final TypeKey DALVIK_Signature = new TypeKey("Ldalvik/annotation/Signature;");
}
