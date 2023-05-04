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

public class EndRecord extends ZipHeader{
    public EndRecord() {
        super(MIN_LENGTH, ZipSignature.END_RECORD);
    }
    public int getNumberOfDisk(){
        return getShortUnsigned(OFFSET_numberOfDisk);
    }
    public void setNumberOfDisk(int value){
        putShort(OFFSET_numberOfDisk, value);
    }
    public int getCentralDirectoryStartDisk(){
        return getShortUnsigned(OFFSET_centralDirectoryStartDisk);
    }
    public void setCentralDirectoryStartDisk(int value){
        putShort(OFFSET_centralDirectoryStartDisk, value);
    }
    public int getNumberOfDirectories(){
        return getShortUnsigned(OFFSET_numberOfDirectories);
    }
    public void setNumberOfDirectories(int value){
        putShort(OFFSET_numberOfDirectories, value);
    }
    public int getTotalNumberOfDirectories(){
        return getShortUnsigned(OFFSET_totalNumberOfDirectories);
    }
    public void setTotalNumberOfDirectories(int value){
        putShort(OFFSET_totalNumberOfDirectories, value);
    }
    public long getLengthOfCentralDirectory(){
        return getIntegerUnsigned(OFFSET_lengthOfCentralDirectory);
    }
    public void setLengthOfCentralDirectory(long value){
        putInteger(OFFSET_lengthOfCentralDirectory, value);
    }
    public long getOffsetOfCentralDirectory(){
        return getIntegerUnsigned(OFFSET_offsetOfCentralDirectory);
    }
    public void setOffsetOfCentralDirectory(int value){
        putInteger(OFFSET_offsetOfCentralDirectory, value);
    }
    public int getLastShort(){
        return getShortUnsigned(OFFSET_lastShort);
    }
    public void getLastShort(int value){
        putShort(OFFSET_lastShort, value);
    }


    @Override
    public String toString(){
        if(countBytes()<getMinByteLength()){
            return "Invalid";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(getSignature());
        builder.append(", disks=").append(getNumberOfDisk());
        builder.append(", start disk=").append(getCentralDirectoryStartDisk());
        builder.append(", dirs=").append(getNumberOfDirectories());
        builder.append(", total dirs=").append(getTotalNumberOfDirectories());
        builder.append(", length=").append(getLengthOfCentralDirectory());
        builder.append(", offset=").append(getOffsetOfCentralDirectory());
        builder.append(", last=").append(HexUtil.toHex8(getLastShort()));
        return builder.toString();
    }

    private static final int OFFSET_numberOfDisk = 4;
    private static final int OFFSET_centralDirectoryStartDisk = 6;
    private static final int OFFSET_numberOfDirectories = 8;
    private static final int OFFSET_totalNumberOfDirectories = 10;
    private static final int OFFSET_lengthOfCentralDirectory = 12;
    private static final int OFFSET_offsetOfCentralDirectory = 16;
    private static final int OFFSET_lastShort = 20;

    public static final int MIN_LENGTH = 22;
    public static final int MAX_LENGTH = 0xffff + 22;
}
