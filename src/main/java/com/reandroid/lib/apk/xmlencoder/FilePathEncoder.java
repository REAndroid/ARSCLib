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

import com.reandroid.lib.apk.ApkUtil;
import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.TypeBlock;
import com.reandroid.lib.arsc.value.EntryBlock;

import java.io.File;
import java.util.List;

public class FilePathEncoder {
    private final EncodeMaterials materials;
    public FilePathEncoder(EncodeMaterials encodeMaterials){
        this.materials =encodeMaterials;
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
    public void encodeFileEntry(File resFile){
        String type = EncodeUtil.getTypeNameFromResFile(resFile);
        PackageBlock packageBlock = materials.getCurrentPackage();
        byte typeId=packageBlock
                .getTypeStringPool().idOf(type);
        String qualifiers = EncodeUtil.getQualifiersFromResFile(resFile);
        TypeBlock typeBlock = packageBlock.getOrCreateTypeBlock(typeId, qualifiers);
        String name = EncodeUtil.getEntryNameFromResFile(resFile);
        int resourceId=materials.resolveLocalResourceId(type, name);

        EntryBlock entryBlock=typeBlock
                .getOrCreateEntry((short) (0xffff & resourceId));

        entryBlock.setValueAsString(EncodeUtil.getEntryPathFromResFile(resFile));
        entryBlock.setSpecReference(materials.getSpecString(name));
        if(resFile.getName().endsWith(".xml")){
            XMLFileEncoder fileEncoder=new XMLFileEncoder(materials);
            fileEncoder.encode(resFile);
        }
    }
}
