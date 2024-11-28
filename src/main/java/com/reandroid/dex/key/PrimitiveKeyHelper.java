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
package com.reandroid.dex.key;

import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.utils.HexUtil;

class PrimitiveKeyHelper {

    public static PrimitiveKey parse(String text) {
        if (text == null || text.length() == 0) {
            return null;
        }
        char first = text.charAt(0);
        if (isNumbersPrefix(first)) {
            return parseNumbers(text);
        }
        if (first == '\'') {
            return parseChar(text);
        }
        if (first == 't' || first == 'f') {
            return parseBoolean(text);
        }
        return null;
    }
    public static PrimitiveKey readSafe(SmaliReader reader) {
        if (reader.finished()) {
            return null;
        }
        int position = reader.position();
        char first = reader.getASCII(position);
        if (isNumbersPrefix(first)) {
            return readNumbers(reader);
        }
        if (first == '\'') {
            return readChar(reader);
        }
        if (first == 't' || first == 'f') {
            return readBoolean(reader);
        }
        reader.position(position);
        return null;
    }
    private static PrimitiveKey readChar(SmaliReader reader) {
        int position = reader.position();
        if (reader.read() != '\'') {
            reader.position(position);
            return null;
        }
        char ch = reader.readASCII();
        if(ch == '\\'){
            ch = reader.readASCII();
            if (ch == 'u') {
                try{
                    int i = HexUtil.parseHex(reader.readString(4));
                    ch = (char) i;
                } catch (NumberFormatException ex) {
                    reader.position(position);
                    return null;
                }
            } else {
                if (ch == 'b') {
                    ch = '\b';
                } else if (ch == 'f') {
                    ch = '\f';
                } else if (ch == 'n') {
                    ch = '\n';
                } else if (ch == 'r') {
                    ch = '\r';
                } else if (ch == 't') {
                    ch = '\t';
                } else {
                    reader.position(position);
                    return null;
                }
            }
        }
        if (reader.read() != '\'') {
            reader.position(position);
            return null;
        }
        return PrimitiveKey.of(ch);
    }
    private static PrimitiveKey parseChar(String text) {
        if (text.length() < 3) {
            return null;
        }
        if (text.charAt(0) != '\'') {
            return null;
        }
        int last = text.length() - 1;
        if (text.charAt(last) != '\'') {
            return null;
        }
        text = text.substring(1, last);
        char c = text.charAt(0);
        if (text.length() == 1) {
            return PrimitiveKey.of(c);
        }
        if (c != '\\') {
            return null;
        }
        c = text.charAt(1);

        if (c == 'u') {
            if (text.length() == 6) {
                try {
                    return PrimitiveKey.of((char) HexUtil.parseHex(text.substring(2)));
                } catch (NumberFormatException ignored) {
                }
            }
        } else if (text.length() == 2){
            if (c == 'b') {
                c = '\b';
            } else if (c == 'f') {
                c = '\f';
            } else if (c == 'n') {
                c = '\n';
            } else if (c == 'r') {
                c = '\r';
            } else if (c == 't') {
                c = '\t';
            } else {
                return null;
            }
            return PrimitiveKey.of(c);
        }
        return null;
    }
    private static PrimitiveKey readBoolean(SmaliReader reader) {
        int position = reader.position();
        char first = reader.readASCII();
        if (first == 't') {
            if (reader.read() == 'r' &&
                    reader.read() == 'u' &&
                    reader.read() == 'e') {
                return PrimitiveKey.of(true);
            }
        } else if (first == 'f') {
            if (reader.read() == 'a' &&
                    reader.read() == 'l' &&
                    reader.read() == 's' &&
                    reader.read() == 'e') {
                return PrimitiveKey.of(false);
            }
        }
        reader.position(position);
        return null;
    }
    private static PrimitiveKey parseBoolean(String text) {
        if (text.equals("true")) {
            return PrimitiveKey.of(true);
        }
        if (text.equals("false")) {
            return PrimitiveKey.of(false);
        }
        return null;
    }
    private static PrimitiveKey readNumbers(SmaliReader reader) {
        int position = reader.position();
        char first = reader.readASCII();
        if (first == '-' || first == '+') {
            first = reader.readASCII();
        }
        char x = 0;
        if (!reader.finished()) {
            x = reader.readASCII();
        }
        reader.position(position);
        String number = reader.readStringForNumber();
        if (first == '0' && x == 'x') {
            try {
                return readHex(number);
            } catch (NumberFormatException ignored) {
                reader.position(position);
                return null;
            }
        }
        if (number.charAt(0) == '+') {
            number = number.substring(1);
        }
        if (isFloatOrDouble(number)) {
            try {
                return parseFloatOrDouble(number);
            } catch (NumberFormatException ignored) {
            }
        } else {
            try {
                return parsePlainNumber(number);
            } catch (NumberFormatException ignored) {
            }
        }
        reader.position(position);
        return null;
    }

    private static PrimitiveKey parseNumbers(String text) {
        int position = 0;
        char first = text.charAt(position);
        int length = text.length();
        if (length > 1 && (first == '-' || first == '+')) {
            first = text.charAt(1);
            position ++;
        }
        char x = 0;
        if (length > position + 1) {
            x = text.charAt(position + 1);
        }
        String number = text;
        if (first == '0' && x == 'x') {
            try {
                return readHex(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        if (number.charAt(0) == '+') {
            number = number.substring(1);
        }
        if (isFloatOrDouble(number)) {
            try {
                return parseFloatOrDouble(number);
            } catch (NumberFormatException ignored) {
            }
        } else {
            try {
                return parsePlainNumber(number);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private static PrimitiveKey parsePlainNumber(String number) {
        char suffix = number.charAt(number.length() - 1);
        if (suffix == 'L' || suffix == 'l') {
            number = number.substring(0, number.length() - 1);
            return PrimitiveKey.of(Long.parseLong(number));
        }
        return PrimitiveKey.of(Integer.parseInt(number));
    }
    private static PrimitiveKey parseFloatOrDouble(String number) {
        int lastIndex = number.length() - 1;
        if (number.charAt(lastIndex) == 'f') {
            number = number.substring(0, lastIndex);
            return PrimitiveKey.of(Float.parseFloat(number));
        }
        return PrimitiveKey.of(Double.parseDouble(number));
    }
    private static boolean isFloatOrDouble(String number) {
        if (number.indexOf('.') >= 0) {
            return true;
        }
        return number.contains("NaN") ||
                number.contains("Infinity");
    }
    private static PrimitiveKey readHex(String hex) {
        char suffix = hex.charAt(hex.length() - 1);
        if (suffix == 'L') {
            return PrimitiveKey.of(HexUtil.parseHexLong(hex));
        }
        if (suffix == 'S' || suffix == 's') {
            return PrimitiveKey.of(HexUtil.parseHexShort(hex));
        }
        if (suffix == 't') {
            return PrimitiveKey.of(HexUtil.parseHexByte(hex));
        }
        return PrimitiveKey.of(HexUtil.parseHexInteger(hex));
    }
    private static boolean isNumbersPrefix(char c) {
        if (c >= '0' && c <= '9') {
            return true;
        }
        return c == '-' ||
                c == '+' ||
                c == 'N' ||
                c == 'I';
    }
}
