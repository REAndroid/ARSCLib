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
package com.reandroid.arsc.array;

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.container.CountedBlockList;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;

public class PackageArray extends CountedBlockList<PackageBlock> implements JSONConvert<JSONArray> {

    public PackageArray(IntegerReference packageCount) {
        super(PackageBlock.CREATOR, packageCount);
    }

    @Override
    public void destroy() {
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).destroy();
        }
        super.destroy();
    }
    public PackageBlock pickOne() {
        return pickOne(0);
    }
    public PackageBlock pickOne(int packageId) {
        int size = size();
        if (size == 0) {
            return null;
        }
        if (size == 1 && packageId == 0) {
            return get(0);
        }
        PackageBlock largest = null;
        for (int i = 0; i < size; i++) {
            PackageBlock packageBlock = get(i);
            if (packageId != 0 && packageId != packageBlock.getId()) {
                continue;
            }
            if (largest == null) {
                largest = packageBlock;
            } else if (packageBlock.getHeaderBlock().getChunkSize() >
                    largest.getHeaderBlock().getChunkSize()) {
                largest = packageBlock;
            }
        }
        return largest;
    }
    public void sort() {
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).sortTypes();
        }
        sort(CompareUtil.getComparableComparator());
    }
    public PackageBlock getOrCreate(int packageId) {
        PackageBlock packageBlock = getPackageBlockById(packageId);
        if (packageBlock == null) {
            packageBlock = createNext(packageId, PackageBlock.EMPTY_PACKAGE_NAME);
        }
        return packageBlock;
    }
    public PackageBlock getOrCreate(int packageId, String name) {
        PackageBlock packageBlock = getPackageBlock(packageId, name);
        if (packageBlock == null) {
            packageBlock = createNext(packageId, name);
        }
        return packageBlock;
    }
    public PackageBlock getOrCreateCompatible(PackageBlock packageBlock) {
        PackageBlock pkg = getCompatible(packageBlock);
        if (pkg == null) {
            pkg = createNext(packageBlock.getId(), packageBlock.getName());
        }
        return pkg;
    }
    public PackageBlock createNext(int packageId, String name) {
        PackageBlock packageBlock = createNext();
        packageBlock.setId(packageId);
        packageBlock.setName(name);
        return packageBlock;
    }
    public PackageBlock getPackageBlockById(int packageId) {
        int size = size();
        for (int i = 0; i < size; i++) {
            PackageBlock packageBlock = get(i);
            if (packageBlock.getId() == packageId) {
                return packageBlock;
            }
        }
        return null;
    }
    public PackageBlock getPackageBlock(int packageId, String name) {
        int size = size();
        for (int i = 0; i < size; i++) {
            PackageBlock packageBlock = get(i);
            if (packageBlock.getId() == packageId
                    && ObjectsUtil.equals(name, packageBlock.getName())) {
                return packageBlock;
            }
        }
        return null;
    }
    public PackageBlock getCompatible(PackageBlock packageBlock) {
        int size = size();
        for (int i = 0; i < size; i++) {
            PackageBlock pkg = get(i);
            if (pkg.isCompatibleTo(packageBlock)) {
                return pkg;
            }
        }
        return null;
    }
    public PackageBlock getPackageBlockByName(String name) {
        int size = size();
        for (int i = 0; i < size; i++) {
            PackageBlock packageBlock = get(i);
            if (ObjectsUtil.equals(name, packageBlock.getName())) {
                return packageBlock;
            }
        }
        return null;
    }
    public void clear() {
        super.clearBlocks();
    }
    @Override
    public JSONArray toJson() {
        return toJsonArray(this);
    }
    @Override
    public void fromJson(JSONArray json) {
        fromJsonArray(this, json);
    }
    public void merge(PackageArray packageArray) {
        if (packageArray == null || packageArray == this) {
            return;
        }
        boolean empty = this.size() == 0;
        int size = packageArray.size();
        for (int i = 0; i < size; i++) {
            PackageBlock packageBlock = packageArray.get(i);
            PackageBlock exist;
            if (empty) {
                exist = createNext(packageBlock.getId(), packageBlock.getName());
            } else {
                exist = getOrCreateCompatible(packageBlock);
            }
            exist.merge(packageBlock);
        }
    }
}
