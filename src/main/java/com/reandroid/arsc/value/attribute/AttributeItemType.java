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
package com.reandroid.arsc.value.attribute;

public enum AttributeItemType {
    FORMAT((short)0x0000),
    MIN((short)0x0001),
    MAX((short)0x0002),
    L10N((short)0x0003);
    private final short mValue;
    AttributeItemType(short value){
        this.mValue=value;
    }
    public short getValue() {
        return mValue;
    }
    @Override
    public String toString(){
        return name().toLowerCase();
    }
    public static AttributeItemType valueOf(short value){
        AttributeItemType[] all=values();
        for(AttributeItemType bagType:all){
            if(bagType.mValue==value){
                return bagType;
            }
        }
        return null;
    }
    public static AttributeItemType fromName(String name){
        if(name==null){
            return null;
        }
        name=name.toUpperCase();
        if(name.equals("FORMATS")){
            return FORMAT;
        }
        for(AttributeItemType bagType:values()){
            if(name.equals(bagType.name())){
                return bagType;
            }
        }
        return null;
    }
}
