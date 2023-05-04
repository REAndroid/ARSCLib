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
import com.reandroid.arsc.util.HexUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class CentralEntryHeader extends CommonHeader {
    private String mComment;
    public CentralEntryHeader(){
        super(OFFSET_fileName, ZipSignature.CENTRAL_FILE, OFFSET_general_purpose);
    }
    public CentralEntryHeader(String name){
        this();
        setFileName(name);
    }

    @Override
    int readComment(InputStream inputStream) throws IOException {
        int commentLength = getCommentLength();
        if(commentLength==0){
            mComment = "";
            return 0;
        }
        setCommentLength(commentLength);
        byte[] bytes = getBytesInternal();
        int read = inputStream.read(bytes, getOffsetComment(), commentLength);
        if(read != commentLength){
            throw new IOException("Stream ended before reading comment: read="
                    +read+", name length="+commentLength);
        }
        mComment = null;
        return commentLength;
    }

    public int getVersionExtract(){
        return getShortUnsigned(OFFSET_versionExtract);
    }
    public void setVersionExtract(int value){
        putShort(OFFSET_versionExtract, value);
    }
    public String getComment(){
        if(mComment == null){
            mComment = decodeComment();
        }
        return mComment;
    }
    public void setComment(String comment){
        if(comment==null){
            comment="";
        }
        byte[] strBytes = ZipStringEncoding.encodeString(isUtf8(), comment);
        int length = strBytes.length;
        setCommentLength(length);
        if(length==0){
            mComment = comment;
            return;
        }
        byte[] bytes = getBytesInternal();
        System.arraycopy(strBytes, 0, bytes, getOffsetComment(), length);
        mComment = comment;
    }


    @Override
    public int getCommentLength(){
        return getShortUnsigned(OFFSET_commentLength);
    }
    public void setCommentLength(int value){
        int length = getOffsetComment() + value;
        setBytesLength(length, false);
        putShort(OFFSET_commentLength, value);
    }
    public long getLocalRelativeOffset(){
        return getIntegerUnsigned(OFFSET_localRelativeOffset);
    }
    public void setLocalRelativeOffset(long offset){
        putInteger(OFFSET_localRelativeOffset, offset);
    }
    @Override
    void onUtf8Changed(boolean oldValue){
        String str = mComment;
        if(str != null){
            setComment(str);
        }
    }

    public boolean matches(LocalFileHeader localFileHeader){
        if(localFileHeader==null){
            return false;
        }
        return getCrc() == localFileHeader.getCrc()
                && Objects.equals(getFileName(), localFileHeader.getFileName());
    }

    @Override
    public String toString(){
        if(countBytes()<getMinByteLength()){
            return "Invalid";
        }
        StringBuilder builder = new StringBuilder();
        builder.append('[').append(getFileOffset()).append(']');
        String str = getFileName();
        boolean appendOnce = false;
        if(str.length()>0){
            builder.append("name=").append(str);
            appendOnce = true;
        }
        str = getComment();
        if(str.length()>0){
            if(appendOnce){
                builder.append(", ");
            }
            builder.append("comment=").append(str);
            appendOnce = true;
        }
        if(appendOnce){
            builder.append(", ");
        }
        builder.append("SIG=").append(getSignature());
        builder.append(", versionMadeBy=").append(HexUtil.toHex4((short) getVersionMadeBy()));
        builder.append(", versionExtract=").append(HexUtil.toHex4((short) getVersionExtract()));
        builder.append(", GP={").append(getGeneralPurposeFlag()).append("}");
        builder.append(", method=").append(getMethod());
        builder.append(", date=").append(getDate());
        builder.append(", crc=").append(HexUtil.toHex8(getCrc()));
        builder.append(", cSize=").append(getCompressedSize());
        builder.append(", size=").append(getSize());
        builder.append(", fileNameLength=").append(getFileNameLength());
        builder.append(", extraLength=").append(getExtraLength());
        builder.append(", commentLength=").append(getCommentLength());
        builder.append(", offset=").append(getLocalRelativeOffset());
        return builder.toString();
    }


    public static CentralEntryHeader fromLocalFileHeader(LocalFileHeader lfh){
        CentralEntryHeader ceh = new CentralEntryHeader();
        ceh.setSignature(ZipSignature.CENTRAL_FILE);
        ceh.setVersionMadeBy(0x0300);
        long offset = lfh.getFileOffset() - lfh.countBytes();
        ceh.setLocalRelativeOffset(offset);
        ceh.getGeneralPurposeFlag().setValue(lfh.getGeneralPurposeFlag().getValue());
        ceh.setMethod(lfh.getMethod());
        ceh.setDosTime(lfh.getDosTime());
        ceh.setCrc(lfh.getCrc());
        ceh.setCompressedSize(lfh.getCompressedSize());
        ceh.setSize(lfh.getSize());
        ceh.setFileName(lfh.getFileName());
        ceh.setExtra(lfh.getExtra());
        return ceh;
    }
    private static final int OFFSET_signature = 0;
    private static final int OFFSET_versionMadeBy = 4;
    private static final int OFFSET_versionExtract = 6;
    private static final int OFFSET_general_purpose = 8;
    private static final int OFFSET_method = 10;
    private static final int OFFSET_dos_time = 12;
    private static final int OFFSET_dos_date = 14;
    private static final int OFFSET_crc = 16;
    private static final int OFFSET_compressed_size = 20;
    private static final int OFFSET_size = 24;
    private static final int OFFSET_fileNameLength = 28;
    private static final int OFFSET_extraLength = 30;
    private static final int OFFSET_commentLength = 32;
    private static final int OFFSET_diskStart = 34;
    private static final int OFFSET_internalFileAttributes = 36;
    private static final int OFFSET_externalFileAttributes = 38;
    private static final int OFFSET_localRelativeOffset = 42;
    private static final int OFFSET_fileName = 46;

}
