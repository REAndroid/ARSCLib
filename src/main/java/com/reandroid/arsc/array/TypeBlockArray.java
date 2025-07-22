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

import com.reandroid.arsc.base.BlockArray;
import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.chunk.SpecBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.header.TypeHeader;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.collection.ComputeIterator;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TypeBlockArray extends BlockArray<TypeBlock>
        implements JSONConvert<JSONArray>, Comparator<TypeBlock> {
    private byte mTypeId;
    private Boolean mHasComplexEntry;
    private Map<String, TypeBlock> mQualifiersMap;

    public TypeBlockArray(){
        super();
    }

    public Boolean hasComplexEntry(){
        if(mHasComplexEntry != null){
            return mHasComplexEntry;
        }
        for(TypeBlock typeBlock : listItems(true)){
            Boolean hasComplex = typeBlock.getEntryArray().hasComplexEntry();
            if(hasComplex != null){
                mHasComplexEntry = hasComplex;
            }
        }
        return mHasComplexEntry;
    }
    public void destroy(){
        for(TypeBlock typeBlock:listItems()){
            if(typeBlock!=null){
                typeBlock.destroy();
            }
        }
        clear();
    }
    public void sort(){
        sort(this);
    }
    public boolean removeNullEntries(int startId){
        boolean result = true;
        for(TypeBlock typeBlock:listItems()){
            boolean removed = typeBlock.removeNullEntries(startId);
            result = result && removed;
        }
        return result;
    }
    public void removeEmptyBlocks(){
        removeIf(TypeBlock::isEmpty);
    }
    public Entry getOrCreateEntry(short entryId, String qualifiers){
        TypeBlock typeBlock=getOrCreate(qualifiers);
        return typeBlock.getOrCreateEntry(entryId & 0xffff);
    }
    public Entry getOrCreateEntry(short entryId, ResConfig resConfig){
        TypeBlock typeBlock = getOrCreate(resConfig);
        return typeBlock.getOrCreateEntry(entryId & 0xffff);
    }
    public boolean isEmpty(){
        for(TypeBlock typeBlock:listItems()){
            if(typeBlock!=null && !typeBlock.isEmpty()){
                return false;
            }
        }
        return true;
    }
    public Entry getEntry(short entryId, String qualifiers){
        TypeBlock typeBlock=getTypeBlock(qualifiers);
        if(typeBlock==null){
            return null;
        }
        return typeBlock.getEntry(entryId);
    }
    public Entry getEntry(ResConfig resConfig, String entryName){
        TypeBlock typeBlock = getTypeBlock(resConfig);
        if(typeBlock != null){
            return typeBlock.getEntry(entryName);
        }
        return null;
    }
    public Entry getEntry(ResConfig resConfig, int entryId){
        TypeBlock typeBlock = getTypeBlock(resConfig);
        if (typeBlock != null) {
            return typeBlock.getEntry(entryId);
        }
        return null;
    }
    public TypeBlock getOrCreate(ResConfig resConfig) {
        TypeBlock typeBlock = getTypeBlock(resConfig);
        if (typeBlock != null) {
            return typeBlock;
        }
        typeBlock = createNext();
        typeBlock.setId(getTypeId() & 0xff);
        ResConfig config = typeBlock.getResConfig();
        config.copyFrom(resConfig);
        return typeBlock;
    }
    public TypeBlock getOrCreate(String qualifiers){
        TypeBlock typeBlock = getTypeBlock(qualifiers);
        if (typeBlock != null) {
            return typeBlock;
        }
        int count = getHighestEntryCount();
        typeBlock = createNext();
        typeBlock.ensureEntriesCount(count);
        ResConfig config = typeBlock.getResConfig();
        config.parseQualifiers(qualifiers);
        return typeBlock;
    }
    public TypeBlock getTypeBlock(String qualifiers) {
        if (qualifiers == null) {
            return null;
        }
        TypeBlock typeBlock = getFromQualifiersMap(qualifiers);
        if (typeBlock != null) {
            return typeBlock;
        }
        Iterator<TypeBlock> iterator = iterator();
        while (iterator.hasNext()){
            typeBlock = iterator.next();
            if(typeBlock.getResConfig().isEqualQualifiers(qualifiers)){
                return typeBlock;
            }
        }
        return null;
    }
    private TypeBlock getFromQualifiersMap(String qualifiers) {
        Map<String, TypeBlock> map = this.mQualifiersMap;
        if (map == null) {
            buildQualifiersMap();
            map = this.mQualifiersMap;
            return map.get(qualifiers);
        }
        TypeBlock typeBlock = map.get(qualifiers);
        if (typeBlock != null && !qualifiers.equals(typeBlock.getQualifiers()) ||
                typeBlock != null && typeBlock.getParent() == null) {
            buildQualifiersMap();
            map = this.mQualifiersMap;
            typeBlock = map.get(qualifiers);
        }
        return typeBlock;
    }
    public TypeBlock getTypeBlock(ResConfig config){
        if(config == null){
            return null;
        }
        TypeBlock typeBlock = getFromQualifiersMap(config.getQualifiers());
        if (typeBlock != null) {
            return typeBlock;
        }
        Iterator<TypeBlock> iterator = iterator();
        while (iterator.hasNext()){
            typeBlock = iterator.next();
            if(typeBlock == null){
                continue;
            }
            if(config.equals(typeBlock.getResConfig())){
                return typeBlock;
            }
        }
        return null;
    }
    private void buildQualifiersMap() {
        Map<String, TypeBlock> map = new HashMap<>(size());
        this.mQualifiersMap = map;
        Iterator<TypeBlock> iterator = iterator();
        while (iterator.hasNext()) {
            TypeBlock typeBlock = iterator.next();
            map.put(typeBlock.getQualifiers(), typeBlock);
        }
    }
    public void setTypeId(byte id){
        this.mTypeId = id;
        Iterator<TypeBlock> iterator = iterator();
        while (iterator.hasNext()){
            TypeBlock typeBlock = iterator.next();
            typeBlock.setTypeId(id);
        }
    }
    public byte getTypeId(){
        SpecBlock specBlock=getSpecBlock();
        if(specBlock != null){
            byte id = specBlock.getTypeId();
            if(id != 0){
                return id;
            }
        }
        if(mTypeId != 0){
            return mTypeId;
        }
        Iterator<TypeBlock> iterator = iterator();
        while (iterator.hasNext()){
            TypeBlock typeBlock = iterator.next();
            if(typeBlock == null){
                continue;
            }
            byte id = typeBlock.getTypeId();
            if(id == 0){
                continue;
            }
            if(specBlock != null){
                specBlock.setTypeId(id);
            }
            mTypeId = id;
            return id;
        }
        return 0;
    }
    public Set<ResConfig> listResConfig(){
        Set<ResConfig> unique = new HashSet<>();
        Iterator<TypeBlock> iterator = iterator();
        while (iterator.hasNext()){
            TypeBlock typeBlock = iterator.next();
            unique.add(typeBlock.getResConfig());
        }
        return unique;
    }
    public Iterator<ResConfig> getResConfigs(){
        return new ComputeIterator<>(super.iterator(true),
                TypeBlock::getResConfig);
    }
    public Iterator<TypeBlock> iteratorNonEmpty(){
        return super.iterator(typeBlock -> {
            if(typeBlock == null || typeBlock.isNull()){
                return false;
            }
            return !typeBlock.isEmpty();
        });
    }
    public Iterator<TypeBlock> iterator(ResConfig resConfig){
        return iterator(typeBlock -> typeBlock.getResConfig().equals(resConfig));
    }
    public boolean hasDuplicateResConfig(boolean ignoreEmpty){
        Set<Integer> uniqueHashSet = new HashSet<>();
        Iterator<TypeBlock> itr;
        if(ignoreEmpty){
            itr = iteratorNonEmpty();
        }else {
            itr = iterator(true);
        }
        while (itr.hasNext()){
            Integer hash = itr.next()
                    .getResConfig().hashCode();
            if(uniqueHashSet.contains(hash)){
                return true;
            }
            uniqueHashSet.add(hash);
        }
        return false;
    }
    private SpecBlock getSpecBlock(){
        SpecTypePair parent = getParent(SpecTypePair.class);
        if(parent != null){
            return parent.getSpecBlock();
        }
        return null;
    }
    @Override
    public TypeBlock newInstance() {
        byte id = getTypeId();
        TypeBlock typeBlock = new TypeBlock();
        typeBlock.setTypeId(id);
        return typeBlock;
    }

    @Override
    protected void onRefreshed() {
        this.mQualifiersMap = null;
    }
    @Override
    public void onChanged() {
        super.onChanged();
        mQualifiersMap = null;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        boolean readOk=true;
        while (readOk){
            readOk=readTypeBlockArray(reader);
        }
        this.mQualifiersMap = null;
    }
    private boolean readTypeBlockArray(BlockReader reader) throws IOException{
        HeaderBlock headerBlock=reader.readHeaderBlock();
        if(headerBlock==null){
            return false;
        }
        ChunkType chunkType=headerBlock.getChunkType();
        if(chunkType!=ChunkType.TYPE){
            return false;
        }
        TypeHeader typeHeader = TypeHeader.read(reader);
        int id = getTypeId();
        if(id!=0 && typeHeader.getId().get() != id){
            return false;
        }
        int pos=reader.getPosition();
        TypeBlock typeBlock=createNext();
        typeBlock.readBytes(reader);
        return reader.getPosition()>pos;
    }
    public int getHighestEntryId(){
        int result = -1;
        Iterator<TypeBlock> iterator = iterator();
        while (iterator.hasNext()){
            TypeBlock typeBlock = iterator.next();
            int high = typeBlock.getEntryArray().getHighestEntryId();
            if(high > result){
                result = high;
            }
        }
        return result;
    }
    public int getHighestEntryCount(){
        int result = 0;
        Iterator<TypeBlock> iterator = iterator();
        while (iterator.hasNext()){
            TypeBlock typeBlock = iterator.next();
            int count = typeBlock.getEntryArray().size();
            if(count > result){
                result = count;
            }
        }
        return result;
    }
    public void setEntryCount(int count){
        Iterator<TypeBlock> iterator = iterator();
        while (iterator.hasNext()){
            TypeBlock typeBlock = iterator.next();
            if(!typeBlock.isSparse()){
                typeBlock.setEntryCount(count);
            }
        }
    }
    public TypeString getTypeString(){
        Iterator<TypeBlock> iterator = iterator();
        while (iterator.hasNext()) {
            TypeBlock typeBlock = iterator.next();
            TypeString typeString = typeBlock.getTypeString();
            if (typeString != null) {
                return typeString;
            }
        }
        return null;
    }
    @Override
    public JSONArray toJson() {
        int size = size();
        JSONArray jsonArray = new JSONArray(size);
        for (int i = 0; i < size; i++) {
            JSONObject jsonObject = get(i).toJson();
            if (jsonObject != null) {
                jsonArray.put(jsonObject);
            }
        }
        return jsonArray;
    }
    @Override
    public void fromJson(JSONArray json) {
        if (json != null) {
            int length = json.length();
            for (int i = 0; i < length; i++) {
                createNext().fromJson(json.getJSONObject(i));
            }
        }
    }
    public void merge(TypeBlockArray typeBlockArray) {
        if (typeBlockArray != null && typeBlockArray != this) {
            int size = typeBlockArray.size();
            for (int i = 0; i < size; i++) {
                TypeBlock typeBlock = typeBlockArray.get(i);
                TypeBlock exist = getOrCreate(typeBlock.getResConfig());
                exist.merge(typeBlock);
            }
        }
    }
    @Override
    public int compare(TypeBlock typeBlock1, TypeBlock typeBlock2) {
        return typeBlock1.compareTo(typeBlock2);
    }
}
