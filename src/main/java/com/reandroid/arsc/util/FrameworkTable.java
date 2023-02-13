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

import com.reandroid.arsc.array.SpecTypePairArray;
import com.reandroid.arsc.array.TypeBlockArray;
import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.group.EntryGroup;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ReferenceItem;
import com.reandroid.arsc.item.TableString;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResConfig;

import java.io.*;
import java.util.*;

public class FrameworkTable extends TableBlock {

    private String mFrameworkTitle;
    private String mFrameworkName;
    private String mFrameworkVersion;
    private ResNameMap<EntryGroup> mNameGroupMap;
    public FrameworkTable(){
        super();
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
            SpecTypePair specTypePair = packageBlock.searchByTypeName(typeName);
            if(specTypePair!=null){
                return specTypePair.searchByEntryName(entryName);
            }
        }
        return null;
    }
    public String getFrameworkTitle(){
        if(mFrameworkTitle==null){
            mFrameworkTitle=loadProperty(PROP_TITLE);
        }
        return mFrameworkTitle;
    }
    public String getFrameworkName(){
        if(mFrameworkName==null){
            mFrameworkName=loadProperty(PROP_NAME);
        }
        return mFrameworkName;
    }
    public String getFrameworkVersion(){
        if(mFrameworkVersion==null){
            mFrameworkVersion=loadProperty(PROP_VERSION);
        }
        return mFrameworkVersion;
    }
    private void setFrameworkTitle(String value){
        mFrameworkTitle=null;
        writeProperty(PROP_TITLE, value);
    }
    public void setFrameworkName(String value){
        mFrameworkName=null;
        writeProperty(PROP_NAME, value);
    }
    public void setFrameworkVersion(String value){
        mFrameworkVersion=null;
        writeProperty(PROP_VERSION, value);
    }
    public int writeTable(File resourcesArscFile) throws IOException{
        File dir=resourcesArscFile.getParentFile();
        if(dir!=null && !dir.exists()){
            dir.mkdirs();
        }
        FileOutputStream outputStream=new FileOutputStream(resourcesArscFile, false);
        return writeTable(outputStream);
    }
    public int writeTable(OutputStream outputStream) throws IOException{
        return writeBytes(outputStream);
    }
    public void readTable(File resourcesArscFile) throws IOException{
        FileInputStream inputStream=new FileInputStream(resourcesArscFile);
        readTable(inputStream);
    }
    public void readTable(InputStream inputStream) throws IOException{
        BlockReader reader=new BlockReader(inputStream);
        super.readBytes(reader);
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        reader.close();
    }
    public void optimize(String frameworkName, String frameworkVersion){
        Map<Integer, EntryGroup> groupMap=scanAllEntryGroups();
        for(EntryGroup group:groupMap.values()){
            List<Entry> entryList =getEntriesToRemove(group);
            removeEntries(entryList);
        }
        for(PackageBlock pkg:listPackages()){
            clearNonDefaultConfigs(pkg);
        }
        for(PackageBlock pkg:listPackages()){
            pkg.removeEmpty();
            pkg.refresh();
        }
        optimizeTableString();
        setFrameworkTitle(TITLE_STRING);
        setFrameworkName(frameworkName);
        setFrameworkVersion(frameworkVersion);
        refresh();
    }
    private void clearNonDefaultConfigs(PackageBlock pkg){
        SpecTypePairArray specTypePairArray = pkg.getSpecTypePairArray();
        specTypePairArray.sort();
        List<SpecTypePair> specTypePairList=new ArrayList<>(specTypePairArray.listItems());
        for(SpecTypePair specTypePair:specTypePairList){
            clearNonDefaultConfigs(specTypePair);
        }
    }
    private void clearNonDefaultConfigs(SpecTypePair specTypePair){
        TypeBlockArray typeBlockArray = specTypePair.getTypeBlockArray();
        if(typeBlockArray.childesCount()<2){
            return;
        }
        List<TypeBlock> typeBlockList=new ArrayList<>(typeBlockArray.listItems());
        TypeBlock defTypeBlock=null;
        for(TypeBlock typeBlock:typeBlockList){
            if(defTypeBlock==null){
                defTypeBlock=typeBlock;
            }
            ResConfig config = typeBlock.getResConfig();
            if(config.isDefault()){
                defTypeBlock=typeBlock;
                break;
            }
        }
        for(TypeBlock typeBlock:typeBlockList){
            if(typeBlock==defTypeBlock){
                continue;
            }
            typeBlockArray.remove(typeBlock);
        }
    }
    private void optimizeTableString(){
        removeUnusedTableString();
        shrinkTableString();
        removeUnusedTableString();
    }
    private void removeUnusedTableString(){
        TableStringPool tableStringPool=getTableStringPool();
        tableStringPool.getStyleArray().clearChildes();
        tableStringPool.removeUnusedStrings();
        tableStringPool.refresh();
    }
    private void shrinkTableString(){
        TableStringPool tableStringPool=getTableStringPool();
        tableStringPool.getStringsArray().ensureSize(1);
        TableString title=tableStringPool.get(0);
        title.set(PROP_TITLE+":"+TITLE_STRING);
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
            TableStringPool tableStringPool=getTableStringPool();
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
        return str.substring(name.length());
    }
    private TableString loadPropertyString(String name){
        if(name==null){
            return null;
        }
        if(!name.endsWith(":")){
            name=name+":";
        }
        TableStringPool tableStringPool=getTableStringPool();
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
        return getFrameworkVersion()!=null;
    }
    @Override
    public String toString(){
        HeaderBlock headerBlock=getHeaderBlock();
        if(headerBlock.getChunkType()!= ChunkType.TABLE){
            return super.toString();
        }
        if(!isOptimized()){
            return "Unoptimized: "+super.toString();
        }
        StringBuilder builder=new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(": SIZE=").append(headerBlock.getChunkSize());
        String str=getFrameworkTitle();
        builder.append("\n");
        if(str==null){
            builder.append(PROP_TITLE).append(":null");
        }else {
            builder.append(str);
        }
        str=getFrameworkName();
        builder.append("\n  ").append(PROP_NAME).append(":");
        if(str==null){
            builder.append("null");
        }else {
            builder.append(str);
        }
        str=getFrameworkVersion();
        builder.append("\n  ").append(PROP_VERSION).append(":");
        if(str==null){
            builder.append("null");
        }else {
            builder.append(str);
        }
        Collection<PackageBlock> allPkg = listPackages();
        builder.append("\n  PACKAGES=").append(allPkg.size());
        for(PackageBlock packageBlock:allPkg){
            builder.append("\n    ");
            builder.append(String.format("0x%02x", packageBlock.getId()));
            builder.append(":").append(packageBlock.getName());
        }
        return builder.toString();
    }
    public static FrameworkTable load(File file) throws IOException{
        return load(new FileInputStream(file));
    }
    public static FrameworkTable load(InputStream inputStream) throws IOException{
        FrameworkTable frameworkTable=new FrameworkTable();
        frameworkTable.readBytes(inputStream);
        return frameworkTable;
    }
    private static final String TITLE_STRING="Framework table";
    private static final String PROP_TITLE="TITLE";
    private static final String PROP_NAME="NAME";
    private static final String PROP_VERSION="VERSION";
    private static final int PROP_COUNT=10;
}
