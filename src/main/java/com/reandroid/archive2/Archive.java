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
package com.reandroid.archive2;

import com.reandroid.archive2.block.*;
import com.reandroid.archive2.io.ArchiveFile;
import com.reandroid.archive2.io.ArchiveUtil;
import com.reandroid.archive2.io.ZipSource;
import com.reandroid.archive2.model.LocalFileDirectory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;

public class Archive {
    private final ZipSource zipSource;
    private final List<ArchiveEntry> entryList;
    private final EndRecord endRecord;
    private final ApkSignatureBlock apkSignatureBlock;
    public Archive(ZipSource zipSource) throws IOException {
        this.zipSource = zipSource;
        LocalFileDirectory lfd = new LocalFileDirectory();
        lfd.visit(zipSource);
        List<LocalFileHeader> localFileHeaderList = lfd.getHeaderList();
        List<CentralEntryHeader> centralEntryHeaderList = lfd.getCentralFileDirectory().getHeaderList();
        List<ArchiveEntry> entryList = new ArrayList<>();
        for(int i=0;i<localFileHeaderList.size();i++){
            LocalFileHeader lfh = localFileHeaderList.get(i);
            CentralEntryHeader ceh = centralEntryHeaderList.get(i);
            ArchiveEntry archiveEntry = new ArchiveEntry(lfh, ceh);
            entryList.add(archiveEntry);
        }
        this.entryList  = entryList;
        this.endRecord = lfd.getCentralFileDirectory().getEndRecord();
        this.apkSignatureBlock = lfd.getApkSigBlock();
    }
    public Archive(File file) throws IOException {
        this(new ArchiveFile(file));
    }
    public InputStream openRawInputStream(ArchiveEntry archiveEntry) throws IOException {
        return zipSource.getInputStream(archiveEntry.getFileOffset(), archiveEntry.getDataSize());
    }
    public InputStream openInputStream(ArchiveEntry archiveEntry) throws IOException {
        InputStream rawInputStream = openRawInputStream(archiveEntry);
        if(archiveEntry.getMethod() == ZipEntry.STORED){
            return rawInputStream;
        }
        return new InflaterInputStream(rawInputStream,
                new Inflater(true), 1024*1000);
    }
    public List<ArchiveEntry> getEntryList() {
        return entryList;
    }

    public ApkSignatureBlock getApkSigBlock() {
        return apkSignatureBlock;
    }
    public EndRecord getEndRecord() {
        return endRecord;
    }

    // for test
    public void extract(File dir) throws IOException {
        for(ArchiveEntry archiveEntry:getEntryList()){
            if(archiveEntry.isDirectory()){
                continue;
            }
            extract(dir, archiveEntry);
        }
    }
    private void extract(File dir, ArchiveEntry archiveEntry) throws IOException{
        File out = toFile(dir, archiveEntry);
        File parent = out.getParentFile();
        if(!parent.exists()){
            parent.mkdirs();
        }
        FileOutputStream outputStream = new FileOutputStream(out);
        ArchiveUtil.writeAll(openInputStream(archiveEntry), outputStream);
        outputStream.close();
    }
    private File toFile(File dir, ArchiveEntry archiveEntry){
        String name = archiveEntry.getName().replace('/', File.separatorChar);
        return new File(dir, name);
    }
    // for test
    public void writeSignatureData(File dir) throws IOException{
        ApkSignatureBlock apkSignatureBlock = getApkSigBlock();
        if(apkSignatureBlock == null){
            throw new IOException("Does not have signature block");
        }
        apkSignatureBlock.writeSignatureData(dir);
    }
}
