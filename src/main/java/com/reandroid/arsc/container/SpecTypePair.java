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
package com.reandroid.arsc.container;

import com.reandroid.arsc.array.EntryArray;
import com.reandroid.arsc.chunk.*;
import com.reandroid.arsc.array.TypeBlockArray;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockContainer;
import com.reandroid.arsc.group.EntryGroup;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.header.TypeHeader;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.pool.SpecStringPool;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.util.HexUtil;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

import java.io.IOException;
import java.util.*;

public class SpecTypePair extends BlockContainer<Block>
        implements JSONConvert<JSONObject>, Comparable<SpecTypePair>{
    private final Block[] mChildes;
    private final SpecBlock mSpecBlock;
    private final TypeBlockArray mTypeBlockArray;

    public SpecTypePair(SpecBlock specBlock, TypeBlockArray typeBlockArray){
        this.mSpecBlock = specBlock;
        this.mTypeBlockArray = typeBlockArray;

        this.mChildes = new Block[]{specBlock, typeBlockArray};

        specBlock.setIndex(0);
        typeBlockArray.setIndex(1);

        specBlock.setParent(this);
        typeBlockArray.setParent(this);
    }
    public SpecTypePair(){
        this(new SpecBlock(), new TypeBlockArray());
    }

    public Boolean hasComplexEntry(){
        return getTypeBlockArray().hasComplexEntry();
    }
    public void linkTableStringsInternal(TableStringPool tableStringPool){
        for(TypeBlock typeBlock:listTypeBlocks()){
            typeBlock.linkTableStringsInternal(tableStringPool);
        }
    }
    public void linkSpecStringsInternal(SpecStringPool specStringPool){
        for(TypeBlock typeBlock:listTypeBlocks()){
            typeBlock.linkSpecStringsInternal(specStringPool);
        }
    }
    public Map<Integer, EntryGroup> createEntryGroups(){
        return createEntryGroups(false);
    }
    public Map<Integer, EntryGroup> createEntryGroups(boolean skipNullEntries){
        Map<Integer, EntryGroup> map = new LinkedHashMap<>();
        for(TypeBlock typeBlock : listTypeBlocks()){
            EntryArray entryArray = typeBlock.getEntryArray();
            for(Entry entry : entryArray.listItems(skipNullEntries)){
                if(entry == null){
                    continue;
                }
                int id = entry.getResourceId();
                EntryGroup entryGroup = map.get(id);
                if(entryGroup == null){
                    entryGroup = new EntryGroup(id);
                    map.put(id, entryGroup);
                }
                entryGroup.add(entry);
            }
        }
        return map;
    }
    public EntryGroup createEntryGroup(int id){
        id = 0xffff & id;
        EntryGroup entryGroup = null;
        for(TypeBlock typeBlock:listTypeBlocks()){
            Entry entry = typeBlock.getEntry(id);
            if(entry == null){
                continue;
            }
            if(entryGroup == null){
                entryGroup = new EntryGroup(entry.getResourceId());
            }
            entryGroup.add(entry);
        }
        return entryGroup;
    }
    public EntryGroup getEntryGroup(String entryName){
        EntryGroup entryGroup = null;
        for(TypeBlock typeBlock:listTypeBlocks()){
            Entry entry = typeBlock.getEntry(entryName);
            if(entry == null){
                continue;
            }
            if(entryGroup == null){
                entryGroup = new EntryGroup(entry.getResourceId());
            }
            entryGroup.add(entry);
        }
        return entryGroup;
    }
    public void destroy(){
        getSpecBlock().destroy();
        getTypeBlockArray().destroy();
    }
    public Entry getAnyEntry(short entryId){
        Entry result = null;
        TypeBlock[] types = getTypeBlockArray().getChildes();
        for(int i = 0; i < types.length; i++){
            TypeBlock typeBlock = types[i];
            if(typeBlock == null){
                continue;
            }
            Entry entry = typeBlock.getEntry(entryId);
            if(entry == null){
                continue;
            }
            if(!entry.isNull()){
                return entry;
            }
            if(result == null){
                result = entry;
            }
        }
        return result;
    }
    public Entry getAnyEntry(String name){
        TypeBlock[] types = getTypeBlockArray().getChildes();
        for(int i = 0; i < types.length; i++){
            TypeBlock typeBlock = types[i];
            if(typeBlock == null){
                continue;
            }
            Entry entry = typeBlock.getEntry(name);
            if(entry != null){
                return entry;
            }
        }
        return null;
    }
    public Entry getEntry(ResConfig resConfig, String entryName){
        return getTypeBlockArray().getEntry(resConfig, entryName);
    }
    public void sortTypes(){
        getTypeBlockArray().sort();
    }
    public boolean removeNullEntries(int startId){
        startId = 0x0000ffff & startId;
        boolean removed = getTypeBlockArray().removeNullEntries(startId);
        if(!removed){
            return false;
        }
        getSpecBlock().setEntryCount(startId);
        return true;
    }
    public void removeEmptyTypeBlocks(){
        getTypeBlockArray().removeEmptyBlocks();
    }
    public boolean isEmpty(){
        return getTypeBlockArray().isEmpty();
    }
    public int countTypeBlocks(){
        return getTypeBlockArray().childesCount();
    }
    public Entry getOrCreateEntry(short entryId, String qualifiers){
        return getTypeBlockArray().getOrCreateEntry(entryId, qualifiers);
    }
    public Entry getEntry(short entryId, String qualifiers){
        return getTypeBlockArray().getEntry(entryId, qualifiers);
    }
    public TypeBlock getOrCreateTypeBlock(String qualifiers){
        return getTypeBlockArray().getOrCreate(qualifiers);
    }
    public TypeBlock getOrCreateTypeBlock(ResConfig resConfig){
        return getTypeBlockArray().getOrCreate(resConfig);
    }
    public TypeBlock getTypeBlock(String qualifiers){
        return getTypeBlockArray().getTypeBlock(qualifiers);
    }
    public TypeBlock getTypeBlock(ResConfig resConfig){
        return getTypeBlockArray().getTypeBlock(resConfig);
    }
    public List<ResConfig> listResConfig(){
        return mTypeBlockArray.listResConfig();
    }

    public Iterator<TypeBlock> iteratorNonEmpty(){
        return mTypeBlockArray.iteratorNonEmpty();
    }
    public boolean hasDuplicateResConfig(boolean ignoreEmpty){
        return mTypeBlockArray.hasDuplicateResConfig(ignoreEmpty);
    }

    public byte getTypeId(){
        return mSpecBlock.getTypeId();
    }
    public int getId(){
        return mSpecBlock.getId();
    }
    public void setTypeId(byte id){
        mSpecBlock.setTypeId(id);
        mTypeBlockArray.setTypeId(id);
    }
    public String getTypeName(){
        TypeString typeString = getTypeString();
        if(typeString!=null){
            return typeString.get();
        }
        return null;
    }
    public boolean isEqualTypeName(String typeName){
        return TypeBlock.isEqualTypeName(getTypeName(), typeName);
    }
    /**
     * TOBEREMOVED
     *
     * It is allowed to have duplicate entry name therefore it is not recommend to use this.
     * Lets depreciate to warn developer
     */
    @Deprecated
    public Entry searchByEntryName(String entryName){
        return getTypeBlockArray().searchByEntryName(entryName);
    }
    public SpecBlock getSpecBlock(){
        return mSpecBlock;
    }
    public TypeBlockArray getTypeBlockArray(){
        return mTypeBlockArray;
    }
    public PackageBlock getPackageBlock(){
        return getParent(PackageBlock.class);
    }
    public List<Entry> listEntries(int entryId){
        List<Entry> results=new ArrayList<>();
        Iterator<TypeBlock> itr = mTypeBlockArray.iterator(true);
        while (itr.hasNext()){
            TypeBlock typeBlock=itr.next();
            Entry entry = typeBlock.getEntry(entryId);
            if(entry ==null|| entry.isNull()){
                continue;
            }
            results.add(entry);
        }
        return results;
    }
    public List<Entry> listEntries(String entryName){
        List<Entry> results = new ArrayList<>();
        Iterator<TypeBlock> itr = mTypeBlockArray.iterator(true);
        while (itr.hasNext()){
            TypeBlock typeBlock = itr.next();
            Entry entry = typeBlock.getEntry(entryName);
            if(entry != null){
                results.add(entry);
            }
        }
        return results;
    }
    public Collection<TypeBlock> listTypeBlocks(){
        return getTypeBlockArray().listItems();
    }

    @Override
    protected void onRefreshed() {

    }
    @Override
    public int childesCount() {
        return mChildes.length;
    }
    @Override
    public Block[] getChildes() {
        return mChildes;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        HeaderBlock headerBlock=reader.readHeaderBlock();
        if(headerBlock==null){
            return;
        }
        ChunkType chunkType=headerBlock.getChunkType();
        if(chunkType == ChunkType.TYPE){
            readTypeBlock(reader);
            return;
        }
        if(chunkType!=ChunkType.SPEC){
            readUnexpectedNonSpecBlock(reader, headerBlock);
        }
        mSpecBlock.readBytes(reader);
    }
    private void readTypeBlock(BlockReader reader) throws IOException {
        TypeHeader typeHeader = reader.readTypeHeader();
        TypeBlock typeBlock = mTypeBlockArray.createNext(typeHeader.isSparse());
        typeBlock.readBytes(reader);
    }
    private void readUnexpectedNonSpecBlock(BlockReader reader, HeaderBlock headerBlock) throws IOException{
        throw new IOException("Unexpected block: "+headerBlock.toString()+", Should be: "+ChunkType.SPEC);
    }
    public int getHighestEntryCount(){
        return getTypeBlockArray().getHighestEntryCount();
    }
    public TypeString getTypeString(){
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock!=null){
            return packageBlock.getTypeStringPool().getById(getId());
        }
        return null;
    }

    @Override
    public JSONObject toJson() {
        return toJson(false);
    }
    @Override
    public void fromJson(JSONObject json) {
        getSpecBlock().fromJson(json.getJSONObject(SpecBlock.NAME_spec));
        getTypeBlockArray().fromJson(json.optJSONArray(NAME_types));
    }
    public JSONObject toJson(boolean specOnly) {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(SpecBlock.NAME_spec, getSpecBlock().toJson());
        if(!specOnly){
            jsonObject.put(NAME_types, getTypeBlockArray().toJson());
        }
        return jsonObject;
    }
    public void merge(SpecTypePair typePair){
        if(typePair==null||typePair==this){
            return;
        }
        if(getTypeId() != typePair.getTypeId()){
            throw new IllegalArgumentException("Can not merge different id types: "
                    +getTypeId()+"!="+typePair.getTypeId());
        }
        getSpecBlock().merge(typePair.getSpecBlock());
        getTypeBlockArray().merge(typePair.getTypeBlockArray());
    }
    @Override
    public int compareTo(SpecTypePair specTypePair) {
        return Integer.compare(getId(), specTypePair.getId());
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(HexUtil.toHex2(getTypeId()));
        builder.append(" (");
        TypeString ts = getTypeString();
        if(ts!=null){
            builder.append(ts.get());
        }else {
            builder.append("null");
        }
        builder.append(") config count=");
        builder.append(getTypeBlockArray().childesCount());
        return builder.toString();
    }

    public static final String NAME_types = "types";
    public static final String NAME_sparse_types = "sparse_types";
}
