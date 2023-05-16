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

import com.reandroid.archive.*;
import com.reandroid.archive2.Archive;
import com.reandroid.archive2.block.ApkSignatureBlock;
import com.reandroid.archive2.writer.ApkWriter;
import com.reandroid.arsc.ApkFile;
import com.reandroid.arsc.array.PackageArray;
import com.reandroid.arsc.chunk.Chunk;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.decoder.Decoder;
import com.reandroid.arsc.group.StringGroup;
import com.reandroid.arsc.item.TableString;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.util.FrameworkTable;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.xml.XMLDocument;
import com.reandroid.xml.XMLElement;
import com.reandroid.xml.XMLException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;

public class ApkModule implements ApkFile {
    private final String moduleName;
    private final APKArchive apkArchive;
    private boolean loadDefaultFramework = true;
    private boolean mDisableLoadFramework = false;
    private TableBlock mTableBlock;
    private AndroidManifestBlock mManifestBlock;
    private final UncompressedFiles mUncompressedFiles;
    private APKLogger apkLogger;
    private Decoder mDecoder;
    private ApkType mApkType;
    private ApkSignatureBlock apkSignatureBlock;
    private Integer preferredFramework;

    public ApkModule(String moduleName, APKArchive apkArchive){
        this.moduleName=moduleName;
        this.apkArchive=apkArchive;
        this.mUncompressedFiles=new UncompressedFiles();
        this.mUncompressedFiles.addPath(apkArchive);
    }

    public ApkSignatureBlock getApkSignatureBlock() {
        return apkSignatureBlock;
    }
    public void setApkSignatureBlock(ApkSignatureBlock apkSignatureBlock) {
        this.apkSignatureBlock = apkSignatureBlock;
    }

    public boolean hasSignatureBlock(){
        return getApkSignatureBlock() != null;
    }

    public void dumpSignatureInfoFiles(File directory) throws IOException{
        ApkSignatureBlock apkSignatureBlock = getApkSignatureBlock();
        if(apkSignatureBlock == null){
            throw new IOException("Don't have signature block");
        }
        apkSignatureBlock.writeSplitRawToDirectory(directory);
    }
    public void dumpSignatureBlock(File file) throws IOException{
        ApkSignatureBlock apkSignatureBlock = getApkSignatureBlock();
        if(apkSignatureBlock == null){
            throw new IOException("Don't have signature block");
        }
        apkSignatureBlock.writeRaw(file);
    }

    public void scanSignatureInfoFiles(File directory) throws IOException{
        if(!directory.isDirectory()){
            throw new IOException("No such directory: " + directory);
        }
        ApkSignatureBlock apkSignatureBlock = this.apkSignatureBlock;
        if(apkSignatureBlock == null){
            apkSignatureBlock = new ApkSignatureBlock();
        }
        apkSignatureBlock.scanSplitFiles(directory);
        setApkSignatureBlock(apkSignatureBlock);
    }
    public void loadSignatureBlock(File file) throws IOException{
        if(!file.isFile()){
            throw new IOException("No such file: " + file);
        }
        ApkSignatureBlock apkSignatureBlock = this.apkSignatureBlock;
        if(apkSignatureBlock == null){
            apkSignatureBlock = new ApkSignatureBlock();
        }
        apkSignatureBlock.read(file);
        setApkSignatureBlock(apkSignatureBlock);
    }

    public String getSplit(){
        if(!hasAndroidManifestBlock()){
            return null;
        }
        return getAndroidManifestBlock().getSplit();
    }
    public FrameworkApk initializeAndroidFramework(TableBlock tableBlock, Integer version) throws IOException {
        if(tableBlock == null || isAndroid(tableBlock)){
            return null;
        }
        List<TableBlock> frameWorkList = tableBlock.getFrameWorks();
        for(TableBlock frameWork:frameWorkList){
            if(isAndroid(frameWork)){
                ApkFile apkFile = frameWork.getApkFile();
                if(apkFile instanceof FrameworkApk){
                    return (FrameworkApk) apkFile;
                }
                return null;
            }
        }
        logMessage("Initializing android framework ...");
        FrameworkApk frameworkApk;
        if(version==null){
            logMessage("Can not read framework version, loading latest");
            frameworkApk = AndroidFrameworks.getLatest();
        }else {
            logMessage("Loading android framework for version: " + version);
            frameworkApk = AndroidFrameworks.getBestMatch(version);
        }
        FrameworkTable frameworkTable = frameworkApk.getTableBlock();
        tableBlock.addFramework(frameworkTable);
        logMessage("Initialized framework: "+frameworkApk.getName());
        return frameworkApk;
    }
    private boolean isAndroid(TableBlock tableBlock){
        if(tableBlock instanceof FrameworkTable){
            FrameworkTable frameworkTable = (FrameworkTable) tableBlock;
            return frameworkTable.isAndroid();
        }
        return false;
    }

    public FrameworkApk initializeAndroidFramework(XmlPullParser parser) throws IOException {
        Map<String, String> manifestAttributes;
        try {
            manifestAttributes = XmlHelper.readAttributes(parser, AndroidManifestBlock.TAG_manifest);
        } catch (XmlPullParserException ex) {
            throw new IOException(ex);
        }
        if(manifestAttributes == null){
            throw new IOException("Invalid AndroidManifest, missing element: '"
                    + AndroidManifestBlock.TAG_manifest + "'");
        }
        return initializeAndroidFramework(manifestAttributes);
    }
    public FrameworkApk initializeAndroidFramework(Map<String, String> manifestAttributes) throws IOException {
        String coreApp = manifestAttributes.get(AndroidManifestBlock.NAME_coreApp);
        String packageName = manifestAttributes.get(AndroidManifestBlock.NAME_PACKAGE);
        if("true".equals(coreApp) && "android".equals(packageName)){
            logMessage("Looks framework itself, skip loading frameworks");
            return null;
        }
        String compileSdkVersion = manifestAttributes.get(AndroidManifestBlock.NAME_compileSdkVersion);
        if(compileSdkVersion == null){
            logMessage("Missing attribute: '" + AndroidManifestBlock.NAME_compileSdkVersion + "', skip loading frameworks");
            return null;
        }
        int version;
        try{
            version = Integer.parseInt(compileSdkVersion);
        }catch (NumberFormatException exception){
            logMessage("NumberFormatException on reading: '"
                    + AndroidManifestBlock.NAME_compileSdkVersion + "=\""
                    + compileSdkVersion +"\"' : " + exception.getMessage());
            return null;
        }
        TableBlock tableBlock = getTableBlock(false);
        return initializeAndroidFramework(tableBlock, version);
    }
    public FrameworkApk initializeAndroidFramework(XMLDocument xmlDocument) throws IOException {
        TableBlock tableBlock = getTableBlock(false);
        if(isAndroidCoreApp(xmlDocument)){
            logMessage("Looks framework itself, skip loading frameworks");
            return null;
        }
        Integer version = readCompileVersionCode(xmlDocument);
        return initializeAndroidFramework(tableBlock, version);
    }
    private boolean isAndroidCoreApp(XMLDocument manifestDocument){
        XMLElement root = manifestDocument.getDocumentElement();
        if(root == null){
            return false;
        }
        if(!"android".equals(root.getAttributeValue("package"))){
            return false;
        }
        String coreApp = root.getAttributeValue("coreApp");
        return "true".equals(coreApp);
    }
    private Integer readCompileVersionCode(XMLDocument manifestDocument) {
        XMLElement root = manifestDocument.getDocumentElement();
        String versionString = readVersionCodeString(root);
        if(versionString==null){
            return null;
        }
        try{
            return Integer.parseInt(versionString);
        }catch (NumberFormatException exception){
            logMessage("NumberFormatException on manifest version reading: '"
                    +versionString+"': "+exception.getMessage());
            return null;
        }
    }
    private String readVersionCodeString(XMLElement manifestRoot){
        String versionString = manifestRoot.getAttributeValue("android:compileSdkVersion");
        if(versionString!=null){
            return versionString;
        }
        versionString = manifestRoot.getAttributeValue("platformBuildVersionCode");
        if(versionString!=null){
            return versionString;
        }
        for(XMLElement element:manifestRoot.listChildElements()){
            if(AndroidManifestBlock.TAG_uses_sdk.equals(element.getTagName())){
                versionString = element.getAttributeValue("android:targetSdkVersion");
                if(versionString!=null){
                    return versionString;
                }
            }
        }
        return null;
    }

    public void setPreferredFramework(Integer version) throws IOException {
        if(version!=null && version.equals(preferredFramework)){
            return;
        }
        this.preferredFramework = version;
        if(version == null || mTableBlock==null){
            return;
        }
        logMessage("Initializing preferred framework: " + version);
        mTableBlock.clearFrameworks();
        FrameworkApk frameworkApk = AndroidFrameworks.getBestMatch(version);
        AndroidFrameworks.setCurrent(frameworkApk);
        mTableBlock.addFramework(frameworkApk.getTableBlock());
        logMessage("Initialized framework: " + frameworkApk.getVersionCode());
    }

    public Integer getAndroidFrameworkVersion(){
        if(preferredFramework != null){
            return preferredFramework;
        }
        if(!hasAndroidManifestBlock()){
            return null;
        }
        AndroidManifestBlock manifestBlock = getAndroidManifestBlock();
        Integer version = manifestBlock.getCompileSdkVersion();
        if(version == null){
            version = manifestBlock.getPlatformBuildVersionCode();
        }
        if(version == null){
            version = manifestBlock.getTargetSdkVersion();
        }
        return version;
    }
    public void removeResFilesWithEntry(int resourceId) {
        removeResFilesWithEntry(resourceId, null, true);
    }
    public void removeResFilesWithEntry(int resourceId, ResConfig resConfig, boolean trimEntryArray) {
        List<Entry> removedList = removeResFiles(resourceId, resConfig);
        SpecTypePair specTypePair = null;
        for(Entry entry:removedList){
            if(entry == null || entry.isNull()){
                continue;
            }
            if(trimEntryArray && specTypePair==null){
                specTypePair = entry.getTypeBlock().getParentSpecTypePair();
            }
            entry.setNull(true);
        }
        if(specTypePair!=null){
            specTypePair.removeNullEntries(resourceId);
        }
    }
    public List<Entry> removeResFiles(int resourceId) {
        return removeResFiles(resourceId, null);
    }
    public List<Entry> removeResFiles(int resourceId, ResConfig resConfig) {
        List<Entry> results = new ArrayList<>();
        if(resourceId == 0 && resConfig==null){
            return results;
        }
        List<ResFile> resFileList = listResFiles(resourceId, resConfig);
        APKArchive archive = getApkArchive();
        for(ResFile resFile:resFileList){
            results.addAll(resFile.getEntryList());
            String path = resFile.getFilePath();
            archive.remove(path);
        }
        return results;
    }
    public XMLDocument decodeXMLFile(String path) throws IOException, XMLException {
        ResXmlDocument resXmlDocument = loadResXmlDocument(path);
        AndroidManifestBlock manifestBlock = getAndroidManifestBlock();
        int pkgId = manifestBlock.guessCurrentPackageId();
        return resXmlDocument.decodeToXml(getTableBlock(), pkgId);
    }
    public List<DexFileInputSource> listDexFiles(){
        List<DexFileInputSource> results=new ArrayList<>();
        for(InputSource source:getApkArchive().listInputSources()){
            if(DexFileInputSource.isDexName(source.getAlias())){
                results.add(new DexFileInputSource(source.getAlias(), source));
            }
        }
        DexFileInputSource.sort(results);
        return results;
    }
    public boolean isBaseModule(){
        if(!hasAndroidManifestBlock()){
            return false;
        }
        AndroidManifestBlock manifestBlock;
        try {
            manifestBlock=getAndroidManifestBlock();
            return manifestBlock.getMainActivity()!=null;
        } catch (Exception ignored) {
            return false;
        }
    }
    public String getModuleName(){
        return moduleName;
    }
    public void writeApk(File file) throws IOException {
        writeApk(file, null);
    }
    public void writeApk(File file, WriteProgress progress) throws IOException {
        writeApk(file, progress, null);
    }
    public void writeApk(File file, WriteProgress progress, WriteInterceptor interceptor) throws IOException {
        APKArchive archive = getApkArchive();
        UncompressedFiles uf = getUncompressedFiles();
        uf.apply(archive);
        ApkWriter apkWriter = new ApkWriter(file, archive.listInputSources());
        apkWriter.setAPKLogger(getApkLogger());
        apkWriter.setWriteProgress(progress);
        apkWriter.setApkSignatureBlock(getApkSignatureBlock());
        apkWriter.write();
        apkWriter.close();
    }
    public void uncompressNonXmlResFiles() {
        for(ResFile resFile:listResFiles()){
            if(resFile.isBinaryXml()){
                continue;
            }
            resFile.getInputSource().setMethod(ZipEntry.STORED);
        }
    }
    public UncompressedFiles getUncompressedFiles(){
        return mUncompressedFiles;
    }
    public void removeDir(String dirName){
        getApkArchive().removeDir(dirName);
    }
    public void validateResourcesDir() {
        List<ResFile> resFileList = listResFiles();
        Set<String> existPaths=new HashSet<>();
        List<InputSource> sourceList = getApkArchive().listInputSources();
        for(InputSource inputSource:sourceList){
            existPaths.add(inputSource.getAlias());
        }
        for(ResFile resFile:resFileList){
            String path=resFile.getFilePath();
            String pathNew=resFile.validateTypeDirectoryName();
            if(pathNew==null || pathNew.equals(path)){
                continue;
            }
            if(existPaths.contains(pathNew)){
                continue;
            }
            existPaths.remove(path);
            existPaths.add(pathNew);
            resFile.setFilePath(pathNew);
            if(resFile.getInputSource().getMethod() == ZipEntry.STORED){
                getUncompressedFiles().replacePath(path, pathNew);
            }
            logVerbose("Dir validated: '"+path+"' -> '"+pathNew+"'");
        }
        TableStringPool stringPool= getTableBlock().getStringPool();
        stringPool.refreshUniqueIdMap();
        getTableBlock().refresh();
    }
    public void setResourcesRootDir(String dirName) {
        List<ResFile> resFileList = listResFiles();
        Set<String> existPaths=new HashSet<>();
        List<InputSource> sourceList = getApkArchive().listInputSources();
        for(InputSource inputSource:sourceList){
            existPaths.add(inputSource.getAlias());
        }
        for(ResFile resFile:resFileList){
            String path=resFile.getFilePath();
            String pathNew=ApkUtil.replaceRootDir(path, dirName);
            if(existPaths.contains(pathNew)){
                continue;
            }
            existPaths.remove(path);
            existPaths.add(pathNew);
            resFile.setFilePath(pathNew);
            if(resFile.getInputSource().getMethod() == ZipEntry.STORED){
                getUncompressedFiles().replacePath(path, pathNew);
            }
            logVerbose("Root changed: '"+path+"' -> '"+pathNew+"'");
        }
        TableStringPool stringPool= getTableBlock().getStringPool();
        stringPool.refreshUniqueIdMap();
        getTableBlock().refresh();
    }
    public List<ResFile> listResFiles() {
        return listResFiles(0, null);
    }
    public List<ResFile> listResFiles(int resourceId, ResConfig resConfig) {
        List<ResFile> results=new ArrayList<>();
        TableBlock tableBlock=getTableBlock();
        if (tableBlock==null){
            return results;
        }
        TableStringPool stringPool= tableBlock.getStringPool();
        for(InputSource inputSource:getApkArchive().listInputSources()){
            String name=inputSource.getAlias();
            StringGroup<TableString> groupTableString = stringPool.get(name);
            if(groupTableString==null){
                continue;
            }
            for(TableString tableString:groupTableString.listItems()){
                List<Entry> entryList = filterResFileEntries(
                        tableString.listReferencedResValueEntries(), resourceId, resConfig);
                if(entryList.size()==0){
                    continue;
                }
                ResFile resFile = new ResFile(inputSource, entryList);
                results.add(resFile);
            }
        }
        return results;
    }
    private List<Entry> filterResFileEntries(List<Entry> entryList, int resourceId, ResConfig resConfig){
        if(resourceId == 0 && resConfig == null || entryList.size()==0){
            return entryList;
        }
        List<Entry> results = new ArrayList<>();
        for(Entry entry:entryList){
            if(entry==null || entry.isNull()){
                continue;
            }
            if(resourceId!=0 && resourceId!=entry.getResourceId()){
                continue;
            }
            if(resConfig!=null && !resConfig.equals(entry.getResConfig())){
                continue;
            }
            results.add(entry);
        }
        return results;
    }
    public String getPackageName(){
        if(hasAndroidManifestBlock()){
            return getAndroidManifestBlock().getPackageName();
        }
        if(!hasTableBlock()){
            return null;
        }
        TableBlock tableBlock=getTableBlock();
        PackageArray pkgArray = tableBlock.getPackageArray();
        PackageBlock pkg = pkgArray.get(0);
        if(pkg==null){
            return null;
        }
        return pkg.getName();
    }
    public void setPackageName(String name) {
        String old=getPackageName();
        if(hasAndroidManifestBlock()){
            getAndroidManifestBlock().setPackageName(name);
        }
        if(!hasTableBlock()){
            return;
        }
        TableBlock tableBlock=getTableBlock();
        PackageArray pkgArray = tableBlock.getPackageArray();
        for(PackageBlock pkg:pkgArray.listItems()){
            if(pkgArray.childesCount()==1){
                pkg.setName(name);
                continue;
            }
            String pkgName=pkg.getName();
            if(pkgName.startsWith(old)){
                pkgName=pkgName.replace(old, name);
                pkg.setName(pkgName);
            }
        }
    }
    public boolean hasAndroidManifestBlock(){
        return mManifestBlock!=null
                || getApkArchive().getInputSource(AndroidManifestBlock.FILE_NAME)!=null;
    }
    public boolean hasTableBlock(){
        return mTableBlock!=null
                || getApkArchive().getInputSource(TableBlock.FILE_NAME)!=null;
    }
    public void destroy(){
        getApkArchive().clear();
        AndroidManifestBlock manifestBlock = this.mManifestBlock;
        if(manifestBlock!=null){
            manifestBlock.destroy();
            this.mManifestBlock = null;
        }
        TableBlock tableBlock = this.mTableBlock;
        if(tableBlock!=null){
            tableBlock.destroy();
            this.mTableBlock = null;
        }
    }
    public void setManifest(AndroidManifestBlock manifestBlock){
        APKArchive archive = getApkArchive();
        if(manifestBlock==null){
            mManifestBlock = null;
            archive.remove(AndroidManifestBlock.FILE_NAME);
            return;
        }
        manifestBlock.setApkFile(this);
        BlockInputSource<AndroidManifestBlock> source =
                new BlockInputSource<>(AndroidManifestBlock.FILE_NAME, manifestBlock);
        archive.add(source);
        mManifestBlock = manifestBlock;
    }
    public void setTableBlock(TableBlock tableBlock){
        APKArchive archive = getApkArchive();
        if(tableBlock == null){
            mTableBlock = null;
            archive.remove(TableBlock.FILE_NAME);
            return;
        }
        tableBlock.setApkFile(this);
        BlockInputSource<TableBlock> source =
                new BlockInputSource<>(TableBlock.FILE_NAME, tableBlock);
        archive.add(source);
        source.setMethod(ZipEntry.STORED);
        getUncompressedFiles().addPath(source);
        mTableBlock = tableBlock;
    }
    @Override
    public AndroidManifestBlock getAndroidManifestBlock() {
        if(mManifestBlock!=null){
            return mManifestBlock;
        }
        APKArchive archive=getApkArchive();
        InputSource inputSource = archive.getInputSource(AndroidManifestBlock.FILE_NAME);
        if(inputSource==null){
            return null;
        }
        InputStream inputStream = null;
        try {
            inputStream = inputSource.openStream();
            AndroidManifestBlock manifestBlock=AndroidManifestBlock.load(inputStream);
            inputStream.close();
            BlockInputSource<AndroidManifestBlock> blockInputSource=new BlockInputSource<>(inputSource.getName(),manifestBlock);
            blockInputSource.setSort(inputSource.getSort());
            blockInputSource.setMethod(inputSource.getMethod());
            archive.add(blockInputSource);
            manifestBlock.setApkFile(this);
            TableBlock tableBlock = this.mTableBlock;
            if(tableBlock != null){
                int packageId = manifestBlock.guessCurrentPackageId();
                if(packageId != 0){
                    manifestBlock.setPackageBlock(tableBlock.pickOne(packageId));
                }else {
                    manifestBlock.setPackageBlock(tableBlock.pickOne());
                }
            }
            mManifestBlock = manifestBlock;
            onManifestBlockLoaded(manifestBlock);
        } catch (IOException exception) {
            throw new IllegalArgumentException(exception);
        }
        return mManifestBlock;
    }
    private void onManifestBlockLoaded(AndroidManifestBlock manifestBlock){
        initializeApkType(manifestBlock);
    }
    public TableBlock getTableBlock(boolean initFramework) {
        if(mTableBlock==null){
            if(!hasTableBlock()){
                return null;
            }
            try {
                mTableBlock = loadTableBlock();
                if(initFramework && loadDefaultFramework){
                    Integer version = getAndroidFrameworkVersion();
                    initializeAndroidFramework(mTableBlock, version);
                }
            } catch (IOException exception) {
                throw new IllegalArgumentException(exception);
            }
        }
        return mTableBlock;
    }
    @Override
    public TableBlock getTableBlock() {
        return getTableBlock(!mDisableLoadFramework);
    }
    @Override
    public ResXmlDocument loadResXmlDocument(String path) throws IOException{
        InputSource inputSource = getApkArchive().getInputSource(path);
        if(inputSource==null){
            throw new FileNotFoundException("No such file in apk: " + path);
        }
        return loadResXmlDocument(inputSource);
    }
    public ResXmlDocument loadResXmlDocument(InputSource inputSource) throws IOException{
        ResXmlDocument resXmlDocument = new ResXmlDocument();
        resXmlDocument.setApkFile(this);
        resXmlDocument.readBytes(inputSource.openStream());
        return resXmlDocument;
    }
    @Override
    public Decoder getDecoder(){
        return mDecoder;
    }
    @Override
    public void setDecoder(Decoder decoder){
        this.mDecoder = decoder;
    }
    public ApkType getApkType(){
        if(mApkType!=null){
            return mApkType;
        }
        return initializeApkType(mManifestBlock);
    }
    public void setApkType(ApkType apkType){
        this.mApkType = apkType;
    }
    private ApkType initializeApkType(AndroidManifestBlock manifestBlock){
        if(mApkType!=null){
            return mApkType;
        }
        ApkType apkType = null;
        if(manifestBlock!=null){
            apkType = manifestBlock.guessApkType();
        }
        if(apkType != null){
            mApkType = apkType;
        }else {
            apkType = ApkType.UNKNOWN;
        }
        return apkType;
    }

    // If we need TableStringPool only, this loads pool without
    // loading packages and other chunk blocks for faster and less memory usage
    public TableStringPool getVolatileTableStringPool() throws IOException{
        if(mTableBlock!=null){
            return mTableBlock.getStringPool();
        }
        InputSource inputSource = getApkArchive()
                .getInputSource(TableBlock.FILE_NAME);
        if(inputSource==null){
            throw new IOException("Module don't have: "+TableBlock.FILE_NAME);
        }
        if((inputSource instanceof ZipEntrySource)
                ||(inputSource instanceof FileInputSource)){
            InputStream inputStream = inputSource.openStream();
            TableStringPool stringPool = TableStringPool.readFromTable(inputStream);
            inputStream.close();
            return stringPool;
        }
        return getTableBlock().getStringPool();
    }
    TableBlock loadTableBlock() throws IOException {
        APKArchive archive=getApkArchive();
        InputSource inputSource = archive.getInputSource(TableBlock.FILE_NAME);
        if(inputSource==null){
            throw new IOException("Entry not found: "+TableBlock.FILE_NAME);
        }
        TableBlock tableBlock;
        if(inputSource instanceof SplitJsonTableInputSource){
            tableBlock=((SplitJsonTableInputSource)inputSource).getTableBlock();
        }else if(inputSource instanceof SingleJsonTableInputSource){
            tableBlock=((SingleJsonTableInputSource)inputSource).getTableBlock();
        }else if(inputSource instanceof BlockInputSource){
            Chunk<?> block = ((BlockInputSource<?>) inputSource).getBlock();
            tableBlock = (TableBlock) block;
        }else {
            InputStream inputStream = inputSource.openStream();
            tableBlock = TableBlock.load(inputStream);
            inputStream.close();
        }
        BlockInputSource<TableBlock> blockInputSource=new BlockInputSource<>(inputSource.getName(), tableBlock);
        blockInputSource.setMethod(inputSource.getMethod());
        blockInputSource.setSort(inputSource.getSort());
        archive.add(blockInputSource);
        tableBlock.setApkFile(this);
        return tableBlock;
    }
    public APKArchive getApkArchive() {
        return apkArchive;
    }
    public void setLoadDefaultFramework(boolean loadDefaultFramework) {
        this.loadDefaultFramework = loadDefaultFramework;
        this.mDisableLoadFramework = !loadDefaultFramework;
    }

    public void merge(ApkModule module) throws IOException {
        if(module==null||module==this){
            return;
        }
        logMessage("Merging: "+module.getModuleName());
        mergeDexFiles(module);
        mergeTable(module);
        mergeFiles(module);
        getUncompressedFiles().merge(module.getUncompressedFiles());
    }
    private void mergeTable(ApkModule module) {
        if(!module.hasTableBlock()){
            return;
        }
        logMessage("Merging resource table: "+module.getModuleName());
        TableBlock exist;
        if(!hasTableBlock()){
            exist=new TableBlock();
            BlockInputSource<TableBlock> inputSource=new BlockInputSource<>(TableBlock.FILE_NAME, exist);
            getApkArchive().add(inputSource);
        }else{
            exist=getTableBlock();
        }
        TableBlock coming=module.getTableBlock();
        exist.merge(coming);
    }
    private void mergeFiles(ApkModule module) {
        APKArchive archiveExist = getApkArchive();
        APKArchive archiveComing = module.getApkArchive();
        Map<String, InputSource> comingAlias=ApkUtil.toAliasMap(archiveComing.listInputSources());
        Map<String, InputSource> existAlias=ApkUtil.toAliasMap(archiveExist.listInputSources());
        UncompressedFiles uncompressedFiles = module.getUncompressedFiles();
        for(InputSource inputSource:comingAlias.values()){
            if(existAlias.containsKey(inputSource.getAlias())||existAlias.containsKey(inputSource.getName())){
                continue;
            }
            if(DexFileInputSource.isDexName(inputSource.getName())){
                continue;
            }
            if (inputSource.getAlias().startsWith("lib/")){
                uncompressedFiles.removePath(inputSource.getAlias());
            }
            logVerbose("Added: "+inputSource.getAlias());
            archiveExist.add(inputSource);
        }
    }
    private void mergeDexFiles(ApkModule module){
        UncompressedFiles uncompressedFiles=module.getUncompressedFiles();
        List<DexFileInputSource> existList=listDexFiles();
        List<DexFileInputSource> comingList=module.listDexFiles();
        APKArchive archive=getApkArchive();
        int index=0;
        if(existList.size()>0){
            index=existList.get(existList.size()-1).getDexNumber();
            if(index==0){
                index=2;
            }else {
                index++;
            }
        }
        for(DexFileInputSource source:comingList){
            uncompressedFiles.removePath(source.getAlias());
            String name= DexFileInputSource.getDexName(index);
            DexFileInputSource add=new DexFileInputSource(name, source.getInputSource());
            archive.add(add);
            logMessage("Added ["+module.getModuleName()+"] "
                    +source.getAlias()+" -> "+name);
            index++;
            if(index==1){
                index=2;
            }
        }
    }
    APKLogger getApkLogger(){
        return apkLogger;
    }
    public void setAPKLogger(APKLogger logger) {
        this.apkLogger = logger;
    }
    void logMessage(String msg) {
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
    @Override
    public String toString(){
        return getModuleName();
    }
    public static ApkModule loadApkFile(File apkFile) throws IOException {
        return loadApkFile(apkFile, ApkUtil.DEF_MODULE_NAME);
    }
    public static ApkModule loadApkFile(File apkFile, String moduleName) throws IOException {
        Archive archive = new Archive(apkFile);
        ApkModule apkModule = new ApkModule(moduleName, archive.createAPKArchive());
        apkModule.setApkSignatureBlock(archive.getApkSignatureBlock());
        return apkModule;
    }
}
