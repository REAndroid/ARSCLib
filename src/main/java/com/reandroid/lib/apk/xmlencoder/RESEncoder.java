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
 package com.reandroid.lib.apk.xmlencoder;

 import com.reandroid.lib.apk.APKLogger;
 import com.reandroid.lib.apk.ApkUtil;
 import com.reandroid.lib.apk.ResourceIds;
 import com.reandroid.lib.arsc.chunk.PackageBlock;
 import com.reandroid.lib.arsc.chunk.TableBlock;
 import com.reandroid.lib.arsc.chunk.xml.AndroidManifestBlock;
 import com.reandroid.lib.common.Frameworks;
 import com.reandroid.xml.XMLDocument;
 import com.reandroid.xml.XMLException;

 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;

 public class RESEncoder {
     private APKLogger apkLogger;
     private final TableBlock tableBlock;
     private final Set<File> parsedFiles;
     public RESEncoder(){
         this.tableBlock = new TableBlock();
         this.parsedFiles = new HashSet<>();
     }
     public TableBlock getTableBlock(){
         return tableBlock;
     }
     public void scanDirectory(File rootDir) throws IOException, XMLException {
         List<File> pubXmlFileList = searchPublicXmlFiles(rootDir);
         if(pubXmlFileList.size()==0){
             throw new IOException("No .*/values/"
                     +ApkUtil.FILE_NAME_PUBLIC_XML+"  file found in '"+rootDir);
         }
         for(File pubXmlFile:pubXmlFileList){
             EncodeMaterials encodeMaterials = loadPublicXml(pubXmlFile);
             addParsedFiles(pubXmlFile);
             File resDir=toResDirectory(pubXmlFile);
             encodeResDir(encodeMaterials, resDir);
         }
         tableBlock.refresh();
     }
     private EncodeMaterials loadPublicXml(File pubXmlFile) throws IOException, XMLException {
         ResourceIds resourceIds=new ResourceIds();
         resourceIds.fromXml(pubXmlFile);

         List<ResourceIds.Table.Package> pkgList = resourceIds
                 .getTable().listPackages();
         if(pkgList.size()!=1){
             throw new IOException("Package count should be 1, count="
                     +pkgList.size()+", in file: "+pubXmlFile);
         }

         XMLDocument manifestDocument = XMLDocument.load(toAndroidManifest(pubXmlFile));
         String packageName = manifestDocument
                 .getDocumentElement().getAttributeValue("package");

         ResourceIds.Table.Package pkgResourceIds = pkgList.get(0);

         PackageCreator packageCreator = new PackageCreator();
         packageCreator.setPackageName(packageName);

         PackageBlock packageBlock = packageCreator.createNew(tableBlock, pkgResourceIds);

         return new EncodeMaterials()
                 .addFramework(Frameworks.getAndroid())
                 .setCurrentPackage(packageBlock)
                 .setPackageIds(pkgResourceIds)
                 .setAPKLogger(apkLogger);
     }
     private void encodeResDir(EncodeMaterials materials, File resDir) throws XMLException {

         List<File> valuesDirList = listValuesDir(resDir);
         for(File valuesDir:valuesDirList){
             encodeValuesDir(materials, valuesDir);
         }
     }
     private void encodeValuesDir(EncodeMaterials materials, File valuesDir) throws XMLException {
         ValuesEncoder valuesEncoder = new ValuesEncoder(materials);
         List<File> xmlFiles = ApkUtil.listFiles(valuesDir, ".xml");
         EncodeUtil.sortValuesXml(xmlFiles);
         for(File file:xmlFiles){
             if(isAlreadyParsed(file)){
                 continue;
             }
             addParsedFiles(file);
             valuesEncoder.encodeValuesXml(file);
         }
     }
     private File toAndroidManifest(File pubXmlFile){
         File resDirectory = toResDirectory(pubXmlFile);
         File packageDirectory = resDirectory.getParentFile();
         File root = packageDirectory.getParentFile();
         return new File(root, AndroidManifestBlock.FILE_NAME);
     }
     private File toResDirectory(File pubXmlFile){
         return pubXmlFile
                 .getParentFile()
                 .getParentFile();
     }
     private List<File> listValuesDir(File resDir){
         List<File> results=new ArrayList<>();
         File def=new File(resDir, "values");
         results.add(def);
         File[] dirList=resDir.listFiles();
         if(dirList!=null){
             for(File dir:dirList){
                 if(def.equals(dir) || !dir.isDirectory()){
                     continue;
                 }
                 if(dir.getName().startsWith("values-")){
                     results.add(dir);
                 }
             }
         }
         return results;
     }
     private List<File> searchPublicXmlFiles(File rootDir){
         logVerbose("Searching public.xml: "+rootDir);
         List<File> xmlFiles = ApkUtil.recursiveFiles(rootDir, ApkUtil.FILE_NAME_PUBLIC_XML);
         List<File> results = new ArrayList<>();
         for(File file:xmlFiles){
             if(toAndroidManifest(file).isFile()){
                 results.add(file);
             }
         }
         return results;
     }
     private boolean isAlreadyParsed(File file){
         return parsedFiles.contains(file);
     }
     private void addParsedFiles(File file){
         parsedFiles.add(file);
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
