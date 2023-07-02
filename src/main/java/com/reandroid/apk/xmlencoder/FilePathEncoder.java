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
import com.reandroid.archive.FileInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.apk.ApkUtil;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.value.Entry;
import com.reandroid.xml.source.XMLFileParserSource;
import com.reandroid.xml.source.XMLParserSource;

import java.io.File;
import java.util.List;
import java.util.zip.ZipEntry;

public class FilePathEncoder {
    private final ApkModule apkModule;
    private APKLogger mLogger;
    private PackageBlock mCurrentPackage;
    public FilePathEncoder(ApkModule apkModule){
        this.apkModule = apkModule;
        this.mLogger = apkModule.getApkLogger();
    }
    private PackageBlock getCurrentPackage() {
        PackageBlock packageBlock = mCurrentPackage;
        if(packageBlock == null){
            TableBlock tableBlock = apkModule.getTableBlock();
            if(tableBlock == null){
                throw new NullPointerException("TableBlock == null");
            }
            packageBlock = tableBlock.pickOne();
            if(packageBlock == null){
                throw new NullPointerException("PackageBlock == null");
            }
            mCurrentPackage = packageBlock;
        }
        return mCurrentPackage;
    }

    public void encodePackageResDir(PackageBlock packageBlock, File resDir){
        this.mCurrentPackage = packageBlock;
        int count = 0;
        String simpleName = resDir.getParentFile().getName()
                + File.separator + resDir.getName();
        logMessage("Scan: " + simpleName);
        List<File> dirList = ApkUtil.listDirectories(resDir);
        for(File dir:dirList){
            if(ApkUtil.isValuesDirectoryName(dir.getName(), true)){
                continue;
            }
            count += encodeTypeDir(dir);
        }
        logMessage("Scanned " + count + " files: " + simpleName);
    }
    public int encodeTypeDir(File dir){
        List<File> fileList = ApkUtil.listFiles(dir, null);
        for(File file:fileList){
            encodeTypeFileEntry(file);
        }
        return fileList.size();
    }
    public InputSource encodeTypeFileEntry(File resFile){
        String type = EncodeUtil.getTypeNameFromResFile(resFile);
        String qualifiers = EncodeUtil.getQualifiersFromResFile(resFile);
        String name = EncodeUtil.getEntryNameFromResFile(resFile);
        String path = EncodeUtil.getEntryPathFromResFile(resFile);

        PackageBlock packageBlock = getCurrentPackage();
        ResourceEntry resourceEntry = packageBlock.getTableBlock()
                .getLocalResource(packageBlock, type, name);
        if(resourceEntry == null){
            throw new EncodeException("Local resource not defined: @" + type + "/" + name
                    + ", for path: " + path);
        }
        Entry entry = resourceEntry.getOrCreate(qualifiers);
        entry.setValueAsString(path);
        InputSource inputSource = createInputSource(
                resourceEntry.getPackageBlock(), path, resFile);
        addInputSource(inputSource);
        return inputSource;
    }
    private InputSource createInputSource(PackageBlock packageBlock, String path, File resFile){
        if(isXmlFile(resFile)){
            return createXMLEncodeInputSource(packageBlock, path, resFile);
        }
        return createRawFileInputSource(path, resFile);
    }
    private InputSource createRawFileInputSource(String path, File resFile){
        FileInputSource inputSource = new FileInputSource(resFile, path);
        inputSource.setMethod(ZipEntry.STORED);
        return inputSource;
    }
    private InputSource createXMLEncodeInputSource(PackageBlock packageBlock, String path, File resFile){
        XMLParserSource xmlSource = new XMLFileParserSource(path, resFile);
        XMLParseEncodeSource encodeSource = new XMLParseEncodeSource(packageBlock, xmlSource);
        encodeSource.setApkLogger(mLogger);
        return encodeSource;
    }
    private boolean isXmlFile(File resFile){
        String name=resFile.getName();
        if(!name.endsWith(".xml")){
            return false;
        }
        String type = EncodeUtil.getTypeNameFromResFile(resFile);
        if(!"raw".equals(type)){
            return true;
        }
        logMessage("WARN: Using un-encoded raw xml: " + resFile);
        return false;
    }
    private void addInputSource(InputSource inputSource){
        if(inputSource != null){
            apkModule.add(inputSource);
        }
    }
    public void setApkLogger(APKLogger logger){
        this.mLogger = logger;
    }
    private void logMessage(String msg){
        APKLogger apkLogger = this.mLogger;
        if(apkLogger != null){
            apkLogger.logMessage(msg);
        }
    }
    private void logVerbose(String msg){
        APKLogger apkLogger = this.mLogger;
        if(apkLogger != null){
            apkLogger.logVerbose(msg);
        }
    }
}
