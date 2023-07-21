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
package com.reandroid.archive.io;

import com.reandroid.archive.InputSource;
import com.reandroid.archive.ArchiveEntry;
import com.reandroid.archive.block.LocalFileHeader;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class ArchiveEntrySource<T extends ZipInput> extends InputSource {
    private final T zipInput;
    private final ArchiveEntry archiveEntry;
    public ArchiveEntrySource(T zipInput, ArchiveEntry archiveEntry){
        super(archiveEntry.getSanitizedName());
        this.zipInput = zipInput;
        this.archiveEntry = archiveEntry;
        setMethod(archiveEntry.getMethod());
    }

    public T getZipSource(){
        return zipInput;
    }
    public ArchiveEntry getArchiveEntry() {
        return archiveEntry;
    }
    @Override
    public InputStream openStream() throws IOException {
        ArchiveEntry archiveEntry = getArchiveEntry();
        LocalFileHeader lfh = archiveEntry.getLocalFileHeader();
        InputStream inputStream = getZipSource().getInputStream(
                archiveEntry.getFileOffset(), archiveEntry.getDataSize());
        if(lfh.getSize() == lfh.getCompressedSize()){
            return inputStream;
        }
        return new InflaterInputStream(inputStream,
                new Inflater(true), 512);
    }
    @Override
    public long getLength() throws IOException{
        return getArchiveEntry().getDataSize();
    }
    @Override
    public long getCrc() throws IOException{
        return getArchiveEntry().getCrc();
    }
}
