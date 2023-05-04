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
package com.reandroid.arsc.util;

public class HexUtil {
    public static String toHex2(byte num){
        return toHex((long)(num & 0x00000000000000ffL), 2);
    }
    public static String toHex4(short num){
        return toHex((long)(num & 0x000000000000ffffL), 4);
    }
    public static String toHex8(int num){
        return toHex(num, 8);
    }
    public static String toHex8(long num){
        return toHex(num, 8);
    }
    public static String toHex(int num, int minLength){
        return toHex((0x00000000ffffffffL & num), minLength);
    }
    public static String toHex(long num, int minLength){
        String hex = Long.toHexString(num);
        StringBuilder builder = new StringBuilder();
        builder.append('0');
        builder.append('x');
        int rem = minLength - hex.length();
        for(int i=0; i < rem; i++){
            builder.append('0');
        }
        builder.append(hex);
        return builder.toString();
    }
    public static String toHexNoPrefix8(int num){
        return toHex(null, (0x00000000ffffffffL & num), 8);
    }
    public static String toHexNoPrefix(int num, int minLength){
        return toHex(null, (0x00000000ffffffffL & num), minLength);
    }
    public static String toHex8(String prefix, int num){
        return toHex(prefix, (0x00000000ffffffffL & num), 8);
    }
    public static String toHex(String prefix, int num, int minLength){
        return toHex(prefix, (0x00000000ffffffffL & num), minLength);
    }
    public static String toHex(String prefix, long num, int minLength){
        String hex = Long.toHexString(num);
        StringBuilder builder = new StringBuilder();
        if(prefix != null){
            builder.append(prefix);
        }
        int rem = minLength - hex.length();
        for(int i=0; i < rem; i++){
            builder.append('0');
        }
        builder.append(hex);
        return builder.toString();
    }
    public static int parseHex(String hexString){
        hexString = trim0x(hexString);
        return (int) Long.parseLong(hexString, 16);
    }
    private static String trim0x(String hexString){
        if(hexString == null || hexString.length() < 3){
            return hexString;
        }
        if(hexString.charAt(0) == '0' && hexString.charAt(1) == 'x'){
            hexString = hexString.substring(2);
        }
        return hexString;
    }
}
