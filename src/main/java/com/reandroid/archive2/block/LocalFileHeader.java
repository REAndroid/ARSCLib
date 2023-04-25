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
package com.reandroid.archive2.block;

import com.reandroid.archive2.ZipSignature;

import java.io.IOException;
import java.io.InputStream;

public class LocalFileHeader extends CommonHeader {
    private DataDescriptor dataDescriptor;
    public LocalFileHeader(){
        super(OFFSET_fileName, ZipSignature.LOCAL_FILE, OFFSET_general_purpose);
    }
    public LocalFileHeader(String name){
        this();
        setFileName(name);
    }

    public void mergeZeroValues(CentralEntryHeader ceh){
        if(getCrc()==0){
            setCrc(ceh.getCrc());
        }
        if(getSize()==0){
            setSize(ceh.getSize());
        }
        if(getCompressedSize()==0){
            setCompressedSize(ceh.getCompressedSize());
        }
        if(getGeneralPurposeFlag().getValue()==0){
            getGeneralPurposeFlag().setValue(ceh.getGeneralPurposeFlag().getValue());
        }
    }

    public DataDescriptor getDataDescriptor() {
        return dataDescriptor;
    }
    public void setDataDescriptor(DataDescriptor dataDescriptor){
        this.dataDescriptor = dataDescriptor;
        getGeneralPurposeFlag().setHasDataDescriptor(dataDescriptor!=null);
    }

    public static LocalFileHeader fromCentralEntryHeader(CentralEntryHeader ceh){
        LocalFileHeader lfh = new LocalFileHeader();
        lfh.setSignature(ZipSignature.LOCAL_FILE);
        lfh.setVersionMadeBy(ceh.getVersionMadeBy());
        lfh.getGeneralPurposeFlag().setValue(ceh.getGeneralPurposeFlag().getValue());
        lfh.setMethod(ceh.getMethod());
        lfh.setDosTime(ceh.getDosTime());
        lfh.setCrc(ceh.getCrc());
        lfh.setCompressedSize(ceh.getCompressedSize());
        lfh.setSize(ceh.getSize());
        lfh.setFileName(ceh.getFileName());
        lfh.setExtra(ceh.getExtra());
        return lfh;
    }

    public static LocalFileHeader read(InputStream inputStream) throws IOException {
        LocalFileHeader localFileHeader = new LocalFileHeader();
        localFileHeader.readBytes(inputStream);
        if(localFileHeader.isValidSignature()){
            return localFileHeader;
        }
        return null;
    }
    private static final int OFFSET_signature = 0;
    private static final int OFFSET_versionMadeBy = 4;
    private static final int OFFSET_platform = 5;
    private static final int OFFSET_general_purpose = 6;
    private static final int OFFSET_method = 8;
    private static final int OFFSET_dos_time = 10;
    private static final int OFFSET_crc = 14;
    private static final int OFFSET_compressed_size = 18;
    private static final int OFFSET_size = 22;
    private static final int OFFSET_fileNameLength = 26;
    private static final int OFFSET_extraLength = 28;

    private static final int OFFSET_fileName = 30;

}
