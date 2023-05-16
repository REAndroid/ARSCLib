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

import com.reandroid.archive.APKArchive;
import com.reandroid.archive.FileInputSource;
import com.reandroid.apk.xmlencoder.RESEncoder;
import com.reandroid.archive2.block.ApkSignatureBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.json.JSONArray;
import com.reandroid.xml.XMLException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class ApkModuleXmlEncoder {
    private final RESEncoder resEncoder;
    public ApkModuleXmlEncoder(){
        this.resEncoder = new RESEncoder();
    }
    public ApkModuleXmlEncoder(ApkModule module, TableBlock tableBlock){
        this.resEncoder = new RESEncoder(module, tableBlock);
    }
    public void scanDirectory(File mainDirectory) throws IOException, XMLException {
        loadUncompressedFiles(mainDirectory);
        resEncoder.scanDirectory(mainDirectory);
        File rootDir=new File(mainDirectory, "root");
        scanRootDir(rootDir);
        restorePathMap(mainDirectory);
        restoreSignatures(mainDirectory);
        sortFiles();
        TableStringPool tableStringPool = getApkModule().getTableBlock().getTableStringPool();
        tableStringPool.removeUnusedStrings();
    }
    private void restoreSignatures(File dir) throws IOException {
        File sigDir = new File(dir, ApkUtil.SIGNATURE_DIR_NAME);
        if(!sigDir.isDirectory()){
            return;
        }
        ApkModule apkModule = getApkModule();
        apkModule.logMessage("Loading signatures ...");
        ApkSignatureBlock signatureBlock = new ApkSignatureBlock();
        signatureBlock.scanSplitFiles(sigDir);
        apkModule.setApkSignatureBlock(signatureBlock);
    }
    private void restorePathMap(File dir) throws IOException{
        File file = new File(dir, PathMap.JSON_FILE);
        if(!file.isFile()){
            return;
        }
        PathMap pathMap = new PathMap();
        JSONArray jsonArray = new JSONArray(file);
        pathMap.fromJson(jsonArray);
        pathMap.restore(getApkModule());
    }
    public ApkModule getApkModule(){
        return resEncoder.getApkModule();
    }

    private void scanRootDir(File rootDir){
        APKArchive archive=getApkModule().getApkArchive();
        List<File> rootFileList=ApkUtil.recursiveFiles(rootDir);
        for(File file:rootFileList){
            String path=ApkUtil.toArchivePath(rootDir, file);
            FileInputSource inputSource=new FileInputSource(file, path);
            archive.add(inputSource);
        }
    }
    private void sortFiles(){
        APKArchive archive = getApkModule().getApkArchive();
        archive.autoSortApkFiles();
    }
    private void loadUncompressedFiles(File mainDirectory) throws IOException {
        File file=new File(mainDirectory, UncompressedFiles.JSON_FILE);
        UncompressedFiles uncompressedFiles = getApkModule().getUncompressedFiles();
        uncompressedFiles.fromJson(file);
    }
    public void setApkLogger(APKLogger apkLogger) {
        this.resEncoder.setAPKLogger(apkLogger);
    }
}
