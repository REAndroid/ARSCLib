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
package com.reandroid.dex.common;

import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.sections.SectionType;

public class OperandType {

    public static final OperandType NONE;
    public static final OperandType HEX;
    public static final OperandType DECIMAL;
    public static final OperandType LABEL;
    public static final OperandType STRING;
    public static final OperandType TYPE;
    public static final OperandType FIELD;
    public static final OperandType PROTO;
    public static final OperandType METHOD;
    public static final OperandType METHOD_HANDLE;
    public static final OperandType CALL_SITE;
    public static final OperandType METHOD_AND_PROTO;

    static {
        NONE = new OperandType("NONE");
        HEX = new OperandType("HEX");
        DECIMAL = new OperandType("DECIMAL");
        LABEL = new OperandType("LABEL");
        STRING = new OperandType("STRING", SectionType.STRING_ID);
        TYPE = new OperandType("TYPE", SectionType.TYPE_ID);
        FIELD = new OperandType("FIELD", SectionType.FIELD_ID);
        PROTO = new OperandType("PROTO", SectionType.PROTO_ID);
        METHOD = new OperandType("METHOD", SectionType.METHOD_ID);
        METHOD_HANDLE = new OperandType("METHOD_HANDLE", SectionType.METHOD_HANDLE);
        CALL_SITE = new OperandType("CALL_SITE", SectionType.CALL_SITE_ID);
        METHOD_AND_PROTO = new OperandType("METHOD_AND_PROTO", SectionType.METHOD_ID, SectionType.PROTO_ID);
    }

    private final String name;
    private final SectionType<? extends IdItem> sectionType;
    private final SectionType<? extends IdItem> sectionType2;

    private OperandType(String name) {
        this(name, null, null);
    }
    private OperandType(String name, SectionType<? extends IdItem> sectionType) {
        this(name, sectionType, null);
    }
    private OperandType(String name, SectionType<? extends IdItem> sectionType,
                        SectionType<? extends IdItem> sectionType2) {
        this.name = name;
        this.sectionType = sectionType;
        this.sectionType2 = sectionType2;
    }

    public SectionType<? extends IdItem> getSectionType() {
        return sectionType;
    }
    public SectionType<? extends IdItem> getSectionType2() {
        return sectionType2;
    }
    public boolean hasSectionId() {
        return sectionType != null;
    }
    public boolean hasSectionId2() {
        return sectionType2 != null;
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
        return name;
    }
}
