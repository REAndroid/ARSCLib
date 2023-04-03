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

import com.reandroid.arsc.item.ByteArray;

import java.io.IOException;
import java.io.InputStream;

public class SignatureFooter extends ZipBlock{
    public SignatureFooter() {
        super(MIN_SIZE);
        setMagic(APK_SIG_BLOCK_MAGIC);
    }
    @Override
    public int readBytes(InputStream inputStream) throws IOException {
        setBytesLength(MIN_SIZE, false);
        byte[] bytes = getBytesInternal();
        return inputStream.read(bytes, 0, bytes.length);
    }
    public long getSigBlockSizeInFooter(){
        return getLong(OFFSET_size);
    }
    public void setSigBlockSizeInFooter(long size){
        putLong(OFFSET_size, size);
    }
    public byte[] getMagic() {
        return getBytes(OFFSET_magic, APK_SIG_BLOCK_MAGIC.length, false);
    }
    public void setMagic(byte[] magic){
        putBytes(magic, 0, OFFSET_magic, magic.length);
    }
    public boolean isValid(){
        return getSigBlockSizeInFooter() > MIN_SIZE
                && ByteArray.equals(APK_SIG_BLOCK_MAGIC, getMagic());
    }
    @Override
    public String toString(){
        return getSigBlockSizeInFooter() + " ["+new String(getMagic())+"]";
    }

    public static final int MIN_SIZE = 24;

    private static final int OFFSET_size = 0;
    private static final int OFFSET_magic = 8;

    private static final byte[] APK_SIG_BLOCK_MAGIC =
            new byte[]{'A', 'P', 'K', ' ', 'S', 'i', 'g', ' ', 'B', 'l', 'o', 'c', 'k', ' ', '4', '2'};
}
