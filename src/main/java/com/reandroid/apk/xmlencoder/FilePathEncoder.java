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

import com.reandroid.archive.APKArchive;
import com.reandroid.archive.FileInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.apk.ApkUtil;
import com.reandroid.apk.UncompressedFiles;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.value.Entry;
import com.reandroid.xml.source.XMLFileSource;
import com.reandroid.xml.source.XMLSource;

import java.io.File;
import java.util.List;

public class FilePathEncoder {
    private final EncodeMaterials materials;
    private APKArchive apkArchive;
    private UncompressedFiles uncompressedFiles;
    public FilePathEncoder(EncodeMaterials encodeMaterials){
        this.materials =encodeMaterials;
    }

    public void setApkArchive(APKArchive apkArchive) {
        this.apkArchive = apkArchive;
    }
    public void setUncompressedFiles(UncompressedFiles uncompressedFiles){
        this.uncompressedFiles=uncompressedFiles;
    }
    public void encodeResDir(File resDir){
        materials.logVerbose("Scanning file list: "
                +resDir.getParentFile().getName()
                +File.separator+resDir.getName());
        List<File> dirList = ApkUtil.listDirectories(resDir);
        for(File dir:dirList){
            if(dir.getName().startsWith("values")){
                continue;
            }
            encodeTypeDir(dir);
        }
    }
    public void encodeTypeDir(File dir){
        List<File> fileList = ApkUtil.listFiles(dir, null);
        for(File file:fileList){
            encodeFileEntry(file);
        }
    }
    public InputSource encodeFileEntry(File resFile){
        String type = EncodeUtil.getTypeNameFromResFile(resFile);
        PackageBlock packageBlock = materials.getCurrentPackage();
        int typeId=packageBlock
                .getTypeStringPool().idOf(type);
        String qualifiers = EncodeUtil.getQualifiersFromResFile(resFile);
        TypeBlock typeBlock = packageBlock.getOrCreateTypeBlock((byte)typeId, qualifiers);
        String name = EncodeUtil.getEntryNameFromResFile(resFile);
        int resourceId=materials.resolveLocalResourceId(type, name);

        Entry entry = typeBlock
                .getOrCreateEntry((short) (0xffff & resourceId));

        String path = EncodeUtil.getEntryPathFromResFile(resFile);
        entry.setValueAsString(path);
        materials.setEntryName(entry, name);
        InputSource inputSource=createInputSource(path, resFile);
        if(inputSource instanceof XMLEncodeSource){
            ((XMLEncodeSource)inputSource).setEntry(entry);
        }
        addInputSource(inputSource);
        return inputSource;
    }
    private InputSource createInputSource(String path, File resFile){
        if(isXmlFile(resFile)){
            return createXMLEncodeInputSource(path, resFile);
        }
        addUncompressedFiles(path);
        return createRawFileInputSource(path, resFile);
    }
    private InputSource createRawFileInputSource(String path, File resFile){
        return new FileInputSource(resFile, path);
    }
    private InputSource createXMLEncodeInputSource(String path, File resFile){
        XMLSource xmlSource = new XMLFileSource(path, resFile);
        return new XMLEncodeSource(materials, xmlSource);
    }
    private boolean isXmlFile(File resFile){
        String name=resFile.getName();
        if(!name.endsWith(".xml")){
            return false;
        }
        String type=EncodeUtil.getTypeNameFromResFile(resFile);
        return !type.equals("raw");
    }
    private void addInputSource(InputSource inputSource){
        if(inputSource!=null && this.apkArchive!=null){
            apkArchive.add(inputSource);
        }
    }
    private void addUncompressedFiles(String path){
        if(uncompressedFiles!=null){
            uncompressedFiles.addPath(path);
        }
    }
}
