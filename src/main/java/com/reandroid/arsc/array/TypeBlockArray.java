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

import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.base.BlockArray;
import com.reandroid.arsc.chunk.SpecBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.header.TypeHeader;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class TypeBlockArray extends BlockArray<TypeBlock>
        implements JSONConvert<JSONArray>, Comparator<TypeBlock> {
    private byte mTypeId;
    private Boolean mHasComplexEntry;

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
        clearChildes();
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
        List<TypeBlock> allTypes=new ArrayList<>(listItems());
        boolean foundEmpty=false;
        for(TypeBlock typeBlock:allTypes){
            if(typeBlock.isEmpty()){
                super.remove(typeBlock, false);
                foundEmpty=true;
            }
        }
        if(foundEmpty){
            trimNullBlocks();
        }
    }
    public Entry getOrCreateEntry(short entryId, String qualifiers){
        TypeBlock typeBlock=getOrCreate(qualifiers);
        return typeBlock.getOrCreateEntry(entryId);
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
    public TypeBlock getOrCreate(ResConfig resConfig){
        return getOrCreate(resConfig, false);
    }
    public TypeBlock getOrCreate(ResConfig resConfig, boolean sparse){
        TypeBlock typeBlock = getTypeBlock(resConfig, sparse);
        if(typeBlock != null){
            return typeBlock;
        }
        byte id = getTypeId();
        typeBlock = createNext(sparse);
        typeBlock.setTypeId(id);
        ResConfig config = typeBlock.getResConfig();
        config.copyFrom(resConfig);
        return typeBlock;
    }
    public TypeBlock getOrCreate(String qualifiers){
        TypeBlock typeBlock=getTypeBlock(qualifiers);
        if(typeBlock!=null){
            return typeBlock;
        }
        typeBlock=createNext();
        ResConfig config=typeBlock.getResConfig();
        config.parseQualifiers(qualifiers);
        return typeBlock;
    }
    public TypeBlock getTypeBlock(String qualifiers){
        TypeBlock[] items=getChildes();
        if(items==null){
            return null;
        }
        int max=items.length;
        for(int i=0;i<max;i++){
            TypeBlock block=items[i];
            if(block.getResConfig().isEqualQualifiers(qualifiers)){
                return block;
            }
        }
        return null;
    }
    public TypeBlock getTypeBlock(ResConfig config){
        return getTypeBlock(config, false);
    }
    public TypeBlock getTypeBlock(ResConfig config, boolean sparse){
        if(config==null){
            return null;
        }
        TypeBlock[] items = getChildes();
        if(items == null){
            return null;
        }
        int length = items.length;
        for(int i = 0; i < length; i++){
            TypeBlock typeBlock = items[i];
            if(typeBlock == null || sparse != typeBlock.isSparse()){
                continue;
            }
            if(config.equals(typeBlock.getResConfig())){
                return typeBlock;
            }
        }
        return null;
    }
    public void setTypeId(byte id){
        this.mTypeId=id;
        TypeBlock[] allChildes=getChildes();
        if(allChildes==null){
            return;
        }
        int max=allChildes.length;
        for(int i=0;i<max;i++){
            TypeBlock typeBlock = allChildes[i];
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
        TypeBlock[] childes = getChildes();
        if(childes == null){
            return 0;
        }
        int length = childes.length;
        for(int i=0; i < length; i++){
            TypeBlock typeBlock = childes[i];
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
    public List<ResConfig> listResConfig(){
        return new AbstractList<ResConfig>() {
            @Override
            public ResConfig get(int i) {
                TypeBlock typeBlock=TypeBlockArray.this.get(i);
                if(typeBlock!=null){
                    return typeBlock.getResConfig();
                }
                return null;
            }

            @Override
            public int size() {
                return TypeBlockArray.this.childesCount();
            }
        };
    }
    public Iterator<TypeBlock> iteratorNonEmpty(){
        return super.iterator(NON_EMPTY_TESTER);
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
    protected boolean remove(TypeBlock block, boolean trim){
        if(block==null){
            return false;
        }
        block.cleanEntries();
        return super.remove(block, trim);
    }
    @Override
    public TypeBlock newInstance() {
        byte id = getTypeId();
        TypeBlock typeBlock = new TypeBlock(false);
        typeBlock.setTypeId(id);
        return typeBlock;
    }
    @Override
    public TypeBlock[] newInstance(int len) {
        return new TypeBlock[len];
    }
    public TypeBlock createNext(boolean sparse){
        byte id = getTypeId();
        TypeBlock typeBlock = new TypeBlock(sparse);
        typeBlock.setTypeId(id);
        add(typeBlock);
        return typeBlock;
    }
    @Override
    protected void onRefreshed() {

    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        boolean readOk=true;
        while (readOk){
            readOk=readTypeBlockArray(reader);
        }
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
        TypeHeader typeHeader = reader.readTypeHeader();
        int id = getTypeId();
        if(id!=0 && typeHeader.getId().unsignedInt() != id){
            return false;
        }
        int pos=reader.getPosition();
        TypeBlock typeBlock=createNext();
        typeBlock.readBytes(reader);
        return reader.getPosition()>pos;
    }
    public int getHighestEntryCount(){
        int result=0;
        for(TypeBlock typeBlock:getChildes()){
            int high = typeBlock.getEntryArray().getHighestEntryId();
            if(high > result){
                result = high;
            }
        }
        return result;
    }
    public void setEntryCount(int count){
        for(TypeBlock typeBlock:getChildes()){
            if(!typeBlock.isSparse()){
                typeBlock.setEntryCount(count);
            }
        }
    }
    public TypeString getTypeString(){
        for(TypeBlock typeBlock:getChildes()){
            TypeString typeString=typeBlock.getTypeString();
            if(typeString!=null){
                return typeString;
            }
        }
        return null;
    }
    @Override
    public JSONArray toJson() {
        JSONArray jsonArray=new JSONArray();
        int i=0;
        for(TypeBlock typeBlock:listItems()){
            JSONObject jsonObject= typeBlock.toJson();
            if(jsonObject==null){
                continue;
            }
            jsonArray.put(i, jsonObject);
            i++;
        }
        return jsonArray;
    }
    @Override
    public void fromJson(JSONArray json) {
        if(json == null){
            return;
        }
        int length = json.length();
        for(int i = 0; i < length; i++){
            JSONObject jsonObject = json.getJSONObject(i);
            TypeBlock typeBlock = createNext(
                    jsonObject.optBoolean(TypeBlock.NAME_is_sparse, false));
            typeBlock.fromJson(jsonObject);
        }
    }
    public void merge(TypeBlockArray typeBlockArray){
        if(typeBlockArray == null || typeBlockArray == this){
            return;
        }
        for(TypeBlock typeBlock:typeBlockArray.listItems()){
            TypeBlock exist = getOrCreate(
                    typeBlock.getResConfig(), typeBlock.isSparse());
            exist.merge(typeBlock);
        }
    }
    /**
     * TOBEREMOVED
     *
     * It's mistake to have this method
     *
     */
    @Deprecated
    public Entry searchByEntryName(String entryName){
        if(entryName==null){
            return null;
        }
        TypeBlock[] childes = getChildes();
        if(childes==null || childes.length==0){
            return null;
        }
        return childes[0].getEntry(entryName);
    }
    @Override
    public int compare(TypeBlock typeBlock1, TypeBlock typeBlock2) {
        return typeBlock1.compareTo(typeBlock2);
    }

    private static final Predicate<TypeBlock> NON_EMPTY_TESTER = new Predicate<TypeBlock>() {
        @Override
        public boolean test(TypeBlock typeBlock) {
            if(typeBlock == null || typeBlock.isNull()){
                return false;
            }
            return !typeBlock.isEmpty();
        }
    };
}
