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
package com.reandroid.dex.value;


public class DexValueType {

    private static final DexValueType[] VALUES;
    private static final DexValueType[] VALUES_COPY;

    public static final DexValueType BYTE;
    public static final DexValueType SHORT;
    public static final DexValueType CHAR;
    public static final DexValueType INT;
    public static final DexValueType LONG;
    public static final DexValueType FLOAT;
    public static final DexValueType DOUBLE;
    public static final DexValueType METHOD_TYPE;
    public static final DexValueType METHOD_HANDLE;
    public static final DexValueType STRING;
    public static final DexValueType TYPE;
    public static final DexValueType FIELD;
    public static final DexValueType METHOD;
    public static final DexValueType ENUM;
    public static final DexValueType ARRAY;
    public static final DexValueType ANNOTATION;
    public static final DexValueType NULL;
    public static final DexValueType BOOLEAN;

    static {
        DexValueType[] valueTypes = new DexValueType[0x1f + 1];
        VALUES = valueTypes;

        BYTE = new DexValueType("BYTE", 0x00, 0, true);
        valueTypes[0x00] = BYTE;
        SHORT = new DexValueType("SHORT", 0x02, 1, true);
        valueTypes[0x02] = SHORT;
        CHAR = new DexValueType("CHAR", 0x03, 1, true);
        valueTypes[0x03] = CHAR;
        INT = new DexValueType("INT", 0x04, 3, true);
        valueTypes[0x04] = INT;
        LONG = new DexValueType("LONG", 0x06, 7, true);
        valueTypes[0x06] = LONG;
        FLOAT = new DexValueType("FLOAT", 0x10, 3, true);
        valueTypes[0x10] = FLOAT;
        DOUBLE = new DexValueType("DOUBLE", 0x11, 7, true);
        valueTypes[0x11] = DOUBLE;
        METHOD_TYPE = new DexValueType("METHOD_TYPE", 0x15, 3, false);
        valueTypes[0x15] = METHOD_TYPE;
        METHOD_HANDLE = new DexValueType("METHOD_HANDLE", 0x16, 3, false);
        valueTypes[0x16] = METHOD_HANDLE;
        STRING = new DexValueType("STRING", 0x17, 3, true);
        valueTypes[0x17] = STRING;
        TYPE = new DexValueType("TYPE", 0x18, 3, true);
        valueTypes[0x18] = TYPE;
        FIELD = new DexValueType("FIELD", 0x19, 3, false);
        valueTypes[0x19] = FIELD;
        METHOD = new DexValueType("METHOD", 0x1a, 3, false);
        valueTypes[0x1a] = METHOD;
        ENUM = new DexValueType("ENUM", 0x1b, 3, false);
        valueTypes[0x1b] = ENUM;
        ARRAY = new DexValueType("ARRAY", 0x1c, 0, false);
        valueTypes[0x1c] = ARRAY;
        ANNOTATION = new DexValueType("ANNOTATION", 0x1d, 0, false);
        valueTypes[0x1d] = ANNOTATION;
        NULL = new DexValueType("NULL", 0x1e, 0, false);
        valueTypes[0x1e] = NULL;
        BOOLEAN = new DexValueType("BOOLEAN", 0x1f, 1, true);
        valueTypes[0x1f] = BOOLEAN;
        int index = 0;
        for(int i = 0; i < valueTypes.length; i++){
            if(valueTypes[i] != null){
                index ++;
            }
        }
        VALUES_COPY = new DexValueType[index];
        index = 0;
        for(int i = 0; i < valueTypes.length; i++){
            if(valueTypes[i] != null){
                VALUES_COPY[index] = valueTypes[i];
                index ++;
            }
        }

    }

    private final String name;
    private final int type;
    private final int size;
    private final boolean primitive;
    private final int flag;

    private DexValueType(String name, int type, int size, boolean primitive){
        this.name = name;
        this.type = type;
        this.size = size;
        this.primitive = primitive;

        flag = (size << 5) | type;
    }

    public int getSize() {
        return size;
    }
    public int getType() {
        return type;
    }
    public boolean isPrimitive() {
        return primitive;
    }

    public int getFlag(){
        return flag;
    }
    public int getFlag(int size){
        return (size << 5) | type;
    }

    @Override
    public String toString() {
        return name;
    }
    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return flag;
    }

    public static DexValueType fromFlag(int flag){
        return VALUES[flag & 0x1f];
    }
    public static int decodeSize(int flag){
        return flag >>> 5;
    }

    public static DexValueType[] values() {
        return VALUES_COPY;
    }
}
