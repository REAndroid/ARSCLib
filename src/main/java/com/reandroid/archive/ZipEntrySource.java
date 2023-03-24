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
package com.reandroid.archive;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;

public class ZipEntrySource extends InputSource {
    private final ZipEntry zipEntry;
    private final FileChannel channel;
    private final long headerOffset;
    private long dataOffset;

    public ZipEntrySource(ZipEntry zipEntry, long headerOffset, FileChannel channel) {
        super(zipEntry.getName());
        this.zipEntry = zipEntry;
        super.setMethod(zipEntry.getMethod());
        this.channel = channel;
        this.headerOffset = headerOffset;
        this.dataOffset = ZipDeserializer.dataOffset(headerOffset, zipEntry);
    }

    @Override
    public long getLength() {
        return zipEntry.getSize();
    }

    @Override
    public long getCrc() {
        return zipEntry.getCrc();
    }

    private InputStream rawStream() {
        return new InputStream() {
            private int cursor = 0;
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                long remaining = zipEntry.getCompressedSize() - cursor;

                if (len == 0) {
                    return 0;
                }
                if (len < 0 || off < 0 || (len + off) < b.length) {
                    throw new IOException("Invalid length/offset.");
                }
                if (remaining == 0) {
                    return -1;
                }

                len = (int) Math.min(remaining, len);
                int n = channel.read(ByteBuffer.wrap(b).position(off).limit(off + len), dataOffset + cursor);
                cursor += n;
                return n;
            }

            @Override
            public int read() throws IOException {
                byte[] b = new byte[1];
                int n = read(b);
                if (n == 1)
                    return b[0] & 0xff;
                return -1;
            }
        };
    }

    @Override
    public InputStream openStream() throws IOException {
        InputStream stream = rawStream();
        switch (zipEntry.getMethod()) {
            case ZipEntry.DEFLATED:
                return new InflaterInputStream(stream, new Inflater(true));
            case ZipEntry.STORED:
                return stream;
            default:
                throw new IOException("Unsupported compression method: " + zipEntry.getMethod());
        }
    }

    public WriteCompressedResult writeCompressed(OutputStream outputStream) throws IOException {
        write(outputStream, rawStream());
        return new WriteCompressedResult(getCrc(), zipEntry.getCompressedSize(), getLength());
    }
}
