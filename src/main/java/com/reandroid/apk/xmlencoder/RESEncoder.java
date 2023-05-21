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

import com.reandroid.apk.*;
import com.reandroid.archive.APKArchive;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.coder.ReferenceString;
import com.reandroid.arsc.util.HexUtil;
import com.reandroid.identifiers.PackageIdentifier;
import com.reandroid.identifiers.ResourceIdentifier;
import com.reandroid.identifiers.TableIdentifier;
import com.reandroid.xml.XMLException;
import com.reandroid.xml.XMLParserFactory;
import com.reandroid.xml.source.XMLFileSource;
import com.reandroid.xml.source.XMLSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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
            module.setLoadDefaultFramework(false);
            BlockInputSource<TableBlock> inputSource =
                    new BlockInputSource<>(TableBlock.FILE_NAME, block);
            inputSource.setMethod(ZipEntry.STORED);
            this.apkModule.setTableBlock(tableBlock);
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
                    + ApkUtil.FILE_NAME_PUBLIC_XML+"  file found in '"+mainDir);
        }
        preloadStringPool(pubXmlFileList);
        EncodeMaterials encodeMaterials = new EncodeMaterials();
        encodeMaterials.setAPKLogger(apkLogger);

        TableIdentifier tableIdentifier = encodeMaterials.getTableIdentifier();
        tableIdentifier.loadPublicXml(pubXmlFileList);
        tableIdentifier.initialize(this.tableBlock);

        excludeIds(pubXmlFileList);
        File manifestFile = initializeFrameworkFromManifest(encodeMaterials, pubXmlFileList);

        encodeAttrs(encodeMaterials, pubXmlFileList);

        encodeValues(encodeMaterials, pubXmlFileList);

        tableBlock.refresh();

        PackageBlock packageBlock = encodeMaterials.pickMainPackageBlock(this.tableBlock);
        if(manifestFile != null){
            if(packageBlock != null){
                encodeMaterials.setCurrentPackage(packageBlock);
            }
            XMLSource xmlSource =
                    new XMLFileSource(AndroidManifestBlock.FILE_NAME, manifestFile);
            XMLEncodeSource xmlEncodeSource =
                    new XMLEncodeSource(encodeMaterials, xmlSource);
            getApkModule().getApkArchive().add(xmlEncodeSource);
        }
    }
    private File initializeFrameworkFromManifest(EncodeMaterials encodeMaterials, List<File> pubXmlFileList) throws  IOException {
         for(File pubXmlFile:pubXmlFileList){
            addParsedFiles(pubXmlFile);
            File manifestFile = toAndroidManifest(pubXmlFile);
            if(!manifestFile.isFile()){
                continue;
            }
            initializeFrameworkFromManifest(encodeMaterials, manifestFile);
            return manifestFile;
        }
        return null;
    }
    private void encodeValues(EncodeMaterials encodeMaterials, List<File> pubXmlFileList) throws XMLException, IOException {
        logMessage("Encoding values ...");
        TableIdentifier tableIdentifier = encodeMaterials.getTableIdentifier();

        for(File pubXmlFile:pubXmlFileList){
            addParsedFiles(pubXmlFile);
            PackageIdentifier packageIdentifier = tableIdentifier.getByTag(pubXmlFile);

            PackageBlock packageBlock = packageIdentifier.getPackageBlock();

            encodeMaterials.setCurrentPackage(packageBlock);

            File resDir=toResDirectory(pubXmlFile);
            encodeResDir(encodeMaterials, resDir);
            FilePathEncoder filePathEncoder = new FilePathEncoder(encodeMaterials);
            filePathEncoder.setApkArchive(getApkModule().getApkArchive());
            filePathEncoder.setUncompressedFiles(getApkModule().getUncompressedFiles());
            filePathEncoder.encodeResDir(resDir);

            packageBlock.sortTypes();
            packageBlock.refresh();
        }
    }
    private void encodeAttrs(EncodeMaterials encodeMaterials, List<File> pubXmlFileList) throws XMLException {
        logMessage("Encoding attrs ...");
        TableIdentifier tableIdentifier = encodeMaterials.getTableIdentifier();

        for(File pubXmlFile : pubXmlFileList){
            addParsedFiles(pubXmlFile);
            PackageIdentifier packageIdentifier = tableIdentifier.getByTag(pubXmlFile);

            PackageBlock packageBlock = packageIdentifier.getPackageBlock();
            encodeMaterials.setCurrentPackage(packageBlock);

            ResourceValuesEncoder valuesEncoder = new ResourceValuesEncoder(encodeMaterials);
            List<File> attrFiles = listAttrs(pubXmlFile);
            if(attrFiles.size() == 0){
                continue;
            }
            for(File file : attrFiles){
                valuesEncoder.encodeValuesXml(file);
                addParsedFiles(file);
            }
            packageBlock.sortTypes();
        }
    }
    private void excludeIds(List<File> pubXmlFileList){
        for(File pubXmlFile : pubXmlFileList){
            addParsedFiles(pubXmlFile);
            File valuesDir = pubXmlFile.getParentFile();
            File file = new File(valuesDir, "ids.xml");
            if(file.isFile()){
                addParsedFiles(file);
            }
        }
    }
    private void initializeFrameworkFromManifest(EncodeMaterials encodeMaterials, File manifestFile) throws IOException {
        XmlPullParser parser;
        try {
            parser = XMLParserFactory.newPullParser(manifestFile);
        } catch (XmlPullParserException ex) {
            throw new IOException(ex);
        }
        FrameworkApk frameworkApk = getApkModule().initializeAndroidFramework(parser);
        encodeMaterials.addFramework(frameworkApk);
        initializeMainPackageId(encodeMaterials, parser);
        XmlHelper.closeSilent(parser);
    }
    private void initializeMainPackageId(EncodeMaterials encodeMaterials, XmlPullParser parser) throws IOException {
        Map<String, String> applicationAttributes;
        try {
            applicationAttributes = XmlHelper.readAttributes(parser, AndroidManifestBlock.TAG_application);
        } catch (XmlPullParserException ex) {
            throw new IOException(ex);
        }
        if(applicationAttributes == null){
            return;
        }
        String iconReference = applicationAttributes.get(AndroidManifestBlock.NAME_icon);
        if(iconReference == null){
            return;
        }
        logMessage("Set main package id from manifest: " + iconReference);
        ReferenceString ref = ReferenceString.parseReference(iconReference);
        if(ref == null){
            logMessage("Something wrong on : " + AndroidManifestBlock.NAME_icon);
            return;
        }
        TableIdentifier tableIdentifier = encodeMaterials.getTableIdentifier();
        ResourceIdentifier resourceIdentifier;
        if(ref.packageName != null){
            resourceIdentifier = tableIdentifier.get(ref.packageName, ref.type, ref.name);
        }else {
            resourceIdentifier = tableIdentifier.get(ref.type, ref.name);
        }
        if(resourceIdentifier == null){
            logMessage("WARN: failed to resolve: " + ref);
            return;
        }
        int packageId = resourceIdentifier.getPackageId();
        encodeMaterials.setMainPackageId(packageId);
        logMessage("Main package id initialized: id = "
                + HexUtil.toHex2((byte)packageId) + ", from: " + ref );
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

    private void encodeResDir(EncodeMaterials materials, File resDir) throws XMLException {

        List<File> valuesDirList = listValuesDir(resDir);
        for(File valuesDir:valuesDirList){
            encodeValuesDir(materials, valuesDir);
        }
    }
    private void encodeValuesDir(EncodeMaterials materials, File valuesDir) throws XMLException {
        ResourceValuesEncoder valuesEncoder = new ResourceValuesEncoder(materials);
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
    private List<File> listAttrs(File pubXmlFile){
        return listValuesXml(pubXmlFile, "attr");
    }
    private List<File> listValuesXml(File pubXmlFile, String type){
        List<File> results = new ArrayList<>();
        File resDir = toResDirectory(pubXmlFile);
        for(File valuesDir : listValuesDir(resDir)){
            results.addAll(findValuesXml(valuesDir, type));
        }
        return results;
    }
    private List<File> findValuesXml(File valuesDir, String type){
        List<File> results = new ArrayList<>();
        File[] xmlFiles = valuesDir.listFiles();
        if(xmlFiles == null){
            return results;
        }
        for(File file : xmlFiles){
            if(!file.isFile()){
                continue;
            }
            String name = file.getName();
            if(name.startsWith(type) && name.endsWith(".xml")){
                results.add(file);
            }
        }
        return results;
    }
    private List<File> listValuesDir(File resDir){
        List<File> results=new ArrayList<>();
        File def = new File(resDir, "values");
        if(def.isDirectory()){
            results.add(def);
        }
        File[] dirList=resDir.listFiles();
        if(dirList != null){
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
            if(!EncodeUtil.isPublicXml(file)){
                continue;
            }
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

    public APKLogger getAPKLogger() {
        if(apkLogger == null){
            apkLogger = apkModule.getApkLogger();
        }
        return apkLogger;
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
