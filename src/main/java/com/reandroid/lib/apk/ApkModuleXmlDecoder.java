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

import com.reandroid.archive.InputSource;
import com.reandroid.lib.apk.xmldecoder.XMLBagDecoder;
import com.reandroid.lib.apk.xmldecoder.XMLNamespaceValidator;
import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.arsc.chunk.TypeBlock;
import com.reandroid.lib.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.lib.arsc.chunk.xml.ResXmlBlock;
import com.reandroid.lib.arsc.container.SpecTypePair;
import com.reandroid.lib.arsc.decoder.ValueDecoder;
import com.reandroid.lib.arsc.value.*;
import com.reandroid.lib.common.EntryStore;
import com.reandroid.lib.common.Frameworks;
import com.reandroid.lib.common.TableEntryStore;
import com.reandroid.xml.XMLAttribute;
import com.reandroid.xml.XMLDocument;
import com.reandroid.xml.XMLElement;
import com.reandroid.xml.XMLException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

 public class ApkModuleXmlDecoder {
    private final ApkModule apkModule;
    private final Map<Integer, Set<ResConfig>> decodedEntries;
    private XMLBagDecoder xmlBagDecoder;
    private final Set<String> mDecodedPaths;
    public ApkModuleXmlDecoder(ApkModule apkModule){
        this.apkModule=apkModule;
        this.decodedEntries = new HashMap<>();
        this.mDecodedPaths = new HashSet<>();
    }
    public void decodeTo(File outDir)
            throws IOException, XMLException {
        this.decodedEntries.clear();
        logMessage("Decoding ...");
        decodeUncompressedFiles(outDir);
        TableEntryStore entryStore=new TableEntryStore();
        entryStore.add(Frameworks.getAndroid());
        TableBlock tableBlock=apkModule.getTableBlock();
        entryStore.add(tableBlock);
        xmlBagDecoder=new XMLBagDecoder(entryStore);

        decodePublicXml(tableBlock, outDir);

        decodeAndroidManifest(entryStore, outDir);

        addDecodedPath(TableBlock.FILE_NAME);

        logMessage("Decoding resource files ...");
        List<ResFile> resFileList=apkModule.listResFiles();
        for(ResFile resFile:resFileList){
            decodeResFile(entryStore, outDir, resFile);
        }
        decodeValues(entryStore, outDir, tableBlock);

        extractRootFiles(outDir);
    }
    private void decodeUncompressedFiles(File outDir)
             throws IOException {
        File file=new File(outDir, UncompressedFiles.JSON_FILE);
        UncompressedFiles uncompressedFiles = apkModule.getUncompressedFiles();
        uncompressedFiles.toJson().write(file);
    }
    private void decodeResFile(EntryStore entryStore, File outDir, ResFile resFile)
            throws IOException, XMLException {
        if(resFile.isBinaryXml()){
            decodeResXml(entryStore, outDir, resFile);
        }else {
            decodeResRaw(outDir, resFile);
        }
        addDecodedPath(resFile.getFilePath());
    }
    private void decodeResRaw(File outDir, ResFile resFile)
            throws IOException {
        EntryBlock entryBlock=resFile.pickOne();
        PackageBlock packageBlock=entryBlock.getPackageBlock();

        File pkgDir=new File(outDir, getPackageDirName(packageBlock));
        File resDir=new File(pkgDir, ApkUtil.RES_DIR_NAME);
        String path=resFile.buildPath();
        path=path.replace('/', File.separatorChar);
        File file=new File(resDir, path);
        File dir=file.getParentFile();
        if(!dir.exists()){
            dir.mkdirs();
        }

        FileOutputStream outputStream=new FileOutputStream(file);
        resFile.getInputSource().write(outputStream);
        outputStream.close();

        addDecodedEntry(resFile.pickOne());
    }
    private void decodeResXml(EntryStore entryStore, File outDir, ResFile resFile)
            throws IOException, XMLException{
        EntryBlock entryBlock=resFile.pickOne();
        PackageBlock packageBlock=entryBlock.getPackageBlock();
        ResXmlBlock resXmlBlock=new ResXmlBlock();
        resXmlBlock.readBytes(resFile.getInputSource().openStream());

        File pkgDir=new File(outDir, getPackageDirName(packageBlock));
        File resDir=new File(pkgDir, ApkUtil.RES_DIR_NAME);
        String path=resFile.buildPath();
        path=path.replace('/', File.separatorChar);
        File file=new File(resDir, path);

        logVerbose("Decoding: "+path);
        XMLNamespaceValidator namespaceValidator=new XMLNamespaceValidator(resXmlBlock);
        namespaceValidator.validate();
        XMLDocument xmlDocument=resXmlBlock.decodeToXml(entryStore, packageBlock.getId());
        xmlDocument.save(file, true);

        addDecodedEntry(resFile.pickOne());
    }
    private void decodePublicXml(TableBlock tableBlock, File outDir)
             throws IOException{
        for(PackageBlock packageBlock:tableBlock.listPackages()){
            decodePublicXml(packageBlock, outDir);
        }
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
    private void decodeAndroidManifest(EntryStore entryStore, File outDir)
            throws IOException, XMLException {
        if(!apkModule.hasAndroidManifestBlock()){
            logMessage("Don't have: "+ AndroidManifestBlock.FILE_NAME);
            return;
        }
        File file=new File(outDir, AndroidManifestBlock.FILE_NAME);
        logMessage("Decoding: "+file.getName());
        AndroidManifestBlock manifestBlock=apkModule.getAndroidManifestBlock();
        XMLNamespaceValidator namespaceValidator=new XMLNamespaceValidator(manifestBlock);
        namespaceValidator.validate();
        int currentPackageId= manifestBlock.guessCurrentPackageId();
        XMLDocument xmlDocument=manifestBlock.decodeToXml(entryStore, currentPackageId);
        xmlDocument.save(file, true);
        addDecodedPath(AndroidManifestBlock.FILE_NAME);
    }
    private void addDecodedEntry(EntryBlock entryBlock){
        if(entryBlock.isNull()){
            return;
        }
        int resourceId=entryBlock.getResourceId();
        Set<ResConfig> resConfigSet=decodedEntries.get(resourceId);
        if(resConfigSet==null){
            resConfigSet=new HashSet<>();
            decodedEntries.put(resourceId, resConfigSet);
        }
        resConfigSet.add(entryBlock.getResConfig());
    }
    private boolean containsDecodedEntry(EntryBlock entryBlock){
        Set<ResConfig> resConfigSet=decodedEntries.get(entryBlock.getResourceId());
        if(resConfigSet==null){
            return false;
        }
        return resConfigSet.contains(entryBlock.getResConfig());
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
        for(EntryBlock entryBlock:typeBlock.listEntries(true)){
            if(containsDecodedEntry(entryBlock)){
                continue;
            }
            docElement.addChild(decodeValue(entryStore, entryBlock));
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
    private XMLElement decodeValue(EntryStore entryStore, EntryBlock entryBlock){
        XMLElement element=new XMLElement(entryBlock.getTypeName());
        int resourceId=entryBlock.getResourceId();
        XMLAttribute attribute=new XMLAttribute("name", entryBlock.getName());
        element.addAttribute(attribute);
        attribute.setNameId(resourceId);
        element.setResourceId(resourceId);
        if(!entryBlock.isEntryTypeBag()){
            ResValueInt resValueInt=(ResValueInt) entryBlock.getResValue();
            if(resValueInt.getValueType()== ValueType.STRING){
                XmlHelper.setTextContent(element,
                        resValueInt.getValueAsPoolString());
            }else {
                String value = ValueDecoder.decodeEntryValue(entryStore,
                        entryBlock.getPackageBlock(),
                        resValueInt.getValueType(),
                        resValueInt.getData());
                element.setTextContent(value);
            }
        }else {
            ResValueBag resValueBag=(ResValueBag) entryBlock.getResValue();
            xmlBagDecoder.decode(resValueBag, element);
            return element;
        }
        return element;
    }
    private String getPackageDirName(PackageBlock packageBlock){
        return packageBlock.getIndex()+"-"+packageBlock.getName();
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
