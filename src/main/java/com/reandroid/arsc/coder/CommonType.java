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
package com.reandroid.arsc.coder;

import com.reandroid.arsc.value.DataType;

import java.util.HashMap;
import java.util.Map;

public class CommonType {

    private static final Map<String, CommonType> TYPE_MAP;

    public static final CommonType ANIM;
    public static final CommonType ANIMATOR;
    public static final CommonType BOOL;
    public static final CommonType COLOR;
    public static final CommonType DIMEN;
    public static final CommonType DRAWABLE;
    public static final CommonType FRACTION;
    public static final CommonType FONT;
    public static final CommonType ID;
    public static final CommonType INTEGER;
    public static final CommonType INTERPOLATOR;
    public static final CommonType LAYOUT;
    public static final CommonType MENU;
    public static final CommonType MIPMAP;
    public static final CommonType NAVIGATION;
    public static final CommonType RAW;
    public static final CommonType STRING;
    public static final CommonType TRANSITION;
    public static final CommonType XML;

    private static final CommonType[] VALUES;
    private static final String[] COMPLEX_TYPES;

    static {
        Map<String, CommonType> map = new HashMap<>();
        TYPE_MAP = map;
        CommonType type  = new CommonType("anim", new DataType[]{
                DataType.STRING,
        });
        map.put(type.name, type);
        ANIM = type;
        type  = new CommonType("animator", new DataType[]{
                DataType.STRING,
        });
        map.put(type.name, type);
        ANIMATOR = type;
        type  = new CommonType("bool", new DataType[]{
                DataType.BOOLEAN
        });
        map.put(type.name, type);
        BOOL = type;
        type  = new CommonType("color", new DataType[]{
                DataType.COLOR_ARGB8,
                DataType.COLOR_RGB8,
                DataType.COLOR_RGB4,
                DataType.COLOR_ARGB4
        });
        map.put(type.name, type);
        COLOR = type;
        type  = new CommonType("dimen", new DataType[]{
                DataType.DIMENSION,
                DataType.FRACTION,
                DataType.FLOAT,
                DataType.DEC
        });
        map.put(type.name, type);
        DIMEN = type;
        type  = new CommonType("drawable", new DataType[]{
                DataType.COLOR_ARGB8,
                DataType.COLOR_RGB8,
                DataType.COLOR_RGB4,
                DataType.COLOR_ARGB4,
                DataType.STRING
        });
        map.put(type.name, type);
        DRAWABLE = type;
        type  = new CommonType("fraction", new DataType[]{
                DataType.FRACTION
        });
        map.put(type.name, type);
        FRACTION = type;
        type  = new CommonType("font", new DataType[]{
                DataType.STRING
        });
        map.put(type.name, type);
        FONT = type;
        type  = new CommonType("id", new DataType[]{
                DataType.BOOLEAN,
                DataType.STRING
        });
        map.put(type.name, type);
        ID = type;
        type  = new CommonType("integer", new DataType[]{
                DataType.DEC,
                DataType.HEX
        });
        map.put(type.name, type);
        INTEGER = type;
        type  = new CommonType("interpolator", new DataType[]{
                DataType.STRING
        });
        map.put(type.name, type);
        INTERPOLATOR = type;
        type  = new CommonType("layout", new DataType[]{
                DataType.STRING
        });
        map.put(type.name, type);
        LAYOUT = type;
        type  = new CommonType("menu", new DataType[]{
                DataType.STRING
        });
        map.put(type.name, type);
        MENU = type;
        type  = new CommonType("mipmap", new DataType[]{
                DataType.COLOR_ARGB8,
                DataType.COLOR_RGB8,
                DataType.COLOR_RGB4,
                DataType.COLOR_ARGB4,
                DataType.STRING
        });
        map.put(type.name, type);
        MIPMAP = type;
        type  = new CommonType("navigation", new DataType[]{
                DataType.STRING
        });
        map.put(type.name, type);
        NAVIGATION = type;
        type  = new CommonType("raw", new DataType[]{
                DataType.STRING
        });
        map.put(type.name, type);
        RAW = type;
        type  = new CommonType("string", new DataType[]{
                DataType.STRING
        });
        map.put(type.name, type);
        STRING = type;
        type  = new CommonType("transition", new DataType[]{
                DataType.STRING
        });
        map.put(type.name, type);
        TRANSITION = type;
        type  = new CommonType("xml", new DataType[]{
                DataType.STRING
        });
        map.put(type.name, type);
        XML = type;

        VALUES = new CommonType[]{
                ANIM,
                ANIMATOR,
                BOOL,
                COLOR,
                DIMEN,
                DRAWABLE,
                FRACTION,
                FONT,
                ID,
                INTEGER,
                INTERPOLATOR,
                LAYOUT,
                MENU,
                MIPMAP,
                NAVIGATION,
                RAW,
                STRING,
                TRANSITION,
                XML,
        };

        COMPLEX_TYPES = new String[]{
                "attr",
                "array",
                "plurals",
                "style"
        };
    }

    private final String name;
    private final DataType[] dataTypes;

    private CommonType(String name, DataType[] dataTypes) {
        this.name = name;
        this.dataTypes = dataTypes;
    }

    public String name() {
        return name;
    }

    public DataType[] dataTypes() {
        return dataTypes;
    }
    public boolean isDifferent(DataType dataType) {
        if (dataType == null
                || dataType == DataType.NULL
                || dataType.isReference()) {
            return false;
        }
        return !contains(dataType);
    }
    public boolean contains(DataType dataType) {
        for (DataType vt : this.dataTypes) {
            if (vt == dataType) {
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return this.name.hashCode() * 31;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        builder.append(", types = [");
        DataType[] dataTypes = this.dataTypes;
        for (int i = 0; i < dataTypes.length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(dataTypes[i]);
        }
        builder.append(']');
        return builder.toString();
    }

    public static CommonType[] getValues() {
        return VALUES.clone();
    }
    public static CommonType valueOf(String typeName) {
        return TYPE_MAP.get(typeName);
    }
    public static DataType[] getExpectedTypes(String typeName) {
        CommonType commonType = valueOf(typeName);
        if (commonType != null) {
            return commonType.dataTypes();
        }
        return null;
    }
    public static boolean isCommonTypeName(String typeName) {
        return TYPE_MAP.containsKey(typeName)
                || isComplexTypeName(typeName);
    }
    public static boolean isComplexTypeName(String typeName) {
        for (String complex : COMPLEX_TYPES) {
            if (complex.equals(typeName)) {
                return true;
            }
        }
        return false;
    }
}
