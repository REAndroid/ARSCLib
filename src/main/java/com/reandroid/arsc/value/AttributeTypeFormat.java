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

public enum AttributeTypeFormat {

    REFERENCE(1<<0,
            new ValueType[]{
                    ValueType.REFERENCE,
                    ValueType.ATTRIBUTE,
                    ValueType.DYNAMIC_REFERENCE,
                    ValueType.DYNAMIC_ATTRIBUTE,
                    ValueType.NULL,
    }),
    STRING(1<<1, new ValueType[]{
            ValueType.STRING
    }),
    INTEGER(1<<2, new ValueType[]{
            ValueType.INT_DEC,
            ValueType.INT_HEX
    }),
    BOOL(1<<3, new ValueType[]{
            ValueType.INT_BOOLEAN
    }),
    COLOR(1<<4, new ValueType[]{
            ValueType.INT_COLOR_RGB4,
            ValueType.INT_COLOR_ARGB4,
            ValueType.INT_COLOR_RGB8,
            ValueType.INT_COLOR_ARGB8
    }),
    FLOAT(1<<5, new ValueType[]{
            ValueType.FLOAT
    }),
    DIMENSION(1<<6, new ValueType[]{
            ValueType.DIMENSION
    }),
    FRACTION(1<<7, new ValueType[]{
            ValueType.FRACTION
    }),
    ANY(0x0000FFFF, ValueType.values().clone()),

    ENUM(1<<16, new ValueType[]{
            ValueType.INT_DEC,
            ValueType.INT_HEX
    }),
    FLAG(1<<17, new ValueType[]{
            ValueType.INT_HEX,
            ValueType.INT_DEC
    });

    private final int mask;
    private final ValueType[] valueTypes;

    AttributeTypeFormat(int mask, ValueType[] valueTypes) {
        this.mask = mask;
        this.valueTypes = valueTypes;
    }

    public int getMask() {
        return mask;
    }
    public boolean matches(int value){
        int mask = this.mask;
        return (value & mask) == mask;
    }

    public ValueType[] getValueTypes() {
        return valueTypes.clone();
    }
    public boolean contains(ValueType valueType){
        ValueType[] valueTypes = this.valueTypes;
        for(int i = 0; i < valueTypes.length; i++){
            if(valueType == valueTypes[i]){
                return true;
            }
        }
        return false;
    }
    public String getName(){
        return name().toLowerCase();
    }
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        builder.append('{');
        ValueType[] valueTypes = this.valueTypes;
        for(int i = 0; i < valueTypes.length; i++){
            if(i != 0){
                builder.append(',');
            }
            builder.append(valueTypes[i]);
        }
        builder.append('}');
        return builder.toString();
    }

    public static String toStringValueTypes(int data){
        return toString(decodeValueTypes(data));
    }
    public static String toString(AttributeTypeFormat[] typeValues){
        if(typeValues == null || typeValues.length == 0){
            return null;
        }
        StringBuilder builder = new StringBuilder();

        boolean appendOnce = false;
        int appendedTypes = 0;
        for(AttributeTypeFormat typeValue : typeValues){
            if(typeValue == ENUM || typeValue == FLAG){
                continue;
            }
            if(typeValue == AttributeTypeFormat.ANY){
                return AttributeTypeFormat.ANY.getName();
            }
            int mask = typeValue.getMask();
            if((appendedTypes & mask) == mask){
                continue;
            }
            if(appendOnce){
                builder.append('|');
            }
            builder.append(typeValue.getName());
            appendOnce = true;
            appendedTypes = appendedTypes | mask;
        }
        return builder.toString();
    }
    public static int sum(AttributeTypeFormat[] typeValues){
        if(typeValues == null){
            return 0;
        }
        int result = 0;
        for(AttributeTypeFormat typeValue:typeValues){
            if(typeValue==null){
                continue;
            }
            result |= typeValue.getMask();
        }
        return result;
    }

    public static AttributeTypeFormat[] decodeValueTypes(int data){
        AttributeTypeFormat[] tmp = new AttributeTypeFormat[VALUE_TYPES.length];
        int length = 0;
        for(AttributeTypeFormat typeValue : VALUE_TYPES){
            int mask = typeValue.getMask();
            if(mask == data){
                return new AttributeTypeFormat[]{typeValue};
            }
            if(typeValue == ANY){
                continue;
            }
            if((data & mask) == mask){
                tmp[length] = typeValue;
                length++;
            }
        }
        if(length == 0){
            return null;
        }
        AttributeTypeFormat[] results = new AttributeTypeFormat[length];
        System.arraycopy(tmp, 0, results, 0, length);
        return results;
    }
    public static AttributeTypeFormat[] parseValueTypes(String valuesTypes){
        if(valuesTypes == null){
            return null;
        }
        valuesTypes = valuesTypes.trim();
        String[] valueNames = valuesTypes.split("\\s*\\|\\s*");
        AttributeTypeFormat[] tmp = new AttributeTypeFormat[VALUE_TYPES.length];
        int length = 0;
        for(String name:valueNames){
            AttributeTypeFormat typeValue = fromValueTypeName(name);
            if(typeValue!=null){
                tmp[length] = typeValue;
                length++;
            }
        }
        if(length == 0){
            return null;
        }
        AttributeTypeFormat[] results = new AttributeTypeFormat[length];
        System.arraycopy(tmp, 0, results, 0, length);
        return results;
    }
    public static AttributeTypeFormat valueOf(int mask){
        for(AttributeTypeFormat typeValue : VALUE_TYPES){
            if(typeValue.getMask() == mask){
                return typeValue;
            }
        }
        return null;
    }
    public static AttributeTypeFormat typeOfBag(int data){
        for(AttributeTypeFormat typeValue : BAG_TYPES){
            if(typeValue.matches(data)){
                return typeValue;
            }
        }
        return null;
    }
    public static AttributeTypeFormat fromValueTypeName(String name){
        if(name == null){
            return null;
        }
        name = name.trim().toUpperCase();
        for(AttributeTypeFormat typeValue : VALUE_TYPES){
            if(name.equals(typeValue.name())){
                return typeValue;
            }
        }
        return null;
    }
    public static AttributeTypeFormat fromBagTypeName(String bagTypeName){
        if(bagTypeName == null){
            return null;
        }
        bagTypeName = bagTypeName.trim().toUpperCase();
        for(AttributeTypeFormat typeValue: BAG_TYPES){
            if(bagTypeName.equals(typeValue.name())){
                return typeValue;
            }
        }
        return null;
    }

    private static final AttributeTypeFormat[] VALUE_TYPES = new AttributeTypeFormat[]{
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

    private static final AttributeTypeFormat[] BAG_TYPES = new AttributeTypeFormat[]{
            ENUM,
            FLAG
    };
}
