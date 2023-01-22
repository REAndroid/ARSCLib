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


public enum ValueType {

    NULL((byte) 0x00),
    REFERENCE((byte) 0x01),
    ATTRIBUTE((byte) 0x02),
    STRING((byte) 0x03),
    FLOAT((byte) 0x04),
    DIMENSION((byte) 0x05),
    FRACTION((byte) 0x06),
    DYNAMIC_REFERENCE((byte) 0x07),
    DYNAMIC_ATTRIBUTE((byte) 0x08),
    INT_DEC((byte) 0x10),
    INT_HEX((byte) 0x11),
    INT_BOOLEAN((byte) 0x12),
    INT_COLOR_ARGB8((byte) 0x1c),
    INT_COLOR_RGB8((byte) 0x1d),
    INT_COLOR_ARGB4((byte) 0x1e),
    INT_COLOR_RGB4((byte) 0x1f);

    private final byte mByte;
    ValueType(byte b) {
        this.mByte=b;
    }
    public byte getByte(){
        return mByte;
    }
    public static ValueType valueOf(byte b){
        ValueType[] all=values();
        for(ValueType vt:all){
            if(vt.mByte==b){
                return vt;
            }
        }
        return null;
    }
    public static ValueType fromName(String name){
        if(name==null){
            return null;
        }
        name=name.toUpperCase();
        ValueType[] all=values();
        for(ValueType vt:all){
            if(name.equals(vt.name())){
                return vt;
            }
        }
        return null;
    }
}
