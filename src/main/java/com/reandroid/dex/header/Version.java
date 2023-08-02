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
package com.reandroid.dex.header;

import com.reandroid.arsc.item.ByteArray;

public class Version extends HeaderPiece {
    public Version(){
        super();
        super.set(DEFAULT_BYTES.clone());
    }
    public int getVersionAsInteger(){
        byte[] bytes = getBytesInternal();
        if(bytes == null || bytes.length != 4){
            return -1;
        }
        return ((bytes[0] & 0xff) - 0x30) * 100
                + ((bytes[1] & 0xff) - 0x30) * 10
                + ((bytes[2] & 0xff) - 0x30);
    }
    public void setVersionAsInteger(int version){
        setSize(4);
        byte[] bytes = getBytesInternal();
        if(version < 0 || version > 999){
            version = 0;
        }
        int i = version / 100;
        bytes[0] = (byte)((i & 0xff) + 0x30);
        version = version - i * 100;
        i = version / 10;
        bytes[1] = (byte)((i & 0xff) + 0x30);
        version = version - i * 10;
        i = version;
        bytes[2] = (byte)((i & 0xff) + 0x30);
        bytes[3] = 0;
    }
    public void resetDefault(){
        super.set(DEFAULT_BYTES.clone());
    }
    public boolean isDefault(){
        return ByteArray.equals(getBytesInternal(), DEFAULT_BYTES);
    }
    @Override
    public String toString(){
        int version = getVersionAsInteger();
        if(version <= 0){
            return super.toString();
        }
        return String.valueOf(version);
    }

    public static final byte[] DEFAULT_BYTES = new byte[]{(byte)'0', (byte)'3', (byte)'5', (byte)0x00};

}
