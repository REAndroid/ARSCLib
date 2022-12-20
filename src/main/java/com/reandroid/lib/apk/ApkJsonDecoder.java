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

import com.reandroid.archive.InputSource;
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.lib.arsc.chunk.xml.ResXmlBlock;
import com.reandroid.lib.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ApkJsonDecoder {
    private final ApkModule apkModule;
    private final Set<String> decodedPaths;
    private final boolean splitTypes;
    public ApkJsonDecoder(ApkModule apkModule, boolean splitTypes){
        this.apkModule = apkModule;
        this.splitTypes = splitTypes;
        this.decodedPaths = new HashSet<>();
    }
    public ApkJsonDecoder(ApkModule apkModule){
        this(apkModule, false);
    }
    public File writeToDirectory(File dir) throws IOException {
        this.decodedPaths.clear();
        writeUncompressed(dir);
        writeManifest(dir);
        writeTable(dir);
        writeResourceIds(dir);
        //writePublicXml(dir);
        writeResources(dir);
        writeRootFiles(dir);
        return new File(dir, apkModule.getModuleName());
    }
    private void writeUncompressed(File dir) throws IOException {
        File file=toUncompressedJsonFile(dir);
        UncompressedFiles uncompressedFiles=new UncompressedFiles();
        uncompressedFiles.addCommonExtensions();
        uncompressedFiles.addPath(apkModule.getApkArchive());
        uncompressedFiles.toJson().write(file);
    }
    private void writeResources(File dir) throws IOException {
        for(ResFile resFile:apkModule.listResFiles()){
            writeResource(dir, resFile);
        }
    }
    private void writeResource(File dir, ResFile resFile) throws IOException {
        if(resFile.isBinaryXml()){
            writeResourceJson(dir, resFile);
        }
    }
    private void writeResourceJson(File dir, ResFile resFile) throws IOException {
        InputSource inputSource= resFile.getInputSource();
        String path=inputSource.getAlias();
        File file=toResJson(dir, path);
        ResXmlBlock resXmlBlock=new ResXmlBlock();
        resXmlBlock.readBytes(inputSource.openStream());
        JSONObject jsonObject=resXmlBlock.toJson();
        jsonObject.write(file);
        addDecoded(path);
    }
    private void writeRootFiles(File dir) throws IOException {
        for(InputSource inputSource:apkModule.getApkArchive().listInputSources()){
            writeRootFile(dir, inputSource);
        }
    }
    private void writeRootFile(File dir, InputSource inputSource) throws IOException {
        String path=inputSource.getAlias();
        if(hasDecoded(path)){
            return;
        }
        File file=toRootFile(dir, path);
        File parent=file.getParentFile();
        if(parent!=null && !parent.exists()){
            parent.mkdirs();
        }
        FileOutputStream outputStream=new FileOutputStream(file);
        inputSource.write(outputStream);
        outputStream.close();
        addDecoded(path);
    }
    private void writeTable(File dir) throws IOException {
        if(!splitTypes){
            writeTableSingle(dir);
            return;
        }
        writeTableSplit(dir);
    }
    private void writeTableSplit(File dir) throws IOException {
        if(!apkModule.hasTableBlock()){
            return;
        }
        TableBlock tableBlock = apkModule.getTableBlock();
        File splitDir= toJsonTableSplitDir(dir);
        TableBlockJson tableBlockJson=new TableBlockJson(tableBlock);
        tableBlockJson.writeJsonFiles(splitDir);
        addDecoded(TableBlock.FILE_NAME);
    }
    private void writeTableSingle(File dir) throws IOException {
        if(!apkModule.hasTableBlock()){
            return;
        }
        TableBlock tableBlock = apkModule.getTableBlock();
        File file= toJsonTableFile(dir);
        tableBlock.toJson().write(file);
        addDecoded(TableBlock.FILE_NAME);
    }
    private void writeResourceIds(File dir) throws IOException {
        if(!apkModule.hasTableBlock()){
            return;
        }
        TableBlock tableBlock = apkModule.getTableBlock();
        ResourceIds resourceIds=new ResourceIds();
        resourceIds.loadTableBlock(tableBlock);
        JSONObject jsonObject= resourceIds.toJson();
        File file=toResourceIds(dir);
        jsonObject.write(file);
    }
    private void writePublicXml(File dir) throws IOException {
        if(!apkModule.hasTableBlock()){
            return;
        }
        TableBlock tableBlock = apkModule.getTableBlock();
        ResourceIds resourceIds=new ResourceIds();
        resourceIds.loadTableBlock(tableBlock);
        File file=toResourceIdsXml(dir);
        resourceIds.writeXml(file);
    }
    private void writeManifest(File dir) throws IOException {
        if(!apkModule.hasAndroidManifestBlock()){
            return;
        }
        AndroidManifestBlock manifestBlock = apkModule.getAndroidManifestBlock();
        File file = toJsonManifestFile(dir);
        manifestBlock.toJson().write(file);
        addDecoded(AndroidManifestBlock.FILE_NAME);
    }
    private boolean hasDecoded(String path){
        return decodedPaths.contains(path);
    }
    private void addDecoded(String path){
        this.decodedPaths.add(path);
    }
    private File toJsonTableFile(File dir){
        File file=new File(dir, apkModule.getModuleName());
        String name = TableBlock.FILE_NAME + ApkUtil.JSON_FILE_EXTENSION;
        return new File(file, name);
    }
    private File toJsonTableSplitDir(File dir){
        File file=new File(dir, apkModule.getModuleName());
        return new File(file, ApkUtil.SPLIT_JSON_DIRECTORY);
    }
    private File toResourceIds(File dir){
        File file=new File(dir, apkModule.getModuleName());
        String name = ResourceIds.JSON_FILE_NAME;
        return new File(file, name);
    }
    private File toResourceIdsXml(File dir){
        File file=new File(dir, apkModule.getModuleName());
        String name = "public.xml";
        return new File(file, name);
    }
    private File toUncompressedJsonFile(File dir){
        File file = new File(dir, apkModule.getModuleName());
        return new File(file, UncompressedFiles.JSON_FILE);
    }
    private File toJsonManifestFile(File dir){
        File file=new File(dir, apkModule.getModuleName());
        String name = AndroidManifestBlock.FILE_NAME + ApkUtil.JSON_FILE_EXTENSION;
        return new File(file, name);
    }
    private File toResJson(File dir, String path){
        File file=new File(dir, apkModule.getModuleName());
        file=new File(file, ApkUtil.RES_JSON_NAME);
        path=path + ApkUtil.JSON_FILE_EXTENSION;
        path=path.replace('/', File.separatorChar);
        return new File(file, path);
    }
    private File toRootFile(File dir, String path){
        File file=new File(dir, apkModule.getModuleName());
        file=new File(file, ApkUtil.ROOT_NAME);
        path=path.replace('/', File.separatorChar);
        return new File(file, path);
    }
}
