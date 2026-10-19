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

import com.reandroid.arsc.chunk.*;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TableBlockJsonBuilder {

    private final Set<File> parsedFiles;

    public TableBlockJsonBuilder() {
        this.parsedFiles = new HashSet<>();
    }

    public TableBlock scanDirectory(File resourcesDir) throws IOException {
        if (!resourcesDir.isDirectory()) {
            throw new IOException("No such directory: "+resourcesDir);
        }
        List<File> packageDirectories = ApkUtil.listPackageDirectories(resourcesDir);
        if (packageDirectories.size() == 0) {
            throw new IOException("No package sub directory found in : " + resourcesDir);
        }
        TableBlock tableBlock = new TableBlock();
        parsedFiles.clear();
        for (File packageDir : packageDirectories) {
            scanPackageDirectory(tableBlock, packageDir);
        }
        tableBlock.sortPackages();
        tableBlock.refresh();
        return tableBlock;
    }
    private void scanPackageDirectory(TableBlock tableBlock, File packageDir) throws IOException {
        File packageJsonFile = new File(packageDir, PackageBlock.FILE_NAME_JSON);
        if (!parsedFiles.add(packageJsonFile)) {
            return;
        }
        if (!packageJsonFile.isFile()) {
            throw new IOException("Invalid package directory! Package file missing: "
                    + packageJsonFile);
        }
        JSONObject jsonObject = new JSONObject(packageJsonFile);

        PackageBlock packageBlock = tableBlock.getOrCreatePackage(
                jsonObject.getInt(PackageBlock.NAME_package_id),
                jsonObject.optString(PackageBlock.NAME_package_name,
                        PackageBlock.EMPTY_PACKAGE_NAME));

        packageBlock.fromJson(jsonObject);

        loadExtras(packageBlock, packageDir);

        List<File> typeFileList = ApkUtil.listFiles(packageDir, ApkUtil.JSON_FILE_EXTENSION);

        for (File typeFile:typeFileList) {
            loadType(packageBlock, typeFile);
        }
        packageBlock.sortTypes();
    }
    private void loadType(PackageBlock packageBlock, File typeJsonFile) throws IOException {
        if (!parsedFiles.add(typeJsonFile)) {
            return;
        }
        JSONObject jsonObject = new JSONObject(typeJsonFile);
        ResConfig resConfig = new ResConfig();
        resConfig.fromJson(jsonObject.getJSONObject(TypeBlock.NAME_config));
        TypeBlock typeBlock = packageBlock.getSpecTypePairArray()
                .getOrCreate(jsonObject.getInt(TypeBlock.NAME_id), resConfig);
        typeBlock.fromJson(jsonObject);
    }
    private void loadExtras(PackageBlock packageBlock, File packageDir) throws IOException {
        loadLibrary(packageBlock, packageDir);
        loadOverlayable(packageBlock, packageDir);
        loadStagedAlias(packageBlock, packageDir);
    }
    private void loadLibrary(PackageBlock packageBlock, File packageDir) throws IOException {
        File file = new File(packageDir, LibraryBlock.FILE_NAME_JSON);
        if (!file.isFile() || !parsedFiles.add(file)) {
            return;
        }
        packageBlock.getLibraryList().fromJson(new JSONArray(file));
    }
    private void loadOverlayable(PackageBlock packageBlock, File packageDir) throws IOException {
        File file = new File(packageDir, Overlayable.FILE_NAME_JSON);
        if (!file.isFile() || !parsedFiles.add(file)) {
            return;
        }
        packageBlock.getOverlayableList().fromJson(new JSONArray(file));
    }
    private void loadStagedAlias(PackageBlock packageBlock, File packageDir) throws IOException {
        File file = new File(packageDir, StagedAlias.FILE_NAME_JSON);
        if (!file.isFile() || !parsedFiles.add(file)) {
            return;
        }
        packageBlock.getStagedAliasList().fromJson(new JSONArray(file));
    }
}
