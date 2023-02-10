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

import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.array.TypeBlockArray;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockContainer;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.SpecBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.value.EntryBlock;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class SpecTypePair extends BlockContainer<Block>
        implements JSONConvert<JSONObject>, Comparable<SpecTypePair>{
    private final Block[] mChildes;
    private final SpecBlock mSpecBlock;
    private final TypeBlockArray mTypeBlockArray;
    public SpecTypePair(SpecBlock specBlock, TypeBlockArray typeBlockArray){
        this.mSpecBlock = specBlock;
        this.mTypeBlockArray = typeBlockArray;
        this.mChildes=new Block[]{specBlock, typeBlockArray};
        mSpecBlock.setIndex(0);
        mTypeBlockArray.setIndex(1);
        mSpecBlock.setParent(this);
        mTypeBlockArray.setParent(this);
    }
    public SpecTypePair(){
        this(new SpecBlock(), new TypeBlockArray());
    }
    public EntryBlock getAnyEntry(String name){
        for(TypeBlock typeBlock:listTypeBlocks()){
            EntryBlock entryBlock=typeBlock.searchByEntryName(name);
            if(entryBlock!=null){
                return entryBlock;
            }
        }
        return null;
    }
    public void sortTypes(){
        getTypeBlockArray().sort();
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
    public EntryBlock getOrCreateEntry(short entryId, String qualifiers){
        return getTypeBlockArray().getOrCreateEntry(entryId, qualifiers);
    }
    public EntryBlock getEntry(short entryId, String qualifiers){
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
    public List<ResConfig> listResConfig(){
        return mTypeBlockArray.listResConfig();
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
    /**
     * It is allowed to have duplicate entry name therefore it is not recommend to use this.
     * Lets depreciate to warn developer
     */
    @Deprecated
    public EntryBlock searchByEntryName(String entryName){
        return getTypeBlockArray().searchByEntryName(entryName);
    }
    public SpecBlock getSpecBlock(){
        return mSpecBlock;
    }
    public TypeBlockArray getTypeBlockArray(){
        return mTypeBlockArray;
    }
    public PackageBlock getPackageBlock(){
        Block parent=getParent();
        while (parent!=null){
            if(parent instanceof PackageBlock){
                return (PackageBlock)parent;
            }
            parent=parent.getParent();
        }
        return null;
    }
    public List<EntryBlock> listEntries(int entryId){
        List<EntryBlock> results=new ArrayList<>();
        Iterator<TypeBlock> itr = mTypeBlockArray.iterator(true);
        while (itr.hasNext()){
            TypeBlock typeBlock=itr.next();
            EntryBlock entryBlock=typeBlock.getEntryBlock(entryId);
            if(entryBlock==null||entryBlock.isNull()){
                continue;
            }
            results.add(entryBlock);
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
        if(chunkType!=ChunkType.SPEC){
            readUnexpectedNonSpecBlock(reader, headerBlock);
        }
        mSpecBlock.readBytes(reader);
        mTypeBlockArray.readBytes(reader);
    }
    private void readUnexpectedNonSpecBlock(BlockReader reader, HeaderBlock headerBlock) throws IOException{
        throw new IOException("Unexpected block: "+headerBlock.toString()+", Should be: "+ChunkType.SPEC);
    }
    public int getHighestEntryCount(){
        int specEntryCount=getSpecBlock().getEntryCount();
        int typeEntryCount=getTypeBlockArray().getHighestEntryCount();
        if(specEntryCount>typeEntryCount){
            return specEntryCount;
        }
        return typeEntryCount;
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
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("id", getSpecBlock().getId());
        jsonObject.put("types", getTypeBlockArray().toJson());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        getSpecBlock().setTypeId((byte) json.getInt("id"));
        getTypeBlockArray().fromJson(json.getJSONArray("types"));
    }
    public void merge(SpecTypePair typePair){
        if(typePair==null||typePair==this){
            return;
        }
        if(getTypeId() != typePair.getTypeId()){
            throw new IllegalArgumentException("Can not merge different id types: "
                    +getTypeId()+"!="+typePair.getTypeId());
        }
        getTypeBlockArray().merge(typePair.getTypeBlockArray());
    }
    @Override
    public int compareTo(SpecTypePair specTypePair) {
        return Integer.compare(getId(), specTypePair.getId());
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(String.format("0x%02x", getTypeId()));
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
}
