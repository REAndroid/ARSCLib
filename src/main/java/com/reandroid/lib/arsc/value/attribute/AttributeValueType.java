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
package com.reandroid.lib.arsc.value.attribute;

import com.reandroid.lib.arsc.value.ValueType;

import java.util.*;

 public enum AttributeValueType {
    REFERENCE((byte) 0x01),
    STRING((byte) 0x02),
    INTEGER((byte) 0x04),
    BOOL((byte) 0x08),
    COLOR((byte) 0x10),
    FLOAT((byte) 0x20),
    DIMENSION((byte) 0x40),
    FRACTION((byte) 0x80),
    ANY((byte) 0xee);

    private final byte mByte;
    AttributeValueType(byte b) {
        this.mByte=b;
    }
    public byte getByte(){
        return mByte;
    }
    @Override
    public String toString(){
        return name().toLowerCase();
    }
    public static String toString(AttributeValueType[] valueTypes){
        if(valueTypes==null || valueTypes.length==0){
            return null;
        }
        StringBuilder builder=new StringBuilder();
        boolean appendOnce=false;
        int sum=0;
        for(AttributeValueType vt:valueTypes){
            if(vt==AttributeValueType.ANY){
                return AttributeValueType.ANY.toString();
            }
            int b=vt.mByte;
            if((sum&b)!=0){
                continue;
            }
            sum=sum+b;
            if(appendOnce){
                builder.append("|");
            }
            builder.append(vt.toString());
            appendOnce=true;
        }
        return builder.toString();
    }
    public static byte getByte(AttributeValueType[] valueTypes){
        if(valueTypes==null){
            return 0;
        }
        int i=0;
        for(AttributeValueType vt:valueTypes){
            if(vt==null){
                continue;
            }
            i=i|(0xff & vt.mByte);
        }
        return (byte) (0xff & i);
    }
    public static AttributeValueType valueOf(byte b){
        AttributeValueType[] all=values();
        for(AttributeValueType vt:all){
            if(vt.mByte==b){
                return vt;
            }
        }
        return null;
    }
    public static AttributeValueType[] valuesOf(short val){
        List<AttributeValueType> results=new ArrayList<>();
        AttributeValueType[] all=values();
        for(AttributeValueType vt:all){
            byte b=vt.mByte;
            if(b==val){
                results.clear();
                results.add(vt);
                break;
            }
            if((val&b)!=0 && vt!=ANY){
                results.add(vt);
            }
        }
        return results.toArray(new AttributeValueType[0]);
    }
    public static AttributeValueType[] valuesOf(String valuesStr){
        if(valuesStr==null){
            return null;
        }
        String[] valueNames=valuesStr.split("[\\s|]+");
        List<AttributeValueType> results=new ArrayList<>();
        for(String name:valueNames){
            AttributeValueType vt=fromName(name);
            if(vt!=null){
                results.add(vt);
            }
        }
        return results.toArray(new AttributeValueType[0]);
    }
    public static AttributeValueType fromName(String name){
        if(name==null){
            return null;
        }
        name=name.toUpperCase();
        AttributeValueType[] all=values();
        for(AttributeValueType vt:all){
            if(name.equals(vt.name())){
                return vt;
            }
        }
        return null;
    }
    public static Set<ValueType> toValueTypes(AttributeValueType[] types){
        Set<ValueType> results=new HashSet<>();
        if(types==null){
            return results;
        }
        for(AttributeValueType type:types){
            if(type==ANY){
                return new HashSet<>(Arrays.asList(ValueType.values()));
            }
            switch (type){
                case REFERENCE:
                    results.add(ValueType.REFERENCE);
                    results.add(ValueType.ATTRIBUTE);
                    results.add(ValueType.DYNAMIC_REFERENCE);
                    results.add(ValueType.DYNAMIC_ATTRIBUTE);
                    break;
                case COLOR:
                    results.add(ValueType.INT_COLOR_ARGB8);
                    results.add(ValueType.INT_COLOR_ARGB4);
                    results.add(ValueType.INT_COLOR_RGB8);
                    results.add(ValueType.INT_COLOR_RGB4);
                    break;
                case STRING:
                    results.add(ValueType.STRING);
                    break;
                case BOOL:
                    results.add(ValueType.INT_BOOLEAN);
                    break;
                case FLOAT:
                case FRACTION:
                    results.add(ValueType.FLOAT);
                    results.add(ValueType.FRACTION);
                    break;
                case DIMENSION:
                    results.add(ValueType.FRACTION);
                    results.add(ValueType.DIMENSION);
                    break;
                case INTEGER:
                    results.add(ValueType.INT_DEC);
                    results.add(ValueType.INT_HEX);
                    break;
            }
        }
        return results;
    }
}
