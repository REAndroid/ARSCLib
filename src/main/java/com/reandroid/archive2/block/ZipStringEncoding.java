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
package com.reandroid.archive2.block;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;

import static java.nio.charset.StandardCharsets.UTF_8;


public class ZipStringEncoding {

    private static final char REPLACEMENT = '?';
    private static final byte[] REPLACEMENT_BYTES = { (byte) REPLACEMENT };
    private static final String REPLACEMENT_STRING = String.valueOf(REPLACEMENT);
    private static final char[] HEX_CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
    private static ByteBuffer encodeFully(final CharsetEncoder enc, final CharBuffer cb, final ByteBuffer out) {
        ByteBuffer o = out;
        while (cb.hasRemaining()) {
            final CoderResult result = enc.encode(cb, o, false);
            if (result.isOverflow()) {
                final int increment = estimateIncrementalEncodingSize(enc, cb.remaining());
                o = growBufferBy(o, increment);
            }
        }
        return o;
    }
    private static CharBuffer encodeSurrogate(final CharBuffer cb, final char c) {
        cb.position(0).limit(6);
        cb.put('%');
        cb.put('U');

        cb.put(HEX_CHARS[(c >> 12) & 0x0f]);
        cb.put(HEX_CHARS[(c >> 8) & 0x0f]);
        cb.put(HEX_CHARS[(c >> 4) & 0x0f]);
        cb.put(HEX_CHARS[c & 0x0f]);
        cb.flip();
        return cb;
    }

    private static int estimateIncrementalEncodingSize(final CharsetEncoder enc, final int charCount) {
        return (int) Math.ceil(charCount * enc.averageBytesPerChar());
    }
    private static int estimateInitialBufferSize(final CharsetEncoder enc, final int charChount) {
        final float first = enc.maxBytesPerChar();
        final float rest = (charChount - 1) * enc.averageBytesPerChar();
        return (int) Math.ceil(first + rest);
    }

    private final Charset charset;
    private final boolean useReplacement;
    private final CharsetEncoder mEncoder;
    private final CharsetDecoder mDecoder;

    ZipStringEncoding(final Charset charset, final boolean useReplacement) {
        this.charset = charset;
        this.useReplacement = useReplacement;
        mEncoder = newEncoder();
        mDecoder = newDecoder();
    }

    public boolean canEncode(final String name) {
        final CharsetEncoder enc = newEncoder();
        return enc.canEncode(name);
    }
    public String decode(byte[] data, int offset, int length) throws IOException {
        return mDecoder.decode(ByteBuffer.wrap(data, offset, length)).toString();
    }
    public byte[] encode(final String text) {
        final CharsetEncoder enc = mEncoder;

        final CharBuffer cb = CharBuffer.wrap(text);
        CharBuffer tmp = null;
        ByteBuffer out = ByteBuffer.allocate(estimateInitialBufferSize(enc, cb.remaining()));

        while (cb.hasRemaining()) {
            final CoderResult res = enc.encode(cb, out, false);

            if (res.isUnmappable() || res.isMalformed()) {
                final int spaceForSurrogate = estimateIncrementalEncodingSize(enc, 6 * res.length());
                if (spaceForSurrogate > out.remaining()) {
                    int charCount = 0;
                    for (int i = cb.position() ; i < cb.limit(); i++) {
                        charCount += !enc.canEncode(cb.get(i)) ? 6 : 1;
                    }
                    final int totalExtraSpace = estimateIncrementalEncodingSize(enc, charCount);
                    out = growBufferBy(out, totalExtraSpace - out.remaining());
                }
                if (tmp == null) {
                    tmp = CharBuffer.allocate(6);
                }
                for (int i = 0; i < res.length(); ++i) {
                    out = encodeFully(enc, encodeSurrogate(tmp, cb.get()), out);
                }

            } else if (res.isOverflow()) {
                final int increment = estimateIncrementalEncodingSize(enc, cb.remaining());
                out = growBufferBy(out, increment);

            } else if (res.isUnderflow() || res.isError()) {
                break;
            }
        }
        // tell the encoder we are done
        enc.encode(cb, out, true);
        // may have caused underflow, but that's been ignored traditionally

        out.limit(out.position());
        out.rewind();
        return out.array();
    }

    public Charset getCharset() {
        return charset;
    }

    private CharsetDecoder newDecoder() {
        if (!useReplacement) {
            return this.charset.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
        }
        return  charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE)
                .replaceWith(REPLACEMENT_STRING);
    }

    private CharsetEncoder newEncoder() {
        if (useReplacement) {
            return charset.newEncoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE)
                    .replaceWith(REPLACEMENT_BYTES);
        }
        return charset.newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
    }

    private static final String UTF8 = UTF_8.name();


    private static ZipStringEncoding getZipEncoding(final String name) {
        Charset cs = Charset.defaultCharset();
        if (name != null) {
            try {
                cs = Charset.forName(name);
            } catch (final UnsupportedCharsetException e) {
            }
        }
        final boolean useReplacement = isUTF8(cs.name());
        return new ZipStringEncoding(cs, useReplacement);
    }

    static ByteBuffer growBufferBy(final ByteBuffer buffer, final int increment) {
        buffer.limit(buffer.position());
        buffer.rewind();

        final ByteBuffer on = ByteBuffer.allocate(buffer.capacity() + increment);

        on.put(buffer);
        return on;
    }

    private static boolean isUTF8(final String charsetName) {
        final String actual = charsetName != null ? charsetName : Charset.defaultCharset().name();
        if (UTF_8.name().equalsIgnoreCase(actual)) {
            return true;
        }
        return UTF_8.aliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(actual));
    }

    public static String decode(boolean isUtf8, byte[] bytes, int offset, int length){
        if(isUtf8){
            return decodeUtf8(bytes, offset, length);
        }
        return decodeDefault(bytes, offset, length);
    }
    private static String decodeUtf8(byte[] bytes, int offset, int length){
        try {
            return UTF8_ENCODING.decode(bytes, offset, length);
        } catch (IOException exception) {
            return new String(bytes, offset, length);
        }
    }
    private static String decodeDefault(byte[] bytes, int offset, int length){
        return new String(bytes, offset, length);
    }
    public static byte[] encodeString(boolean isUtf8, String text){
        if(text==null || text.length()==0){
            return new byte[0];
        }
        if(isUtf8){
            return UTF8_ENCODING.encode(text);
        }
        return DEFAULT_ENCODING.encode(text);
    }

    private static final ZipStringEncoding UTF8_ENCODING = getZipEncoding(UTF8);
    private static final ZipStringEncoding DEFAULT_ENCODING = getZipEncoding(Charset.defaultCharset().name());

}

