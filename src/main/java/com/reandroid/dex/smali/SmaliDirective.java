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
package com.reandroid.dex.smali;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SmaliDirective implements SmaliFormat {

    private static final byte[] END_BYTES;
    public static final SmaliDirective CLASS;
    public static final SmaliDirective SUPER;
    public static final SmaliDirective SOURCE;
    public static final SmaliDirective IMPLEMENTS;
    public static final SmaliDirective ANNOTATION;
    public static final SmaliDirective SUB_ANNOTATION;
    public static final SmaliDirective FIELD;
    public static final SmaliDirective METHOD;

    public static final SmaliDirective CATCH;
    public static final SmaliDirective LOCALS;

    public static final SmaliDirective ARRAY_DATA;
    public static final SmaliDirective PACKED_SWITCH;
    public static final SmaliDirective PARAM;

    private static final SmaliDirective[] VALUES;

    static {

        END_BYTES = new byte[]{'e', 'n', 'd'};

        CLASS = new SmaliDirective("class");
        SUPER = new SmaliDirective("super");
        SOURCE = new SmaliDirective("source");
        IMPLEMENTS = new SmaliDirective("implements");
        ANNOTATION = new SmaliDirective("annotation");
        SUB_ANNOTATION = new SmaliDirective("subannotation");
        FIELD = new SmaliDirective("field");
        METHOD = new SmaliDirective("method");

        LOCALS = new SmaliDirective("locals");
        CATCH = new SmaliDirective("catch");
        ARRAY_DATA = new SmaliDirective("array-data");
        PACKED_SWITCH = new SmaliDirective("packed-switch");

        PARAM = new SmaliDirective("param");


        VALUES = new SmaliDirective[]{
                CLASS,
                SUPER,
                SOURCE,
                IMPLEMENTS,
                ANNOTATION,
                SUB_ANNOTATION,
                FIELD,
                METHOD,
                LOCALS,
                CATCH,
                ARRAY_DATA,
                PACKED_SWITCH,
                PARAM
        };
    }

    private final String name;
    private final byte[] nameBytes;

    SmaliDirective(String name, byte[] nameBytes){
        this.name = name;
        this.nameBytes = nameBytes;
    }
    SmaliDirective(String name){
        this(name, name.getBytes(StandardCharsets.UTF_8));
    }

    public String getName() {
        return name;
    }
    public boolean is(SmaliDirective smaliDirective) {
        return smaliDirective == this;
    }
    boolean readMatches(SmaliReader reader){
        if(reader.startsWith(nameBytes)){
            reader.skip(nameBytes.length);
            return true;
        }
        return false;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append('.');
        writer.append(getName());
        writer.append(' ');
    }
    public void appendEnd(SmaliWriter writer) throws IOException {
        writer.newLine();
        writer.append(".end ");
        writer.append(getName());
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "." + getName();
    }

    public static SmaliDirective parse(SmaliReader reader){
        if(reader.get() != '.'){
            return null;
        }
        reader.skip(1);
        if(reader.startsWith(END_BYTES)){
            reader.skip(END_BYTES.length);
            reader.skipWhitespaces();
        }
        return directiveOf(reader);
    }
    private static SmaliDirective directiveOf(SmaliReader reader){
        for(SmaliDirective smaliDirective : VALUES){
            if(smaliDirective.readMatches(reader)){
                return smaliDirective;
            }
        }
        return null;
    }

}
