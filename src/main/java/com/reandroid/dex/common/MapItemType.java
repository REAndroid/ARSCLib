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

public enum MapItemType {

    HEADER_ITEM(0x0000),
    STRING_ID_ITEM(0x0001),
    TYPE_ID_ITEM(0x0002),
    PROTO_ID_ITEM(0x0003),
    FIELD_ID_ITEM(0x0004),
    METHOD_ID_ITEM(0x0005),
    CLASS_DEF_ITEM(0x0006),
    CALL_SITE_ID_ITEM(0x0007),
    METHOD_HANDLE_ITEM(0x0008),
    MAP_LIST(0x1000),
    TYPE_LIST(0x1001),
    ANNOTATION_SET_REF_LIST(0x1002),
    ANNOTATION_SET_ITEM(0x1003),
    CLASS_DATA_ITEM(0x2000),
    CODE_ITEM(0x2001),
    STRING_DATA_ITEM(0x2002),
    DEBUG_INFO_ITEM(0x2003),
    ANNOTATION_ITEM(0x2004),
    ENCODED_ARRAY_ITEM(0x2005),
    ANNOTATIONS_DIRECTORY_ITEM(0x2006),
    HIDDEN_API_CLASS_DATA(0xF000);
    private final int type;
    MapItemType(int type){
        this.type = type;
    }

    public int getType() {
        return type;
    }
    public static MapItemType get(int type){
        for(MapItemType itemType : values()){
            if(type == itemType.type){
                return itemType;
            }
        }
        return null;
    }
}
