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

import com.reandroid.archive.APKArchive;
import com.reandroid.archive.InputSource;
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.lib.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.lib.arsc.chunk.xml.ResXmlElement;
import com.reandroid.lib.arsc.util.FrameworkTable;
import com.reandroid.lib.arsc.value.ValueType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

 /*
  * Produces compressed framework apk by removing unnecessary files and entries,
  * basically it keeps only resources.arsc and AndroidManifest.xml
  */
public class FrameworkApk extends ApkModule{
     public FrameworkApk(String moduleName, APKArchive apkArchive) {
         super(moduleName, apkArchive);
     }
     public FrameworkApk(APKArchive apkArchive) {
         this("framework", apkArchive);
     }
     @Override
     public APKArchive getApkArchive() {
         APKArchive archive=super.getApkArchive();
         clearFiles(archive);
         return archive;
     }
     private void clearFiles(APKArchive archive){
         if(archive.entriesCount()==2){
             return;
         }
         InputSource tableSource= archive.getInputSource(TableBlock.FILE_NAME);
         InputSource manifestSource= archive.getInputSource(AndroidManifestBlock.FILE_NAME);
         archive.clear();
         archive.add(tableSource);
         archive.add(manifestSource);
     }
     public FrameworkTable getTableBlock() throws IOException {
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
         if(hasAndroidManifestBlock()){
             optimizeTable(frameworkTable);
         }
         BlockInputSource<FrameworkTable> blockInputSource=new BlockInputSource<>(inputSource.getName(), frameworkTable);
         blockInputSource.setMethod(inputSource.getMethod());
         blockInputSource.setSort(inputSource.getSort());
         archive.add(blockInputSource);
         return frameworkTable;
     }
     private void optimizeTable(FrameworkTable table) throws IOException {
         if(table.isOptimized()){
             return;
         }
         int prev=table.countBytes();
         logMessage("Optimizing ...");
         AndroidManifestBlock manifestBlock = getAndroidManifestBlock();
         String version=String.valueOf(manifestBlock.getVersionCode());
         String name=manifestBlock.getPackageName();
         table.optimize(name, version);
         long diff=prev - table.countBytes();
         long percent=(diff*100L)/prev;
         logMessage("Optimized: "+percent+" %");
     }
     public static FrameworkApk loadApkFile(File apkFile) throws IOException {
         APKArchive archive=APKArchive.loadZippedApk(apkFile);
         return new FrameworkApk(archive);
     }
     public static FrameworkApk loadApkFile(File apkFile, String moduleName) throws IOException {
         APKArchive archive=APKArchive.loadZippedApk(apkFile);
         return new FrameworkApk(moduleName, archive);
     }
     public static boolean isFramework(ApkModule apkModule) throws IOException {
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
}
