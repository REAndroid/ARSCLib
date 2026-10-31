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
package com.reandroid.arsc.io;

import com.reandroid.arsc.header.InfoHeader;
import com.reandroid.utils.NumbersUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class BlockReader extends InputStream {

    private byte[] buffer;
    private byte[] mOneByte;
    private boolean mFinished;

    private final int mStart;
    private final int mLength;
    private int mPosition;

    private boolean mIsClosed;
    private int mMark;

    public BlockReader(byte[] buffer, int start, int length) {
        this.buffer = buffer;
        this.mStart = start;
        this.mLength = length;
        this.mPosition = 0;
    }
    public BlockReader(byte[] buffer) {
        this(buffer, 0, buffer.length);
    }
    public BlockReader(InputStream in) throws IOException {
        this(loadBuffer(in));
    }
    public BlockReader(InputStream in, int length) throws IOException {
        this(loadBuffer(in, length));
    }
    public BlockReader(File file) throws IOException {
        this(loadBuffer(file));
    }
    
    public int peekUShort() throws IOException {
        return peekShort() & 0xffff;
    }
    public short peekShort() throws IOException {
        int position = position();
        byte[] bytes = new byte[2];
        readFully(bytes);
        position(position);
        return toShort(bytes);
    }
    public int peekInteger() throws IOException {
        int position = position();
        byte[] bytes = new byte[4];
        readFully(bytes);
        position(position);
        return toInt(bytes);
    }
    public byte[] buffer() {
        return buffer;
    }
    public byte[] getBytes() {
        int length = length();
        byte[] buffer = buffer();
        if (buffer.length == length) {
            return buffer;
        }
        byte[] bytes = new byte[length];
        if (length == 0) {
            return bytes;
        }
        System.arraycopy(buffer, mStart, bytes, 0, length);
        return bytes;
    }
    public BlockReader create(int length) {
        return create(position(), length);
    }
    public BlockReader create(int start, int length) {
        int max = start + length;
        if (length < 0 || max > this.mLength) {
            length = this.mLength - start;
        }
        start = start + this.mStart;
        return new BlockReader(buffer(), start, length);
    }

    public int length() {
        return mLength;
    }
    public int start() {
        return mStart;
    }

    public byte[] readBytes(int length) throws IOException {
        byte[] bytes = new byte[length];
        readFully(bytes);
        return bytes;
    }
    public int readFully(byte[] bytes) throws IOException {
        int length = bytes.length;
        if (read(bytes, 0, length) != length) {
            throw new IOException("Finished reading: " + position());
        }
        return length;
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        return read(bytes, 0, bytes.length);
    }

    @Override
    public int read(byte[] bytes, int start, int length) throws IOException {
        if (length == 0) {
            return 0;
        }
        requireNotClosed();
        int available = requireNotFinished();
        if (available <= 0) {
            return -1;
        }
        int arrayPosition = mStart + mPosition;
        length = NumbersUtil.min(length, available);
        System.arraycopy(buffer(), arrayPosition, bytes, start, length);
        mPosition += length;
        return length;
    }
    private void requireNotClosed() throws IOException {
        if (mIsClosed) {
            throw new IOException("Stream is closed");
        }
    }
    private int requireNotFinished() throws IOException {
        int available = available();
        if (available > 0) {
            mFinished = false;
            return available;
        } else if (mFinished) {
            throw new IOException("Finished reading: " + position());
        } else {
            mFinished = true;
        }
        return available;
    }

    public int position() {
        return mPosition;
    }
    public void position(int position) {
        if (position < 0) {
            position = 0;
        }
        int length = length();
        if (position > length) {
            position = length;
        }
        mPosition = position;
    }
    public boolean isAvailable() {
        if (mIsClosed) {
            return false;
        }
        return available() > 0;
    }
    public void offset(int offset) {
        position(position() + offset);
    }
    @Override
    public int read() throws IOException {
        byte[] oneByte = this.mOneByte;
        if (oneByte == null) {
            oneByte = new byte[1];
            this.mOneByte = oneByte;
        }
        if (read(oneByte, 0, 1) == 1) {
            return oneByte[0] & 0xff;
        }
        return -1;
    }
    @Override
    public void mark(int position) {
        mMark = position;
    }
    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int available() {
        return mLength - mPosition;
    }
    @Override
    public void reset() throws IOException {
        requireNotClosed();
        position(mMark);
    }
    @Override
    public void close() {
        mIsClosed = true;
        mPosition = mLength;
        mMark = 0;
        buffer = null;
        mOneByte = null;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(": ");
        if (mIsClosed) {
            builder.append("Closed");
        } else {
            int available = available();
            int position = position();
            if (available == 0) {
                builder.append("Finished: ");
                builder.append(position);
            } else {
                if (mStart > 0) {
                    builder.append("START=");
                    builder.append(mStart);
                    builder.append(", ACTUAL=");
                    builder.append(mStart + position);
                    builder.append(", ");
                }
                builder.append("POS=");
                builder.append(position());
                builder.append(", available=");
                builder.append(available);
            }
        }
        return builder.toString();
    }


    private static byte[] loadBuffer(File file) throws IOException {
        if (!file.isFile()) {
            throw new FileNotFoundException("No such file: " + file);
        }
        FileInputStream in = new FileInputStream(file);
        byte[] result = loadBuffer(in, (int) file.length());
        in.close();
        return result;
    }
    private static byte[] loadBuffer(InputStream in) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int bufferLength = 40960;
        byte[] buffer = new byte[bufferLength];
        int read;
        while ((read = in.read(buffer, 0, bufferLength)) >= 0) {
            outputStream.write(buffer, 0, read);
        }
        if (in instanceof FileInputStream) {
            in.close();
        }
        outputStream.close();
        return outputStream.toByteArray();
    }
    private static byte[] loadBuffer(InputStream in, int length) throws IOException {
        byte[] buffer = new byte[length];
        if (length == 0) {
            return buffer;
        }
        int read = -1;
        int offset = 0;
        while (length > 0 && (read = in.read(buffer, offset, length)) >= 0) {
            length -= read;
            offset += read;
        }
        if (length > 0) {
            throw new IOException("Too few bytes to read");
        }
        return buffer;
    }
    private static int toInt(byte[] bytes) {
        return bytes[0] & 0xff |
                (bytes[1] & 0xff) << 8 |
                (bytes[2] & 0xff) << 16 |
                (bytes[3] & 0xff) << 24;
    }
    private static short toShort(byte[] bytes) {
        return (short) (bytes[0] & 0xff |
                (bytes[1] & 0xff) << 8);
    }

    // use InfoHeader.read(reader)
    @Deprecated
    public InfoHeader readHeaderBlock() throws IOException {
        return InfoHeader.read(this);
    }
    // use position(int position)
    @Deprecated
    public void seek(int position) {
        position(position);
    }
    @Deprecated
    public int getActualPosition() {
        return mStart + position();
    }
    // use position()
    @Deprecated
    public int getPosition() {
        return position();
    }
    @Deprecated
    public static InfoHeader readHeaderBlock(InputStream inputStream) throws IOException {
        return InfoHeader.readHeaderBlock(inputStream);
    }
    @Deprecated
    public static InfoHeader readHeaderBlock(byte[] bytes) throws IOException {
        return InfoHeader.readHeaderBlock(bytes);
    }
}
