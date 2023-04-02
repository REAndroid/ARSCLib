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

public class DataDescriptor extends ZipHeader{
    public DataDescriptor() {
        super(MIN_LENGTH, ZipSignature.DATA_DESCRIPTOR);
    }
    public long getCrc(){
        return getUnsignedLong(OFFSET_crc);
    }
    public void setCrc(long value){
        putInteger(OFFSET_crc, value);
    }
    public long getCompressedSize(){
        return getUnsignedLong(OFFSET_compressed_size);
    }
    public void setCompressedSize(long value){
        putInteger(OFFSET_compressed_size, value);
    }
    public long getSize(){
        return getUnsignedLong(OFFSET_size);
    }
    public void setSize(long value){
        putInteger(OFFSET_size, value);
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(getSignature());
        builder.append(", crc=").append(String.format("0x%08x", getCrc()));
        builder.append(", compressed=").append(getCompressedSize());
        builder.append(", size=").append(getSize());
        return builder.toString();
    }

    private static final int OFFSET_crc = 4;
    private static final int OFFSET_compressed_size = 8;
    private static final int OFFSET_size = 12;

    public static final int MIN_LENGTH = 16;
}
