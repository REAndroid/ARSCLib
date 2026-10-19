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

import com.reandroid.arsc.array.SpecTypePairArray;
import com.reandroid.arsc.chunk.LibraryBlock;
import com.reandroid.arsc.chunk.Overlayable;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.StagedAlias;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.TypeBlock;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class SplitJsonResourceDecoder {

    private final TableBlock tableBlock;

    public SplitJsonResourceDecoder(TableBlock tableBlock) {
        this.tableBlock = tableBlock;
    }

    public void decodeSplitJsonFiles(File resourcesDir) throws IOException {
        TableBlock tableBlock = this.tableBlock;
        int size = tableBlock.size();
        for (int i = 0; i < size; i++) {
            decodeSplitPackageJson(resourcesDir, tableBlock.get(i));
        }
    }
    private void decodeSplitPackageJson(File resourcesDir, PackageBlock packageBlock) throws IOException {
        File packageDir = new File(resourcesDir, packageBlock.buildDecodeDirectoryName());

        writeSplitPackageInfoJson(packageDir, packageBlock);

        SpecTypePairArray specTypePairArray = packageBlock.getSpecTypePairArray();
        int size = specTypePairArray.size();
        for (int i = 0; i < size; i++) {
            Iterator<TypeBlock> iterator = specTypePairArray.get(i).getTypeBlocks();
            while (iterator.hasNext()) {
                writeSplitTypeJsonFiles(packageDir, iterator.next());
            }
        }
    }
    private void writeSplitPackageInfoJson(File packageDirectory, PackageBlock packageBlock) throws IOException {
        packageBlock.toJson(false, false).write(
                new File(packageDirectory, PackageBlock.FILE_NAME_JSON));
        if (!packageBlock.getOverlayableList().isEmpty()) {
            packageBlock.getOverlayableList().toJson().write(
                    new File(packageDirectory, Overlayable.FILE_NAME_JSON));
        }
        if (!packageBlock.getStagedAliasList().isBlank()) {
            packageBlock.getStagedAliasList().toJson().write(
                    new File(packageDirectory, StagedAlias.FILE_NAME_JSON));
        }
        if (!packageBlock.getLibraryList().isBlank()) {
            packageBlock.getLibraryList().toJson().write(
                    new File(packageDirectory, LibraryBlock.FILE_NAME_JSON));
        }
    }
    private void writeSplitTypeJsonFiles(File packageDirectory, TypeBlock typeBlock) throws IOException {
        File file = new File(packageDirectory,
                typeBlock.buildUniqueDirectoryName() + ApkUtil.JSON_FILE_EXTENSION);
        typeBlock.toJson().write(file);
    }
}
