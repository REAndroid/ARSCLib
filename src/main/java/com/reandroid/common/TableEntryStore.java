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
import com.reandroid.arsc.util.FrameworkTable;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.StagedAliasEntry;

import java.util.*;

public class TableEntryStore implements EntryStore{
    private final Map<Integer, Set<PackageBlock>> mLocalPackagesMap;
    private final Map<Integer, Set<PackageBlock>> mFrameworkPackagesMap;
    public TableEntryStore(){
        this.mLocalPackagesMap = new HashMap<>();
        this.mFrameworkPackagesMap = new HashMap<>();
    }

    public String getEntryName(int resourceId){
        Entry entry = getEntry(resourceId);
        if(entry ==null){
            return null;
        }
        return entry.getName();
    }
    public Entry getEntry(int resourceId){
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
        Set<PackageBlock> packageBlockSet;
        if(packageBlock.getTableBlock() instanceof FrameworkTable){
            packageBlockSet = getOrCreateFrameworks(packageBlock.getId());
        }else {
            packageBlockSet = getOrCreateLocal(packageBlock.getId());
        }
        if(packageBlockSet.contains(packageBlock)){
            return;
        }
        packageBlockSet.add(packageBlock);
    }
    private Set<PackageBlock> getOrCreateLocal(int packageId){
        Integer id=packageId;
        Set<PackageBlock> packageBlockSet = mLocalPackagesMap.get(id);
        if(packageBlockSet==null){
            packageBlockSet=new HashSet<>();
            mLocalPackagesMap.put(id, packageBlockSet);
        }
        return packageBlockSet;
    }
    private Set<PackageBlock> getOrCreateFrameworks(int packageId){
        Integer id=packageId;
        Set<PackageBlock> packageBlockSet = mFrameworkPackagesMap.get(id);
        if(packageBlockSet==null){
            packageBlockSet=new HashSet<>();
            mFrameworkPackagesMap.put(id, packageBlockSet);
        }
        return packageBlockSet;
    }
    @Override
    public List<EntryGroup> getEntryGroups(int resourceId) {
        List<EntryGroup> results = searchEntryGroupsLocal(resourceId);
        if(results.size()>0){
            return results;
        }
        int alias = searchIdAliasLocal(resourceId);
        results = searchEntryGroupsLocal(alias);
        if(results.size()>0){
            return results;
        }
        results = searchEntryGroupsFramework(resourceId);
        if(results.size()>0){
            return results;
        }
        alias = searchIdAliasFramework(resourceId);
        return searchEntryGroupsFramework(alias);
    }
    @Override
    public EntryGroup getEntryGroup(int resourceId) {
        EntryGroup entryGroup = searchEntryLocal(resourceId);
        if(entryGroup==null){
            entryGroup = searchEntryLocal(searchIdAliasLocal(resourceId));
        }
        if(entryGroup==null){
            entryGroup = searchEntryFramework(resourceId);
        }
        if(entryGroup==null){
            entryGroup = searchEntryFramework(searchIdAliasFramework(resourceId));
        }
        return entryGroup;
    }
    @Override
    public List<PackageBlock> getPackageBlocks(int packageId) {
        List<PackageBlock> results=new ArrayList<>();
        packageId = 0xff & packageId;
        Set<PackageBlock> packageBlockSet = mLocalPackagesMap.get(packageId);
        if(packageBlockSet==null){
            packageBlockSet = mFrameworkPackagesMap.get(packageId);
        }
        if(packageBlockSet!=null){
            results.addAll(packageBlockSet);
        }
        return results;
    }
    private List<EntryGroup> searchEntryGroupsLocal(int resourceId) {
        if(resourceId==0){
            return new ArrayList<>();
        }
        List<EntryGroup> results=new ArrayList<>();
        int pkgId = (resourceId>>24)&0xff;
        Set<PackageBlock> packageBlockSet = mLocalPackagesMap.get(pkgId);
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
    private List<EntryGroup> searchEntryGroupsFramework(int resourceId) {
        if(resourceId==0){
            return new ArrayList<>();
        }
        List<EntryGroup> results=new ArrayList<>();
        int pkgId = (resourceId>>24)&0xff;
        Set<PackageBlock> packageBlockSet = mFrameworkPackagesMap.get(pkgId);
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
    private EntryGroup searchEntryLocal(int resourceId) {
        int packageId = (resourceId>>24)&0xff;
        Set<PackageBlock> packageBlockSet = mLocalPackagesMap.get(packageId);
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
    private EntryGroup searchEntryFramework(int resourceId) {
        int packageId = (resourceId>>24)&0xff;
        Set<PackageBlock> packageBlockSet = mFrameworkPackagesMap.get(packageId);
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
    private int searchIdAliasLocal(int resourceId) {
        for(Set<PackageBlock> packageBlockSet : mLocalPackagesMap.values()){
            for(PackageBlock packageBlock:packageBlockSet){
                StagedAliasEntry stagedAliasEntry = packageBlock.searchByStagedResId(resourceId);
                if(stagedAliasEntry!=null){
                    return stagedAliasEntry.getFinalizedResId();
                }
            }
        }
        return 0;
    }
    private int searchIdAliasFramework(int resourceId) {
        for(Set<PackageBlock> packageBlockSet : mLocalPackagesMap.values()){
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
