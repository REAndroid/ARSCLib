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
import com.reandroid.archive2.block.ApkSignatureBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class ApkJsonEncoder {
    private APKArchive apkArchive;
    private APKLogger apkLogger;
    public ApkJsonEncoder(){
    }
    public ApkModule scanDirectory(File moduleDir){
        this.apkArchive=new APKArchive();
        String moduleName=moduleDir.getName();
        scanManifest(moduleDir);
        scanTable(moduleDir);
        scanResJsonDirs(moduleDir);
        scanRootDirs(moduleDir);
        ApkModule module=new ApkModule(moduleName, apkArchive);
        module.setLoadDefaultFramework(false);
        module.setAPKLogger(apkLogger);
        loadUncompressed(module, moduleDir);
        //applyResourceId(module, moduleDir);
        restorePathMap(moduleDir, module);
        restoreSignatures(moduleDir, module);
        return module;
    }
    private void restoreSignatures(File dir, ApkModule apkModule){
        File sigDir = new File(dir, ApkUtil.SIGNATURE_DIR_NAME);
        if(!sigDir.isDirectory()){
            return;
        }
        logMessage("Loading signatures ...");
        ApkSignatureBlock signatureBlock = new ApkSignatureBlock();
        try {
            signatureBlock.scanSplitFiles(sigDir);
            apkModule.setApkSignatureBlock(signatureBlock);
        } catch (IOException exception){
            logError("Failed to load signatures: ", exception);
        }
    }
    private void restorePathMap(File dir, ApkModule apkModule){
        File file = new File(dir, PathMap.JSON_FILE);
        if(!file.isFile()){
            return;
        }
        logMessage("Restoring file path ...");
        PathMap pathMap = new PathMap();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException exception) {
            logError("Failed to load path-map", exception);
            return;
        }
        JSONArray jsonArray = new JSONArray(inputStream);
        pathMap.fromJson(jsonArray);
        pathMap.restore(apkModule);
    }
    private void applyResourceId(ApkModule apkModule, File moduleDir) {
        if(!apkModule.hasTableBlock()){
            return;
        }
        File pubXml=toResourceIdsXml(moduleDir);
        if(!pubXml.isFile()){
            return;
        }
        ResourceIds resourceIds=new ResourceIds();
        try {
            resourceIds.fromXml(pubXml);
            resourceIds.applyTo(apkModule.getTableBlock());
        } catch (IOException exception) {
            throw new IllegalArgumentException(exception.getMessage());
        }
    }
    private void loadUncompressed(ApkModule module, File moduleDir){
        File jsonFile=toUncompressedJsonFile(moduleDir);
        UncompressedFiles uf= module.getUncompressedFiles();
        try {
            uf.fromJson(jsonFile);
        } catch (IOException ignored) {
        }
    }
    private void scanRootDirs(File moduleDir){
        File rootDir=toRootDir(moduleDir);
        List<File> jsonFileList=ApkUtil.recursiveFiles(rootDir);
        for(File file:jsonFileList){
            scanRootFile(rootDir, file);
        }
    }
    private void scanRootFile(File rootDir, File file){
        String path=ApkUtil.toArchivePath(rootDir, file);
        FileInputSource inputSource=new FileInputSource(file, path);
        apkArchive.add(inputSource);
    }
    private void scanResJsonDirs(File moduleDir){
        File resJsonDir=toResJsonDir(moduleDir);
        List<File> jsonFileList=ApkUtil.recursiveFiles(resJsonDir);
        for(File file:jsonFileList){
            scanResJsonFile(resJsonDir, file);
        }
    }
    private void scanResJsonFile(File resJsonDir, File file){
        JsonXmlInputSource inputSource=JsonXmlInputSource.fromFile(resJsonDir, file);
        apkArchive.add(inputSource);
    }
    private void scanManifest(File moduleDir){
        File file=toJsonManifestFile(moduleDir);
        if(!file.isFile()){
            return;
        }
        JsonManifestInputSource inputSource=JsonManifestInputSource.fromFile(moduleDir, file);
        inputSource.setAPKLogger(apkLogger);
        apkArchive.add(inputSource);
    }
    private void scanTable(File moduleDir) {
        boolean splitFound=scanTableSplitJson(moduleDir);
        if(splitFound){
            return;
        }
        scanTableSingleJson(moduleDir);
    }
    private boolean scanTableSplitJson(File moduleDir) {
        File dir=toJsonTableSplitDir(moduleDir);
        if(!dir.isDirectory()){
            return false;
        }
        SplitJsonTableInputSource inputSource=new SplitJsonTableInputSource(dir);
        inputSource.setAPKLogger(apkLogger);
        apkArchive.add(inputSource);
        return true;
    }
    private void scanTableSingleJson(File moduleDir) {
        File file=toJsonTableFile(moduleDir);
        if(!file.isFile()){
            return;
        }
        SingleJsonTableInputSource inputSource= SingleJsonTableInputSource.fromFile(moduleDir, file);
        inputSource.setAPKLogger(apkLogger);
        apkArchive.add(inputSource);
    }
    private File toJsonTableFile(File dir){
        String name = TableBlock.FILE_NAME + ApkUtil.JSON_FILE_EXTENSION;
        return new File(dir, name);
    }
    private File toJsonManifestFile(File dir){
        String name = AndroidManifestBlock.FILE_NAME + ApkUtil.JSON_FILE_EXTENSION;
        return new File(dir, name);
    }
    private File toResourceIdsXml(File dir){
        String name = "public.xml";
        return new File(dir, name);
    }
    private File toUncompressedJsonFile(File dir){
        return new File(dir, UncompressedFiles.JSON_FILE);
    }
    private File toJsonTableSplitDir(File dir){
        return new File(dir, ApkUtil.SPLIT_JSON_DIRECTORY);
    }
    private File toResJsonDir(File dir){
        return new File(dir, ApkUtil.RES_JSON_NAME);
    }
    private File toRootDir(File dir){
        return new File(dir, ApkUtil.ROOT_NAME);
    }

    public void setAPKLogger(APKLogger logger) {
        this.apkLogger = logger;
    }
    private void logMessage(String msg) {
        if(apkLogger!=null){
            apkLogger.logMessage(msg);
        }
    }
    private void logError(String msg, Throwable tr) {
        if(apkLogger!=null){
            apkLogger.logError(msg, tr);
        }
    }
    private void logVerbose(String msg) {
        if(apkLogger!=null){
            apkLogger.logVerbose(msg);
        }
    }
}
