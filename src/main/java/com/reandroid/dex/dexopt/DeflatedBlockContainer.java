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
package com.reandroid.dex.dexopt;

import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.BooleanReference;
import com.reandroid.arsc.item.IntegerReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

public class DeflatedBlockContainer extends FixedBlockContainer {

    private final BooleanReference is_deflated;
    private final IntegerReference sizeUncompressed;
    private final IntegerReference sizeCompressed;

    public DeflatedBlockContainer(int childesCount,
                                  BooleanReference is_deflated,
                                  IntegerReference sizeUncompressed,
                                  IntegerReference sizeCompressed) {
        super(childesCount);
        this.is_deflated = is_deflated;
        this.sizeUncompressed = sizeUncompressed;
        this.sizeCompressed = sizeCompressed;
    }

    public boolean isDeflated() {
        return is_deflated.get();
    }
    @Override
    public int countBytes() {
        if (isDeflated()) {
            return sizeCompressed.get();
        }
        return super.countBytes();
    }
    private void updateDeflatedBytesCount() {
        if (isDeflated()) {
            try {
                byte[] bytes = deflate(sizeUncompressed);
                sizeCompressed.set(bytes.length);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        updateDeflatedBytesCount();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        BlockReader deflatedReader;
        if (isDeflated()) {
            byte[] uncompressed = readCompressed(
                    reader,
                    sizeCompressed.get(),
                    sizeUncompressed.get());
            deflatedReader = new BlockReader(uncompressed);
        } else {
            deflatedReader = reader;
        }
        super.onReadBytes(deflatedReader);
    }

    @Override
    public int onWriteBytes(OutputStream stream) throws IOException {
        if (isDeflated()) {
            byte[] bytes = deflate(null);
            int length = bytes.length;
            stream.write(bytes, 0, length);
            return length;
        } else {
            return super.onWriteBytes(stream);
        }
    }
    private byte[] deflate(IntegerReference uncompressedReference) throws IOException {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(arrayOutputStream,
                new Deflater(Deflater.BEST_SPEED));
        int uncompressed = super.onWriteBytes(deflaterOutputStream);
        if (uncompressedReference != null) {
            uncompressedReference.set(uncompressed);
        }
        deflaterOutputStream.close();
        arrayOutputStream.close();
        return arrayOutputStream.toByteArray();
    }

    private static byte[] readCompressed(InputStream is, int compressedDataSize, int uncompressedDataSize) throws IOException {
        Inflater inf = new Inflater();
        try {
            byte[] result = new byte[uncompressedDataSize];
            int totalBytesRead = 0;
            int totalBytesInflated = 0;
            byte[] input = new byte[2048];
            while (
                    !inf.finished() &&
                            !inf.needsDictionary() &&
                            totalBytesRead < compressedDataSize
            ) {
                int bytesRead = is.read(input);
                if (bytesRead < 0) {
                    throw new IOException(
                            "Invalid zip data. Stream ended after $totalBytesRead bytes. " +
                                    "Expected " + compressedDataSize + " bytes"
                    );
                }
                inf.setInput(input, 0, bytesRead);
                try {
                    totalBytesInflated += inf.inflate(
                            result,
                            totalBytesInflated,
                            uncompressedDataSize - totalBytesInflated
                    );
                } catch (DataFormatException e) {
                    throw new IOException(e.getMessage());
                }
                totalBytesRead += bytesRead;
            }
            if (totalBytesRead != compressedDataSize) {
                throw new IOException(
                        "Didn't read enough bytes during decompression." +
                                " expected=" + compressedDataSize +
                                " actual=" + totalBytesRead
                );
            }
            if (!inf.finished()) {
                throw new IOException("Inflater did not finish");
            }
            return result;
        } finally {
            inf.end();
        }
    }
}
