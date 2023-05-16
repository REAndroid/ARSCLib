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
package com.reandroid.arsc.chunk;

import com.reandroid.arsc.BuildInfo;
import com.reandroid.arsc.array.LibraryInfoArray;
import com.reandroid.arsc.array.SpecTypePairArray;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.PackageBody;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.group.EntryGroup;
import com.reandroid.arsc.header.PackageHeader;
import com.reandroid.arsc.list.OverlayableList;
import com.reandroid.arsc.list.StagedAliasList;
import com.reandroid.arsc.pool.SpecStringPool;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.pool.TypeStringPool;
import com.reandroid.arsc.util.HexUtil;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.LibraryInfo;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.arsc.value.StagedAliasEntry;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

import java.util.*;


public class PackageBlock extends Chunk<PackageHeader>
        implements ParentChunk,
        JSONConvert<JSONObject>,
        Comparable<PackageBlock> {

    private final TypeStringPool mTypeStringPool;
    private final SpecStringPool mSpecStringPool;

    private final PackageBody mBody;

    private final Map<Integer, EntryGroup> mEntriesGroup;
    private boolean entryGroupMapLocked;

    public PackageBlock() {
        super(new PackageHeader(), 3);
        PackageHeader header = getHeaderBlock();

        this.mTypeStringPool=new TypeStringPool(false, header.getTypeIdOffsetItem());
        this.mSpecStringPool=new SpecStringPool(true);

        this.mBody = new PackageBody();

        this.mEntriesGroup = new HashMap<>();
        this.entryGroupMapLocked = true;

        addChild(mTypeStringPool);
        addChild(mSpecStringPool);
        addChild(mBody);
    }
    public void linkTableStringsInternal(TableStringPool tableStringPool){
        for(SpecTypePair specTypePair : listSpecTypePairs()){
            specTypePair.linkTableStringsInternal(tableStringPool);
        }
    }
    public void linkSpecStringsInternal(SpecStringPool specStringPool){
        for(SpecTypePair specTypePair : listSpecTypePairs()){
            specTypePair.linkSpecStringsInternal(specStringPool);
        }
    }
    public void destroy(){
        getEntriesGroupMap().clear();
        getPackageBody().destroy();
        getTypeStringPool().destroy();
        getSpecStringPool().destroy();
        setId(0);
        setName("");
    }
    public Entry getEntry(String qualifiers, String type, String name){
        return getSpecTypePairArray().getEntry(qualifiers, type, name);
    }
    public Entry getEntry(ResConfig resConfig, String type, String name){
        return getSpecTypePairArray().getEntry(resConfig, type, name);
    }
    public Entry getOrCreate(String qualifiers, String type, String name){
        return getOrCreate(ResConfig.parse(qualifiers), type, name);
    }
    public Entry getOrCreate(ResConfig resConfig, String typeName, String name){
        SpecTypePair specTypePair = getOrCreateSpecTypePair(typeName);
        TypeBlock typeBlock = specTypePair.getOrCreateTypeBlock(resConfig);
        return typeBlock.getOrCreateEntry(name);
    }
    public TypeBlock getOrCreateTypeBlock(String qualifiers, String typeName){
        SpecTypePair specTypePair = getOrCreateSpecTypePair(typeName);
        return specTypePair.getOrCreateTypeBlock(qualifiers);
    }
    public TypeBlock getOrCreateTypeBlock(ResConfig resConfig, String typeName){
        SpecTypePair specTypePair = getOrCreateSpecTypePair(typeName);
        return specTypePair.getOrCreateTypeBlock(resConfig);
    }
    public SpecTypePair getOrCreateSpecTypePair(String typeName){
        return getSpecTypePairArray().getOrCreate(typeName);
    }
    public int getTypeIdOffset(){
        return getHeaderBlock().getTypeIdOffset();
    }
    public BlockList<UnknownChunk> getUnknownChunkList(){
        return mBody.getUnknownChunkList();
    }

    public StagedAliasEntry searchByStagedResId(int stagedResId){
        for(StagedAlias stagedAlias:getStagedAliasList().getChildes()){
            StagedAliasEntry entry=stagedAlias.getStagedAliasEntryArray()
                    .searchByStagedResId(stagedResId);
            if(entry!=null){
                return entry;
            }
        }
        return null;
    }
    public List<StagedAlias> listStagedAlias(){
        return getStagedAliasList().getChildes();
    }
    public StagedAliasList getStagedAliasList(){
        return mBody.getStagedAliasList();
    }
    public OverlayableList getOverlayableList(){
        return mBody.getOverlayableList();
    }
    public BlockList<OverlayablePolicy> getOverlayablePolicyList(){
        return mBody.getOverlayablePolicyList();
    }
    public void sortTypes(){
        getSpecTypePairArray().sort();
    }

    public void removeEmpty(){
        getSpecTypePairArray().removeEmptyPairs();
    }
    public boolean isEmpty(){
        return getSpecTypePairArray().isEmpty();
    }
    public int getId(){
        return getHeaderBlock().getPackageId().get();
    }
    public void setId(byte id){
        setId(0xff & id);
    }
    public void setId(int id){
        getHeaderBlock().getPackageId().set(id);
    }
    public String getName(){
        return getHeaderBlock().getPackageName().get();
    }
    public void setName(String name){
        getHeaderBlock().getPackageName().set(name);
    }
    public TableBlock getTableBlock(){
        Block parent=getParent();
        while(parent!=null){
            if(parent instanceof TableBlock){
                return (TableBlock)parent;
            }
            parent=parent.getParent();
        }
        return null;
    }
    public TypeStringPool getTypeStringPool(){
        return mTypeStringPool;
    }
    @Override
    public SpecStringPool getSpecStringPool(){
        return mSpecStringPool;
    }
    @Override
    public TableBlock getMainChunk(){
        return getTableBlock();
    }
    @Override
    public PackageBlock getPackageBlock(){
        return this;
    }
    public PackageBody getPackageBody() {
        return mBody;
    }
    public SpecTypePairArray getSpecTypePairArray(){
        return mBody.getSpecTypePairArray();
    }
    public Collection<LibraryInfo> listLibraryInfo(){
        return getLibraryBlock().listLibraryInfo();
    }

    public void addLibrary(LibraryBlock libraryBlock){
        if(libraryBlock==null){
            return;
        }
        for(LibraryInfo info:libraryBlock.getLibraryInfoArray().listItems()){
            addLibraryInfo(info);
        }
    }
    public void addLibraryInfo(LibraryInfo info){
        getLibraryBlock().addLibraryInfo(info);
    }
    public LibraryBlock getLibraryBlock(){
        return mBody.getLibraryBlock();
    }
    public Set<Integer> listResourceIds(){
        return getEntriesGroupMap().keySet();
    }
    public Entry getOrCreateEntry(byte typeId, short entryId, String qualifiers){
        return getSpecTypePairArray().getOrCreateEntry(typeId, entryId, qualifiers);
    }

    public Entry getAnyEntry(int resourceId){
        int packageId = (resourceId >> 24) & 0xff;
        if(packageId != getId()){
            return null;
        }
        byte typeId = (byte) ((resourceId >> 16) & 0xff);
        short entryId = (short) (resourceId & 0xffff);
        return getSpecTypePairArray().getAnyEntry(typeId, entryId);
    }
    public Entry getEntry(byte typeId, short entryId, String qualifiers){
        return getSpecTypePairArray().getEntry(typeId, entryId, qualifiers);
    }
    public TypeBlock getOrCreateTypeBlock(byte typeId, String qualifiers){
        return getSpecTypePairArray().getOrCreateTypeBlock(typeId, qualifiers);
    }
    public TypeBlock getTypeBlock(byte typeId, String qualifiers){
        return getSpecTypePairArray().getTypeBlock(typeId, qualifiers);
    }

    private void unlockEntryGroup() {
        synchronized (this){
            if(!this.entryGroupMapLocked){
                return;
            }
            entryGroupMapLocked = false;
            Map<Integer, EntryGroup> map = this.mEntriesGroup;
            map.clear();
            createEntryGroupMap(map);
        }
    }
    private void createEntryGroupMap(Map<Integer, EntryGroup> map){
        map.clear();
        for(SpecTypePair specTypePair : listSpecTypePairs()){
            map.putAll(specTypePair.createEntryGroups(true));
        }
    }
    public Map<Integer, EntryGroup> getEntriesGroupMap(){
        unlockEntryGroup();
        return mEntriesGroup;
    }
    public Collection<EntryGroup> listEntryGroup(){
        return getEntriesGroupMap().values();
    }

    /**
     * Searches entries by resource id from local map, then if not find
     * search by alias resource id
     * */
    public EntryGroup getEntryGroup(int resourceId){
        if(resourceId==0){
            return null;
        }
        EntryGroup entryGroup=getEntriesGroupMap().get(resourceId);
        if(entryGroup!=null){
            return entryGroup;
        }
        StagedAliasEntry stagedAliasEntry = searchByStagedResId(resourceId);
        if(stagedAliasEntry!=null){
            return getEntriesGroupMap()
                    .get(stagedAliasEntry.getFinalizedResId());
        }
        return null;
    }
    public void updateEntry(Entry entry){
        if(this.entryGroupMapLocked){
            return;
        }
        if(entry == null || entry.isNull()){
            return;
        }
        int resourceId = entry.getResourceId();
        Map<Integer, EntryGroup> map = getEntriesGroupMap();
        EntryGroup group = map.get(resourceId);
        if(group == null){
            group = new EntryGroup(resourceId);
            map.put(resourceId, group);
        }
        group.add(entry);
    }
    public void removeEntryGroup(Entry entry){
        if(entry == null){
            return;
        }
        int resourceId = entry.getResourceId();
        Map<Integer, EntryGroup> map = getEntriesGroupMap();
        EntryGroup group = map.get(resourceId);
        if(group == null){
            return;
        }
        group.remove(entry);
        if(group.size() == 0){
            map.remove(resourceId);
        }
    }
    public List<Entry> listEntries(byte typeId, int entryId){
        List<Entry> results=new ArrayList<>();
        for(SpecTypePair pair:listSpecTypePair(typeId)){
            results.addAll(pair.listEntries(entryId));
        }
        return results;
    }
    public List<SpecTypePair> listSpecTypePair(byte typeId){
        List<SpecTypePair> results = new ArrayList<>();
        for(SpecTypePair specTypePair : listSpecTypePairs()){
            if(typeId == specTypePair.getTypeId()){
                results.add(specTypePair);
            }
        }
        return results;
    }
    public SpecTypePair getSpecTypePair(String typeName){
        return getSpecTypePairArray().getSpecTypePair(typeName);
    }
    public SpecTypePair getSpecTypePair(int typeId){
        return getSpecTypePairArray().getSpecTypePair((byte) typeId);
    }
    public EntryGroup getEntryGroup(String typeName, String entryName){
        return getSpecTypePairArray().getEntryGroup(typeName, entryName);
    }
    public Collection<SpecTypePair> listSpecTypePairs(){
        return getSpecTypePairArray().listItems();
    }
    /**
     * Use listSpecTypePairs()
     * */
    @Deprecated
    public Collection<SpecTypePair> listAllSpecTypePair(){
        return listSpecTypePairs();
    }

    private void refreshTypeStringPoolOffset(){
        int pos=countUpTo(mTypeStringPool);
        getHeaderBlock().getTypeStringPoolOffset().set(pos);
    }
    private void refreshTypeStringPoolCount(){
        getHeaderBlock().getTypeStringPoolCount().set(mTypeStringPool.countStrings());
    }
    private void refreshSpecStringPoolOffset(){
        int pos=countUpTo(mSpecStringPool);
        getHeaderBlock().getSpecStringPoolOffset().set(pos);
    }
    private void refreshSpecStringCount(){
        getHeaderBlock().getSpecStringPoolCount().set(mSpecStringPool.countStrings());
    }
    private void refreshTypeIdOffset(){
        // TODO: find solution
        //int largest=getSpecTypePairArray().getHighestTypeId();
        //int count=getTypeStringPool().countStrings();
        //getHeaderBlock().getTypeIdOffset().set(count-largest);
        getHeaderBlock().getTypeIdOffsetItem().set(0);
    }
    public void onEntryAdded(Entry entry){
        updateEntry(entry);
    }
    @Override
    public void onChunkLoaded() {
    }

    @Override
    protected void onChunkRefreshed() {
        refreshTypeStringPoolOffset();
        refreshTypeStringPoolCount();
        refreshSpecStringPoolOffset();
        refreshSpecStringCount();
        refreshTypeIdOffset();
    }

    @Override
    public JSONObject toJson() {
        return toJson(true);
    }
    public JSONObject toJson(boolean addTypes) {
        JSONObject jsonObject=new JSONObject();

        jsonObject.put(BuildInfo.NAME_arsc_lib_version, BuildInfo.getVersion());

        jsonObject.put(NAME_package_id, getId());
        jsonObject.put(NAME_package_name, getName());
        jsonObject.put(NAME_specs, getSpecTypePairArray().toJson(!addTypes));
        LibraryInfoArray libraryInfoArray = getLibraryBlock().getLibraryInfoArray();
        if(libraryInfoArray.childesCount()>0){
            jsonObject.put(NAME_libraries,libraryInfoArray.toJson());
        }
        StagedAlias stagedAlias =
                StagedAlias.mergeAll(getStagedAliasList().getChildes());
        if(stagedAlias!=null){
            jsonObject.put(NAME_staged_aliases,
                    stagedAlias.getStagedAliasEntryArray().toJson());
        }
        JSONArray jsonArray = getOverlayableList().toJson();
        if(jsonArray!=null){
            jsonObject.put(NAME_overlaybles, jsonArray);
        }
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setId(json.getInt(NAME_package_id));
        setName(json.getString(NAME_package_name));
        getSpecTypePairArray().fromJson(json.optJSONArray(NAME_specs));
        LibraryInfoArray libraryInfoArray = getLibraryBlock().getLibraryInfoArray();
        libraryInfoArray.fromJson(json.optJSONArray(NAME_libraries));
        if(json.has(NAME_staged_aliases)){
            StagedAlias stagedAlias=new StagedAlias();
            stagedAlias.getStagedAliasEntryArray()
                    .fromJson(json.getJSONArray(NAME_staged_aliases));
            getStagedAliasList().add(stagedAlias);
        }
        if(json.has(NAME_overlaybles)){
            getOverlayableList().fromJson(json.getJSONArray(NAME_overlaybles));
        }
    }
    public void merge(PackageBlock packageBlock){
        if(packageBlock==null||packageBlock==this){
            return;
        }
        if(getId()!=packageBlock.getId()){
            throw new IllegalArgumentException("Can not merge different id packages: "
                    +getId()+"!="+packageBlock.getId());
        }
        setName(packageBlock.getName());
        getLibraryBlock().merge(packageBlock.getLibraryBlock());
        mergeSpecStringPool(packageBlock);
        getSpecTypePairArray().merge(packageBlock.getSpecTypePairArray());
        getOverlayableList().merge(packageBlock.getOverlayableList());
        getStagedAliasList().merge(packageBlock.getStagedAliasList());
    }
    private void mergeSpecStringPool(PackageBlock coming){
        this.getSpecStringPool().addStrings(
                coming.getSpecStringPool().toStringList());
    }

    @Override
    public int compareTo(PackageBlock pkg) {
        return Integer.compare(getId(), pkg.getId());
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(super.toString());
        builder.append(", id=");
        builder.append(HexUtil.toHex2((byte) getId()));
        builder.append(", name=");
        builder.append(getName());
        int libCount=getLibraryBlock().getLibraryCount();
        if(libCount>0){
            builder.append(", libraries=");
            builder.append(libCount);
        }
        return builder.toString();
    }

    public static final String NAME_package_id = "package_id";
    public static final String NAME_package_name = "package_name";
    public static final String JSON_FILE_NAME = "package.json";
    private static final String NAME_specs = "specs";
    public static final String NAME_libraries = "libraries";
    public static final String NAME_staged_aliases = "staged_aliases";
    public static final String NAME_overlaybles = "overlaybles";
}
