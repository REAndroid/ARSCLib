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

import com.reandroid.apk.xmlencoder.EncodeMaterials;
import com.reandroid.apk.xmlencoder.XMLEncodeSource;
import com.reandroid.apk.xmlencoder.XMLTableBlockEncoder;
import com.reandroid.archive.FileInputSource;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.value.Entry;
import com.reandroid.xml.source.XMLFileSource;
import com.reandroid.xml.source.XMLSource;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ApkModuleXmlEncoder extends ApkModuleEncoder{
    private final XMLTableBlockEncoder tableBlockEncoder;
    private EncodeMaterials mEncodeMaterials;
    public ApkModuleXmlEncoder(){
        this.tableBlockEncoder = new XMLTableBlockEncoder();
    }
    public ApkModuleXmlEncoder(ApkModule module, TableBlock tableBlock){
        this.tableBlockEncoder = new XMLTableBlockEncoder(module, tableBlock);
    }

    @Override
    public void buildResources(File mainDirectory) throws IOException{
        encodeManifestBinary(mainDirectory);
        buildTableBlock(mainDirectory);
        encodeManifestXml(mainDirectory);
        scanResFilesDirectory(mainDirectory);
    }
    @Override
    public ApkModule getApkModule(){
        return tableBlockEncoder.getApkModule();
    }

    private void buildTableBlock(File mainDirectory) throws IOException {
        XMLTableBlockEncoder tableBlockEncoder = this.tableBlockEncoder;
        tableBlockEncoder.setEncodeMaterials(getEncodeMaterials());
        tableBlockEncoder.scanMainDirectory(mainDirectory);
    }
    private void encodeManifestBinary(File mainDirectory) {
        File file = new File(mainDirectory, AndroidManifestBlock.FILE_NAME_BIN);
        if(!file.isFile()){
            return;
        }
        logMessage("Encode binary manifest: " + file.getName());
        FileInputSource inputSource =
                new FileInputSource(file, AndroidManifestBlock.FILE_NAME_BIN);
        getApkModule().add(inputSource);
    }
    private void encodeManifestXml(File mainDirectory) {
        if(mainDirectory == null){
            return;
        }
        File file = new File(mainDirectory, AndroidManifestBlock.FILE_NAME);
        if(!file.isFile()){
            return;
        }
        logMessage("Encode manifest: " + file.getName());
        EncodeMaterials encodeMaterials = getEncodeMaterials();
        TableBlock tableBlock = getApkModule().getTableBlock();
        PackageBlock packageBlock = encodeMaterials.pickMainPackageBlock(tableBlock);
        if(packageBlock != null){
            encodeMaterials.setCurrentPackage(packageBlock);
            tableBlock.setCurrentPackage(packageBlock);
        }
        XMLSource xmlSource =
                new XMLFileSource(AndroidManifestBlock.FILE_NAME, file);
        XMLEncodeSource xmlEncodeSource =
                new XMLEncodeSource(encodeMaterials, xmlSource);
        getApkModule().add(xmlEncodeSource);
    }
    private void scanResFilesDirectory(File mainDirectory) {
        File resFilesDirectory = new File(mainDirectory, TableBlock.RES_FILES_DIRECTORY_NAME);
        if(!resFilesDirectory.isDirectory()){
            return;
        }
        logMessage("Searching files: " + resFilesDirectory.getName());
        List<File> fileList = ApkUtil.recursiveFiles(resFilesDirectory);
        for(File file : fileList){
            encodeResFile(resFilesDirectory, file);
        }
    }
    private void encodeResFile(File resFilesDirectory, File file){
        String path = ApkUtil.toArchivePath(resFilesDirectory, file);
        logVerbose(path);
        Entry entry = getEntry(path);
        EncodeMaterials encodeMaterials = getEncodeMaterials();
        if(file.getName().endsWith(".xml")){
            XMLSource xmlSource =
                    new XMLFileSource(path, file);
            XMLEncodeSource xmlEncodeSource =
                    new XMLEncodeSource(encodeMaterials, xmlSource);
            xmlEncodeSource.setEntry(entry);
            getApkModule().add(xmlEncodeSource);
        }else {
            FileInputSource inputSource = new FileInputSource(file, path);
            getApkModule().add(inputSource);
        }
    }
    private Entry getEntry(String path){
        List<Entry> entryList = getApkModule().listReferencedEntries(path);
        if(entryList.size() > 0){
            return entryList.get(0);
        }
        return null;
    }
    public EncodeMaterials getEncodeMaterials(){
        EncodeMaterials materials = this.mEncodeMaterials;
        if(materials == null){
            materials = new EncodeMaterials();
            materials.setAPKLogger(getApkLogger());
            this.mEncodeMaterials = materials;
        }
        return materials;
    }
    public void setEncodeMaterials(EncodeMaterials encodeMaterials){
        this.mEncodeMaterials = encodeMaterials;
    }
    @Override
    public void setApkLogger(APKLogger apkLogger) {
        super.setApkLogger(apkLogger);
        this.tableBlockEncoder.setApkLogger(apkLogger);
    }
}
