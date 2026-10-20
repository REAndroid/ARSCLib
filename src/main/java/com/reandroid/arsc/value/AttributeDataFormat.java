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

public enum AttributeDataFormat {

    REFERENCE(1<<0,
            new DataType[]{
                    DataType.REFERENCE,
                    DataType.ATTRIBUTE,
                    DataType.DYNAMIC_REFERENCE,
                    DataType.DYNAMIC_ATTRIBUTE,
                    DataType.NULL,
    }),
    INTEGER(1<<2, new DataType[]{
            DataType.DEC,
            DataType.HEX
    }),
    BOOL(1<<3, new DataType[]{
            DataType.BOOLEAN
    }),
    COLOR(1<<4, new DataType[]{
            DataType.COLOR_ARGB8,
            DataType.COLOR_RGB8,
            DataType.COLOR_RGB4,
            DataType.COLOR_ARGB4
    }),
    FLOAT(1<<5, new DataType[]{
            DataType.FLOAT
    }),
    DIMENSION(1<<6, new DataType[]{
            DataType.DIMENSION
    }),
    FRACTION(1<<7, new DataType[]{
            DataType.FRACTION
    }),
    ANY(0x0000FFFF, DataType.values()),

    ENUM(1<<16, new DataType[]{
            DataType.DEC,
            DataType.HEX
    }),
    FLAG(1<<17, new DataType[]{
            DataType.HEX,
            DataType.DEC
    }),
    STRING(1<<1, new DataType[]{
            DataType.STRING
    });

    private final int mask;
    private final DataType[] dataTypes;

    AttributeDataFormat(int mask, DataType[] dataTypes) {
        this.mask = mask;
        this.dataTypes = dataTypes;
    }

    public int getMask() {
        return mask;
    }
    public boolean matches(int value) {
        int mask = this.mask;
        return (value & mask) == mask;
    }

    public DataType[] dataTypes() {
        return dataTypes;
    }
    public DataType[] getDataTypes() {
        return dataTypes.clone();
    }
    public boolean contains(DataType dataType) {
        DataType[] dataTypes = this.dataTypes;
        for (int i = 0; i < dataTypes.length; i++) {
            if (dataType == dataTypes[i]) {
                return true;
            }
        }
        return false;
    }
    public String getName() {
        return name().toLowerCase();
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        builder.append('{');
        DataType[] dataTypes = this.dataTypes;
        for (int i = 0; i < dataTypes.length; i++) {
            if (i != 0) {
                builder.append(',');
            }
            builder.append(dataTypes[i]);
        }
        builder.append('}');
        return builder.toString();
    }

    public static String toStringDataTypes(int data) {
        return toString(decodeDataTypes(data));
    }
    public static String toString(AttributeDataFormat[] typeValues) {
        if (typeValues == null || typeValues.length == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();

        boolean appendOnce = false;
        int appendedTypes = 0;
        for (AttributeDataFormat typeValue : typeValues) {
            if (typeValue == ENUM || typeValue == FLAG) {
                continue;
            }
            if (typeValue == AttributeDataFormat.ANY) {
                return AttributeDataFormat.ANY.getName();
            }
            int mask = typeValue.getMask();
            if ((appendedTypes & mask) == mask) {
                continue;
            }
            if (appendOnce) {
                builder.append('|');
            }
            builder.append(typeValue.getName());
            appendOnce = true;
            appendedTypes = appendedTypes | mask;
        }
        return builder.toString();
    }
    public static int sum(AttributeDataFormat[] typeValues) {
        if (typeValues == null) {
            return 0;
        }
        int result = 0;
        for (AttributeDataFormat typeValue:typeValues) {
            if (typeValue==null) {
                continue;
            }
            result |= typeValue.getMask();
        }
        return result;
    }

    public static AttributeDataFormat[] decodeDataTypes(int data) {
        if ((data & 0xffff) == 0xffff) {
            return new AttributeDataFormat[]{ANY};
        }
        data &= 0xff;
        if (data == 0) {
            return null;
        }
        AttributeDataFormat[] results = new AttributeDataFormat[Integer.bitCount(data)];
        AttributeDataFormat[] dataTypes = DATA_TYPES;
        int length = dataTypes.length - 1;
        int j = 0;
        for (int i = 0; i < length; i++) {
            if ((data & 1) != 0) {
                results[j] = dataTypes[i];
                j ++;
            }
            data = data >> 1;
        }
        return results;
    }
    public static AttributeDataFormat[] parseValueTypes(String valuesTypes) {
        if (valuesTypes == null) {
            return null;
        }
        valuesTypes = valuesTypes.trim();
        String[] valueNames = valuesTypes.split("\\s*\\|\\s*");
        AttributeDataFormat[] tmp = new AttributeDataFormat[DATA_TYPES.length];
        int length = 0;
        for (String name:valueNames) {
            AttributeDataFormat typeValue = fromDataTypeName(name);
            if (typeValue!=null) {
                tmp[length] = typeValue;
                length++;
            }
        }
        if (length == 0) {
            return null;
        }
        AttributeDataFormat[] results = new AttributeDataFormat[length];
        System.arraycopy(tmp, 0, results, 0, length);
        return results;
    }
    public static AttributeDataFormat valueOf(int mask) {
        for (AttributeDataFormat typeValue : DATA_TYPES) {
            if (typeValue.getMask() == mask) {
                return typeValue;
            }
        }
        return null;
    }
    public static AttributeDataFormat typeOfBag(int data) {
        for (AttributeDataFormat typeValue : BAG_TYPES) {
            if (typeValue.matches(data)) {
                return typeValue;
            }
        }
        return null;
    }
    public static AttributeDataFormat fromDataTypeName(String name) {
        if (name == null) {
            return null;
        }
        name = StringsUtil.toUpperCase(name.trim());
        for (AttributeDataFormat typeValue : DATA_TYPES) {
            if (name.equals(typeValue.name())) {
                return typeValue;
            }
        }
        return null;
    }
    public static AttributeDataFormat fromBagTypeName(String bagTypeName) {
        if (bagTypeName == null) {
            return null;
        }
        bagTypeName = StringsUtil.toUpperCase(bagTypeName.trim());
        for (AttributeDataFormat typeValue: BAG_TYPES) {
            if (bagTypeName.equals(typeValue.name())) {
                return typeValue;
            }
        }
        return null;
    }

    public static DataType[] getExpectedDataTypes(String typeName) {
        AttributeDataFormat dataFormat = fromDataTypeName(typeName);
        if (dataFormat != null) {
            return dataFormat.dataTypes();
        }
        return null;
    }

    public static boolean contains(AttributeDataFormat[] formats, DataType dataType) {
        if (formats == null || dataType == null) {
            return false;
        }
        for (AttributeDataFormat dataFormat : formats) {
            if (dataFormat != null && dataFormat.contains(dataType)) {
                return true;
            }
        }
        return false;
    }

    private static final AttributeDataFormat[] DATA_TYPES = new AttributeDataFormat[]{
            REFERENCE,
            STRING,
            INTEGER,
            BOOL,
            COLOR,
            FLOAT,
            DIMENSION,
            FRACTION,
            ANY
    };

    private static final AttributeDataFormat[] BAG_TYPES = new AttributeDataFormat[]{
            ENUM,
            FLAG
    };
}
