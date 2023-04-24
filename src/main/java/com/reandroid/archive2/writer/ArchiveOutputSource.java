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
package com.reandroid.archive2.writer;

import com.reandroid.archive.InputSource;
import com.reandroid.archive2.block.LocalFileHeader;
import com.reandroid.archive2.io.ArchiveEntrySource;
import com.reandroid.archive2.io.ZipFileInput;
import com.reandroid.archive2.io.ZipInput;


public class ArchiveOutputSource extends OutputSource{
    public ArchiveOutputSource(InputSource inputSource){
        super(inputSource);
    }

    ArchiveEntrySource getArchiveSource(){
        return (ArchiveEntrySource) super.getInputSource();
    }
    @Override
    EntryBuffer makeFromEntry(){
        ArchiveEntrySource entrySource = getArchiveSource();
        ZipInput zip = entrySource.getZipSource();
        if(!(zip instanceof ZipFileInput)){
            return null;
        }
        LocalFileHeader lfh = entrySource.getArchiveEntry().getLocalFileHeader();
        if(lfh.getMethod() != getInputSource().getMethod()){
            return null;
        }
        return new EntryBuffer((ZipFileInput) zip,
                lfh.getFileOffset(),
                lfh.getDataSize());
    }
    @Override
    public LocalFileHeader createLocalFileHeader(){
        ArchiveEntrySource source = getArchiveSource();
        return source.getArchiveEntry().getLocalFileHeader();
    }
}
