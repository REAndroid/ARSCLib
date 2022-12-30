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

import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.StagedAlias;
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.arsc.chunk.TypeBlock;
import com.reandroid.lib.arsc.container.SpecTypePair;
import com.reandroid.lib.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class TableBlockJson {
    private final TableBlock tableBlock;
    public TableBlockJson(TableBlock tableBlock){
        this.tableBlock=tableBlock;
    }
    public void writeJsonFiles(File outDir) throws IOException {
        for(PackageBlock packageBlock: tableBlock.listPackages()){
            writeJsonFiles(outDir, packageBlock);
        }
    }
    private void writeJsonFiles(File rootDir, PackageBlock packageBlock) throws IOException {
        File pkgDir=new File(rootDir, getDirName(packageBlock));
        if(!pkgDir.exists()){
            pkgDir.mkdirs();
        }
        File infoFile=new File(pkgDir, PackageBlock.JSON_FILE_NAME);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(PackageBlock.NAME_package_id, packageBlock.getId());
        jsonObject.put(PackageBlock.NAME_package_name, packageBlock.getName());
        StagedAlias stagedAlias=StagedAlias.mergeAll(packageBlock.getStagedAliasList().getChildes());
        if(stagedAlias!=null){
            jsonObject.put(PackageBlock.NAME_staged_aliases, stagedAlias.getStagedAliasEntryArray().toJson());
        }
        jsonObject.write(infoFile);
        for(SpecTypePair specTypePair: packageBlock.listAllSpecTypePair()){
            for(TypeBlock typeBlock:specTypePair.getTypeBlockArray().listItems()){
                writeJsonFiles(pkgDir, typeBlock);
            }
        }
    }
    private void writeJsonFiles(File pkgDir, TypeBlock typeBlock) throws IOException {
        String name= getFileName(typeBlock)+".json";
        File file=new File(pkgDir, name);
        JSONObject jsonObject = typeBlock.toJson();
        jsonObject.write(file);
    }
    private String getFileName(TypeBlock typeBlock){
        StringBuilder builder=new StringBuilder();
        builder.append(String.format("0x%02x", typeBlock.getTypeId()));
        String name= typeBlock.getTypeName();
        builder.append('-').append(name);
        builder.append(typeBlock.getResConfig().getQualifiers());
        return builder.toString();
    }
    private String getDirName(PackageBlock packageBlock){
        StringBuilder builder=new StringBuilder();
        builder.append(String.format("0x%02x", packageBlock.getId()));
        builder.append("-");
        builder.append(packageBlock.getIndex());
        String name= packageBlock.getName();
        if(name!=null){
            builder.append('-');
            builder.append(name);
        }
        return builder.toString();
    }
}
