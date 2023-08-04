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
package com.reandroid.dex.item;

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.DexItem;
import com.reandroid.dex.io.ByteReader;
import com.reandroid.dex.io.StreamUtil;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class StringIndex extends DexItem {
    private String mCache;

    public StringIndex() {
        super(0);
    }

    public String getString(){
        return mCache;
    }

    @Override
    public String toString(){
        String text = getString();
        if(text != null){
            return text;
        }
        return "NULL";
    }

    @Override
    protected void onBytesChanged() {
        mCache = decodeString();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        if(reader.available()<4){
            return;
        }
        int position = reader.getPosition();
        String text = decodeString(StreamUtil.createByteReader(reader));
        int length = reader.getPosition() - position;
        reader.seek(position);
        setBytesLength(length, false);
        byte[] bytes = getBytesInternal();
        reader.readFully(bytes);
        mCache = text;
    }

    private String decodeString(){
        String text;
        try {
            text = decodeString(StreamUtil.createByteReader(getBytesInternal()));
        } catch (IOException exception) {
            text = null;
        }
        return text;
    }

    // copied from JesusFreke/smali
    private static String decodeString(ByteReader reader) throws IOException {
        int utf16Length = readUleb128(reader);
        char[] chars = new char[utf16Length];
        int outAt = 0;

        int at = 0;
        for (at = 0; utf16Length > 0; utf16Length--) {
            int v0 = reader.read();
            char out;
            switch (v0 >> 4) {
                case 0x00: case 0x01: case 0x02: case 0x03:
                case 0x04: case 0x05: case 0x06: case 0x07: {
                    // 0XXXXXXX -- single-byte encoding
                    if (v0 == 0) {
                        // A single zero byte is illegal.
                        return throwBadUtf8(v0, at);
                    }
                    out = (char) v0;
                    at++;
                    break;
                }
                case 0x0c: case 0x0d: {
                    // 110XXXXX -- two-byte encoding
                    int v1 = reader.read() & 0xFF;
                    if ((v1 & 0xc0) != 0x80) {
                        return throwBadUtf8(v1, at + 1);
                    }
                    int value = ((v0 & 0x1f) << 6) | (v1 & 0x3f);
                    if ((value != 0) && (value < 0x80)) {
                        /*
                         * This should have been represented with
                         * one-byte encoding.
                         */
                        return throwBadUtf8(v1, at + 1);
                    }
                    out = (char) value;
                    at += 2;
                    break;
                }
                case 0x0e: {
                    // 1110XXXX -- three-byte encoding
                    int v1 = reader.read();
                    if ((v1 & 0xc0) != 0x80) {
                        return throwBadUtf8(v1, at + 1);
                    }
                    int v2 = reader.read();
                    if ((v2 & 0xc0) != 0x80) {
                        return throwBadUtf8(v2, at + 2);
                    }
                    int value = ((v0 & 0x0f) << 12) | ((v1 & 0x3f) << 6) |
                            (v2 & 0x3f);
                    if (value < 0x800) {
                        /*
                         * This should have been represented with one- or
                         * two-byte encoding.
                         */
                        return throwBadUtf8(v2, at + 2);
                    }
                    out = (char) value;
                    at += 3;
                    break;
                }
                default: {
                    // 10XXXXXX, 1111XXXX -- illegal
                    return throwBadUtf8(v0, at);
                }
            }
            chars[outAt] = out;
            outAt++;
        }
        return new String(chars, 0, outAt);
    }
    private static String throwBadUtf8(int value, int offset) throws IOException {
        throw new IOException("bad utf-8 byte " + HexUtil.toHex2("", (byte)value)
                + " at offset " + offset);
    }
}
