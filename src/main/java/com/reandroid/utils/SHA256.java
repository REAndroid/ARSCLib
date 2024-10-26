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
package com.reandroid.utils;

public class SHA256 extends ByteDigest {

    private final int digestLength;
    private final int blockSize;

    private final int[] WORD;
    private final int[] state;
    private final byte[] buffer;
    private int bufferOffset;
    private long bytesProcessed;

    public SHA256() {
        super();
        this.digestLength = 32;
        this.blockSize = 64;

        this.state = new int[8];
        this.WORD = new int[64];
        this.buffer = new byte[blockSize];
        this.reset();
    }

    @Override
    public void update(byte[] bytes, int offset, int length) {
        if (length != 0) {
            if (this.bytesProcessed < 0L) {
                this.restart();
            }

            this.bytesProcessed += length;
            int limit;
            if (this.bufferOffset != 0) {
                limit = Math.min(length, this.blockSize - this.bufferOffset);
                System.arraycopy(bytes, offset, this.buffer, this.bufferOffset, limit);
                this.bufferOffset += limit;
                offset += limit;
                length -= limit;
                if (this.bufferOffset >= this.blockSize) {
                    this.compress(this.buffer, 0);
                    this.bufferOffset = 0;
                }
            }

            if (length >= this.blockSize) {
                limit = offset + length;
                offset = this.compressMultiBlock(bytes, offset, limit - this.blockSize);
                length = limit - offset;
            }

            if (length > 0) {
                System.arraycopy(bytes, offset, this.buffer, 0, length);
                this.bufferOffset = length;
            }
        }
    }
    @Override
    public void digest(byte[] out, int outOffset)  {
        if (this.bytesProcessed < 0L) {
            this.restart();
        }

        long bitsProcessed = this.bytesProcessed << 3;
        int index = (int)this.bytesProcessed & 63;
        int padLen = index < 56 ? 56 - index : 120 - index;
        this.update(PADDING, 0, padLen);
        putBigEndianInteger(this.buffer, 56, (int)(bitsProcessed >>> 32));
        putBigEndianInteger(this.buffer, 60, (int)bitsProcessed);
        this.compress(this.buffer, 0);
        i2bBig(this.state, out, outOffset, this.getDigestLength());

        this.bytesProcessed = -1L;
    }
    @Override
    public void reset() {
        int[] state = this.state;
        state[0] = 0x6a09e667;
        state[1] = 0xbb67ae85;
        state[2] = 0x3c6ef372;
        state[3] = 0xa54ff53a;
        state[4] = 0x510e527f;
        state[5] = 0x9b05688c;
        state[6] = 0x1f83d9ab;
        state[7] = 0x5be0cd19;
        fillZero(this.WORD);
    }
    @Override
    public int getDigestLength() {
        return this.digestLength;
    }

    private void restart() {
        if (this.bytesProcessed != 0L) {
            this.reset();
            this.bufferOffset = 0;
            this.bytesProcessed = 0L;
            fillZero(this.buffer);
        }
    }

    private int compressMultiBlock(byte[] b, int ofs, int limit) {
        while(ofs <= limit) {
            this.compress(b, ofs);
            ofs += this.blockSize;
        }
        return ofs;
    }

    private void compress(byte[] buf, int ofs) {
        this.compressCheck(buf, ofs);
        this.compressWord();
    }

    private void compressCheck(byte[] buf, int ofs) {
        b2iBig64(buf, ofs, this.WORD);
    }

    private void compressWord() {
        int[] word = this.WORD;
        int[] state = this.state;
        int a;
        int b;
        int c;
        int d;
        int e;
        for(a = 16; a < 64; ++a) {
            b = word[a - 2];
            c = word[a - 15];
            d = (c >>> 7 | c << 25) ^ (c >>> 18 | c << 14) ^ c >>> 3;
            e = (b >>> 17 | b << 15) ^ (b >>> 19 | b << 13) ^ b >>> 10;
            word[a] = d + e + word[a - 7] + word[a - 16];
        }

        a = state[0];
        b = state[1];
        c = state[2];
        d = state[3];
        e = state[4];
        int f = state[5];
        int g = state[6];
        int h = state[7];

        for(int i = 0; i < 64; i ++) {
            int T1 = h + sigmaCh(e, f, g) + ROUND_CONSTS[i] + word[i];
            int T2 = T2(a, b, c);
            h = g;
            g = f;
            f = e;
            e = d + T1;
            d = c;
            c = b;
            b = a;
            a = T1 + T2;
        }
        state[0] += a;
        state[1] += b;
        state[2] += c;
        state[3] += d;
        state[4] += e;
        state[5] += f;
        state[6] += g;
        state[7] += h;
    }

    private int sigmaCh(int e, int f, int g) {
        int sigma = (e >>> 6 | e << 26) ^ (e >>> 11 | e << 21) ^ (e >>> 25 | e << 7);
        int ch = e & f ^ ~e & g;
        return sigma + ch;
    }
    private int T2(int a, int b, int c) {
        int sigma = (a >>> 2 | a << 30) ^ (a >>> 13 | a << 19) ^ (a >>> 22 | a << 10);
        int maj = a & b ^ a & c ^ b & c;
        return sigma + maj;
    }

    private static void b2iBig64(byte[] in, int inOfs, int[] out) {
        if ((inOfs & 3) == 0) {
            for (int i = 0; i < 16; i++) {
                out[i] = getBigEndianInteger(in, inOfs + i * 4);
            }
        } else {
            int outOfs = 0;
            int len = 64;
            for(len += inOfs; inOfs < len; inOfs += 4) {
                out[outOfs++] = getBigEndianInteger(in, inOfs);
            }
        }
    }

    private static void i2bBig(int[] in, byte[] out, int outOffset, int len) {
        int inOfs = 0;
        int i;
        if ((outOffset & 3) == 0) {
            for(len += outOffset; outOffset < len; outOffset += 4) {
                putBigEndianInteger(out, outOffset, in[inOfs++]);
            }
        } else {
            for(len += outOffset; outOffset < len; out[outOffset++] = (byte)i) {
                i = in[inOfs++];
                out[outOffset++] = (byte)(i >> 24);
                out[outOffset++] = (byte)(i >> 16);
                out[outOffset++] = (byte)(i >> 8);
            }
        }
    }

    private static final int[] ROUND_CONSTS;
    private static final byte[] PADDING;

    static {
        ROUND_CONSTS = new int[]{
                0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
                0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
                0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
                0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
                0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
                0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
                0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
                0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
        };
        byte[] pad = new byte[136];
        PADDING = pad;
        pad[0] = -128;
    }
}
