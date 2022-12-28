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
package com.reandroid.lib.apk;

import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.lib.arsc.chunk.xml.ResXmlBlock;
import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.common.EntryStore;
import com.reandroid.lib.common.Frameworks;
import com.reandroid.lib.common.TableEntryStore;
import com.reandroid.xml.XMLDocument;
import com.reandroid.xml.XMLException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ApkModuleXmlDecoder {
    private final ApkModule apkModule;
    public ApkModuleXmlDecoder(ApkModule apkModule){
        this.apkModule=apkModule;
    }
    public void decodeTo(File outDir)
            throws IOException, XMLException {
        TableEntryStore entryStore=new TableEntryStore();
        entryStore.add(Frameworks.getAndroid());
        entryStore.add(apkModule.getTableBlock());
        decodeAndroidManifest(entryStore, outDir);
        logMessage("Decoding resource files ...");
        List<ResFile> resFileList=apkModule.listResFiles();
        for(ResFile resFile:resFileList){
            decodeResFile(entryStore, outDir, resFile);
        }
    }
    private void decodeResFile(EntryStore entryStore, File outDir, ResFile resFile)
            throws IOException, XMLException {
        if(resFile.isBinaryXml()){
            decodeResXml(entryStore, outDir, resFile);
        }else {
            decodeResRaw(outDir, resFile);
        }
    }
    private void decodeResRaw(File outDir, ResFile resFile)
            throws IOException {
        EntryBlock entryBlock=resFile.pickOne();
        PackageBlock packageBlock=entryBlock.getPackageBlock();

        File pkgDir=new File(outDir, packageBlock.getName());
        File resDir=new File(pkgDir, ApkUtil.RES_DIR_NAME);
        String path=resFile.buildPath();
        path=path.replace('/', File.separatorChar);
        File file=new File(resDir, path);
        File dir=file.getParentFile();
        if(!dir.exists()){
            dir.mkdirs();
        }

        FileOutputStream outputStream=new FileOutputStream(file);
        resFile.getInputSource().write(outputStream);
        outputStream.close();
    }
    private void decodeResXml(EntryStore entryStore, File outDir, ResFile resFile)
            throws IOException, XMLException{
        EntryBlock entryBlock=resFile.pickOne();
        PackageBlock packageBlock=entryBlock.getPackageBlock();
        ResXmlBlock resXmlBlock=new ResXmlBlock();
        resXmlBlock.readBytes(resFile.getInputSource().openStream());

        File pkgDir=new File(outDir, packageBlock.getName());
        File resDir=new File(pkgDir, ApkUtil.RES_DIR_NAME);
        String path=resFile.buildPath();
        path=path.replace('/', File.separatorChar);
        File file=new File(resDir, path);

        logVerbose("Decoding: "+path);
        XMLDocument xmlDocument=resXmlBlock.decodeToXml(entryStore, packageBlock.getId());
        xmlDocument.save(file, true);
    }
    private void decodeAndroidManifest(EntryStore entryStore, File outDir)
            throws IOException, XMLException {
        if(!apkModule.hasAndroidManifestBlock()){
            logMessage("Don't have: "+ AndroidManifestBlock.FILE_NAME);
            return;
        }
        File file=new File(outDir, AndroidManifestBlock.FILE_NAME);
        logMessage("Decoding: "+file.getName());
        AndroidManifestBlock manifestBlock=apkModule.getAndroidManifestBlock();
        int currentPackageId= manifestBlock.guessCurrentPackageId();
        XMLDocument xmlDocument=manifestBlock.decodeToXml(entryStore, currentPackageId);
        xmlDocument.save(file, true);
    }
    private void logMessage(String msg) {
        APKLogger apkLogger=apkModule.getApkLogger();
        if(apkLogger!=null){
            apkLogger.logMessage(msg);
        }
    }
    private void logError(String msg, Throwable tr) {
        APKLogger apkLogger=apkModule.getApkLogger();
        if(apkLogger!=null){
            apkLogger.logError(msg, tr);
        }
    }
    private void logVerbose(String msg) {
        APKLogger apkLogger=apkModule.getApkLogger();
        if(apkLogger!=null){
            apkLogger.logVerbose(msg);
        }
    }
}
