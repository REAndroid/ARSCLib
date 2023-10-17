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
package com.reandroid.dex.id;

import com.reandroid.dex.data.StringData;

public class TypeName {
    private final StringData stringData;
    private String name;
    private final String prefix;
    private final char postfix;

    public TypeName(StringData stringData, String name, String prefix, char postfix){
        this.stringData = stringData;
        this.name = name;
        this.prefix = prefix;
        this.postfix = postfix;
    }

    public String get() {
        int length = name.length() + 1;
        StringBuilder builder;
        if(prefix != null){
            builder = new StringBuilder(length + prefix.length());
            builder.append(prefix);
        }else {
            builder = new StringBuilder(length);
        }
        builder.append(name);
        builder.append(postfix);
        return builder.toString();
    }
    public String getKey(){
        return getName() + postfix;
    }
    public String getName() {
        return name;
    }
    public void setName(TypeName typeName){
        if(typeName == null || typeName == this){
            return;
        }
        setName(typeName.getName());
    }
    public void setName(String name){
        if(isPostfix(name.charAt(name.length() - 1))){
            name = trimName(name);
        }
        if(name == null){
            return;
        }
        if(name.equals(this.name)){
            return;
        }
        this.name = name;
        onNameChanged();
    }
    private void onNameChanged(){
        this.stringData.setString(get());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TypeName typeName = (TypeName) obj;
        return stringData == typeName.stringData;
    }
    @Override
    public int hashCode() {
        return stringData.hashCode();
    }
    @Override
    public String toString() {
        return get();
    }

    public static TypeName createOrDefault(StringData stringData){
        TypeName typeName = create(stringData);
        if(typeName == null){
            typeName = DEFAULT;
        }
        return typeName;
    }
    public static TypeName create(StringData stringData){
        if(stringData == null){
            return null;
        }
        return create(stringData, stringData.getString());
    }

    public static TypeName create(StringData stringData, String className){
        String prefix = trimPrefix(className);
        if(prefix != null){
            className = className.substring(prefix.length());
        }
        String name = trimNameNoPrefix(className, true);
        if(name == null){
            return null;
        }
        char postfix = className.charAt(className.length() - 1);
        return new TypeName(stringData, name, prefix, postfix);
    }


    public static String sanitize(String className){
        String prefix = trimPrefix(className);
        if(prefix != null){
            className = className.substring(prefix.length());
        }
        return trimNameNoPrefix(className, false);
    }

    public static String trimName(String className) {
        if(className.length() < 3){
            return null;
        }
        String prefix = trimPrefix(className);
        if(prefix != null){
            className = className.substring(prefix.length());
        }
        return trimNameNoPrefix(className, true);
    }
    private static String trimNameNoPrefix(String className, boolean dropPostfix) {
        int length = className.length();
        if(length < 3){
            return null;
        }
        char first = className.charAt(0);
        if(first != 'L'){
            return null;
        }
        length = length - 1;
        if(!isPostfix(className.charAt(length))){
            return null;
        }
        for(int i = 0; i < length; i++){
            if(isBadNameChar(className.charAt(i))){
                return null;
            }
        }
        if(!dropPostfix){
            return className;
        }
        return className.substring(0, length);
    }
    private static String trimPrefix(String className) {
        if(className.length() < 4){
            return null;
        }
        StringBuilder prefix = new StringBuilder();
        if(className.startsWith(ANNOTATION_PREFIX)){
            prefix.append(ANNOTATION_PREFIX);
            className = className.substring(ANNOTATION_PREFIX.length());
        }
        int i = 0;
        while (className.charAt(i) == '['){
            prefix.append(className.charAt(i));
            i++;
        }
        if(prefix.length() != 0){
            return prefix.toString();
        }
        return null;
    }
    private static boolean isPostfix(char ch){
        return ch == ';' || ch == '<';
    }
    private static boolean isBadNameChar(char ch){
        switch (ch){
            case ' ':
            case ';':
            case ':':
            case '|':
            case '[':
            case ']':
            case '}':
            case '{':
            case '<':
            case '>':
            case '(':
            case ')':
            case '?':
            case ',':
            case '*':
            case '\n':
            case '\r':
            case '\t':
            case '\\':
                return true;
            default:
                return false;
        }
    }

    private static final String ANNOTATION_PREFIX = "L:";

    public static final TypeName DEFAULT = new TypeName(null, "null", null, ';'){

        @Override
        public String get() {
            return getName();
        }
        @Override
        public void setName(TypeName typeName){
        }
        @Override
        public void setName(String name){
        }
        @Override
        public boolean equals(Object obj) {
            return obj == this;
        }
        @Override
        public int hashCode() {
            return 0;
        }
        @Override
        public String toString() {
            return getName();
        }
    };
}
