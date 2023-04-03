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
package com.reandroid.archive2.model;

import com.reandroid.archive2.block.*;
import com.reandroid.archive2.io.ZipSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LocalFileDirectory {
    private final CentralFileDirectory centralFileDirectory;
    private final List<LocalFileHeader> headerList;
    private ApkSigBlock apkSigBlock;
    private long mTotalDataLength;
    public LocalFileDirectory(CentralFileDirectory centralFileDirectory){
        this.centralFileDirectory = centralFileDirectory;
        this.headerList = new ArrayList<>();
    }
    public LocalFileDirectory(){
        this(new CentralFileDirectory());
    }
    public void visit(ZipSource zipSource) throws IOException {
        getCentralFileDirectory().visit(zipSource);
        visitLocalFile(zipSource);
        visitApkSigBlock(zipSource);
    }
    private void visitLocalFile(ZipSource zipSource) throws IOException {
        EndRecord endRecord = getCentralFileDirectory().getEndRecord();
        InputStream inputStream = zipSource.getInputStream(0, endRecord.getOffsetOfCentralDirectory());
        visitLocalFile(inputStream);
        inputStream.close();
    }
    private void visitLocalFile(InputStream inputStream) throws IOException {
        List<LocalFileHeader> headerList = this.getHeaderList();
        long offset = 0;
        int read;
        CentralFileDirectory centralFileDirectory = getCentralFileDirectory();
        LocalFileHeader lfh = new LocalFileHeader();
        read = lfh.readBytes(inputStream);
        int index = 0;
        while (lfh.isValidSignature()){
            offset += read;
            lfh.setIndex(index);
            CentralEntryHeader ceh = centralFileDirectory.get(lfh);
            lfh.mergeZeroValues(ceh);
            lfh.setFileOffset(offset);
            ceh.setFileOffset(offset);
            offset += inputStream.skip(lfh.getDataSize());
            DataDescriptor dataDescriptor = null;
            if(lfh.hasDataDescriptor()){
                dataDescriptor = new DataDescriptor();
                read = dataDescriptor.readBytes(inputStream);
                if(read>0){
                    offset += read;
                }
            }
            lfh.setDataDescriptor(dataDescriptor);
            headerList.add(lfh);
            index++;

            lfh = new LocalFileHeader();
            read = lfh.readBytes(inputStream);
        }
        mTotalDataLength = offset;
    }
    private void visitApkSigBlock(ZipSource zipSource) throws IOException{
        CentralFileDirectory cfd = getCentralFileDirectory();
        SignatureFooter footer = cfd.getSignatureFooter();
        if(footer == null || !footer.isValid()){
            return;
        }
        EndRecord endRecord = cfd.getEndRecord();
        long length = footer.getSigBlockSizeInFooter() + 8;
        long offset = endRecord.getOffsetOfCentralDirectory()
                - length;
        ApkSigBlock apkSigBlock = new ApkSigBlock(footer);
        apkSigBlock.readBytes(zipSource.getInputStream(offset, length));
        this.apkSigBlock = apkSigBlock;
    }
    public ApkSigBlock getApkSigBlock() {
        return apkSigBlock;
    }
    public CentralFileDirectory getCentralFileDirectory() {
        return centralFileDirectory;
    }
    public List<LocalFileHeader> getHeaderList() {
        return headerList;
    }
    public long getTotalDataLength() {
        return mTotalDataLength;
    }
}
