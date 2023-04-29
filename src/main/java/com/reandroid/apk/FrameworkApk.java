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
import com.reandroid.archive.ByteInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.archive.InputSourceUtil;
import com.reandroid.archive2.Archive;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.arsc.util.FrameworkTable;
import com.reandroid.arsc.value.ValueType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

/*
 * Produces compressed framework apk by removing irrelevant files and entries,
 * basically it keeps only resources.arsc and AndroidManifest.xml
 */
public class FrameworkApk extends ApkModule{
    private final Object mLock = new Object();
    private int versionCode;
    private String versionName;
    private String packageName;
    private boolean mOptimizing;
    private boolean mDestroyed;
    public FrameworkApk(String moduleName, APKArchive apkArchive) {
        super(moduleName, apkArchive);
        super.setLoadDefaultFramework(false);
    }
    public FrameworkApk(APKArchive apkArchive) {
        this("framework", apkArchive);
    }

    public void destroy(){
        synchronized (mLock){
            this.versionCode = -1;
            this.versionName = "-1";
            this.packageName = "destroyed";
            super.destroy();
            this.mDestroyed = true;
        }
    }
    public boolean isDestroyed() {
        synchronized (mLock){
            if(!mDestroyed){
                return false;
            }
            if(hasTableBlock()){
                this.versionCode = 0;
                this.versionName = null;
                this.packageName = null;
                mDestroyed = false;
                return false;
            }
            return true;
        }
    }

    public int getVersionCode() {
        if(this.versionCode == 0){
            initValues();
        }
        return this.versionCode;
    }
    public String getVersionName() {
        if(this.versionName == null){
            initValues();
        }
        return this.versionName;
    }
    @Override
    public String getPackageName() {
        if(this.packageName == null){
            initValues();
        }
        return this.packageName;
    }
    @Override
    public void setPackageName(String packageName) {
        super.setPackageName(packageName);
        this.packageName = null;
    }
    private void initValues() {
        if(hasAndroidManifestBlock()){
            AndroidManifestBlock manifest = getAndroidManifestBlock();
            Integer code = manifest.getVersionCode();
            if(code!=null){
                this.versionCode = code;
            }
            if(this.versionName == null){
                this.versionName = manifest.getVersionName();
            }
            if(this.packageName == null){
                this.packageName = manifest.getPackageName();
            }
        }
        if(hasTableBlock()){
            FrameworkTable table = getTableBlock();
            if(this.versionCode == 0 && table.isOptimized()){
                int version = table.getVersionCode();
                if(version!=0){
                    versionCode = version;
                    if(this.versionName == null){
                        this.versionName = String.valueOf(version);
                    }
                }
            }
            if(this.packageName == null){
                PackageBlock packageBlock = table.pickOne();
                if(packageBlock!=null){
                    this.packageName = packageBlock.getName();
                }
            }
        }
    }
    @Override
    public void setManifest(AndroidManifestBlock manifestBlock){
        synchronized (mLock){
            super.setManifest(manifestBlock);
            this.versionCode = 0;
            this.versionName = null;
            this.packageName = null;
        }
    }
    @Override
    public void setTableBlock(TableBlock tableBlock){
        synchronized (mLock){
            super.setTableBlock(tableBlock);
            this.versionCode = 0;
            this.versionName = null;
            this.packageName = null;
        }
    }
    @Override
    public FrameworkTable getTableBlock() {
        return (FrameworkTable) super.getTableBlock();
    }
    @Override
    FrameworkTable loadTableBlock() throws IOException {
        APKArchive archive=getApkArchive();
        InputSource inputSource = archive.getInputSource(TableBlock.FILE_NAME);
        if(inputSource==null){
            throw new IOException("Entry not found: "+TableBlock.FILE_NAME);
        }
        InputStream inputStream = inputSource.openStream();
        FrameworkTable frameworkTable=FrameworkTable.load(inputStream);
        frameworkTable.setApkFile(this);

        BlockInputSource<FrameworkTable> blockInputSource=new BlockInputSource<>(inputSource.getName(), frameworkTable);
        blockInputSource.setMethod(inputSource.getMethod());
        blockInputSource.setSort(inputSource.getSort());
        archive.add(blockInputSource);
        return frameworkTable;
    }
    public void optimize(){
        synchronized (mLock){
            if(mOptimizing){
                return;
            }
            if(!hasTableBlock()){
                mOptimizing = false;
                return;
            }
            FrameworkTable frameworkTable = getTableBlock();
            if(frameworkTable.isOptimized()){
                mOptimizing = false;
                return;
            }
            FrameworkOptimizer optimizer = new FrameworkOptimizer(this);
            optimizer.optimize();
            mOptimizing = false;
        }
    }
    public String getName(){
        if(isDestroyed()){
            return "destroyed";
        }
        String pkg = getPackageName();
        if(pkg==null){
            return "";
        }
        return pkg + "-" + getVersionCode();
    }
    @Override
    public int hashCode(){
        return Objects.hash(getClass(), getName());
    }
    @Override
    public boolean equals(Object obj){
        if(obj==this){
            return true;
        }
        if(getClass()!=obj.getClass()){
            return false;
        }
        FrameworkApk other = (FrameworkApk) obj;
        return getName().equals(other.getName());
    }
    @Override
    public String toString(){
        return getName();
    }
    public static FrameworkApk loadApkFile(File apkFile) throws IOException {
        Archive archive = new Archive(apkFile);
        APKArchive apkArchive = new APKArchive(archive.mapEntrySource());
        return new FrameworkApk(apkArchive);
    }
    public static FrameworkApk loadApkFile(File apkFile, String moduleName) throws IOException {
        Archive archive = new Archive(apkFile);
        APKArchive apkArchive = new APKArchive(archive.mapEntrySource());
        return new FrameworkApk(moduleName, apkArchive);
    }
    public static boolean isFramework(ApkModule apkModule) {
        if(!apkModule.hasAndroidManifestBlock()){
            return false;
        }
        return isFramework(apkModule.getAndroidManifestBlock());
    }
    public static boolean isFramework(AndroidManifestBlock manifestBlock){
        ResXmlElement root = manifestBlock.getManifestElement();
        ResXmlAttribute attribute = root.getStartElement()
                .searchAttributeByName(AndroidManifestBlock.NAME_coreApp);
        if(attribute==null || attribute.getValueType()!= ValueType.INT_BOOLEAN){
            return false;
        }
        return attribute.getValueAsBoolean();
    }
    public static FrameworkApk loadApkBuffer(InputStream inputStream) throws IOException{
        return loadApkBuffer("framework", inputStream);
    }
    public static FrameworkApk loadApkBuffer(String moduleName, InputStream inputStream) throws IOException {
        APKArchive archive = new APKArchive();
        FrameworkApk frameworkApk = new FrameworkApk(moduleName, archive);
        Map<String, ByteInputSource> inputSourceMap = InputSourceUtil.mapInputStreamAsBuffer(inputStream);
        ByteInputSource source = inputSourceMap.get(TableBlock.FILE_NAME);
        FrameworkTable tableBlock = new FrameworkTable();
        if(source!=null){
            tableBlock.readBytes(source.openStream());
        }
        frameworkApk.setTableBlock(tableBlock);

        AndroidManifestBlock manifestBlock = new AndroidManifestBlock();
        source = inputSourceMap.get(AndroidManifestBlock.FILE_NAME);
        if(source!=null){
            manifestBlock.readBytes(source.openStream());
        }
        frameworkApk.setManifest(manifestBlock);
        archive.addAll(inputSourceMap.values());
        return frameworkApk;
    }
    public static void optimize(File in, File out, APKLogger apkLogger) throws IOException{
        FrameworkApk frameworkApk = FrameworkApk.loadApkFile(in);
        frameworkApk.setAPKLogger(apkLogger);
        frameworkApk.optimize();
        frameworkApk.writeApk(out);
    }
}
