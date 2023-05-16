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

import com.reandroid.apk.xmldecoder.*;
import com.reandroid.archive.InputSource;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.value.*;
import com.reandroid.identifiers.PackageIdentifier;
import com.reandroid.json.JSONObject;
import com.reandroid.xml.XMLDocument;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class ApkModuleXmlDecoder extends ApkDecoder implements Predicate<Entry> {
    private final ApkModule apkModule;
    private final Map<Integer, Set<ResConfig>> decodedEntries;

    private ResXmlDocumentSerializer documentSerializer;
    private XMLEntryDecoderSerializer entrySerializer;


    public ApkModuleXmlDecoder(ApkModule apkModule){
        super();
        this.apkModule = apkModule;
        this.decodedEntries = new HashMap<>();
        super.setApkLogger(apkModule.getApkLogger());
    }
    public void sanitizeFilePaths(){
        PathSanitizer sanitizer = PathSanitizer.create(apkModule);
        sanitizer.sanitize();
    }
    @Override
    void onDecodeTo(File outDir) throws IOException{
        this.decodedEntries.clear();
        logMessage("Decoding ...");

        if(!apkModule.hasTableBlock()){
            logOrThrow(null, new IOException("Don't have resource table"));
            return;
        }

        decodeUncompressedFiles(outDir);

        TableBlock tableBlock = apkModule.getTableBlock();

        this.entrySerializer = new XMLEntryDecoderSerializer(tableBlock);
        this.entrySerializer.setDecodedEntries(this);

        decodeAndroidManifest(outDir, apkModule.getAndroidManifestBlock());
        decodeTableBlock(outDir, tableBlock);

        logMessage("Decoding resource files ...");
        List<ResFile> resFileList = apkModule.listResFiles();
        for(ResFile resFile:resFileList){
            decodeResFile(outDir, resFile);
        }
        decodeValues(outDir, tableBlock);

        extractRootFiles(outDir);

        writePathMap(outDir, apkModule.getApkArchive().listInputSources());

        dumpSignatures(outDir, apkModule.getApkSignatureBlock());
    }
    private void decodeTableBlock(File outDir, TableBlock tableBlock) throws IOException {
        try{
            decodePackageInfo(outDir, tableBlock);
            decodePublicXml(tableBlock, outDir);
            addDecodedPath(TableBlock.FILE_NAME);
        }catch (IOException exception){
            logOrThrow("Error decoding resource table", exception);
        }
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
        PackageIdentifier packageIdentifier = new PackageIdentifier();
        packageIdentifier.load(packageBlock);
        packageIdentifier.writePublicXml(file);
    }
    private void decodeAndroidManifest(File outDir, AndroidManifestBlock manifestBlock)
            throws IOException {
        if(!apkModule.hasAndroidManifestBlock()){
            logMessage("Don't have: "+ AndroidManifestBlock.FILE_NAME);
            return;
        }
        File file=new File(outDir, AndroidManifestBlock.FILE_NAME);
        logMessage("Decoding: "+file.getName());
        int currentPackageId = manifestBlock.guessCurrentPackageId();
        serializeXml(currentPackageId, manifestBlock, file);
        addDecodedPath(AndroidManifestBlock.FILE_NAME);
    }
    private void serializeXml(int currentPackageId, ResXmlDocument document, File outFile)
            throws IOException {
        XMLNamespaceValidator.validateNamespaces(document);
        ResXmlDocumentSerializer serializer = getDocumentSerializer();
        if(currentPackageId != 0){
            serializer.getDecoder().setCurrentPackageId(currentPackageId);
        }
        try {
            serializer.write(document, outFile);
        } catch (XmlPullParserException ex) {
            throw new IOException("Error: "+outFile.getName(), ex);
        }
    }
    private void serializeXml(int currentPackageId, InputSource inputSource, File outFile)
            throws IOException {
        ResXmlDocumentSerializer serializer = getDocumentSerializer();
        if(currentPackageId != 0){
            serializer.getDecoder().setCurrentPackageId(currentPackageId);
        }
        try {
            serializer.write(inputSource, outFile);
        } catch (XmlPullParserException ex) {
            throw new IOException("Error: "+outFile.getName(), ex);
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
    private void decodeValues(File outDir, TableBlock tableBlock) throws IOException {
        for(PackageBlock packageBlock:tableBlock.listPackages()){
            decodeValues(outDir, packageBlock);
        }
    }
    private void decodeValues(File outDir, PackageBlock packageBlock) throws IOException {
        logMessage("Decoding values: "
                + getPackageDirName(packageBlock));

        packageBlock.sortTypes();

        File pkgDir = new File(outDir, getPackageDirName(packageBlock));
        File resDir = new File(pkgDir, ApkUtil.RES_DIR_NAME);

        for(SpecTypePair specTypePair : packageBlock.listSpecTypePairs()){
            decodeValues(resDir, specTypePair);
        }
    }
    private void decodeValues(File outDir, SpecTypePair specTypePair) throws IOException {
        entrySerializer.decode(outDir, specTypePair);
    }
    private String getPackageDirName(PackageBlock packageBlock){
        String name = ApkUtil.sanitizeForFileName(packageBlock.getName());
        if(name==null){
            name="package";
        }
        TableBlock tableBlock = packageBlock.getTableBlock();
        int index = packageBlock.getIndex();
        String prefix;
        if(index < 10 && tableBlock.countPackages() > 10){
            prefix = "0" + index;
        }else {
            prefix = Integer.toString(index);
        }
        return prefix + "-" + name;
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
    @Override
    public boolean test(Entry entry) {
        return !containsDecodedEntry(entry);
    }
}
