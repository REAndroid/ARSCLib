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
import com.reandroid.utils.CompareUtil;

public class TypeKey implements Key{

    private final String type;
    private String packageName;
    private String simpleName;

    public TypeKey(String type) {
        this.type = type;
    }

    public String[] generateTypeNames(){
        if(!isTypeObject()){
            return new String[0];
        }
        TypeKey main = getMainType();
        if(main != this){
            return main.generateTypeNames();
        }
        return new String[]{
                getMainTypeName(),
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
        TypeKey main = getMainType();
        if(main != this){
            return main.generateInnerTypePrefix();
        }
        String type = getType();
        return new String[]{
                type.replace(';', '$'),
                getArrayType(1).replace(';', '$'),
                getArrayType(2).replace(';', '$'),
                getArrayType(3).replace(';', '$'),
        };
    }

    public String getType() {
        return type;
    }

    public String toJavaType(){
        return DexUtils.toJavaName(getMainTypeName());
    }
    public TypeKey getMainType(){
        String main = getMainTypeName();
        if(main.equals(getType())){
            return this;
        }
        return TypeKey.create(main);
    }
    public String getMainTypeName() {
        return DexUtils.toMainType(getType());
    }
    public String getSignatureTypeName() {
        return DexUtils.toSignatureType(getType());
    }
    public String getArrayType(int dimension){
        return DexUtils.makeArrayType(getType(), dimension);
    }
    public int getArrayDimension(){
        return DexUtils.countArrayPrefix(getType());
    }
    public boolean isTypeSignature(){
        return DexUtils.isTypeSignature(getType());
    }
    public boolean isTypeArray(){
        return DexUtils.isTypeArray(getType());
    }
    public boolean isTypeObject(){
        return DexUtils.isTypeObject(getType());
    }
    public boolean isPrimitive(){
        return DexUtils.isPrimitive(getType());
    }

    public TypeKey changePackageName(String packageName){
        String type = getType();
        return new TypeKey(packageName + type.substring(getPackageName().length()));
    }
    public String getSimpleName() {
        if(simpleName == null){
            simpleName = DexUtils.getSimpleName(getType());
        }
        return simpleName;
    }
    public String getPackageName() {
        if(packageName == null){
            packageName = DexUtils.getPackageName(getType());
        }
        return packageName;
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
        return CompareUtil.compare(getType(), key.getType());
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
        return getType().equals(key.getType());
    }
    @Override
    public int hashCode() {
        return getType().hashCode();
    }
    @Override
    public String toString() {
        return getType();
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
            case 'Z':
                return TYPE_Z;
            default:
                return null;
        }
    }

    public static final TypeKey TYPE_B = new TypeKey("B");
    public static final TypeKey TYPE_C = new TypeKey("C");
    public static final TypeKey TYPE_D = new TypeKey("D");
    public static final TypeKey TYPE_F = new TypeKey("F");
    public static final TypeKey TYPE_I = new TypeKey("I");
    public static final TypeKey TYPE_J = new TypeKey("J");
    public static final TypeKey TYPE_S = new TypeKey("S");
    public static final TypeKey TYPE_Z = new TypeKey("Z");

}
