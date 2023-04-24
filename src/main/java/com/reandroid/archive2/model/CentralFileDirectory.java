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

import com.reandroid.archive2.block.CentralEntryHeader;
import com.reandroid.archive2.block.EndRecord;
import com.reandroid.archive2.block.LocalFileHeader;
import com.reandroid.archive2.block.SignatureFooter;
import com.reandroid.archive2.io.ZipInput;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CentralFileDirectory {
    private final List<CentralEntryHeader> headerList;
    private EndRecord endRecord;
    private SignatureFooter signatureFooter;
    public CentralFileDirectory(){
        this.headerList = new ArrayList<>();
    }
    public CentralEntryHeader get(LocalFileHeader lfh){
        String name = lfh.getFileName();
        CentralEntryHeader ceh = get(lfh.getIndex());
        if(ceh!=null && Objects.equals(ceh.getFileName() , name)){
            return ceh;
        }
        return get(name);
    }
    public CentralEntryHeader get(String name){
        if(name == null){
            name = "";
        }
        for(CentralEntryHeader ceh:getHeaderList()){
            if(name.equals(ceh.getFileName())){
                return ceh;
            }
        }
        return null;
    }
    public CentralEntryHeader get(int i){
        if(i<0 || i>=headerList.size()){
            return null;
        }
        return headerList.get(i);
    }
    public int count(){
        return headerList.size();
    }
    public List<CentralEntryHeader> getHeaderList() {
        return headerList;
    }

    public SignatureFooter getSignatureFooter() {
        return signatureFooter;
    }
    public EndRecord getEndRecord() {
        return endRecord;
    }
    public void visit(ZipInput zipInput) throws IOException {
        byte[] footer = zipInput.getFooter(SignatureFooter.MIN_SIZE + EndRecord.MAX_LENGTH);
        EndRecord endRecord = findEndRecord(footer);
        int length = (int) endRecord.getLengthOfCentralDirectory();
        int endLength = endRecord.countBytes();
        if(footer.length < (length + endLength)){
            footer = zipInput.getFooter(SignatureFooter.MIN_SIZE + length + endLength);
        }
        int offset = footer.length - length - endLength;
        this.endRecord = endRecord;
        loadCentralFileHeaders(footer, offset, length);
        this.signatureFooter = tryFindSignatureFooter(footer, endRecord);
    }
    private void loadCentralFileHeaders(byte[] footer, int offset, int length) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(footer, offset, length);
        loadCentralFileHeaders(inputStream);
    }
    private void loadCentralFileHeaders(InputStream inputStream) throws IOException {
        List<CentralEntryHeader> headerList = this.headerList;
        CentralEntryHeader ceh = new CentralEntryHeader();
        ceh.readBytes(inputStream);
        while (ceh.isValidSignature()){
            headerList.add(ceh);
            ceh = new CentralEntryHeader();
            ceh.readBytes(inputStream);
        }
        inputStream.close();
    }
    private EndRecord findEndRecord(byte[] footer) throws IOException{
        int length = footer.length;
        int minLength = EndRecord.MIN_LENGTH;
        int start = length - minLength;
        for(int offset=start; offset>=0; offset--){
            EndRecord endRecord = new EndRecord();
            endRecord.putBytes(footer, offset, 0, minLength);
            if(endRecord.isValidSignature()){
                return endRecord;
            }
        }
        throw new IOException("Failed to find end record");
    }
    private SignatureFooter tryFindSignatureFooter(byte[] footer, EndRecord endRecord) throws IOException {
        int lenCd = (int) endRecord.getLengthOfCentralDirectory();
        int endLength = endRecord.countBytes();
        int length = SignatureFooter.MIN_SIZE;
        int offset = footer.length - endLength - lenCd - length;
        if(offset < 0){
            return null;
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(footer, offset, length);
        SignatureFooter signatureFooter = new SignatureFooter();
        signatureFooter.readBytes(inputStream);
        if(signatureFooter.isValid()){
            return signatureFooter;
        }
        return null;
    }
}
