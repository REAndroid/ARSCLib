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
package com.reandroid.arsc.value;

import com.reandroid.utils.StringsUtil;

import java.util.HashMap;
import java.util.Map;

public class DataType {

    public static final DataType NULL;
    public static final DataType REFERENCE;
    public static final DataType ATTRIBUTE;
    public static final DataType STRING;
    public static final DataType FLOAT;
    public static final DataType DIMENSION;
    public static final DataType FRACTION;
    public static final DataType DYNAMIC_REFERENCE;
    public static final DataType DYNAMIC_ATTRIBUTE;
    public static final DataType DEC;
    public static final DataType HEX;
    public static final DataType BOOLEAN;
    public static final DataType COLOR_ARGB8;
    public static final DataType COLOR_RGB8;
    public static final DataType COLOR_ARGB4;
    public static final DataType COLOR_RGB4;

    private static final DataType[] SORTED_VALUES;
    private static final Map<String, DataType> nameMap;

    static {

        DataType[] values = new DataType[0x20];
        SORTED_VALUES = values;

        values[0x00] = NULL = new DataType(0x00, "NULL", "");
        values[0x01] = REFERENCE = new DataType(0x01, "REFERENCE", "reference");
        values[0x02] = ATTRIBUTE = new DataType(0x02, "ATTRIBUTE", "reference");
        values[0x03] = STRING = new DataType(0x03, "STRING", "string");
        values[0x04] = FLOAT = new DataType(0x04, "FLOAT", "float");
        values[0x05] = DIMENSION = new DataType(0x05, "DIMENSION", "dimension");
        values[0x06] = FRACTION = new DataType(0x06, "FRACTION", "fraction");
        values[0x07] = DYNAMIC_REFERENCE = new DataType(0x07, "DYNAMIC_REFERENCE", "reference");
        values[0x08] = DYNAMIC_ATTRIBUTE = new DataType(0x08, "DYNAMIC_ATTRIBUTE", "reference");
        values[0x10] = DEC = new DataType(0x10, "DEC", "integer");
        values[0x11] = HEX = new DataType(0x11, "HEX", "integer");
        values[0x12] = BOOLEAN = new DataType(0x12, "BOOLEAN", "bool");
        values[0x1c] = COLOR_ARGB8 = new DataType(0x1c, "COLOR_ARGB8", "color");
        values[0x1d] = COLOR_RGB8 = new DataType(0x1d, "COLOR_RGB8", "color");
        values[0x1e] = COLOR_ARGB4 = new DataType(0x1e, "COLOR_ARGB4", "color");
        values[0x1f] = COLOR_RGB4 = new DataType(0x1f, "COLOR_RGB4", "color");

        Map<String, DataType> map = new HashMap<>();
        nameMap = map;

        for (DataType type : values) {
            if (type != null) {
                map.put(type.name, type);
            }
        }
    }

    private final int flag;
    private final String name;
    private final String typeName;
    
    private DataType(int flag, String name, String typeName) {
        this.flag = flag;
        this.name = name;
        this.typeName = typeName;
    }

    public int flag() {
        return flag;
    }
    public String name() {
        return name;
    }
    public String typeName() {
        return typeName;
    }
    public boolean isColor() {
        return this == COLOR_ARGB8
                || this == COLOR_RGB8
                || this == COLOR_ARGB4
                || this == COLOR_RGB4;
    }
    public boolean isInteger() {
        return this == DEC
                || this == HEX;
    }
    public boolean isReference() {
        return this == REFERENCE
                || this == ATTRIBUTE
                || this == DYNAMIC_REFERENCE
                || this == DYNAMIC_ATTRIBUTE;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return flag;
    }
    @Override
    public String toString() {
        return name;
    }

    public static DataType valueOf(int i) {
        DataType[] values = SORTED_VALUES;
        if (i >= 0 && i < values.length) {
            return values[i];
        }
        return null;
    }
    public static DataType valueOf(String name) {
        return fromName(name);
    }
    public static DataType fromName(String name) {
        return nameMap.get(StringsUtil.toUpperCase(name));
    }
    public static DataType[] values() {
        DataType[] values = new DataType[16];
        int index = 0;
        for (DataType type : SORTED_VALUES) {
            if (type != null) {
                values[index ++] = type;
            }
        }
        return values;
    }
}
