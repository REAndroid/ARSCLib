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

import com.reandroid.archive2.block.CentralEntryHeader;
import com.reandroid.archive2.block.LocalFileHeader;
import com.reandroid.arsc.util.HexUtil;

import java.util.zip.ZipEntry;

public class ArchiveEntry extends ZipEntry {
    private final CentralEntryHeader centralEntryHeader;
    private final LocalFileHeader localFileHeader;
    public ArchiveEntry(LocalFileHeader lfh, CentralEntryHeader ceh){
        super(lfh.getFileName());
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

    @Override
    public int getMethod(){
        return localFileHeader.getMethod();
    }
    @Override
    public void setMethod(int method){
        localFileHeader.setMethod(method);
        centralEntryHeader.setMethod(method);
    }
    @Override
    public long getSize() {
        return centralEntryHeader.getSize();
    }
    @Override
    public void setSize(long size) {
        centralEntryHeader.setSize(size);
        localFileHeader.setSize(size);
    }
    @Override
    public long getCrc() {
        return centralEntryHeader.getCrc();
    }
    @Override
    public void setCrc(long crc) {
        centralEntryHeader.setCrc(crc);
        localFileHeader.setCrc(crc);
    }
    @Override
    public long getCompressedSize() {
        return centralEntryHeader.getCompressedSize();
    }
    @Override
    public void setCompressedSize(long csize) {
        centralEntryHeader.setCompressedSize(csize);
        localFileHeader.setCompressedSize(csize);
    }
    public long getFileOffset() {
        return localFileHeader.getFileOffset();
    }
    @Override
    public String getName(){
        return centralEntryHeader.getFileName();
    }
    public void setName(String name){
        centralEntryHeader.setFileName(name);
        localFileHeader.setFileName(name);
    }
    @Override
    public String getComment(){
        return centralEntryHeader.getComment();
    }
    @Override
    public void setComment(String name){
        centralEntryHeader.setComment(name);
    }
    @Override
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

    @Override
    public String toString(){
        return "["+ getFileOffset()+"] " + getName() + getComment()
                + HexUtil.toHex(" 0x", getCrc(), 8);
    }
}
