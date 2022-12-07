package com.reandroid.lib.apk;

import com.reandroid.archive.APKArchive;
import com.reandroid.archive.FileInputSource;
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.arsc.chunk.xml.AndroidManifestBlock;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ApkJsonEncoder {
    private APKArchive apkArchive;
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
        loadUncompressed(module, moduleDir);
        return module;
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
        apkArchive.add(inputSource);
        return true;
    }
    private void scanTableSingleJson(File moduleDir) {
        File file=toJsonTableFile(moduleDir);
        if(!file.isFile()){
            return;
        }
        SingleJsonTableInputSource inputSource= SingleJsonTableInputSource.fromFile(moduleDir, file);
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
}
