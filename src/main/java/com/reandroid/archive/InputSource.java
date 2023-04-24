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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.*;

public abstract class InputSource {
    private final String name;
    private String alias;
    private long mCrc;
    private long mLength;
    private int method = ZipEntry.DEFLATED;
    private int sort;

    public InputSource(String name) {
        this.name = name;
        this.alias = InputSourceUtil.sanitize(name);
    }

    public void disposeInputSource() {
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getMethod() {
        return method;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public String getAlias() {
        if (alias != null) {
            return alias;
        }
        return getName();
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void close(InputStream inputStream) throws IOException {
        inputStream.close();
    }

    public long write(OutputStream outputStream) throws IOException {
        return write(outputStream, openStream());
    }

    protected final long write(OutputStream outputStream, InputStream inputStream) throws IOException {
        long result = 0;
        byte[] buffer = new byte[10240];
        int len;
        while ((len = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
            result += len;
        }
        close(inputStream);
        return result;
    }

    public static class WriteCompressedResult {
        public long crc;
        public long compressedSize;
        public long uncompressedSize;

        WriteCompressedResult(long crc, long compressedSize, long uncompressedSize) {
            this.crc = crc;
            this.compressedSize = compressedSize;
            this.uncompressedSize = uncompressedSize;
        }
    }

    public WriteCompressedResult writeCompressed(OutputStream outputStream) throws IOException {
        CRC32 checksum = null;
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        DeflaterOutputStream deflatedStream = new DeflaterOutputStream(outputStream, deflater);

        OutputStream targetStream;
        if (mCrc == 0) {
            checksum = new CRC32();
            targetStream = new CheckedOutputStream(deflatedStream, checksum);
        } else {
            targetStream = deflatedStream;
        }

        write(targetStream);
        targetStream.flush();
        deflatedStream.finish();

        mLength = deflater.getBytesRead();
        if (checksum != null) {
            mCrc = checksum.getValue();
        }

        return new WriteCompressedResult(mCrc, deflater.getBytesWritten(), mLength);
    }

    public String getName() {
        return name;
    }

    public long getLength() throws IOException {
        if (mLength == 0) {
            calculateCrc();
        }
        return mLength;
    }

    public long getCrc() throws IOException {
        if (mCrc == 0) {
            calculateCrc();
        }
        return mCrc;
    }

    public abstract InputStream openStream() throws IOException;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InputSource)) {
            return false;
        }
        InputSource that = (InputSource) o;
        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getName();
    }

    private void calculateCrc() throws IOException {
        InputStream inputStream = openStream();
        long length = 0;
        CRC32 crc = new CRC32();
        int bytesRead;
        byte[] buffer = new byte[1024 * 64];
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            crc.update(buffer, 0, bytesRead);
            length += bytesRead;
        }
        close(inputStream);
        mCrc = crc.getValue();
        mLength = length;
    }
}
