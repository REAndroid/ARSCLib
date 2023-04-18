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

import com.reandroid.apk.xmldecoder.ResXmlDocumentSerializer;
import com.reandroid.archive.InputSource;
import com.reandroid.apk.xmldecoder.XMLBagDecoder;
import com.reandroid.apk.xmldecoder.XMLNamespaceValidator;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.decoder.ValueDecoder;
import com.reandroid.arsc.value.*;
import com.reandroid.common.EntryStore;
import com.reandroid.json.JSONObject;
import com.reandroid.xml.XMLAttribute;
import com.reandroid.xml.XMLDocument;
import com.reandroid.xml.XMLElement;
import com.reandroid.xml.XMLException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

 public class ApkModuleXmlDecoder {
    private final ApkModule apkModule;
    private final Map<Integer, Set<ResConfig>> decodedEntries;
    private XMLBagDecoder xmlBagDecoder;
    private final Set<String> mDecodedPaths;
    private ResXmlDocumentSerializer documentSerializer;
    private boolean useAndroidSerializer;
    public ApkModuleXmlDecoder(ApkModule apkModule){
        this.apkModule=apkModule;
        this.decodedEntries = new HashMap<>();
        this.mDecodedPaths = new HashSet<>();
        this.useAndroidSerializer = true;
    }
    public void setUseAndroidSerializer(boolean useAndroidSerializer) {
        this.useAndroidSerializer = useAndroidSerializer;
    }
    public void sanitizeFilePaths(){
        sanitizeFilePaths(false);
    }
    public void sanitizeFilePaths(boolean sanitizeResourceFiles){
        PathSanitizer sanitizer = new PathSanitizer(apkModule, sanitizeResourceFiles);
        sanitizer.sanitize();
    }
    public void decodeTo(File outDir)
            throws IOException, XMLException {
        this.decodedEntries.clear();
        logMessage("Decoding ...");
        decodeUncompressedFiles(outDir);
        apkModule.initializeAndroidFramework();
        TableBlock tableBlock=apkModule.getTableBlock();

        decodePackageInfo(outDir, tableBlock);

        xmlBagDecoder=new XMLBagDecoder(tableBlock);

        decodePublicXml(tableBlock, outDir);

        decodeAndroidManifest(outDir);

        addDecodedPath(TableBlock.FILE_NAME);

        logMessage("Decoding resource files ...");
        List<ResFile> resFileList=apkModule.listResFiles();
        for(ResFile resFile:resFileList){
            decodeResFile(outDir, resFile);
        }
        decodeValues(tableBlock, outDir, tableBlock);

        extractRootFiles(outDir);

        writePathMap(outDir);
    }
    private void writePathMap(File dir) throws IOException {
        PathMap pathMap = new PathMap();
        pathMap.add(apkModule.getApkArchive());
        File file = new File(dir, PathMap.JSON_FILE);
        pathMap.toJson().write(file);
    }
    private void decodePackageInfo(File outDir, TableBlock tableBlock) throws IOException {
        for(PackageBlock packageBlock:tableBlock.listPackages()){
            decodePackageInfo(outDir, packageBlock);
        }
    }
    private void decodePackageInfo(File outDir, PackageBlock packageBlock) throws IOException {
        File pkgDir = new File(outDir, getPackageDirName(packageBlock));
        File packageJsonFile = new File(pkgDir, PackageBlock.JSON_FILE_NAME);
        JSONObject jsonObject = packageBlock.toJson(false);
        jsonObject.write(packageJsonFile);
    }
    private void decodeUncompressedFiles(File outDir)
             throws IOException {
        File file=new File(outDir, UncompressedFiles.JSON_FILE);
        UncompressedFiles uncompressedFiles = apkModule.getUncompressedFiles();
        uncompressedFiles.toJson().write(file);
    }
    private void decodeResFile(File outDir, ResFile resFile)
            throws IOException{
        if(resFile.isBinaryXml()){
            decodeResXml(outDir, resFile);
        }else {
            decodeResRaw(outDir, resFile);
        }
        addDecodedPath(resFile.getFilePath());
    }
    private void decodeResRaw(File outDir, ResFile resFile)
            throws IOException {
        Entry entry = resFile.pickOne();
        PackageBlock packageBlock= entry.getPackageBlock();

        File pkgDir=new File(outDir, getPackageDirName(packageBlock));
        String alias = resFile.buildPath(ApkUtil.RES_DIR_NAME);
        String path = alias.replace('/', File.separatorChar);
        File file=new File(pkgDir, path);
        File dir=file.getParentFile();
        if(!dir.exists()){
            dir.mkdirs();
        }
        FileOutputStream outputStream=new FileOutputStream(file);
        resFile.getInputSource().write(outputStream);
        outputStream.close();
        resFile.setFilePath(alias);

        addDecodedEntry(entry);
    }
    private void decodeResXml(File outDir, ResFile resFile)
             throws IOException{
        Entry entry = resFile.pickOne();
        PackageBlock packageBlock = entry.getPackageBlock();

        File pkgDir = new File(outDir, getPackageDirName(packageBlock));
        String alias = resFile.buildPath(ApkUtil.RES_DIR_NAME);
        String path = alias.replace('/', File.separatorChar);
        path = path.replace('/', File.separatorChar);
        File file = new File(pkgDir, path);

        logVerbose("Decoding: " + path);
        serializeXml(packageBlock.getId(), resFile.getInputSource(), file);

        resFile.setFilePath(alias);
        addDecodedEntry(entry);
    }
    private ResXmlDocumentSerializer getDocumentSerializer(){
        if(documentSerializer == null){
            documentSerializer = new ResXmlDocumentSerializer(apkModule);
            documentSerializer.setValidateXmlNamespace(true);
        }
        return documentSerializer;
    }
    private TableBlock getTableBlock(){
        return apkModule.getTableBlock();
    }
    private void decodePublicXml(TableBlock tableBlock, File outDir)
             throws IOException{
        for(PackageBlock packageBlock:tableBlock.listPackages()){
            decodePublicXml(packageBlock, outDir);
        }
        if(tableBlock.getPackageArray().childesCount()==0){
            decodeEmptyTable(outDir);
        }
    }
    private void decodeEmptyTable(File outDir) throws IOException {
        logMessage("Decoding empty table ...");
        String pkgName = apkModule.getPackageName();
        if(pkgName==null){
            return;
        }
        File pkgDir = new File(outDir, "0-"+pkgName);
        File resDir = new File(pkgDir, ApkUtil.RES_DIR_NAME);
        File values = new File(resDir, "values");
        File pubXml = new File(values, ApkUtil.FILE_NAME_PUBLIC_XML);
        XMLDocument xmlDocument = new XMLDocument("resources");
        xmlDocument.save(pubXml, false);
    }
    private void decodePublicXml(PackageBlock packageBlock, File outDir)
             throws IOException {
        String packageDirName=getPackageDirName(packageBlock);
        logMessage("Decoding public.xml: "+packageDirName);
        File file=new File(outDir, packageDirName);
        file=new File(file, ApkUtil.RES_DIR_NAME);
        file=new File(file, "values");
        file=new File(file, ApkUtil.FILE_NAME_PUBLIC_XML);
        ResourceIds resourceIds=new ResourceIds();
        resourceIds.loadPackageBlock(packageBlock);
        resourceIds.writeXml(file);
    }
    private void decodeAndroidManifest(File outDir)
             throws IOException {
        if(!apkModule.hasAndroidManifestBlock()){
            logMessage("Don't have: "+ AndroidManifestBlock.FILE_NAME);
            return;
        }
        File file=new File(outDir, AndroidManifestBlock.FILE_NAME);
        logMessage("Decoding: "+file.getName());
        AndroidManifestBlock manifestBlock=apkModule.getAndroidManifestBlock();
        int currentPackageId = manifestBlock.guessCurrentPackageId();
        serializeXml(currentPackageId, manifestBlock, file);
        addDecodedPath(AndroidManifestBlock.FILE_NAME);
    }
    private void serializeXml(int currentPackageId, ResXmlDocument document, File outFile)
            throws IOException {
        XMLNamespaceValidator namespaceValidator = new XMLNamespaceValidator(document);
        namespaceValidator.validate();
        if(useAndroidSerializer){
            ResXmlDocumentSerializer serializer = getDocumentSerializer();
            if(currentPackageId != 0){
                serializer.getDecoder().setCurrentPackageId(currentPackageId);
            }
            try {
                serializer.write(document, outFile);
            } catch (XmlPullParserException ex) {
                throw new IOException("Error: "+outFile.getName(), ex);
            }
        }else {
            try {
                XMLDocument xmlDocument = document.decodeToXml(getTableBlock(), currentPackageId);
                xmlDocument.save(outFile, true);
            } catch (XMLException ex) {
                throw new IOException("Error: "+outFile.getName(), ex);
            }
        }
    }
    private void serializeXml(int currentPackageId, InputSource inputSource, File outFile)
             throws IOException {

         if(useAndroidSerializer){
             ResXmlDocumentSerializer serializer = getDocumentSerializer();
             if(currentPackageId != 0){
                 serializer.getDecoder().setCurrentPackageId(currentPackageId);
             }
             try {
                 serializer.write(inputSource, outFile);
             } catch (XmlPullParserException ex) {
                 throw new IOException("Error: "+outFile.getName(), ex);
             }
         }else {
             try {
                 ResXmlDocument document = apkModule.loadResXmlDocument(inputSource);
                 XMLNamespaceValidator namespaceValidator = new XMLNamespaceValidator(document);
                 namespaceValidator.validate();
                 XMLDocument xmlDocument = document.decodeToXml(getTableBlock(), currentPackageId);
                 xmlDocument.save(outFile, true);
             } catch (XMLException ex) {
                 throw new IOException("Error: "+outFile.getName(), ex);
             }
         }
     }
    private void addDecodedEntry(Entry entry){
        if(entry.isNull()){
            return;
        }
        int resourceId= entry.getResourceId();
        Set<ResConfig> resConfigSet=decodedEntries.get(resourceId);
        if(resConfigSet==null){
            resConfigSet=new HashSet<>();
            decodedEntries.put(resourceId, resConfigSet);
        }
        resConfigSet.add(entry.getResConfig());
    }
    private boolean containsDecodedEntry(Entry entry){
        Set<ResConfig> resConfigSet=decodedEntries.get(entry.getResourceId());
        if(resConfigSet==null){
            return false;
        }
        return resConfigSet.contains(entry.getResConfig());
    }
    private void decodeValues(EntryStore entryStore, File outDir, TableBlock tableBlock) throws IOException {
        for(PackageBlock packageBlock:tableBlock.listPackages()){
            decodeValues(entryStore, outDir, packageBlock);
        }
    }
    private void decodeValues(EntryStore entryStore, File outDir, PackageBlock packageBlock) throws IOException {
        logMessage("Decoding values: "
                +packageBlock.getIndex()
                +"-"+packageBlock.getName());

        packageBlock.sortTypes();

        for(SpecTypePair specTypePair: packageBlock.listAllSpecTypePair()){
            for(TypeBlock typeBlock:specTypePair.listTypeBlocks()){
                decodeValues(entryStore, outDir, typeBlock);
            }
        }
    }
    private void decodeValues(EntryStore entryStore, File outDir, TypeBlock typeBlock) throws IOException {
        XMLDocument xmlDocument = new XMLDocument("resources");
        XMLElement docElement = xmlDocument.getDocumentElement();
        for(Entry entry :typeBlock.listEntries(true)){
            if(containsDecodedEntry(entry)){
                continue;
            }
            docElement.addChild(decodeValue(entryStore, entry));
        }
        if(docElement.getChildesCount()==0){
            return;
        }
        File file=new File(outDir, getPackageDirName(typeBlock.getPackageBlock()));
        file=new File(file, ApkUtil.RES_DIR_NAME);
        file=new File(file, "values"+typeBlock.getQualifiers());
        String type=typeBlock.getTypeName();
        if(!type.endsWith("s")){
            type=type+"s";
        }
        file=new File(file, type+".xml");
        xmlDocument.save(file, false);
    }
    private XMLElement decodeValue(EntryStore entryStore, Entry entry){
        XMLElement element=new XMLElement(XmlHelper.toXMLTagName(entry.getTypeName()));
        int resourceId= entry.getResourceId();
        XMLAttribute attribute=new XMLAttribute("name", entry.getName());
        element.addAttribute(attribute);
        attribute.setNameId(resourceId);
        element.setResourceId(resourceId);
        if(!entry.isComplex()){
            ResValue resValue =(ResValue) entry.getTableEntry().getValue();
            if(resValue.getValueType()== ValueType.STRING){
                XmlHelper.setTextContent(element,
                        resValue.getDataAsPoolString());
            }else {
                String value = ValueDecoder.decodeEntryValue(entryStore,
                        entry.getPackageBlock(),
                        resValue.getValueType(),
                        resValue.getData());
                element.setTextContent(value);
            }
        }else {
            ResTableMapEntry mapEntry = (ResTableMapEntry) entry.getTableEntry();
            xmlBagDecoder.decode(mapEntry, element);
            return element;
        }
        return element;
    }
    private String getPackageDirName(PackageBlock packageBlock){
        String name = ApkUtil.sanitizeForFileName(packageBlock.getName());
        if(name==null){
            name="package";
        }
        return packageBlock.getIndex()+"-"+name;
    }
    private void extractRootFiles(File outDir) throws IOException {
        logMessage("Extracting root files");
        File rootDir = new File(outDir, "root");
        for(InputSource inputSource:apkModule.getApkArchive().listInputSources()){
            if(containsDecodedPath(inputSource.getAlias())){
                continue;
            }
            extractRootFiles(rootDir, inputSource);
            addDecodedPath(inputSource.getAlias());
        }
    }
    private void extractRootFiles(File rootDir, InputSource inputSource) throws IOException {
        String path=inputSource.getAlias();
        path=path.replace(File.separatorChar, '/');
        File file=new File(rootDir, path);
        File dir=file.getParentFile();
        if(!dir.exists()){
            dir.mkdirs();
        }
        FileOutputStream outputStream=new FileOutputStream(file);
        inputSource.write(outputStream);
        outputStream.close();
    }
    private boolean containsDecodedPath(String path){
        return mDecodedPaths.contains(path);
    }
    private void addDecodedPath(String path){
        mDecodedPaths.add(path);
    }

    private void logMessage(String msg) {
        APKLogger apkLogger=apkModule.getApkLogger();
        if(apkLogger!=null){
            apkLogger.logMessage(msg);
        }
    }
    private void logError(String msg, Throwable tr) {
        APKLogger apkLogger=apkModule.getApkLogger();
        if(apkLogger!=null){
            apkLogger.logError(msg, tr);
        }
    }
    private void logVerbose(String msg) {
        APKLogger apkLogger=apkModule.getApkLogger();
        if(apkLogger!=null){
            apkLogger.logVerbose(msg);
        }
    }
}
