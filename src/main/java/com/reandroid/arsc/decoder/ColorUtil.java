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
package com.reandroid.arsc.decoder;

import com.reandroid.arsc.util.HexUtil;
import com.reandroid.arsc.value.ValueType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    public static String decode(ValueType valueType, int data){
        if(valueType == null){
            return null;
        }
        int index;
        switch (valueType){
            case INT_COLOR_RGB4:
                index = 5;
                break;
            case INT_COLOR_ARGB4:
                index = 4;
                break;
            case INT_COLOR_RGB8:
                index = 2;
                break;
            case INT_COLOR_ARGB8:
                index = 0;
                break;
            default:
                return null;
        }
        String hex = HexUtil.toHexNoPrefix8(data);
        return "#" + hex.substring(index);
    }
    public static ValueDecoder.EncodeResult encode(String hexColor){
        if(hexColor == null){
            return null;
        }
        hexColor = hexColor.toUpperCase();
        Matcher matcher = PATTERN_COLOR.matcher(hexColor);
        if(!matcher.matches()){
            return null;
        }
        char[] s = hexColor.toCharArray();
        int len = s.length;
        ValueType valueType;
        int color = 0;

        if (len == 4) {
            valueType = ValueType.INT_COLOR_RGB4;
            color |= 0xFF0000;
            color |= get_hex(s[1]) << 20;
            color |= get_hex(s[1]) << 16;
            color |= get_hex(s[2]) << 12;
            color |= get_hex(s[2]) << 8;
            color |= get_hex(s[3]) << 4;
            color |= get_hex(s[3]);
        } else if (len == 5) {
            valueType = ValueType.INT_COLOR_ARGB4;
            color |= 0xFFFF0000;
            color |= get_hex(s[1]) << 28;
            color |= get_hex(s[1]) << 24;
            color |= get_hex(s[2]) << 20;
            color |= get_hex(s[2]) << 16;
            color |= get_hex(s[3]) << 12;
            color |= get_hex(s[3]) << 8;
            color |= get_hex(s[4]) << 4;
            color |= get_hex(s[4]);
        } else if (len == 7) {
            valueType = ValueType.INT_COLOR_RGB8;
            color |= 0xFF000000;
            color |= get_hex(s[1]) << 20;
            color |= get_hex(s[2]) << 16;
            color |= get_hex(s[3]) << 12;
            color |= get_hex(s[4]) << 8;
            color |= get_hex(s[5]) << 4;
            color |= get_hex(s[6]);
        } else if (len == 9) {
            valueType = ValueType.INT_COLOR_ARGB8;
            color |= get_hex(s[1]) << 28;
            color |= get_hex(s[2]) << 24;
            color |= get_hex(s[3]) << 20;
            color |= get_hex(s[4]) << 16;
            color |= get_hex(s[5]) << 12;
            color |= get_hex(s[6]) << 8;
            color |= get_hex(s[7]) << 4;
            color |= get_hex(s[8]);
        }else {
            return null;
        }
        return new ValueDecoder.EncodeResult(valueType, color);
    }
    private static int get_hex(char ch){
        if(ch <= '9'){
            return ch - '0';
        }
        return 10 + (ch - 'A');
    }
    public static final Pattern PATTERN_COLOR = Pattern.compile("^#([0-9a-fA-F]{3,8})$");
}
