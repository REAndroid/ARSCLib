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
package com.reandroid.arsc.util;

import com.reandroid.arsc.BuildInfo;
import com.reandroid.arsc.array.SpecTypePairArray;
import com.reandroid.arsc.array.TypeBlockArray;
import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.group.EntryGroup;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.item.ReferenceItem;
import com.reandroid.arsc.item.TableString;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.common.FileChannelInputStream;

import java.io.*;
import java.util.*;

public class FrameworkTable extends TableBlock {

    private String frameworkName;
    private int versionCode;
    private int mainPackageId;
    private ResNameMap<EntryGroup> mNameGroupMap;
    private boolean mOptimized;
    private boolean mOptimizeChecked;
    public FrameworkTable(){
        super();
    }

    public boolean isAndroid(){
        return "android".equals(getFrameworkName())
                && getMainPackageId() == 0x01;
    }

    public int getMainPackageId() {
        if(mainPackageId!=0){
            return mainPackageId;
        }
        PackageBlock packageBlock = pickOne();
        if(packageBlock!=null){
            mainPackageId = packageBlock.getId();
        }
        return mainPackageId;
    }

    @Override
    public void destroy(){
        clearResourceNameMap();
        this.frameworkName = null;
        this.versionCode = 0;
        this.mainPackageId = 0;
        super.destroy();
    }
    public int resolveResourceId(String typeName, String entryName){
        Entry entry = searchEntry(typeName, entryName);
        if(entry !=null){
            return entry.getResourceId();
        }
        return 0;
    }
    /**
     * Loads all resource name map to memory for faster use
     * Call this if you plan to search entries frequently
     */
    public void loadResourceNameMap(){
        ResNameMap<EntryGroup> resNameMap = mNameGroupMap;
        if(resNameMap == null){
            resNameMap = new ResNameMap<>();
            for(PackageBlock packageBlock:listPackages()){
                for(EntryGroup group:packageBlock.listEntryGroup()){
                    resNameMap.add(group.getTypeName(),
                            group.getSpecName(),
                            group);
                }
            }
            mNameGroupMap = resNameMap;
        }
    }
    /**
     * Clears resource name map from memory
     */
    public void clearResourceNameMap(){
        if(mNameGroupMap!=null){
            mNameGroupMap.clear();
            mNameGroupMap =null;
        }
    }
    private boolean hasResourceGroupMap(){
        return mNameGroupMap!=null;
    }
    private Entry searchEntryFromMap(String typeName, String entryName){
        if(mNameGroupMap ==null){
            return null;
        }
        EntryGroup entryGroup = mNameGroupMap.get(typeName, entryName);
        if(entryGroup!=null){
            return entryGroup.pickOne();
        }
        return null;
    }
    public Entry searchEntry(String typeName, String entryName){
        if(hasResourceGroupMap()){
            return searchEntryFromMap(typeName, entryName);
        }
        return searchEntryFromTable(typeName, entryName);
    }
    /**
     * Since this is framework, we are sure of proper names.
     */
    public Entry searchEntryFromTable(String typeName, String entryName){
        for(PackageBlock packageBlock:listPackages()){
            SpecTypePair specTypePair = packageBlock.getSpecTypePair(typeName);
            if(specTypePair!=null){
                return specTypePair.getAnyEntry(entryName);
            }
        }
        return null;
    }
    public int getVersionCode(){
        if(versionCode == 0 && isOptimized()){
            String version = loadProperty(PROP_VERSION_CODE);
            if(version!=null){
                try{
                    versionCode = Integer.parseInt(version);
                }catch (NumberFormatException ignored){
                }
            }
        }
        return versionCode;
    }
    public void setVersionCode(int value){
        versionCode = value;
        if(isOptimized()){
            writeVersionCode(value);
        }
    }
    public String getFrameworkName(){
        if(frameworkName == null){
            frameworkName = loadProperty(PROP_NAME);
        }
        if(frameworkName == null){
            PackageBlock packageBlock = pickOne();
            if(packageBlock!=null){
                String name = packageBlock.getName();
                if(name!=null && !name.trim().isEmpty()){
                    frameworkName = name;
                }
            }
        }
        return frameworkName;
    }
    public void setFrameworkName(String value){
        frameworkName = value;
        if(isOptimized()){
            writeProperty(PROP_NAME, value);
        }
    }
    public void optimize(String name, int version){
        mOptimizeChecked = true;
        mOptimized = false;
        ensureTypeBlockNonNullEntries();
        optimizeEntries();
        optimizeTableString();
        writeVersionCode(version);
        mOptimizeChecked = false;
        setFrameworkName(name);
        refresh();
    }
    private void ensureTypeBlockNonNullEntries(){
        for(PackageBlock packageBlock:listPackages()){
            ensureTypeBlockNonNullEntries(packageBlock);
        }
    }
    private void ensureTypeBlockNonNullEntries(PackageBlock packageBlock){
        for(SpecTypePair specTypePair:packageBlock.listSpecTypePairs()){
            ensureTypeBlockNonNullEntries(specTypePair);
        }
    }
    private void ensureTypeBlockNonNullEntries(SpecTypePair specTypePair){
        Map<Integer, EntryGroup> map = specTypePair.createEntryGroups();
        for(EntryGroup entryGroup:map.values()){
            ensureNonNullDefaultEntry(entryGroup);
        }
    }
    private void ensureNonNullDefaultEntry(EntryGroup entryGroup){
        Entry defEntry = entryGroup.getDefault(false);
        Entry entry;
        if(defEntry==null){
            entry = entryGroup.pickOne();
            if(entry == null){
                return;
            }
            SpecTypePair specTypePair = entry.getTypeBlock().getParentSpecTypePair();
            TypeBlock type = specTypePair.getOrCreateTypeBlock(new ResConfig());
            defEntry = type.getOrCreateEntry((short) (entry.getId() & 0xffff));
        }
        if(!defEntry.isNull()){
            return;
        }
        entry = entryGroup.pickOne();
        if(entry.isNull()){
            return;
        }
        defEntry.merge(entry);
        defEntry.isDefault();
    }
    private void optimizeEntries(){
        Map<Integer, EntryGroup> groupMap=scanAllEntryGroups();
        for(EntryGroup group:groupMap.values()){
            List<Entry> entryList = getEntriesToRemove(group);
            removeEntries(entryList);
        }
        for(PackageBlock pkg:listPackages()){
            removeEmptyBlocks(pkg);
        }
        for(PackageBlock pkg:listPackages()){
            pkg.removeEmpty();
            pkg.refresh();
        }
    }
    private void removeEmptyBlocks(PackageBlock pkg){
        SpecTypePairArray specTypePairArray = pkg.getSpecTypePairArray();
        specTypePairArray.sort();
        List<SpecTypePair> specTypePairList=new ArrayList<>(specTypePairArray.listItems());
        for(SpecTypePair specTypePair:specTypePairList){
            removeEmptyBlocks(specTypePair);
        }
    }
    private void removeEmptyBlocks(SpecTypePair specTypePair){
        TypeBlockArray typeBlockArray = specTypePair.getTypeBlockArray();
        if(typeBlockArray.childesCount()<2){
            return;
        }
        typeBlockArray.removeEmptyBlocks();
    }
    private void optimizeTableString(){
        removeUnusedTableString();
        shrinkTableString();
        getStringPool().getStyleArray().clearChildes();
        removeUnusedTableString();
    }
    private void removeUnusedTableString(){
        TableStringPool tableStringPool=getStringPool();
        tableStringPool.removeUnusedStrings();
        tableStringPool.refresh();
    }
    private void shrinkTableString(){
        TableStringPool tableStringPool=getStringPool();
        tableStringPool.getStringsArray().ensureSize(1);
        TableString title=tableStringPool.get(0);
        title.set(BuildInfo.getRepo());
        for(TableString tableString:tableStringPool.getStringsArray().listItems()){
            if(tableString==title){
                continue;
            }
            shrinkTableString(title, tableString);
        }
        tableStringPool.refresh();
    }
    private void shrinkTableString(TableString zero, TableString tableString){
        List<ReferenceItem> allRef = new ArrayList<>(tableString.getReferencedList());
        tableString.removeAllReference();
        for(ReferenceItem item:allRef){
            item.set(zero.getIndex());
        }
        zero.addReference(allRef);
    }
    private void removeEntries(List<Entry> removeList){
        for(Entry entry :removeList){
            removeEntry(entry);
        }
    }
    private void removeEntry(Entry entry){
        TypeBlock typeBlock= entry.getTypeBlock();
        if(typeBlock==null){
            return;
        }
        typeBlock.removeEntry(entry);

    }
    private List<Entry> getEntriesToRemove(EntryGroup group){
        List<Entry> results=new ArrayList<>();
        Entry mainEntry=group.pickOne();
        if(mainEntry==null){
            return results;
        }
        Iterator<Entry> itr = group.iterator(true);
        while (itr.hasNext()){
            Entry entry =itr.next();
            if(entry ==mainEntry){
                continue;
            }
            results.add(entry);
        }
        return results;
    }
    private Map<Integer, EntryGroup> scanAllEntryGroups(){
        Map<Integer, EntryGroup> results=new HashMap<>();
        for(PackageBlock packageBlock:listPackages()){
            Map<Integer, EntryGroup> map=packageBlock.getEntriesGroupMap();
            for(Map.Entry<Integer, EntryGroup> entry:map.entrySet()){
                int id=entry.getKey();
                EntryGroup group=entry.getValue();
                EntryGroup exist=results.get(id);
                if(exist!=null && exist.getDefault()!=null){
                    if(exist.getDefault()!=null){
                        continue;
                    }
                    results.remove(id);
                }
                results.put(id, group);
            }
        }
        return results;
    }
    private TableString writeProperty(String name, String value){
        if(!name.endsWith(":")){
            name=name+":";
        }
        if(value==null){
            value="";
        }
        if(!value.startsWith(name)){
            value=name+value;
        }
        TableString tableString=loadPropertyString(name);
        if(tableString!=null){
            tableString.set(value);
        }else {
            TableStringPool tableStringPool=getStringPool();
            tableString=tableStringPool.getOrCreate(value);
        }
        return tableString;
    }
    private String loadProperty(String name){
        if(name==null){
            return null;
        }
        if(!name.endsWith(":")){
            name=name+":";
        }
        TableString tableString=loadPropertyString(name);
        if(tableString==null){
            return null;
        }
        String str=tableString.get().trim();
        return str.substring(name.length()).trim();
    }
    private TableString loadPropertyString(String name){
        if(name==null){
            return null;
        }
        if(!name.endsWith(":")){
            name=name+":";
        }
        TableStringPool tableStringPool=getStringPool();
        int max=PROP_COUNT;
        for(int i=0;i<max;i++){
            TableString tableString=tableStringPool.get(i);
            if(tableString==null){
                break;
            }
            String str=tableString.get();
            if(str==null){
                continue;
            }
            str=str.trim();
            if(str.startsWith(name)){
                return tableString;
            }
        }
        return null;
    }
    public boolean isOptimized(){
        if(!mOptimizeChecked){
            mOptimizeChecked = true;
            String version = loadProperty(PROP_VERSION_CODE);
            if(version!=null){
                try{
                    int v = Integer.parseInt(version);
                    mOptimized = (v!=0);
                }catch (NumberFormatException ignored){
                }
            }
        }
        return mOptimized;
    }
    private void writeVersionCode(int value){
        writeProperty(PROP_VERSION_CODE, String.valueOf(value));
    }
    @Override
    public String toString(){
        HeaderBlock headerBlock=getHeaderBlock();
        if(headerBlock.getChunkType()!= ChunkType.TABLE){
            return super.toString();
        }
        if(!mOptimized){
            return "Unoptimized: "+super.toString();
        }
        return getFrameworkName()+'-'+getVersionCode();
    }
    public static FrameworkTable load(File file) throws IOException{
        return load(new FileChannelInputStream(file));
    }
    public static FrameworkTable load(InputStream inputStream) throws IOException{
        FrameworkTable frameworkTable=new FrameworkTable();
        frameworkTable.readBytes(inputStream);
        return frameworkTable;
    }

    private static final String PROP_NAME = "NAME";
    private static final String PROP_VERSION_CODE = "VERSION_CODE";
    private static final String PROP_VERSION_NAME = "VERSION_NAME";
    private static final int PROP_COUNT=10;
}
