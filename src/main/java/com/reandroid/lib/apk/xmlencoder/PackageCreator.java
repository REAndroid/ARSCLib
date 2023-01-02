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

import com.reandroid.lib.apk.ResourceIds;
import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.arsc.pool.TypeStringPool;

import java.util.ArrayList;
import java.util.List;

class PackageCreator {
    private List<String> mSpecNames;
    private String mPackageName;
    private int mPackageId;
    public PackageCreator(){
    }
    public void setPackageName(String name){
        this.mPackageName=name;
    }
    public PackageBlock createNew(TableBlock tableBlock, ResourceIds.Table.Package pkgResourceIds){
        loadNames(pkgResourceIds);
        PackageBlock packageBlock=new PackageBlock();
        packageBlock.setName(mPackageName);
        packageBlock.setId(mPackageId);
        tableBlock.getPackageArray()
                .add(packageBlock);
        packageBlock.getSpecStringPool()
                .addStrings(mSpecNames);

        initTypeStringPool(packageBlock, pkgResourceIds);

        return packageBlock;
    }
    private void initTypeStringPool(PackageBlock packageBlock,
                                    ResourceIds.Table.Package pkgResourceIds){

        TypeStringPool typeStringPool = packageBlock.getTypeStringPool();

        for(ResourceIds.Table.Package.Type type:pkgResourceIds.listTypes()){
            typeStringPool.getOrCreate(type.getIdInt(), type.getName());
        }
    }
    private void loadNames(ResourceIds.Table.Package pkg){
        this.mSpecNames = new ArrayList<>();
        if(pkg.name!=null){
            this.mPackageName=pkg.name;
        }
        if(this.mPackageName==null){
            this.mPackageName = EncodeUtil.NULL_PACKAGE_NAME;
        }
        this.mPackageId=pkg.getIdInt();
        for(ResourceIds.Table.Package.Type.Entry entry:pkg.listEntries()){
            mSpecNames.add(entry.getName());
        }
    }
}
