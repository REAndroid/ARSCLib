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

import com.reandroid.archive.block.CentralEntryHeader;
import com.reandroid.archive.block.LocalFileHeader;
import com.reandroid.utils.HexUtil;

import java.util.zip.ZipEntry;

public class ArchiveEntry {
    private final CentralEntryHeader centralEntryHeader;
    private final LocalFileHeader localFileHeader;
    public ArchiveEntry(LocalFileHeader lfh, CentralEntryHeader ceh){
        this.localFileHeader = lfh;
        this.centralEntryHeader = ceh;
    }
    public ArchiveEntry(String name){
        this(new LocalFileHeader(name), new CentralEntryHeader(name));
    }
    public ArchiveEntry(){
        this(new LocalFileHeader(), new CentralEntryHeader());
    }

    public long getDataSize(){
        if(getMethod() == ZipEntry.STORED){
            return getSize();
        }
        return getCompressedSize();
    }

    public int getMethod(){
        return localFileHeader.getMethod();
    }
    public void setMethod(int method){
        localFileHeader.setMethod(method);
        centralEntryHeader.setMethod(method);
    }
    public long getSize() {
        return centralEntryHeader.getSize();
    }
    public void setSize(long size) {
        centralEntryHeader.setSize(size);
        localFileHeader.setSize(size);
    }
    public long getCrc() {
        return centralEntryHeader.getCrc();
    }
    public void setCrc(long crc) {
        centralEntryHeader.setCrc(crc);
        localFileHeader.setCrc(crc);
    }
    public long getCompressedSize() {
        return centralEntryHeader.getCompressedSize();
    }
    public void setCompressedSize(long csize) {
        centralEntryHeader.setCompressedSize(csize);
        localFileHeader.setCompressedSize(csize);
    }
    public long getFileOffset() {
        return localFileHeader.getFileOffset();
    }
    public String getName(){
        return centralEntryHeader.getFileName();
    }
    public void setName(String name){
        centralEntryHeader.setFileName(name);
        localFileHeader.setFileName(name);
    }
    public String getComment(){
        return centralEntryHeader.getComment();
    }
    public void setComment(String name){
        centralEntryHeader.setComment(name);
    }
    public boolean isDirectory() {
        return this.getName().endsWith("/");
    }
    public CentralEntryHeader getCentralEntryHeader(){
        return centralEntryHeader;
    }
    public LocalFileHeader getLocalFileHeader() {
        return localFileHeader;
    }
    public boolean matches(CentralEntryHeader centralEntryHeader){
        if(centralEntryHeader==null){
            return false;
        }
        return false;
    }
    public String toString(){
        return "["+ getFileOffset()+"] " + getName() + getComment()
                + HexUtil.toHex(" 0x", getCrc(), 8);
    }
}
