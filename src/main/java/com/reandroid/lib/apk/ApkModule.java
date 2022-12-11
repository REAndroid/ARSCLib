package com.reandroid.lib.apk;

import com.reandroid.archive.*;
import com.reandroid.lib.arsc.array.PackageArray;
import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.lib.arsc.group.StringGroup;
import com.reandroid.lib.arsc.item.TableString;
import com.reandroid.lib.arsc.pool.TableStringPool;
import com.reandroid.lib.arsc.value.EntryBlock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;

public class ApkModule {
    private final String moduleName;
    private final APKArchive apkArchive;
    private boolean loadDefaultFramework = true;
    private TableBlock mTableBlock;
    private AndroidManifestBlock mManifestBlock;
    private final UncompressedFiles mUncompressedFiles;
    private APKLogger apkLogger;
    ApkModule(String moduleName, APKArchive apkArchive){
        this.moduleName=moduleName;
        this.apkArchive=apkArchive;
        this.mUncompressedFiles=new UncompressedFiles();
        this.mUncompressedFiles.addPath(apkArchive);
    }
    public boolean isBaseModule(){
        if(!hasAndroidManifestBlock()){
            return false;
        }
        AndroidManifestBlock manifestBlock;
        try {
            manifestBlock=getAndroidManifestBlock();
        } catch (IOException ignored) {
            return false;
        }
        return manifestBlock.getMainActivity()!=null;
    }
    public String getModuleName(){
        return moduleName;
    }
    public void writeApk(File file) throws IOException {
        writeApk(file, null);
    }
    public void writeApk(File file, WriteProgress progress) throws IOException {
        ZipArchive archive=new ZipArchive();
        archive.addAll(getApkArchive().listInputSources());
        UncompressedFiles uf=getUncompressedFiles();
        uf.apply(archive);
        int i=1;
        for(InputSource inputSource:archive.listInputSources()){
            if(inputSource.getSort()==0){
                inputSource.setSort(i);
                i++;
            }
        }
        InputSource manifest=archive.getInputSource(AndroidManifestBlock.FILE_NAME);
        if(manifest!=null){
            manifest.setSort(0);
        }
        ZipSerializer serializer=new ZipSerializer(archive.listInputSources(), false);
        serializer.setWriteProgress(progress);
        serializer.writeZip(file);
    }
    public UncompressedFiles getUncompressedFiles(){
        return mUncompressedFiles;
    }
    public void removeDir(String dirName){
        getApkArchive().removeDir(dirName);
    }
    public void validateResourcesDir() throws IOException {
        List<ResFile> resFileList = listResFiles();
        Set<String> existPaths=new HashSet<>();
        List<InputSource> sourceList = getApkArchive().listInputSources();
        for(InputSource inputSource:sourceList){
            existPaths.add(inputSource.getAlias());
        }
        for(ResFile resFile:resFileList){
            String path=resFile.getFilePath();
            String pathNew=resFile.validateTypeDirectoryName();
            if(pathNew==null || pathNew.equals(path)){
                continue;
            }
            if(existPaths.contains(pathNew)){
                continue;
            }
            existPaths.remove(path);
            existPaths.add(pathNew);
            resFile.setFilePath(pathNew);
            if(resFile.getInputSource().getMethod() == ZipEntry.STORED){
                getUncompressedFiles().replacePath(path, pathNew);
            }
            logVerbose("Dir validated: '"+path+"' -> '"+pathNew+"'");
        }
        TableStringPool stringPool= getTableBlock().getTableStringPool();
        stringPool.refreshUniqueIdMap();
        getTableBlock().refresh();
    }
    public void setResourcesRootDir(String dirName) throws IOException {
        List<ResFile> resFileList = listResFiles();
        Set<String> existPaths=new HashSet<>();
        List<InputSource> sourceList = getApkArchive().listInputSources();
        for(InputSource inputSource:sourceList){
            existPaths.add(inputSource.getAlias());
        }
        for(ResFile resFile:resFileList){
            String path=resFile.getFilePath();
            String pathNew=ApkUtil.replaceRootDir(path, dirName);
            if(existPaths.contains(pathNew)){
                continue;
            }
            existPaths.remove(path);
            existPaths.add(pathNew);
            resFile.setFilePath(pathNew);
            if(resFile.getInputSource().getMethod() == ZipEntry.STORED){
                getUncompressedFiles().replacePath(path, pathNew);
            }
            logVerbose("Root changed: '"+path+"' -> '"+pathNew+"'");
        }
        TableStringPool stringPool= getTableBlock().getTableStringPool();
        stringPool.refreshUniqueIdMap();
        getTableBlock().refresh();
    }
    public List<ResFile> listResFiles() throws IOException {
        List<ResFile> results=new ArrayList<>();
        TableBlock tableBlock=getTableBlock();
        TableStringPool stringPool= tableBlock.getTableStringPool();
        for(InputSource inputSource:getApkArchive().listInputSources()){
            String name=inputSource.getAlias();
            StringGroup<TableString> groupTableString = stringPool.get(name);
            if(groupTableString==null){
                continue;
            }
            for(TableString tableString:groupTableString.listItems()){
                List<EntryBlock> entryBlockList = tableString.listReferencedEntries();
                ResFile resFile=new ResFile(inputSource, entryBlockList);
                results.add(resFile);
            }
        }
        return results;
    }
    public String getPackageName() throws IOException {
        if(hasAndroidManifestBlock()){
            return getAndroidManifestBlock().getPackageName();
        }
        if(!hasTableBlock()){
            return null;
        }
        TableBlock tableBlock=getTableBlock();
        PackageArray pkgArray = tableBlock.getPackageArray();
        PackageBlock pkg = pkgArray.get(0);
        if(pkg==null){
            return null;
        }
        return pkg.getName();
    }
    public void setPackageName(String name) throws IOException {
        String old=getPackageName();
        if(hasAndroidManifestBlock()){
            getAndroidManifestBlock().setPackageName(name);
        }
        if(!hasTableBlock()){
            return;
        }
        TableBlock tableBlock=getTableBlock();
        PackageArray pkgArray = tableBlock.getPackageArray();
        for(PackageBlock pkg:pkgArray.listItems()){
            if(pkgArray.childesCount()==1){
                pkg.setName(name);
                continue;
            }
            String pkgName=pkg.getName();
            if(pkgName.startsWith(old)){
                pkgName=pkgName.replace(old, name);
                pkg.setName(pkgName);
            }
        }
    }
    public boolean hasAndroidManifestBlock(){
        return mManifestBlock!=null
                || getApkArchive().getInputSource(AndroidManifestBlock.FILE_NAME)!=null;
    }
    public AndroidManifestBlock getAndroidManifestBlock() throws IOException {
        if(mManifestBlock!=null){
            return mManifestBlock;
        }
        APKArchive archive=getApkArchive();
        InputSource inputSource = archive.getInputSource(AndroidManifestBlock.FILE_NAME);
        if(inputSource==null){
            throw new IOException("Entry not found: "+AndroidManifestBlock.FILE_NAME);
        }
        InputStream inputStream = inputSource.openStream();
        AndroidManifestBlock manifestBlock=AndroidManifestBlock.load(inputStream);
        inputStream.close();
        BlockInputSource<AndroidManifestBlock> blockInputSource=new BlockInputSource<>(inputSource.getName(),manifestBlock);
        blockInputSource.setSort(inputSource.getSort());
        blockInputSource.setMethod(inputSource.getMethod());
        archive.add(blockInputSource);
        mManifestBlock=manifestBlock;
        return mManifestBlock;
    }
    public boolean hasTableBlock(){
        return mTableBlock!=null
                || getApkArchive().getInputSource(TableBlock.FILE_NAME)!=null;
    }
    public TableBlock getTableBlock() throws IOException {
        if(mTableBlock!=null){
            return mTableBlock;
        }
        APKArchive archive=getApkArchive();
        InputSource inputSource = archive.getInputSource(TableBlock.FILE_NAME);
        if(inputSource==null){
            throw new IOException("Entry not found: "+TableBlock.FILE_NAME);
        }
        TableBlock tableBlock;
        if(inputSource instanceof SplitJsonTableInputSource){
            tableBlock=((SplitJsonTableInputSource)inputSource).getTableBlock();
        }else if(inputSource instanceof SingleJsonTableInputSource){
            tableBlock=((SingleJsonTableInputSource)inputSource).getTableBlock();
        }else {
            InputStream inputStream = inputSource.openStream();
            if(loadDefaultFramework){
                tableBlock=TableBlock.loadWithAndroidFramework(inputStream);
            }else {
                tableBlock=TableBlock.load(inputStream);
            }
            inputStream.close();
        }
        mTableBlock=tableBlock;
        BlockInputSource<TableBlock> blockInputSource=new BlockInputSource<>(inputSource.getName(),tableBlock);
        blockInputSource.setMethod(inputSource.getMethod());
        blockInputSource.setSort(inputSource.getSort());
        archive.add(blockInputSource);
        return mTableBlock;
    }
    public APKArchive getApkArchive() {
        return apkArchive;
    }
    public void setLoadDefaultFramework(boolean loadDefaultFramework) {
        this.loadDefaultFramework = loadDefaultFramework;
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
    public static ApkModule loadApkFile(File apkFile) throws IOException {
        return loadApkFile(apkFile, ApkUtil.DEF_MODULE_NAME);
    }
    public static ApkModule loadApkFile(File apkFile, String moduleName) throws IOException {
        APKArchive archive=APKArchive.loadZippedApk(apkFile);
        return new ApkModule(moduleName, archive);
    }
}
