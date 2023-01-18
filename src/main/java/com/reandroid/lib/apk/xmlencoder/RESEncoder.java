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

 import com.reandroid.archive.APKArchive;
 import com.reandroid.lib.apk.*;
 import com.reandroid.lib.arsc.chunk.PackageBlock;
 import com.reandroid.lib.arsc.chunk.TableBlock;
 import com.reandroid.lib.arsc.chunk.xml.AndroidManifestBlock;
 import com.reandroid.lib.common.Frameworks;
 import com.reandroid.xml.XMLDocument;
 import com.reandroid.xml.XMLException;
 import com.reandroid.xml.source.XMLFileSource;
 import com.reandroid.xml.source.XMLSource;

 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 import java.util.zip.ZipEntry;

 public class RESEncoder {
     private APKLogger apkLogger;
     private final TableBlock tableBlock;
     private final Set<File> parsedFiles = new HashSet<>();
     private final ApkModule apkModule;
     public RESEncoder(){
         this(new ApkModule("encoded",
                 new APKArchive()), new TableBlock());
     }
     public RESEncoder(ApkModule module, TableBlock block){
         this.apkModule = module;
         this.tableBlock = block;
         if(!module.hasTableBlock()){
             BlockInputSource<TableBlock> inputSource=
                     new BlockInputSource<>(TableBlock.FILE_NAME, block);
             inputSource.setMethod(ZipEntry.STORED);
             this.apkModule.getUncompressedFiles().addPath(inputSource);
             this.apkModule.getApkArchive().add(inputSource);
         }
     }
     public TableBlock getTableBlock(){
         return tableBlock;
     }
     public ApkModule getApkModule(){
         return apkModule;
     }
     public void scanDirectory(File mainDir) throws IOException, XMLException {
         scanResourceFiles(mainDir);
     }
     private void scanResourceFiles(File mainDir) throws IOException, XMLException {
         List<File> pubXmlFileList = searchPublicXmlFiles(mainDir);
         if(pubXmlFileList.size()==0){
             throw new IOException("No .*/values/"
                     +ApkUtil.FILE_NAME_PUBLIC_XML+"  file found in '"+mainDir);
         }
         preloadStringPool(pubXmlFileList);

         EncodeMaterials encodeMaterials=new EncodeMaterials();

         Map<File, ResourceIds.Table.Package> map =
                 initializeEncodeMaterials(pubXmlFileList, encodeMaterials);

         Map<File, PackageBlock> packageBlockMap=new HashMap<>();

         for(File pubXmlFile:pubXmlFileList){
             ResourceIds.Table.Package pkgResourceIds=map.get(pubXmlFile);
             addParsedFiles(pubXmlFile);

             PackageBlock packageBlock = createPackage(pkgResourceIds, pubXmlFile);
             encodeMaterials.setCurrentPackage(packageBlock);
             packageBlockMap.put(pubXmlFile, packageBlock);

             ValuesEncoder valuesEncoder = new ValuesEncoder(encodeMaterials);
             File fileIds = toId(pubXmlFile);
             if(fileIds.isFile()){
                 valuesEncoder.encodeValuesXml(fileIds);
                 packageBlock.sortTypes();
                 packageBlock.refresh();
                 addParsedFiles(fileIds);
             }
             File fileAttrs = toAttr(pubXmlFile);
             if(fileAttrs.isFile()){
                 valuesEncoder.encodeValuesXml(fileAttrs);
                 packageBlock.sortTypes();
                 packageBlock.refresh();
                 addParsedFiles(fileAttrs);
             }
         }
         for(File pubXmlFile:pubXmlFileList){
             ResourceIds.Table.Package pkgResourceIds=map.get(pubXmlFile);
             addParsedFiles(pubXmlFile);

             PackageBlock packageBlock=packageBlockMap.get(pubXmlFile);

             if(packageBlock==null){
                 packageBlock = createPackage(pkgResourceIds, pubXmlFile);
             }
             encodeMaterials.setCurrentPackage(packageBlock);

             File resDir=toResDirectory(pubXmlFile);
             encodeResDir(encodeMaterials, resDir);
             FilePathEncoder filePathEncoder = new FilePathEncoder(encodeMaterials);
             filePathEncoder.setApkArchive(getApkModule().getApkArchive());
             filePathEncoder.setUncompressedFiles(getApkModule().getUncompressedFiles());
             filePathEncoder.encodeResDir(resDir);

             packageBlock.sortTypes();
             packageBlock.refresh();

             File manifestFile=toAndroidManifest(pubXmlFile);
             XMLSource xmlSource =
                     new XMLFileSource(AndroidManifestBlock.FILE_NAME, manifestFile);
             XMLEncodeSource xmlEncodeSource =
                     new XMLEncodeSource(encodeMaterials, xmlSource);

             getApkModule().getApkArchive().add(xmlEncodeSource);
         }
         tableBlock.refresh();
     }
     private PackageBlock createPackage(ResourceIds.Table.Package pkgResourceIds
             , File pubXmlFile){
         PackageCreator packageCreator = new PackageCreator();
         packageCreator.setPackageName(pkgResourceIds.name);
         packageCreator.setAPKLogger(apkLogger);
         packageCreator.setPackageDirectory(toPackageDirectory(pubXmlFile));
         return packageCreator.createNew(this.tableBlock, pkgResourceIds);
     }
     private void preloadStringPool(List<File> pubXmlFileList){
         logMessage("Loading string pool ...");
         ValuesStringPoolBuilder poolBuilder=new ValuesStringPoolBuilder();
         for(File pubXml:pubXmlFileList){
             File resDir=toResDirectory(pubXml);
             List<File> valuesDirList = listValuesDir(resDir);
             for(File dir:valuesDirList){
                 logVerbose(poolBuilder.size()+" building pool: "+dir.getName());
                 poolBuilder.scanValuesDirectory(dir);
             }
         }
         poolBuilder.addTo(tableBlock.getTableStringPool());
     }

     private Map<File, ResourceIds.Table.Package> initializeEncodeMaterials(
             List<File> pubXmlFileList, EncodeMaterials encodeMaterials)
             throws IOException, XMLException {

         Map<File, ResourceIds.Table.Package> results = new HashMap<>();

         String packageName=null;
         for(File pubXmlFile:pubXmlFileList){
             if(packageName==null){
                 packageName=readManifestPackageName(toAndroidManifest(pubXmlFile));
             }
             ResourceIds resourceIds=new ResourceIds();
             resourceIds.fromXml(pubXmlFile);
             ResourceIds.Table.Package pkg = resourceIds.getTable()
                     .listPackages().get(0);
             if(pkg.name==null){
                 pkg.name=packageName;
             }
             encodeMaterials.addPackageIds(pkg);
             results.put(pubXmlFile, pkg);
         }

         encodeMaterials.addFramework(Frameworks.getAndroid())
                 .setAPKLogger(apkLogger);
         return results;
     }
     private String readManifestPackageName(File manifestFile) throws XMLException {
         XMLDocument manifestDocument = XMLDocument.load(manifestFile);
         return manifestDocument
                 .getDocumentElement().getAttributeValue("package");
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
     private File toPackageDirectory(File pubXmlFile){
         return toResDirectory(pubXmlFile)
                 .getParentFile();
     }
     private File toResDirectory(File pubXmlFile){
         return pubXmlFile
                 .getParentFile()
                 .getParentFile();
     }
     private File toId(File pubXmlFile){
         return new File(pubXmlFile.getParentFile(), "ids.xml");
     }
     private File toAttr(File pubXmlFile){
         return new File(pubXmlFile.getParentFile(), "attrs.xml");
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
     private List<File> searchPublicXmlFiles(File mainDir){
         logVerbose("Searching public.xml: "+mainDir);
         List<File> dirList=ApkUtil.listDirectories(mainDir);
         List<File> xmlFiles = new ArrayList<>();
         for(File dir:dirList){
             if(dir.getName().equals("root")){
                 continue;
             }
             xmlFiles.addAll(
                     ApkUtil.recursiveFiles(dir, ApkUtil.FILE_NAME_PUBLIC_XML));
         }
         List<File> results = new ArrayList<>();
         for(File file:xmlFiles){
             if(toAndroidManifest(file).isFile()){
                 results.add(file);
             }
         }
         EncodeUtil.sortPublicXml(results);
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
         this.apkModule.setAPKLogger(logger);
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
