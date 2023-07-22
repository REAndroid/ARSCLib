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
package com.reandroid.apk;

import com.reandroid.archive.FileInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.archive.ZipEntryMap;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;

import java.io.File;

public class ApkModuleRawEncoder extends ApkModuleEncoder {
    private final ApkModule apkModule;
    public ApkModuleRawEncoder(){
        this.apkModule = new ApkModule("encoded_raw", new ZipEntryMap());
    }
    @Override
    public void buildResources(File mainDirectory) {
        addTableBlock(mainDirectory);
    }
    @Override
    public ApkModule getApkModule() {
        return apkModule;
    }
    @Override
    public void encodeBinaryManifest(File mainDirectory){
        File file = new File(mainDirectory, AndroidManifestBlock.FILE_NAME_BIN);
        if(!file.isFile()){
            file = new File(mainDirectory, AndroidManifestBlock.FILE_NAME);
            if(!file.isFile() || !AndroidManifestBlock.isResXmlBlock(file)){
                logMessage("Warn: File not found: " + AndroidManifestBlock.FILE_NAME_BIN);
                return;
            }
        }
        logMessage("Loaded binary manifest: " + file.getName());
        FileInputSource inputSource = new FileInputSource(file, AndroidManifestBlock.FILE_NAME);
        getApkModule().add(inputSource);
    }
    private void addTableBlock(File mainDirectory){
        File file = new File(mainDirectory, TableBlock.FILE_NAME);
        if(!file.isFile()){
            logMessage("Warn: File not found: " + TableBlock.FILE_NAME);
            return;
        }
        InputSource inputSource = new FileInputSource(file, TableBlock.FILE_NAME);
        getApkModule().add(inputSource);
    }
    @Override
    void refreshTable(){
        if(getApkModule().getLoadedTableBlock() != null){
            super.refreshTable();
        }
    }
}
