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

import com.reandroid.apk.APKLogger;
import com.reandroid.archive.block.*;
import com.reandroid.archive.io.*;
import com.reandroid.archive.model.CentralFileDirectory;
import com.reandroid.archive.model.LocalFileDirectory;
import com.reandroid.utils.collection.ArrayIterator;
import com.reandroid.utils.collection.FilterIterator;
import com.reandroid.utils.io.FileUtil;
import com.reandroid.utils.io.IOUtil;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public abstract class Archive<T extends ZipInput> implements Closeable {
    private final T zipInput;
    private final ArchiveEntry[] entryList;
    private final EndRecord endRecord;
    private final ApkSignatureBlock apkSignatureBlock;

    public Archive(T zipInput) throws IOException {
        this.zipInput = zipInput;
        CentralFileDirectory cfd = new CentralFileDirectory();
        cfd.visit(zipInput);
        this.endRecord = cfd.getEndRecord();
        LocalFileDirectory lfd = new LocalFileDirectory(cfd);
        lfd.visit(zipInput);
        this.entryList  = lfd.buildArchiveEntryList();
        this.apkSignatureBlock = lfd.getApkSigBlock();
    }

    public ZipEntryMap createZipEntryMap(){
        return new ZipEntryMap(mapEntrySource());
    }

    public LinkedHashMap<String, InputSource> mapEntrySource(){
        ArchiveEntry[] entryList = this.entryList;
        int length = entryList.length;
        LinkedHashMap<String, InputSource> map = new LinkedHashMap<>(length);
        for(int i = 0; i < length; i++){
            ArchiveEntry entry = entryList[i];
            if(entry.isDirectory()){
                continue;
            }
            InputSource inputSource = createInputSource(entry);
            inputSource.setSort(i);
            map.put(inputSource.getAlias(), inputSource);
        }
        return map;
    }

    public T getZipInput() {
        return zipInput;
    }

    abstract InputSource createInputSource(ArchiveEntry entry);
    public InputSource getEntrySource(String path){
        if(path == null){
            return null;
        }
        ArchiveEntry[] entryList = this.entryList;
        int length = entryList.length;
        for(int i = 0; i < length; i++){
            ArchiveEntry entry = entryList[i];
            if(entry.isDirectory()){
                continue;
            }
            if(path.equals(entry.getName())){
                return createInputSource(entry);
            }
        }
        return null;
    }
    public InputStream openRawInputStream(ArchiveEntry archiveEntry) throws IOException {
        return zipInput.getInputStream(archiveEntry.getFileOffset(), archiveEntry.getDataSize());
    }
    public InputStream openInputStream(ArchiveEntry archiveEntry) throws IOException {
        InputStream rawInputStream = openRawInputStream(archiveEntry);
        if(!archiveEntry.isCompressed()){
            return rawInputStream;
        }
        return new InflaterInputStream(rawInputStream,
                new Inflater(true), 1024*1000);
    }
    public Iterator<ArchiveEntry> iterator() {
        return new ArrayIterator<>(entryList);
    }
    public ApkSignatureBlock getApkSignatureBlock() {
        return apkSignatureBlock;
    }
    public EndRecord getEndRecord() {
        return endRecord;
    }

    public int extractAll(File dir) throws IOException {
        return extractAll(dir, null, null);
    }
    public int extractAll(File dir, APKLogger logger) throws IOException {
        return extractAll(dir, null, logger);
    }
    public int extractAll(File dir, Predicate<ArchiveEntry> filter) throws IOException {
        return extractAll(dir, filter, null);
    }
    public int extractAll(File dir, Predicate<ArchiveEntry> filter, APKLogger logger) throws IOException {
        FilterIterator<ArchiveEntry> iterator =
                new FilterIterator<ArchiveEntry>(this.iterator(), filter){
                    @Override
                    public boolean test(ArchiveEntry archiveEntry){
                        return archiveEntry != null && !archiveEntry.isDirectory();
                    }
                };
        int result = 0;
        while (iterator.hasNext()){
            ArchiveEntry archiveEntry = iterator.next();
            extract(toFile(dir, archiveEntry), archiveEntry, logger);
            result ++;
        }
        return result;
    }
    public void extract(File file, ArchiveEntry archiveEntry) throws IOException{
        extract(file, archiveEntry, null);
    }
    public void extract(File file, ArchiveEntry archiveEntry, APKLogger logger) throws IOException{
        File parent = file.getParentFile();
        if(parent != null && !parent.exists()){
            parent.mkdirs();
        }
        if(logger != null){
            long size = archiveEntry.getDataSize();
            if(size > LOG_LARGE_FILE_SIZE){
                logger.logVerbose("Extracting ["
                        + FileUtil.toReadableFileSize(size) + "] "+ archiveEntry.getName());
            }
        }
        if(archiveEntry.getMethod() != Archive.STORED){
            extractCompressed(file, archiveEntry);
        }else {
            extractStored(file, archiveEntry);
        }
    }
    abstract void extractStored(File file, ArchiveEntry archiveEntry) throws IOException;
    private void extractCompressed(File file, ArchiveEntry archiveEntry) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(file);
        IOUtil.writeAll(openInputStream(archiveEntry), outputStream);
        outputStream.close();
    }
    private File toFile(File dir, ArchiveEntry archiveEntry){
        String name = archiveEntry.getName().replace('/', File.separatorChar);
        return new File(dir, name);
    }
    @Override
    public void close() throws IOException {
        this.zipInput.close();
    }
    private static final long LOG_LARGE_FILE_SIZE = 1024 * 1000 * 20;


    public static final int STORED = 0;
    public static final int DEFLATED = 8;
}
