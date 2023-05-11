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
import com.reandroid.archive2.block.ApkSignatureBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.pool.builder.StringPoolMerger;

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
        ApkModule result = new ApkModule(generateMergedModuleName(), new APKArchive());
        result.setAPKLogger(apkLogger);
        result.setLoadDefaultFramework(false);

        mergeStringPools(result);

        ApkModule base=getBaseModule();
        if(base==null){
            base=getLargestTableModule();
        }
        result.merge(base);
        ApkSignatureBlock signatureBlock = null;
        for(ApkModule module:moduleList){
            ApkSignatureBlock asb = module.getApkSignatureBlock();
            if(module==base){
                if(asb != null){
                    signatureBlock = asb;
                }
                continue;
            }
            if(signatureBlock == null){
                signatureBlock = asb;
            }
            result.merge(module);
        }

        result.setApkSignatureBlock(signatureBlock);

        if(result.hasTableBlock()){
            TableBlock tableBlock=result.getTableBlock();
            tableBlock.sortPackages();
            tableBlock.refresh();
        }
        result.getApkArchive().autoSortApkFiles();
        return result;
    }
    private void mergeStringPools(ApkModule mergedModule) throws IOException {
        if(!hasOneTableBlock() || mergedModule.hasTableBlock()){
            return;
        }
        logMessage("Merging string pools ... ");
        TableBlock createdTable = new TableBlock();
        BlockInputSource<TableBlock> inputSource=
                new BlockInputSource<>(TableBlock.FILE_NAME, createdTable);
        mergedModule.getApkArchive().add(inputSource);

        StringPoolMerger poolMerger = new StringPoolMerger();

        for(ApkModule apkModule:getModules()){
            if(!apkModule.hasTableBlock()){
                continue;
            }
            TableStringPool stringPool = apkModule.getVolatileTableStringPool();
            poolMerger.add(stringPool);
        }

        poolMerger.mergeTo(createdTable.getTableStringPool());

        logMessage("Merged string pools="+poolMerger.getMergedPools()
                +", style="+poolMerger.getMergedStyleStrings()
                +", strings="+poolMerger.getMergedStrings());
    }
    private String generateMergedModuleName(){
        Set<String> moduleNames=mModulesMap.keySet();
        String merged="merged";
        int i=1;
        String name=merged;
        while (moduleNames.contains(name)){
            name=merged+"_"+i;
            i++;
        }
        return name;
    }
    private ApkModule getLargestTableModule(){
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
    public void loadApkDirectory(File dir) throws IOException{
        loadApkDirectory(dir, false);
    }
    public void loadApkDirectory(File dir, boolean recursive) throws IOException {
        if(!dir.isDirectory()){
            throw new FileNotFoundException("No such directory: "+dir);
        }
        List<File> apkList;
        if(recursive){
            apkList = ApkUtil.recursiveFiles(dir, ".apk");
        }else {
            apkList = ApkUtil.listFiles(dir, ".apk");
        }
        if(apkList.size()==0){
            throw new FileNotFoundException("No '*.apk' files in directory: "+dir);
        }
        logMessage("Found apk files: "+apkList.size());
        for(File file:apkList){
            logVerbose("Loading: "+file.getName());
            String name = ApkUtil.toModuleName(file);
            ApkModule module = ApkModule.loadApkFile(file, name);
            module.setAPKLogger(apkLogger);
            addModule(module);
        }
    }
    public void addModule(ApkModule apkModule){
        apkModule.setLoadDefaultFramework(false);
        String name = apkModule.getModuleName();
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
    private boolean hasOneTableBlock(){
        for(ApkModule apkModule:getModules()){
            if(apkModule.hasTableBlock()){
                return true;
            }
        }
        return false;
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
