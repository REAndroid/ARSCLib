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

import com.reandroid.arsc.value.ValueType;

public class ColorUtil {

    public static String decode(ValueType valueType, int data){
        if(valueType == null){
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append('#');
        switch (valueType){
            case INT_COLOR_RGB4:
                builder.append(byteToHex(data >> 16));
                builder.append(byteToHex(data >> 8));
                builder.append(byteToHex(data));
                break;
            case INT_COLOR_ARGB4:
                builder.append(byteToHex(data >> 24));
                builder.append(byteToHex(data >> 16));
                builder.append(byteToHex(data >> 8));
                builder.append(byteToHex(data));
                break;
            case INT_COLOR_RGB8:
                builder.append(byteToHex(data >> 20));
                builder.append(byteToHex(data >> 16));
                builder.append(byteToHex(data >> 12));
                builder.append(byteToHex(data >> 8));
                builder.append(byteToHex(data >> 4));
                builder.append(byteToHex(data));
                break;
            case INT_COLOR_ARGB8:
                builder.append(byteToHex(data >> 28));
                builder.append(byteToHex(data >> 24));
                builder.append(byteToHex(data >> 20));
                builder.append(byteToHex(data >> 16));
                builder.append(byteToHex(data >> 12));
                builder.append(byteToHex(data >> 8));
                builder.append(byteToHex(data >> 4));
                builder.append(byteToHex(data));
                break;
            default:
                return null;
        }
        return builder.toString();
    }
    public static ValueDecoder.EncodeResult encode(String hexColor){
        int[] values = hexToIntegers(hexColor);
        if(values == null){
            return null;
        }
        ValueType valueType;
        int color = 0;

        int len = values.length;
        if (len == 4) {
            valueType = ValueType.INT_COLOR_RGB4;
            color |= 0xFF000000;
            color |= (values[1] << 20);
            color |= (values[1] << 16);
            color |= (values[2] << 12);
            color |= (values[2] << 8);
            color |= (values[3] << 4);
            color |= values[3];
        } else if (len == 5) {
            valueType = ValueType.INT_COLOR_ARGB4;
            color |= values[1] << 28;
            color |= values[1] << 24;
            color |= values[2] << 20;
            color |= values[2] << 16;
            color |= values[3] << 12;
            color |= values[3] << 8;
            color |= values[4] << 4;
            color |= values[4];
        } else if (len == 7) {
            valueType = ValueType.INT_COLOR_RGB8;
            color |= 0xFF000000;
            color |= values[1] << 20;
            color |= values[2] << 16;
            color |= values[3] << 12;
            color |= values[4] << 8;
            color |= values[5] << 4;
            color |= values[6];
        } else if (len == 9) {
            valueType = ValueType.INT_COLOR_ARGB8;
            color |= values[1] << 28;
            color |= values[2] << 24;
            color |= values[3] << 20;
            color |= values[4] << 16;
            color |= values[5] << 12;
            color |= values[6] << 8;
            color |= values[7] << 4;
            color |= values[8];
        }else {
            return null;
        }
        return new ValueDecoder.EncodeResult(valueType, color);
    }
    private static char byteToHex(int i){
        i = i & 0xf;
        if(i < 0xa){
            return (char) ('0' + i);
        }
        i = i - 0xa;
        return (char) ('a' + i);
    }
    private static int[] hexToIntegers(String hexColor){
        if(hexColor == null){
            return null;
        }
        int length = hexColor.length();
        if(length < 4 || length > 9){
            return null;
        }
        hexColor = hexColor.toUpperCase();
        char[] chars = hexColor.toCharArray();
        if(chars[0] != '#'){
            return null;
        }
        length = chars.length;
        int[] result = new int[length];
        for(int i = 1; i < length; i++){
            int ch = chars[i];
            int value;
            if(ch >= '0' && ch <= '9'){
                value = ch - '0';
            }else if(ch >= 'A' && ch <= 'F'){
                value = 10 + (ch - 'A');
            }else {
                return null;
            }
            result[i] = value;
        }
        return result;
    }
}
