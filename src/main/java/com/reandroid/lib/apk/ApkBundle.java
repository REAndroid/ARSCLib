package com.reandroid.lib.apk;

import com.reandroid.archive.APKArchive;
import com.reandroid.lib.arsc.chunk.TableBlock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class ApkBundle {
    private final Map<String, ApkModule> mModulesMap;
    private APKLogger apkLogger;
    public ApkBundle(){
        this.mModulesMap=new HashMap<>();
    }

    public ApkModule mergeModules() throws IOException {
        List<ApkModule> moduleList=getApkModuleList();
        if(moduleList.size()==0){
            throw new FileNotFoundException("Nothing to merge, empty modules");
        }
        ApkModule result=new ApkModule("merged", new APKArchive());
        result.setAPKLogger(apkLogger);
        ApkModule base=getBaseModule();
        if(base==null){
            base=getLargestTableModule();
        }
        result.merge(base);
        for(ApkModule module:moduleList){
            if(module==base){
                continue;
            }
            result.merge(module);
        }
        if(result.hasTableBlock()){
            TableBlock tableBlock=result.getTableBlock();
            tableBlock.sortPackages();
            tableBlock.refresh();
        }
        result.sortApkFiles();
        return result;
    }
    private ApkModule getLargestTableModule() throws IOException {
        ApkModule apkModule=null;
        int chunkSize=0;
        for(ApkModule module:getApkModuleList()){
            if(!module.hasTableBlock()){
                continue;
            }
            TableBlock tableBlock=module.getTableBlock();
            int size=tableBlock.getHeaderBlock().getChunkSize();
            if(apkModule==null || size>chunkSize){
                chunkSize=size;
                apkModule=module;
            }
        }
        return apkModule;
    }
    public ApkModule getBaseModule(){
        for(ApkModule module:getApkModuleList()){
            if(module.isBaseModule()){
                return module;
            }
        }
        return null;
    }
    public List<ApkModule> getApkModuleList(){
        return new ArrayList<>(mModulesMap.values());
    }
    public void loadApkDirectory(File dir) throws IOException {
        if(!dir.isDirectory()){
            throw new FileNotFoundException("No such directory: "+dir);
        }
        List<File> apkList=ApkUtil.listFiles(dir, ".apk");
        if(apkList.size()==0){
            throw new FileNotFoundException("No '*.apk' files in directory: "+dir);
        }
        logMessage("Found apk files: "+apkList.size());
        for(File file:apkList){
            logVerbose("Loading: "+file.getName());
            String name=ApkUtil.toModuleName(file);
            ApkModule module=ApkModule.loadApkFile(file, name);
            module.setAPKLogger(apkLogger);
            addModule(module);
        }
    }
    public void addModule(ApkModule apkModule){
        String name=apkModule.getModuleName();
        mModulesMap.remove(name);
        mModulesMap.put(name, apkModule);
    }
    public boolean containsApkModule(String moduleName){
        return mModulesMap.containsKey(moduleName);
    }
    public ApkModule removeApkModule(String moduleName){
        return mModulesMap.remove(moduleName);
    }
    public ApkModule getApkModule(String moduleName){
        return mModulesMap.get(moduleName);
    }
    public List<String> listModuleNames(){
        return new ArrayList<>(mModulesMap.keySet());
    }
    public int countModules(){
        return mModulesMap.size();
    }
    public Collection<ApkModule> getModules(){
        return mModulesMap.values();
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
