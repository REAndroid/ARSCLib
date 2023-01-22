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
package com.reandroid.common;

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.group.EntryGroup;
import com.reandroid.arsc.item.TableString;
import com.reandroid.arsc.value.EntryBlock;
import com.reandroid.arsc.value.StagedAliasEntry;

import java.util.*;

public class TableEntryStore implements EntryStore{
    private final Map<Integer, Set<PackageBlock>> mPackagesMap;
    public TableEntryStore(){
        this.mPackagesMap = new HashMap<>();
    }

    public String getEntryName(int resourceId){
        EntryBlock entryBlock=getEntryBlock(resourceId);
        if(entryBlock==null){
            return null;
        }
        return entryBlock.getName();
    }
    public EntryBlock getEntryBlock(int resourceId){
        if(resourceId==0){
            return null;
        }
        EntryGroup entryGroup=getEntryGroup(resourceId);
        if(entryGroup==null){
            return null;
        }
        return entryGroup.pickOne();
    }
    public void add(TableBlock tableBlock){
        if(tableBlock==null){
            return;
        }
        for(PackageBlock packageBlock:tableBlock.listPackages()){
            add(packageBlock);
        }
    }
    public void add(PackageBlock packageBlock){
        if(packageBlock==null){
            return;
        }
        Set<PackageBlock> packageBlockSet=getOrCreate(packageBlock.getId());
        if(packageBlockSet.contains(packageBlock)){
            return;
        }
        packageBlockSet.add(packageBlock);
    }
    private Set<PackageBlock> getOrCreate(int packageId){
        Integer id=packageId;
        Set<PackageBlock> packageBlockSet=mPackagesMap.get(id);
        if(packageBlockSet==null){
            packageBlockSet=new HashSet<>();
            mPackagesMap.put(id, packageBlockSet);
        }
        return packageBlockSet;
    }
    @Override
    public EntryGroup searchEntryGroup(String packageName, String type, String name) {
        return null;
    }
    private Set<TableBlock> getTableBlocks(int packageId) {
        Set<TableBlock> results=new HashSet<>();
        Set<PackageBlock> packageBlockSet = mPackagesMap.get(packageId);
        if(packageBlockSet!=null){
            for(PackageBlock packageBlock:packageBlockSet){
                TableBlock tableBlock=packageBlock.getTableBlock();
                results.add(tableBlock);
            }
        }
        return results;
    }
    @Override
    public List<EntryGroup> getEntryGroups(int resourceId) {
        List<EntryGroup> results = searchEntryGroups(resourceId);
        if(results.size()>0){
            return results;
        }
        return searchEntryGroups(searchIdAlias(resourceId));
    }
    @Override
    public EntryGroup getEntryGroup(int resourceId) {
        EntryGroup entryGroup = searchEntryGroup(resourceId);
        if(entryGroup!=null){
            return entryGroup;
        }
        return searchEntryGroup(searchIdAlias(resourceId));
    }
    @Override
    public List<PackageBlock> getPackageBlocks(byte packageId) {
        List<PackageBlock> results=new ArrayList<>();
        Set<PackageBlock> packageBlockSet = mPackagesMap.get(0xff & packageId);
        if(packageBlockSet!=null){
            results.addAll(packageBlockSet);
        }
        return results;
    }
    @Override
    public List<TableString> getTableStrings(byte packageId, int stringReference) {
        List<TableString> results=new ArrayList<>();
        Set<TableBlock> tableBlockSet=getTableBlocks(0xff & packageId);
        for(TableBlock tableBlock:tableBlockSet){
            TableString tableString=tableBlock.getTableStringPool().get(stringReference);
            if(tableString!=null){
                results.add(tableString);
            }
        }
        return results;
    }
    private List<EntryGroup> searchEntryGroups(int resourceId) {
        if(resourceId==0){
            return new ArrayList<>();
        }
        List<EntryGroup> results=new ArrayList<>();
        int pkgId = (resourceId>>24)&0xff;
        Set<PackageBlock> packageBlockSet = mPackagesMap.get(pkgId);
        if(packageBlockSet==null){
            return results;
        }
        for(PackageBlock packageBlock: packageBlockSet){
            EntryGroup group=packageBlock.getEntryGroup(resourceId);
            if(group!=null){
                results.add(group);
            }
        }
        return results;
    }
    private EntryGroup searchEntryGroup(int resourceId) {
        if(resourceId==0){
            return null;
        }
        int packageId = (resourceId>>24)&0xff;
        Set<PackageBlock> packageBlockSet = mPackagesMap.get(packageId);
        if(packageBlockSet==null){
            return null;
        }
        for(PackageBlock packageBlock: packageBlockSet){
            EntryGroup group=packageBlock.getEntryGroup(resourceId);
            if(group!=null && group.pickOne()!=null){
                return group;
            }
        }
        return null;
    }
    private int searchIdAlias(int resourceId) {
        for(Set<PackageBlock> packageBlockSet : mPackagesMap.values()){
            for(PackageBlock packageBlock:packageBlockSet){
                StagedAliasEntry stagedAliasEntry = packageBlock.searchByStagedResId(resourceId);
                if(stagedAliasEntry!=null){
                    return stagedAliasEntry.getFinalizedResId();
                }
            }
        }
        return 0;
    }
}
