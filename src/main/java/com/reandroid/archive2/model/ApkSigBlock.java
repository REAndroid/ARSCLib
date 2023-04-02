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

import java.io.IOException;

public class ApkSigBlock {
    public ApkSigBlock(){
    }

    //TODO : implement all
    public void parse(byte[] bytes) throws IOException {
        int offset = findSignature(bytes);
        if(offset<0){
            return;
        }
        offset += APK_SIGNING_BLOCK_MAGIC.length;
        int length = bytes.length - offset;
    }
    private int findSignature(byte[] bytes){
        for(int i=0;i<bytes.length;i++){
            if (matchesSignature(bytes, i)){
                return i;
            }
        }
        return -1;
    }
    private boolean matchesSignature(byte[] bytes, int offset){
        byte[] sig = APK_SIGNING_BLOCK_MAGIC;
        int length = bytes.length - offset;
        if (length<sig.length){
            return false;
        }
        for(int i=0;i<sig.length;i++){
            int j = offset+i;
            if(sig[i] != bytes[j]){
                return false;
            }
            if(bytes[j] ==0){
                return false;
            }
        }
        return true;
    }

    private static final long CONTENT_DIGESTED_CHUNK_MAX_SIZE_BYTES = 1024 * 1024;
    public static final int ANDROID_COMMON_PAGE_ALIGNMENT_BYTES = 4096;

    private static final byte[] APK_SIGNING_BLOCK_MAGIC = new byte[] {
                    0x41, 0x50, 0x4b, 0x20, 0x53, 0x69, 0x67, 0x20,
                    0x42, 0x6c, 0x6f, 0x63, 0x6b, 0x20, 0x34, 0x32,
            };
    public static final int VERITY_PADDING_BLOCK_ID = 0x42726577;

    public static final int VERSION_SOURCE_STAMP = 0;
    public static final int VERSION_JAR_SIGNATURE_SCHEME = 1;
    public static final int VERSION_APK_SIGNATURE_SCHEME_V2 = 2;
    public static final int VERSION_APK_SIGNATURE_SCHEME_V3 = 3;
    public static final int VERSION_APK_SIGNATURE_SCHEME_V31 = 31;
    public static final int VERSION_APK_SIGNATURE_SCHEME_V4 = 4;

}
