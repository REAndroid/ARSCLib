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

import com.reandroid.archive2.block.ApkSignature;
import com.reandroid.archive2.block.LongBlock;
import com.reandroid.archive2.block.SignatureFooter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ApkSigBlock {
    private final SignatureFooter signatureFooter;
    private final LongBlock mSize;
    private final List<ApkSignature> mSignatures;
    public ApkSigBlock(SignatureFooter signatureFooter){
        this.signatureFooter = signatureFooter;
        this.mSize = new LongBlock();
        this.mSignatures = new ArrayList<>();
    }

    public LongBlock getTotalSize(){
        return mSize;
    }
    public List<ApkSignature> getSignatures() {
        return mSignatures;
    }

    public void readBytes(InputStream inputStream) throws IOException {
        mSize.readBytes(inputStream);
        ApkSignature apkSignature;
        while ((apkSignature = readNext(inputStream))!=null){
            mSignatures.add(apkSignature);
        }
    }
    private ApkSignature readNext(InputStream inputStream) throws IOException {
        ApkSignature apkSignature = new ApkSignature();
        apkSignature.readBytes(inputStream);
        if(apkSignature.isValid()){
            return apkSignature;
        }
        return null;
    }
    public void writeSigData(File dir) throws IOException{
        for(ApkSignature apkSignature:mSignatures){
            apkSignature.writeToDir(dir);
        }
    }
    @Override
    public String toString(){
        return "size=" + getTotalSize() + ", count=" + getSignatures().size();
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
