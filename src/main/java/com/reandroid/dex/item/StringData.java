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

import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.base.OffsetSupplier;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.*;
import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.index.StringId;
import com.reandroid.dex.io.ByteReader;
import com.reandroid.dex.io.StreamUtil;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class StringData extends DexBlockItem
        implements SmaliFormat, BlockRefresh,
        OffsetSupplier, OffsetReceiver, StringKeyItemCreate,
        Comparable<StringData> {

    private String mCache;
    private StringId mStringId;
    private int stringUsage;

    public StringData() {
        super(0);
        this.mCache = "";
    }
    @SuppressWarnings("unchecked")
    public void removeSelf(){
        DexItemArray<StringData> itemArray = getParentInstance(DexItemArray.class);
        if(itemArray != null){
            itemArray.remove(this);
        }
        StringId stringId = this.mStringId;
        this.mStringId = null;
        if(stringId != null){
            stringId.setStringData(null);
            stringId.removeSelf();
        }
    }

    public String getStringUsageName() {
        if(containsUsage(USAGE_NONE)){
            return "NONE";
        }
        StringBuilder builder = new StringBuilder();
        if(containsUsage(USAGE_INSTRUCTION)){
            builder.append("INSTRUCTION");
        }
        if(containsUsage(USAGE_INITIAL)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("INITIAL");
        }
        if(containsUsage(USAGE_ANNOTATION)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("ANNOTATION");
        }
        if(containsUsage(USAGE_TYPE)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("TYPE");
        }
        if(containsUsage(USAGE_FIELD)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("FIELD");
        }
        if(containsUsage(USAGE_METHOD)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("METHOD");
        }
        if(containsUsage(USAGE_SHORTY)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("SHORTY");
        }
        if(containsUsage(USAGE_SOURCE)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("SOURCE");
        }
        if(containsUsage(USAGE_DEBUG)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("DEBUG");
        }
        return builder.toString();
    }
    public boolean containsUsage(int usage){
        if(usage == 0){
            return this.stringUsage == 0;
        }
        return (this.stringUsage & usage) == usage;
    }
    public int getStringUsage() {
        return stringUsage;
    }
    public void addStringUsage(int usage){
        this.stringUsage |= usage;
    }

    @Override
    public String getKey(){
        return getString();
    }
    @Override
    public void setKey(String key){
        setString(key);
    }
    public String getString(){
        return mCache;
    }
    public void setString(String value){
        if(value.equals(mCache)){
            return;
        }
        mCache = value;
        encodeString(value);
        getStringId();
    }

    @Override
    public IntegerReference getOffsetReference() {
        return getStringId();
    }
    @Override
    public void setOffsetReference(IntegerReference reference) {
        this.mStringId = (StringId) reference;
        this.mStringId.setStringData(this);
    }

    public StringId getStringId() {
        StringId stringId = this.mStringId;
        if(stringId == null){
            Section<StringId> section = getSection(SectionType.STRING_ID);
            DexItemArray<StringId> itemArray = section.getItemArray();
            int index = getIndex();
            itemArray.ensureSize(index + 1);
            stringId = itemArray.get(index);
            this.mStringId = stringId;
            stringId.setStringData(this);
        }
        return stringId;
    }
    @Override
    protected void onBytesChanged() {
        mCache = decodeString();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        if(reader.available() < 4){
            return;
        }
        int position = reader.getPosition();
        String text = decodeString(StreamUtil.createByteReader(reader));
        int length = reader.getPosition() - position;
        reader.seek(position);
        setBytesLength(length + 1, false);
        byte[] bytes = getBytesInternal();
        reader.readFully(bytes);
        mCache = text;
    }
    @Override
    public void refresh() {
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        DexUtils.appendQuotedString(writer, getString());
    }
    @Override
    public int compareTo(StringData stringData) {
        if(stringData == null){
            return -1;
        }
        return getString().compareTo(stringData.getString());
    }
    @Override
    public String toString(){
        String text = getString();
        if(text != null){
            return text;
        }
        return "NULL";
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
    private void encodeString(String text){
        int length = text.length();
        setBytesLength(length * 3 + 4, false);
        final byte[] buffer = getBytesInternal();
        int position = writeUleb128(buffer, 0, length);
        for (int i = 0; i < length; i++) {
            char ch = text.charAt(i);
            if ((ch != 0) && (ch < 0x80)) {
                buffer[position++] = (byte)ch;
            } else if (ch < 0x800) {
                buffer[position++] = (byte)(((ch >> 6) & 0x1f) | 0xc0);
                buffer[position++] = (byte)((ch & 0x3f) | 0x80);
            } else {
                buffer[position++] = (byte)(((ch >> 12) & 0x0f) | 0xe0);
                buffer[position++] = (byte)(((ch >> 6) & 0x3f) | 0x80);
                buffer[position++] = (byte)((ch & 0x3f) | 0x80);
            }
        }
        buffer[position++] = 0;
        setBytesLength(position, false);
    }
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

    public static final int USAGE_NONE = 0x0000;
    public static final int USAGE_INSTRUCTION = 1;
    public static final int USAGE_INITIAL = 1 << 1;
    public static final int USAGE_ANNOTATION = 1 << 2;
    public static final int USAGE_TYPE = 1 << 4;
    public static final int USAGE_FIELD = 1 << 5;
    public static final int USAGE_METHOD = 1 << 6;
    public static final int USAGE_SHORTY = 4 << 7;
    public static final int USAGE_SOURCE = 1 << 8;
    public static final int USAGE_DEBUG = 1 << 9;
}
