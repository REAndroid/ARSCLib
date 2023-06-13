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

import com.android.org.kxml2.io.KXmlParser;
import com.reandroid.apk.*;
import com.reandroid.archive.APKArchive;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.coder.ReferenceString;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.group.ResourceEntry;
import com.reandroid.arsc.util.FrameworkTable;
import com.reandroid.arsc.util.HexUtil;
import com.reandroid.arsc.value.Entry;
import com.reandroid.common.FileChannelInputStream;
import com.reandroid.xml.XMLException;
import com.reandroid.xml.XMLParserFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;

public class XMLTableBlockEncoder {
    private APKLogger apkLogger;
    private final TableBlock tableBlock;
    private final Set<File> parsedFiles = new HashSet<>();
    private final ApkModule apkModule;
    private EncodeMaterials mEncodeMaterials;

    public XMLTableBlockEncoder(ApkModule apkModule, TableBlock tableBlock){
        this.apkModule = apkModule;
        this.tableBlock = tableBlock;
        if(!apkModule.hasTableBlock()){
            BlockInputSource<TableBlock> inputSource =
                    new BlockInputSource<>(TableBlock.FILE_NAME, tableBlock);
            inputSource.setMethod(ZipEntry.STORED);
            this.apkModule.setTableBlock(this.tableBlock);
            apkModule.setLoadDefaultFramework(true);
        }
        apkLogger = apkModule.getApkLogger();
    }
    public XMLTableBlockEncoder(){
        this(new ApkModule("encoded",
                new APKArchive()), new TableBlock());
    }
    public EncodeMaterials getEncodeMaterials(){
        EncodeMaterials materials = this.mEncodeMaterials;
        if(materials == null){
            materials = new EncodeMaterials();
            materials.setAPKLogger(getApkLogger());
            this.mEncodeMaterials = materials;
        }
        return materials;
    }
    public void setEncodeMaterials(EncodeMaterials encodeMaterials){
        this.mEncodeMaterials = encodeMaterials;
    }
    public TableBlock getTableBlock(){
        return tableBlock;
    }
    public ApkModule getApkModule(){
        return apkModule;
    }
    public void scanMainDirectory(File mainDirectory) throws IOException {
        File resourcesDirectory = new File(mainDirectory, TableBlock.DIRECTORY_NAME);
        scanResourcesDirectory(resourcesDirectory);
    }
    public void scanResourcesDirectory(File resourcesDirectory) throws IOException {
        try {
            scanResourceFiles(resourcesDirectory);
        } catch (XMLException ex) {
            throw new IOException(ex);
        }
    }
    private void scanResourceFiles(File resourcesDirectory) throws IOException, XMLException {
        List<File> pubXmlFileList = ApkUtil.listPublicXmlFiles(resourcesDirectory);
        if(pubXmlFileList.size() == 0){
            throw new IOException("No .*/values/"
                    + PackageBlock.PUBLIC_XML
                    + "  file found in '" +resourcesDirectory + "'");
        }
        preloadStringPool(pubXmlFileList);

        loadPublicXmlFiles(pubXmlFileList);

        excludeIds(pubXmlFileList);
        initializeFrameworkFromManifest(pubXmlFileList);

        encodeAttrs(pubXmlFileList);

        encodeValues(pubXmlFileList);

        tableBlock.refresh();

    }
    private void loadPublicXmlFiles(List<File> pubXmlFileList) throws IOException {
        for(File pubXmlFile:pubXmlFileList){
            loadPublicXmlFile(pubXmlFile);
        }
    }
    private void loadPublicXmlFile(File pubXmlFile) throws IOException {
        XmlPullParser parser = new KXmlParser();
        try {
            parser.setInput(new FileChannelInputStream(pubXmlFile), StandardCharsets.UTF_8.name());
            PackageBlock packageBlock = tableBlock.parsePublicXml(parser);
            packageBlock.setTag(pubXmlFile);
            initializeIds(packageBlock);
        } catch (XmlPullParserException ex) {
           throw new IOException(ex);
        }
    }
    private void initializeIds(PackageBlock packageBlock){
        int typeId = packageBlock.typeIdOf("id");
        SpecTypePair specTypePair = packageBlock.getSpecTypePair(typeId);
        if(specTypePair == null){
            return;
        }
        Iterator<ResourceEntry> itr = specTypePair.getResources();
        while (itr.hasNext()){
            Iterator<Entry> entryIterator = itr.next()
                    .iterator(false);
            while (entryIterator.hasNext()){
                Entry entry = entryIterator.next();
                if(!entry.isNull()){
                    continue;
                }
                if(entry.getSpecReference() < 0){
                    continue;
                }
                entry.setValueAsBoolean(false);
            }
        }
    }
    private void initializeFrameworkFromManifest(List<File> pubXmlFileList) throws  IOException {
        for(File pubXmlFile:pubXmlFileList){
            File manifestFile = toAndroidManifest(pubXmlFile);
            if(!manifestFile.isFile()){
                continue;
            }
            initializeFrameworkFromManifest(manifestFile);
            return;
        }
    }
    private void encodeValues(List<File> pubXmlFileList) throws XMLException {
        logMessage("Encoding values ...");
        EncodeMaterials encodeMaterials = getEncodeMaterials();
        TableBlock tableBlock = getTableBlock();

        for(File pubXmlFile:pubXmlFileList){
            addParsedFiles(pubXmlFile);
            PackageBlock packageBlock = tableBlock.getPackageBlockByTag(pubXmlFile);
            encodeMaterials.setCurrentPackage(packageBlock);
            File resDir = toResDirectory(pubXmlFile);
            encodeResDir(resDir);
            FilePathEncoder filePathEncoder = new FilePathEncoder(encodeMaterials);
            filePathEncoder.setApkArchive(getApkModule().getApkArchive());
            filePathEncoder.setUncompressedFiles(getApkModule().getUncompressedFiles());
            filePathEncoder.encodePackageResDir(resDir);

            packageBlock.sortTypes();
            packageBlock.refresh();
        }
    }
    private void encodeAttrs(List<File> pubXmlFileList) throws XMLException {
        logMessage("Encoding attrs ...");

        EncodeMaterials encodeMaterials = getEncodeMaterials();
        TableBlock tableBlock = getTableBlock();

        for(File pubXmlFile : pubXmlFileList){
            addParsedFiles(pubXmlFile);

            PackageBlock packageBlock = tableBlock.getPackageBlockByTag(pubXmlFile);
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
    private void initializeFrameworkFromManifest(File manifestFile) throws IOException {
        if(AndroidManifestBlock.FILE_NAME_BIN.equals(manifestFile.getName())){
            initializeFrameworkFromBinaryManifest();
            return;
        }
        XmlPullParser parser;
        try {
            parser = XMLParserFactory.newPullParser(manifestFile);
        } catch (XmlPullParserException ex) {
            throw new IOException(ex);
        }
        EncodeMaterials encodeMaterials = getEncodeMaterials();
        FrameworkApk frameworkApk = getApkModule().initializeAndroidFramework(parser);
        if(frameworkApk == null){
            for(TableBlock frame : getApkModule().getLoadedFrameworks()){
                if(frame instanceof FrameworkTable){
                    encodeMaterials.addFramework((FrameworkTable) frame);
                }
            }
        }else {
            encodeMaterials.addFramework(frameworkApk);
        }
        initializeMainPackageId(encodeMaterials, parser);
        XmlHelper.closeSilent(parser);
    }
    private void initializeFrameworkFromBinaryManifest() throws IOException {
        ApkModule apkModule = getApkModule();
        if(!apkModule.hasTableBlock() || !apkModule.hasAndroidManifestBlock()){
            return;
        }
        logMessage("Initialize framework from binary manifest ...");
        FrameworkApk frameworkApk = apkModule.initializeAndroidFramework(
                apkModule.getAndroidFrameworkVersion());
        getEncodeMaterials().addFramework(frameworkApk);
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
        TableBlock tableBlock = getTableBlock();
        int resourceId = tableBlock.resolveResourceId(ref.packageName, ref.type, ref.name);
        if(resourceId == 0){
            logMessage("WARN: failed to resolve: " + ref);
            return;
        }
        int packageId = (resourceId >> 24 ) & 0xff;
        encodeMaterials.setMainPackageId(packageId);
        logMessage("Main package id initialized: id = "
                + HexUtil.toHex2((byte)packageId) + ", from: " + ref );
    }
    private void preloadStringPool(List<File> pubXmlFileList){
        logMessage("Loading string pool ...");
        ValuesStringPoolBuilder poolBuilder=new ValuesStringPoolBuilder();
        for(File pubXml : pubXmlFileList){
            File resDir = toResDirectory(pubXml);
            List<File> valuesDirList = ApkUtil.listValuesDirectory(resDir);
            for(File dir : valuesDirList){
                logVerbose(poolBuilder.size() + " building pool: " + dir.getName());
                poolBuilder.scanValuesDirectory(dir);
            }
        }
        poolBuilder.addTo(tableBlock.getTableStringPool());
    }

    private void encodeResDir(File resDir) throws XMLException {
        List<File> valuesDirList = ApkUtil.listValuesDirectory(resDir);
        for(File valuesDir : valuesDirList){
            encodeValuesDir(valuesDir);
        }
    }
    private void encodeValuesDir(File valuesDir) throws XMLException {
        EncodeMaterials materials = getEncodeMaterials();
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
        File resourcesDir = packageDirectory.getParentFile();
        File root = resourcesDir.getParentFile();
        File file = new File(root, AndroidManifestBlock.FILE_NAME_BIN);
        if(!file.isFile()){
            file = new File(root, AndroidManifestBlock.FILE_NAME);
        }
        return file;
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
        List<File> valuesDirList = ApkUtil.listValuesDirectory(resDir);
        for(File valuesDir : valuesDirList){
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
            if(!name.endsWith(".xml")){
                continue;
            }
            name = EncodeUtil.sanitizeType(name);
            if(name.equals(type)){
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

    public APKLogger getApkLogger() {
        return apkLogger;
    }
    public void setApkLogger(APKLogger logger) {
        this.apkLogger = logger;
        if(logger != null && apkModule.getApkLogger() == null){
            this.apkModule.setAPKLogger(logger);
        }
    }
    private void logMessage(String msg) {
        APKLogger apkLogger = getApkLogger();
        if(apkLogger!=null){
            apkLogger.logMessage(msg);
        }
    }
    private void logError(String msg, Throwable tr) {
        APKLogger apkLogger = getApkLogger();
        if(apkLogger!=null){
            apkLogger.logError(msg, tr);
        }
    }
    private void logVerbose(String msg) {
        APKLogger apkLogger = getApkLogger();
        if(apkLogger!=null){
            apkLogger.logVerbose(msg);
        }
    }
}
