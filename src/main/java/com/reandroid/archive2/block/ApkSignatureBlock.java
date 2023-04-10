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


import java.io.File;
import java.io.IOException;
import java.util.List;

public class ApkSignatureBlock extends LengthPrefixedList<SignatureInfo> {
    public ApkSignatureBlock(SignatureFooter signatureFooter){
        super(true);
        setBottomBlock(signatureFooter);
    }
    public List<SignatureInfo> getSignatures(){
        return super.getElements();
    }
    public int countSignatures(){
        return super.getElementsCount();
    }
    public SignatureInfo getSignature(SignatureId signatureId){
        for(SignatureInfo signatureInfo:getSignatures()){
            if(signatureInfo.getId().equals(signatureId)){
                return signatureInfo;
            }
        }
        return null;
    }
    public SignatureFooter getSignatureFooter(){
        return (SignatureFooter) getBottomBlock();
    }
    @Override
    public SignatureInfo newInstance() {
        return new SignatureInfo();
    }
    @Override
    protected void onRefreshed(){
        SignatureFooter footer = getSignatureFooter();
        footer.updateMagic();
        super.onRefreshed();
        footer.setSignatureSize(getDataSize());
    }
    // for test
    public void writeSignatureData(File dir) throws IOException{
        for(SignatureInfo signatureInfo:getElements()){
            signatureInfo.writeToDir(dir);
        }
    }

    private static final long CONTENT_DIGESTED_CHUNK_MAX_SIZE_BYTES = 1024 * 1024;
    public static final int ANDROID_COMMON_PAGE_ALIGNMENT_BYTES = 4096;

    private static final byte[] APK_SIGNING_BLOCK_MAGIC = new byte[] {
                    0x41, 0x50, 0x4b, 0x20, 0x53, 0x69, 0x67, 0x20,
                    0x42, 0x6c, 0x6f, 0x63, 0x6b, 0x20, 0x34, 0x32,
            };

    public static final int VERSION_SOURCE_STAMP = 0;
    public static final int VERSION_JAR_SIGNATURE_SCHEME = 1;
    public static final int VERSION_APK_SIGNATURE_SCHEME_V2 = 2;
    public static final int VERSION_APK_SIGNATURE_SCHEME_V3 = 3;
    public static final int VERSION_APK_SIGNATURE_SCHEME_V31 = 31;
    public static final int VERSION_APK_SIGNATURE_SCHEME_V4 = 4;

}
