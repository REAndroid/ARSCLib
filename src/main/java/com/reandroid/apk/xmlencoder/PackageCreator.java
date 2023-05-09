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
package com.reandroid.apk.xmlencoder;

import com.reandroid.apk.APKLogger;
import com.reandroid.apk.ResourceIds;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.pool.TypeStringPool;
import com.reandroid.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class PackageCreator {
    private List<String> mSpecNames;
    private String mPackageName;
    private int mPackageId;
    private File packageDirectory;
    private APKLogger apkLogger;
    public PackageCreator(){
    }

    public void setPackageDirectory(File packageDirectory) {
        this.packageDirectory = packageDirectory;
    }
    public void setPackageName(String name){
        this.mPackageName=name;
    }
    public PackageBlock createNew(TableBlock tableBlock, ResourceIds.Table.Package pkgResourceIds){
        loadNames(pkgResourceIds);
        PackageBlock packageBlock = tableBlock.getPackageArray().createNext();
        packageBlock.setName(mPackageName);
        packageBlock.setId(mPackageId);

        loadPackageInfoJson(packageBlock);
        this.mPackageName = packageBlock.getName();

        packageBlock.getSpecStringPool()
                .addStrings(mSpecNames);

        initTypeStringPool(packageBlock, pkgResourceIds);

        return packageBlock;
    }
    private void loadPackageInfoJson(PackageBlock packageBlock){
        File dir = this.packageDirectory;
        if(dir==null || !dir.isDirectory()){
            return;
        }
        String simplePath = dir.getName() + File.separator
                + PackageBlock.JSON_FILE_NAME;
        logMessage("Loading: " + simplePath);
        File file = new File(dir, PackageBlock.JSON_FILE_NAME);
        if(!file.isFile()){
            logMessage("W: File not found, this could be decompiled using old version: '"
                    + simplePath+"'");
            return;
        }
        JSONObject jsonObject;
        try {
            FileInputStream inputStream = new FileInputStream(file);
            jsonObject = new JSONObject(inputStream);
            inputStream.close();
        } catch (IOException ex) {
            logMessage("Error loading: '" + simplePath
                    + "', "+ex.getMessage());
            return;
        }
        packageBlock.fromJson(jsonObject);
        logMessage("OK: " + simplePath);
    }
    private void initTypeStringPool(PackageBlock packageBlock,
                                    ResourceIds.Table.Package pkgResourceIds){

        TypeStringPool typeStringPool = packageBlock.getTypeStringPool();

        for(ResourceIds.Table.Package.Type type:pkgResourceIds.listTypes()){
            typeStringPool.getOrCreate(type.getIdInt(), type.getName());
        }
    }
    private void loadNames(ResourceIds.Table.Package pkg){
        this.mSpecNames = new ArrayList<>();
        if(pkg.name!=null){
            this.mPackageName=pkg.name;
        }
        if(this.mPackageName==null){
            this.mPackageName = EncodeUtil.NULL_PACKAGE_NAME;
        }
        this.mPackageId=pkg.getIdInt();
        for(ResourceIds.Table.Package.Type.Entry entry:pkg.listEntries()){
            mSpecNames.add(entry.getName());
        }
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
