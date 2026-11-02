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
import com.reandroid.apk.ApkModule;
import com.reandroid.apk.ApkUtil;
import com.reandroid.apk.FrameworkApk;
import com.reandroid.archive.BlockInputSource;
import com.reandroid.archive.ZipEntryMap;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.coder.ReferenceString;
import com.reandroid.arsc.coder.xml.XmlCoder;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.io.FileUtil;
import com.reandroid.utils.io.IOUtil;
import com.reandroid.xml.*;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XMLTableBlockEncoder {

    private APKLogger apkLogger;
    private final TableBlock tableBlock;
    private final Set<File> parsedFiles;
    private final Map<File, PackageBlock> publicXmlMap;
    private final ApkModule apkModule;
    private Integer mMainPackageId;

    public XMLTableBlockEncoder(ApkModule apkModule, TableBlock tableBlock) {
        this.apkModule = apkModule;
        this.tableBlock = tableBlock;
        this.parsedFiles = new HashSet<>();
        this.publicXmlMap = new LinkedHashMap<>();

        if (!apkModule.hasTableBlock()) {
            BlockInputSource<TableBlock> inputSource = new BlockInputSource<>(TableBlock.FILE_NAME, tableBlock);
            inputSource.setUncompressed(true);
            inputSource.setSort(1);
            apkModule.setTableBlock(tableBlock);
            apkModule.setLoadDefaultFramework(true);
        }
        apkLogger = apkModule.getApkLogger();
    }
    public XMLTableBlockEncoder() {
        this(new ApkModule("encoded", new ZipEntryMap()),
                new TableBlock());
    }

    public Integer getMainPackageId() {
        return mMainPackageId;
    }
    public TableBlock getTableBlock() {
        return tableBlock;
    }
    public ApkModule getApkModule() {
        return apkModule;
    }
    public void scanMainDirectory(File mainDirectory) throws IOException {
        File resourcesDirectory = new File(mainDirectory, TableBlock.DIRECTORY_NAME);
        scanResourcesDirectory(resourcesDirectory);
    }
    public void scanResourcesDirectory(File resourcesDirectory) throws IOException {
        try {
            scanResourceFiles(resourcesDirectory);
            ensureEmptyTable();
        } catch (XmlPullParserException ex) {
            throw new IOException(ex);
        }
    }
    private void ensureEmptyTable() {
        TableBlock tableBlock = this.getTableBlock();
        if (tableBlock.initializeAsEmpty()) {
            logMessage("Using <NULL> resource table");
        }
    }
    private void scanResourceFiles(File resourcesDirectory) throws IOException, XmlPullParserException {
        List<File> pubXmlFileList = ApkUtil.listPublicXmlFiles(resourcesDirectory);
        if (pubXmlFileList.size() == 0) {
            throw new IOException("No .*/values/"
                    + PackageBlock.FILE_NAME_PUBLIC_XML
                    + "  file found in '" +resourcesDirectory + "'");
        }
        loadPublicXmlFiles(pubXmlFileList);

        initializeFrameworkFromManifest(pubXmlFileList);

        encodeAttrs(pubXmlFileList);

        encodeValues(pubXmlFileList);

        tableBlock.refresh();

    }
    private void loadPublicXmlFiles(List<File> pubXmlFileList) throws IOException, XmlPullParserException {
        for (File pubXmlFile : pubXmlFileList) {
            if (addParsedFiles(pubXmlFile)) {
                addParsedPublicXml(pubXmlFile, loadPublicXmlFile(pubXmlFile));
            }
        }
    }
    private PackageBlock loadPublicXmlFile(File pubXmlFile) throws IOException, XmlPullParserException {
        XmlPullParser parser = XMLFactory.newPullParser(pubXmlFile);
        PackageBlock packageBlock = tableBlock.parsePublicXml(parser);
        IOUtil.close(parser);
        return packageBlock;
    }
    private void initializeFrameworkFromManifest(List<File> pubXmlFileList) throws  IOException {
        for (File pubXmlFile : pubXmlFileList) {
            File manifestFile = toAndroidManifest(pubXmlFile);
            if (manifestFile.isFile()) {
                initializeFrameworkFromManifest(manifestFile);
                break;
            }
        }
    }
    private void initializeFrameworkFromManifest(File manifestFile) throws IOException {
        if (AndroidManifestBlock.FILE_NAME_BIN.equals(manifestFile.getName())) {
            initializeFrameworkFromBinaryManifest();
            return;
        }
        XMLDocument xmlDocument;
        try {
            xmlDocument = XMLDocument.load(manifestFile);
        } catch (XmlPullParserException ex) {
            throw new IOException(ex);
        }
        TableBlock tableBlock = getTableBlock();
        FrameworkApk frameworkApk = getApkModule().initializeAndroidFramework(xmlDocument);
        if (frameworkApk != null) {
            tableBlock.addFramework(frameworkApk.getTableBlock());
        }
        initializeMainPackageId(xmlDocument);
    }
    private void initializeFrameworkFromBinaryManifest() throws IOException {
        ApkModule apkModule = getApkModule();
        if (!apkModule.hasTableBlock() || !apkModule.hasAndroidManifest()) {
            return;
        }
        logMessage("Initialize framework from binary manifest ...");
        FrameworkApk frameworkApk = apkModule.initializeAndroidFramework(
                apkModule.getAndroidFrameworkVersion());
        getTableBlock().addFramework(frameworkApk.getTableBlock());
    }
    private void initializeMainPackageId(XMLDocument xmlDocument) {
        XMLElement manifestRoot = xmlDocument.getDocumentElement();
        if (manifestRoot == null) {
            return;
        }
        XMLElement application = manifestRoot.getElement(AndroidManifestBlock.TAG_application);
        if (application == null) {
            return;
        }

        String iconReference = application.getAttributeValue(AndroidManifestBlock.NAME_icon);
        if (iconReference == null) {
            return;
        }
        logMessage("Set main package id from manifest: " + iconReference);
        ReferenceString ref = ReferenceString.parseReference(iconReference);
        if (ref == null) {
            logMessage("Something wrong on : " + AndroidManifestBlock.NAME_icon);
            return;
        }
        TableBlock tableBlock = getTableBlock();
        int resourceId = tableBlock.resolveResourceId(ref.packageName, ref.type, ref.name);
        if (resourceId == 0) {
            logMessage("WARN: failed to resolve: " + ref);
            return;
        }
        int packageId = (resourceId >> 24 ) & 0xff;
        this.mMainPackageId = packageId;
        logMessage("Main package id initialized: id = "
                + HexUtil.toHex2(packageId) + ", from: " + ref );
    }

    private void encodeAttrs(List<File> pubXmlFileList) throws IOException, XmlPullParserException {
        logMessage("Encoding attrs ...");

        TableBlock tableBlock = getTableBlock();

        for (File pubXmlFile : pubXmlFileList) {

            PackageBlock packageBlock = getPackageBlock(pubXmlFile);
            tableBlock.setCurrentPackage(packageBlock);
            List<File> attrFiles = listAttrs(pubXmlFile);

            for (File file : attrFiles) {
                if (addParsedFiles(file)) {
                    logVerbose("Encoding: " + FileUtil.shortPath(file, 4));
                    XmlCoder xmlCoder = XmlCoder.getInstance();
                    xmlCoder.VALUES_XML.encode(file, packageBlock);
                }
            }
        }
    }

    private void encodeValues(List<File> pubXmlFileList) throws IOException, XmlPullParserException {
        logMessage("Encoding values ...");
        FilePathEncoder filePathEncoder = new FilePathEncoder(getApkModule());
        TableBlock tableBlock = getTableBlock();

        for (File pubXmlFile : pubXmlFileList) {

            PackageBlock packageBlock = getPackageBlock(pubXmlFile);
            tableBlock.setCurrentPackage(packageBlock);

            File resDir = toResDirectory(pubXmlFile);
            encodeResDir(packageBlock, resDir);

            filePathEncoder.setApkLogger(getApkLogger());
            filePathEncoder.encodePackageResDir(packageBlock, resDir);

            packageBlock.sortTypes();
        }
    }
    private void encodeResDir(PackageBlock packageBlock, File resDir) throws IOException, XmlPullParserException {
        preloadStyledStrings(resDir);
        List<File> valuesDirList = ApkUtil.listValuesDirectory(resDir);
        for (File valuesDir : valuesDirList) {
            encodeValuesDir(packageBlock, valuesDir);
        }
    }
    private void preloadStyledStrings(File resDir) throws IOException, XmlPullParserException {
        logVerbose("Preloading styled strings ...");
        List<File> valuesDirList = ApkUtil.listValuesDirectory(resDir);
        for (File valuesDir : valuesDirList) {
            List<File> xmlFiles = ApkUtil.listFiles(valuesDir, "strings.xml");
            for (File file : xmlFiles) {
                preloadStyledStringsXml(file);
            }
        }
    }
    private void preloadStyledStringsXml(File file) throws IOException, XmlPullParserException {
        XmlPullParser parser = XMLFactory.newPullParser(file);
        XMLUtil.ensureStartTag(parser);
        if (!PackageBlock.TAG_resources.equals(parser.getName())) {
            IOUtil.close(parser);
            return;
        }
        parser.next();
        TableStringPool stringPool = getTableBlock().getStringPool();
        while (XMLUtil.ensureStartTag(parser) == XmlPullParser.START_TAG) {
            XMLElement element = XMLElement.parseElement(parser);
            if (element.hasChildElements()) {
                stringPool.getOrCreate(StyleDocument.copyInner(element));
            }
        }
        IOUtil.close(parser);
    }
    private void encodeValuesDir(PackageBlock packageBlock, File valuesDir) throws IOException, XmlPullParserException {
        List<File> xmlFiles = ApkUtil.listFiles(valuesDir, ".xml");
        xmlFiles.sort(ApkUtil.getValuesXmlComparator());

        for (File file : xmlFiles) {
            if (addParsedFiles(file)) {
                logVerbose("Encoding: " + FileUtil.shortPath(file, 4));
                XmlCoder xmlCoder = XmlCoder.getInstance();
                xmlCoder.VALUES_XML.encode(file, packageBlock);
            }
        }
    }

    private PackageBlock getPackageBlock(File pubXmlFile) {
        return publicXmlMap.get(pubXmlFile);
    }
    private void addParsedPublicXml(File pubXmlFile, PackageBlock packageBlock) {
        publicXmlMap.putIfAbsent(pubXmlFile, packageBlock);
    }
    private File toAndroidManifest(File pubXmlFile) {
        File resDirectory = toResDirectory(pubXmlFile);
        File packageDirectory = resDirectory.getParentFile();
        File resourcesDir = packageDirectory.getParentFile();
        File root = resourcesDir.getParentFile();
        File file = new File(root, AndroidManifestBlock.FILE_NAME_BIN);
        if (!file.isFile()) {
            file = new File(root, AndroidManifestBlock.FILE_NAME);
        }
        return file;
    }
    private File toResDirectory(File pubXmlFile) {
        return pubXmlFile
                .getParentFile()
                .getParentFile();
    }
    private List<File> listAttrs(File pubXmlFile) {
        List<File> results = new ArrayList<>();
        File resDir = toResDirectory(pubXmlFile);
        List<File> valuesDirList = ApkUtil.listValuesDirectory(resDir);
        for (File valuesDir : valuesDirList) {
            findAttrsXml(results, valuesDir);
        }
        return results;
    }
    private void findAttrsXml(List<File> results, File valuesDir) {
        List<File> xmlFiles = ApkUtil.listFiles(valuesDir, ".xml");
        for (File file : xmlFiles) {
            String name = EncodeUtil.sanitizeType(file.getName().toLowerCase());
            if (name.equals("attr")) {
                results.add(file);
            }
        }
    }
    private boolean addParsedFiles(File file) {
        return parsedFiles.add(file);
    }

    public APKLogger getApkLogger() {
        return apkLogger;
    }
    public void setApkLogger(APKLogger logger) {
        this.apkLogger = logger;
        if (logger != null && apkModule.getApkLogger() == null) {
            this.apkModule.setAPKLogger(logger);
        }
    }
    private void logMessage(String msg) {
        APKLogger apkLogger = getApkLogger();
        if (apkLogger != null) {
            apkLogger.logMessage(msg);
        }
    }
    private void logVerbose(String msg) {
        APKLogger apkLogger = getApkLogger();
        if (apkLogger != null) {
            apkLogger.logVerbose(msg);
        }
    }
}
